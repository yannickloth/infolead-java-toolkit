package eu.infolead.jtk.async;

import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.fp.either.Result;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Retries an asynchronous operation according to a {@link RetryPolicy}.
 *
 * <h2>When an attempt is repeated</h2>
 *
 * <p>An attempt is repeated only when all of the following hold:
 *
 * <ol>
 *   <li>the policy has attempts left;
 *   <li>the operation was not cancelled (a cancellation is never repeated, no matter what the
 *       classifier says);
 *   <li>the failure's {@link SystemError#isRetryable()} decision &mdash; exposed through {@link
 *       FailureStrategy#retryable()} &mdash; accepts it.
 * </ol>
 *
 * <p>The third condition is what keeps deterministic failures from being repeated: parameter,
 * business-rule, authorization and invariant failures are not retryable, while server errors, rate
 * limits and deadlines are.
 *
 * <h2>Delay</h2>
 *
 * <p>The delay comes from the policy's {@link Backoff}, raised to the {@link
 * SystemError#retryAfter()} hint when the failure carries one. A {@code 429} that says "wait 30
 * seconds" therefore waits at least 30 seconds even when the policy asks for one. The next attempt
 * is scheduled on the context's timer; no thread sleeps while waiting.
 *
 * <h2>Exhaustion</h2>
 *
 * <p>When no attempt is left, the engine returns the <em>last</em> failure unchanged, with the
 * earlier failures attached as suppressed exceptions. It never invents a synthetic "retries
 * exhausted" anomaly, so callers keep seeing the real problem.
 *
 * <p>Retrying is only safe for idempotent operations. The engine cannot know whether a side effect
 * already happened, so the caller must choose the failure types it retries.
 */
public final class Retries {

    private Retries() {
        // Utility class
    }

    /**
     * Runs an operation until it succeeds, is rejected as non-retryable, or the policy is
     * exhausted.
     *
     * <p>Each attempt receives a fresh child context. Use it to create the task, so that a
     * per-attempt timeout and an outer cancellation can interrupt the worker:
     *
     * <pre>{@code
     * Retries.run(context, policy,
     *         attempt -> Async.supply(attempt, () -> client.fetch(id)));
     * }</pre>
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param context the operation context
     * @param policy the retry policy
     * @param attempt the operation to run, receiving the attempt's context
     * @return a future completing with the first success or the last failure
     */
    public static <F, S> CompletableFuture<Result<F, S>> run(
            final AsyncContext<F> context,
            final RetryPolicy policy,
            final Mapper<? extends CompletionStage<Result<F, S>>, AsyncContext<F>> attempt) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(attempt, "attempt");
        return new Engine<>(context, policy, attempt).start();
    }

    /**
     * One retry run.
     *
     * @param <F> the failure type
     * @param <S> the success type
     */
    private static final class Engine<F, S> {

        private static final long MIN_DELAY_NANOS = 1_000_000L;

        private final AsyncContext<F> context;
        private final RetryPolicy policy;
        private final Mapper<? extends CompletionStage<Result<F, S>>, AsyncContext<F>> attempt;
        private final CancellationToken token;
        private final CompletableFuture<Result<F, S>> target = new CompletableFuture<>();
        private final AtomicBoolean finished = new AtomicBoolean();
        private final List<F> previousFailures = new CopyOnWriteArrayList<>();
        private final AtomicReference<ScheduledFuture<?>> pending = new AtomicReference<>();
        private final AtomicReference<ScheduledFuture<?>> deadlineTimer = new AtomicReference<>();
        private final AtomicReference<Runnable> deregistration =
                new AtomicReference<>(
                        () -> {
                            // Replaced as soon as the cancellation hook is registered.
                        });
        private final long deadlineNanos;

        Engine(
                final AsyncContext<F> context,
                final RetryPolicy policy,
                final Mapper<? extends CompletionStage<Result<F, S>>, AsyncContext<F>> attempt) {
            this.context = context;
            this.policy = policy;
            this.attempt = attempt;
            // A dedicated run scope: the overall deadline and the caller can
            // both cancel it without either affecting the caller's token.
            this.token = context.cancellation().child();
            this.deadlineNanos =
                    policy.overallDeadline()
                            .fold(
                                    () -> Long.MAX_VALUE,
                                    duration ->
                                            saturatedAdd(System.nanoTime(), duration.toNanos()));
        }

        CompletableFuture<Result<F, S>> start() {
            deregistration.set(token.onCancel(this::cancelRun));
            if (token.isCancelled().toBoolean()) {
                return target;
            }
            if (policy.overallDeadline().isPresent().toBoolean()) {
                final Duration budget = policy.overallDeadline().or(Duration.ZERO);
                try {
                    deadlineTimer.set(
                            context.scheduler()
                                    .schedule(
                                            () -> token.cancel(CancellationReason.DEADLINE),
                                            Math.max(0L, budget.toNanos()),
                                            TimeUnit.NANOSECONDS));
                } catch (final RejectedExecutionException rejection) {
                    // Without a timer the budget is still enforced between
                    // attempts by deadlineNanos.
                }
            }
            attempt(1);
            return target;
        }

        private void cancelRun() {
            cancelTimers();
            finish(
                    Result.failure(
                            context.failures()
                                    .fromAnomaly(Anomalies.forCancellation(context, token))));
        }

        private void cancelTimers() {
            final ScheduledFuture<?> deadline = deadlineTimer.getAndSet(null);
            if (deadline != null) {
                deadline.cancel(false);
            }
            final ScheduledFuture<?> scheduled = pending.getAndSet(null);
            if (scheduled != null) {
                scheduled.cancel(false);
            }
        }

        private void attempt(final int number) {
            if (finished.get()) {
                return;
            }
            if (deadlineExpired()) {
                finishWithLastFailure();
                return;
            }

            final CancellationToken attemptToken = token.child();
            final AsyncContext<F> attemptContext = context.withCancellation(attemptToken);
            final CompletableFuture<Result<F, S>> stage;
            try {
                stage =
                        Objects.requireNonNull(attempt.map(attemptContext), "attempt returned null")
                                .toCompletableFuture();
            } catch (final RuntimeException failure) {
                DefaultCancellationToken.detach(attemptToken);
                handle(number, Result.failure(context.failures().fromException(failure)));
                return;
            }

            final ScheduledFuture<?> timer = scheduleAttemptTimeout(attemptToken);
            stage.whenComplete(
                    (result, error) -> {
                        if (timer != null) {
                            timer.cancel(false);
                        }
                        DefaultCancellationToken.detach(attemptToken);
                        if (result != null) {
                            handle(number, result);
                            return;
                        }
                        if (error == null) {
                            finishExceptionally(
                                    new NullPointerException("attempt produced no result"));
                            return;
                        }
                        final Throwable cause = Anomalies.unwrap(error);
                        if (cause instanceof Error fatal) {
                            finishExceptionally(fatal);
                            return;
                        }
                        handle(
                                number,
                                Result.failure(
                                        context.failures()
                                                .fromException(Anomalies.asException(cause))));
                    });
        }

        private void handle(final int number, final Result<F, S> result) {
            if (finished.get()) {
                return;
            }
            if (result.isSuccess().toBoolean()) {
                finish(result);
                return;
            }
            final F failure = Anomalies.leftOf(result);
            final List<F> earlier = List.copyOf(previousFailures);
            previousFailures.add(failure);
            if (!shouldRetry(number, failure)) {
                finish(Result.failure(withHistory(failure, earlier)));
                return;
            }
            final long delayNanos = delayNanos(number, failure);
            if (delayNanos < 0L) {
                finish(Result.failure(withHistory(failure, earlier)));
                return;
            }
            scheduleNext(number + 1, delayNanos, failure, earlier);
        }

        private boolean shouldRetry(final int number, final F failure) {
            if (!policy.hasAttemptsLeft(number)) {
                return false;
            }
            if (token.isCancelled().toBoolean()) {
                return false;
            }
            if (Anomalies.isCancellation(failure)) {
                return false;
            }
            return context.failures().retryable().test(failure);
        }

        /**
         * Computes the delay, or returns a negative value when the overall deadline leaves no room
         * for another attempt.
         */
        private long delayNanos(final int number, final F failure) {
            Duration delay = policy.backoff().delayFor(number);
            if (failure instanceof SystemError error) {
                final Duration floor = error.retryAfter().or(Duration.ZERO);
                if (floor.compareTo(delay) > 0) {
                    delay = floor;
                }
            }
            final long nanos;
            try {
                nanos = delay.toNanos();
            } catch (final ArithmeticException overflow) {
                return -1L;
            }
            final long bounded = Math.max(nanos, MIN_DELAY_NANOS);
            if (deadlineNanos != Long.MAX_VALUE) {
                final long remaining = deadlineNanos - System.nanoTime();
                if (remaining <= 0L || bounded >= remaining) {
                    return -1L;
                }
            }
            return bounded;
        }

        private void scheduleNext(
                final int number, final long delayNanos, final F failure, final List<F> earlier) {
            final ScheduledFuture<?> scheduled;
            try {
                scheduled =
                        context.scheduler()
                                .schedule(() -> attempt(number), delayNanos, TimeUnit.NANOSECONDS);
            } catch (final RejectedExecutionException rejection) {
                finish(Result.failure(withHistory(failure, earlier)));
                return;
            }
            pending.set(scheduled);
            if (finished.get() || token.isCancelled().toBoolean()) {
                scheduled.cancel(false);
            }
        }

        private ScheduledFuture<?> scheduleAttemptTimeout(final CancellationToken attemptToken) {
            final Duration timeout = policy.attemptTimeout().orNull();
            if (timeout == null) {
                return null;
            }
            if (timeout.isZero()) {
                attemptToken.cancel(CancellationReason.DEADLINE);
                return null;
            }
            try {
                return context.scheduler()
                        .schedule(
                                () -> attemptToken.cancel(CancellationReason.DEADLINE),
                                timeout.toNanos(),
                                TimeUnit.NANOSECONDS);
            } catch (final RejectedExecutionException rejection) {
                attemptToken.cancel(CancellationReason.DEADLINE);
                return null;
            }
        }

        private void finishWithLastFailure() {
            if (previousFailures.isEmpty()) {
                finish(
                        Result.failure(
                                context.failures().fromAnomaly(Anomalies.forDeadline(context))));
                return;
            }
            final F last = previousFailures.get(previousFailures.size() - 1);
            finish(
                    Result.failure(
                            withHistory(
                                    last,
                                    previousFailures.subList(0, previousFailures.size() - 1))));
        }

        private void finish(final Result<F, S> result) {
            if (finished.compareAndSet(false, true)) {
                deregistration.get().run();
                cancelTimers();
                DefaultCancellationToken.detach(token);
                target.complete(result);
            }
        }

        private void finishExceptionally(final Throwable fatal) {
            if (finished.compareAndSet(false, true)) {
                deregistration.get().run();
                cancelTimers();
                DefaultCancellationToken.detach(token);
                target.completeExceptionally(fatal);
            }
        }

        @SuppressWarnings("unchecked")
        private F withHistory(final F failure, final List<F> earlier) {
            if (earlier.isEmpty() || !(failure instanceof SystemError current)) {
                return failure;
            }
            SystemError enriched = current;
            for (final F old : earlier) {
                if (old instanceof SystemError previousError) {
                    enriched = enriched.withSuppressedException(previousError.toException());
                }
            }
            return (F) enriched;
        }

        private boolean deadlineExpired() {
            return deadlineNanos != Long.MAX_VALUE && System.nanoTime() - deadlineNanos >= 0L;
        }

        private static long saturatedAdd(final long left, final long right) {
            final long sum = left + right;
            if (((left ^ sum) & (right ^ sum)) < 0L) {
                return Long.MAX_VALUE;
            }
            return sum;
        }
    }
}
