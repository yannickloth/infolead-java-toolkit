package eu.infolead.jtk.async;

import eu.infolead.jtk.fp.CheckedRunnable;
import eu.infolead.jtk.fp.CheckedSupplier;
import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Result;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Bridges between the synchronous railway ({@link Result}, {@link Either}) and {@link
 * CompletableFuture}.
 *
 * <p>The canonical asynchronous value is {@code CompletableFuture<Result<F, S>>}. This class adds
 * the operations the JDK does not provide: translating exceptional completion into failure values,
 * preserving the error-versus-exception policy of {@code Result.fromTry}, composing railway steps,
 * and enforcing a deadline with a real interruption.
 *
 * <h2>Invariant: anomaly-complete futures</h2>
 *
 * <p>A future produced by this toolkit completes normally with a {@link Result} for every ordinary
 * failure. It completes exceptionally only when a computation throws an {@link Error}; those are
 * JVM failures and are deliberately not converted into values, matching {@link Result#fromTry}.
 *
 * <h2>Cancellation</h2>
 *
 * <p>A plain {@link CompletableFuture} cannot interrupt its producer. Cancellation therefore
 * travels through the {@link CancellationToken} held by the {@link AsyncContext}: use {@link
 * #supply(AsyncContext, CheckedSupplier)} or {@link #within(AsyncContext, Duration, Mapper)} rather
 * than {@link CompletableFuture#cancel(boolean)}.
 */
public final class Async {

    private Async() {
        // Utility class
    }

    /**
     * Wraps an already computed result.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param result the result
     * @return a completed future carrying the result
     */
    public static <F, S> CompletableFuture<Result<F, S>> completed(final Result<F, S> result) {
        return CompletableFuture.completedFuture(Objects.requireNonNull(result, "result"));
    }

    /**
     * Creates a completed success.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param value the success value
     * @return a completed future carrying the success
     */
    public static <F, S> CompletableFuture<Result<F, S>> success(final S value) {
        return completed(Result.success(value));
    }

    /**
     * Creates a completed failure.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param anomaly the failure value
     * @return a completed future carrying the failure
     */
    public static <F, S> CompletableFuture<Result<F, S>> failure(final F anomaly) {
        return completed(Result.failure(anomaly));
    }

    /**
     * Adapts a plain {@link Either} into the railway.
     *
     * <p>{@link Result} is already an {@code Either}, so this is the bridge for values produced by
     * code that uses the general two-track type. The returned future is already completed.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param either the value to lift
     * @return a completed future carrying the same value as a result
     */
    public static <F, S> CompletableFuture<Result<F, S>> fromEither(final Either<F, S> either) {
        Objects.requireNonNull(either, "either");
        return completed(either.fold(Result::failure, Result::success));
    }

    /**
     * Runs a value-producing computation on the context's executor.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param context the operation context
     * @param work the computation, which may throw a checked exception
     * @return the anomaly-complete future
     */
    public static <F, S> CompletableFuture<Result<F, S>> supply(
            final AsyncContext<F> context, final CheckedSupplier<S> work) {
        return TaskRunner.submit(context, work);
    }

    /**
     * Runs a side effect on the context's executor.
     *
     * @param <F> the failure type
     * @param context the operation context
     * @param work the side effect, which may throw a checked exception
     * @return a future completing with an empty success or a failure
     */
    public static <F> CompletableFuture<Result<F, Void>> run(
            final AsyncContext<F> context, final CheckedRunnable work) {
        return TaskRunner.submitRun(context, work);
    }

    /**
     * Adapts a foreign {@link CompletionStage} into the railway.
     *
     * <p>Exceptional completion is translated into a failure value. A cancellation becomes the
     * cancelled anomaly, an {@link Error} propagates, and an {@link InterruptedException} becomes a
     * cancelled anomaly with the interrupt status restored.
     *
     * <p>Cancellation of the foreign stage cannot interrupt whatever produces it: only futures
     * created from a jtk-async context are interruptible.
     *
     * @param <F> the failure type
     * @param <T> the success type
     * @param context the operation context
     * @param stage the foreign stage
     * @return the anomaly-complete future
     */
    public static <F, T> CompletableFuture<Result<F, T>> fromStage(
            final AsyncContext<F> context, final CompletionStage<T> stage) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(stage, "stage");
        final CompletableFuture<Result<F, T>> target = new CompletableFuture<>();
        stage.whenComplete(
                (value, error) -> {
                    if (error == null) {
                        try {
                            target.complete(Result.success(value));
                        } catch (final NullPointerException nullSuccess) {
                            target.completeExceptionally(nullSuccess);
                        }
                        return;
                    }
                    final Throwable cause = Anomalies.unwrap(error);
                    if (cause instanceof Error fatal) {
                        target.completeExceptionally(fatal);
                        return;
                    }
                    if (cause instanceof InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        target.complete(
                                Result.failure(
                                        context.failures()
                                                .fromAnomaly(
                                                        Anomalies.forInterruption(
                                                                context, interrupted))));
                        return;
                    }
                    if (cause instanceof CancellationException cancelled) {
                        target.complete(
                                Result.failure(
                                        context.failures()
                                                .fromAnomaly(
                                                        Anomalies.forForeignCancellation(
                                                                context, cancelled))));
                        return;
                    }
                    target.complete(
                            Result.failure(
                                    context.failures()
                                            .fromException(Anomalies.asException(cause))));
                });
        return target;
    }

    /**
     * Adapts a foreign {@link CompletableFuture} into the railway.
     *
     * @param <F> the failure type
     * @param <T> the success type
     * @param context the operation context
     * @param future the foreign future
     * @return the anomaly-complete future
     */
    public static <F, T> CompletableFuture<Result<F, T>> fromFuture(
            final AsyncContext<F> context, final CompletableFuture<T> future) {
        return fromStage(context, future);
    }

    /**
     * Maps the success value, leaving a failure untouched.
     *
     * @param <F> the failure type
     * @param <S> the current success type
     * @param <R> the new success type
     * @param future the future to map
     * @param mapper the success mapper
     * @return the mapped future
     */
    public static <F, S, R> CompletableFuture<Result<F, R>> map(
            final CompletableFuture<Result<F, S>> future, final Mapper<R, ? super S> mapper) {
        Objects.requireNonNull(future, "future");
        Objects.requireNonNull(mapper, "mapper");
        return future.thenApply(result -> result.map(mapper));
    }

    /**
     * Maps the failure value, leaving a success untouched.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param future the future to map
     * @param mapper the failure mapper
     * @return the mapped future
     */
    public static <F, S> CompletableFuture<Result<F, S>> mapFailure(
            final CompletableFuture<Result<F, S>> future, final Mapper<F, ? super F> mapper) {
        Objects.requireNonNull(future, "future");
        Objects.requireNonNull(mapper, "mapper");
        return future.thenApply(
                result ->
                        result.fold(
                                failure -> Result.<F, S>failure(mapper.map(failure)),
                                Result::success));
    }

    /**
     * Chains a railway step that itself returns a future.
     *
     * @param <F> the failure type
     * @param <S> the current success type
     * @param <R> the new success type
     * @param future the future to chain
     * @param mapper the step producing the next future
     * @return the chained future
     */
    public static <F, S, R> CompletableFuture<Result<F, R>> flatMap(
            final CompletableFuture<Result<F, S>> future,
            final Mapper<? extends CompletionStage<Result<F, R>>, ? super S> mapper) {
        Objects.requireNonNull(future, "future");
        Objects.requireNonNull(mapper, "mapper");
        return future.thenCompose(
                result ->
                        result.fold(
                                failure ->
                                        CompletableFuture.completedFuture(
                                                Result.<F, R>failure(failure)),
                                success -> mapper.map(success).toCompletableFuture()));
    }

    /**
     * Recovers from a failure, possibly by returning another failure.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param future the future to recover
     * @param recovery the recovery step
     * @return the recovered future
     */
    public static <F, S> CompletableFuture<Result<F, S>> recover(
            final CompletableFuture<Result<F, S>> future,
            final Mapper<Result<F, S>, ? super F> recovery) {
        Objects.requireNonNull(future, "future");
        Objects.requireNonNull(recovery, "recovery");
        return future.thenApply(result -> result.fold(recovery::map, Result::success));
    }

    /**
     * Folds both railway tracks into a single value.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param <R> the folded type
     * @param future the future to fold
     * @param failureMapper the failure mapper
     * @param successMapper the success mapper
     * @return the folded value
     */
    public static <F, S, R> CompletableFuture<R> fold(
            final CompletableFuture<Result<F, S>> future,
            final Mapper<? extends R, ? super F> failureMapper,
            final Mapper<? extends R, ? super S> successMapper) {
        Objects.requireNonNull(future, "future");
        Objects.requireNonNull(failureMapper, "failureMapper");
        Objects.requireNonNull(successMapper, "successMapper");
        return future.thenApply(result -> result.fold(failureMapper, successMapper));
    }

    /**
     * Blocks until the future completes, returning a railway value.
     *
     * <p>This is the boundary for synchronous callers such as servlet handlers and command-line
     * entry points. It never throws for an ordinary failure: an interrupted wait becomes a
     * non-retryable cancellation, and an elapsed deadline becomes a timeout anomaly. An {@link
     * Error} still propagates.
     *
     * <p>Its deadline bounds the <em>wait</em>, not the operation: the future keeps running. Use
     * {@link #within(AsyncContext, Duration, Mapper)} when the operation itself must be stopped at
     * the deadline.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param context the operation context
     * @param future the future to await
     * @param deadline the maximum time to wait, or {@link Maybe#none()} to wait indefinitely
     * @return the completed result
     */
    public static <F, S> Result<F, S> await(
            final AsyncContext<F> context,
            final CompletableFuture<Result<F, S>> future,
            final Maybe<Duration> deadline) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(future, "future");
        Objects.requireNonNull(deadline, "deadline");
        try {
            if (deadline.isPresent().toBoolean()) {
                final Duration limit = deadline.or(Duration.ZERO);
                return future.get(Math.max(0L, limit.toNanos()), TimeUnit.NANOSECONDS);
            }
            return future.get();
        } catch (final TimeoutException timedOut) {
            return Result.failure(context.failures().fromAnomaly(Anomalies.forDeadline(context)));
        } catch (final ExecutionException failed) {
            final Throwable cause = Anomalies.unwrap(failed);
            if (cause instanceof Error fatal) {
                throw fatal;
            }
            return Result.failure(context.failures().fromException(Anomalies.asException(cause)));
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return Result.failure(
                    context.failures()
                            .fromAnomaly(Anomalies.forInterruption(context, interrupted)));
        } catch (final CancellationException cancelled) {
            return Result.failure(
                    context.failures()
                            .fromAnomaly(Anomalies.forForeignCancellation(context, cancelled)));
        }
    }

    /**
     * Runs an operation under a deadline.
     *
     * <p>The deadline covers queueing, execution and everything the operation does, because it
     * cancels the scoped token supplied to the mapper. The operation must use that scoped context
     * for the cancellation to reach its worker thread.
     *
     * <pre>{@code
     * Async.within(context, Duration.ofSeconds(2),
     *         scoped -> Async.supply(scoped, () -> client.fetch(id)));
     * }</pre>
     *
     * <p>On expiry the future completes with the timeout anomaly and the scoped task is
     * interrupted. A foreign stage that ignores the scoped context completes with the timeout
     * anomaly but keeps running.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param context the operation context
     * @param deadline the maximum duration
     * @param scoped the operation, receiving a context with the deadline token
     * @return the bounded future
     * @throws IllegalArgumentException if the deadline is negative
     */
    public static <F, S> CompletableFuture<Result<F, S>> within(
            final AsyncContext<F> context,
            final Duration deadline,
            final Mapper<CompletableFuture<Result<F, S>>, AsyncContext<F>> scoped) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(deadline, "deadline");
        Objects.requireNonNull(scoped, "scoped");
        if (deadline.isNegative()) {
            throw new IllegalArgumentException("deadline must not be negative");
        }
        if (deadline.isZero()) {
            return CompletableFuture.completedFuture(
                    Result.failure(context.failures().fromAnomaly(Anomalies.forDeadline(context))));
        }

        final CancellationToken child = context.cancellation().child();
        if (child.isCancelled().toBoolean()) {
            DefaultCancellationToken.detach(child);
            return CompletableFuture.completedFuture(
                    Result.failure(
                            context.failures()
                                    .fromAnomaly(Anomalies.forCancellation(context, child))));
        }
        final AsyncContext<F> scopedContext = context.withCancellation(child);
        final ScheduledFuture<?> timer;
        try {
            timer =
                    context.scheduler()
                            .schedule(
                                    () -> child.cancel(CancellationReason.DEADLINE),
                                    deadline.toNanos(),
                                    TimeUnit.NANOSECONDS);
        } catch (final RejectedExecutionException rejection) {
            DefaultCancellationToken.detach(child);
            return CompletableFuture.completedFuture(
                    Result.failure(context.failures().fromException(rejection)));
        }

        // The aggregate completes on the deadline even when the scoped stage
        // ignores the token (a foreign future). The stage keeps running in that
        // case; only jtk-async tasks are actually interrupted.
        final CompletableFuture<Result<F, S>> aggregate = new CompletableFuture<>();
        final Runnable deregister =
                child.onCancel(
                        () ->
                                aggregate.complete(
                                        Result.failure(
                                                context.failures()
                                                        .fromAnomaly(
                                                                Anomalies.forCancellation(
                                                                        context, child)))));

        final CompletableFuture<Result<F, S>> bounded;
        try {
            bounded =
                    Objects.requireNonNull(scoped.map(scopedContext), "scoped mapper returned null")
                            .toCompletableFuture();
        } catch (final RuntimeException failure) {
            timer.cancel(false);
            deregister.run();
            DefaultCancellationToken.detach(child);
            aggregate.complete(Result.failure(context.failures().fromException(failure)));
            return aggregate;
        }
        bounded.whenComplete(
                (result, error) -> {
                    timer.cancel(false);
                    deregister.run();
                    DefaultCancellationToken.detach(child);
                    if (result != null) {
                        aggregate.complete(result);
                        return;
                    }
                    if (error == null) {
                        aggregate.completeExceptionally(
                                new NullPointerException("scoped stage produced no result"));
                        return;
                    }
                    final Throwable cause = Anomalies.unwrap(error);
                    if (cause instanceof Error fatal) {
                        aggregate.completeExceptionally(fatal);
                        return;
                    }
                    aggregate.complete(
                            Result.failure(
                                    context.failures()
                                            .fromException(Anomalies.asException(cause))));
                });
        return aggregate;
    }
}
