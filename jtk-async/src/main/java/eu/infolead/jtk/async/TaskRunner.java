package eu.infolead.jtk.async;

import eu.infolead.jtk.fp.CheckedRunnable;
import eu.infolead.jtk.fp.CheckedSupplier;
import eu.infolead.jtk.fp.either.Result;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Runs one computation on an executor so that its outcome is always a {@link Result} and
 * cancellation reaches the worker thread.
 *
 * <p>Invariants enforced here:
 *
 * <ul>
 *   <li>every outcome is published through {@link CompletableFuture#complete}, and the first
 *       publisher wins;
 *   <li>an {@link InterruptedException} becomes a failure value and the interrupt flag is cleared
 *       before the thread returns to its pool;
 *   <li>an {@link Error} completes the future exceptionally and is rethrown;
 *   <li>a task that was cancelled before it started does not run at all.
 * </ul>
 */
final class TaskRunner {

    private TaskRunner() {
        // Utility class
    }

    /**
     * Submits a computation and returns its anomaly-complete future.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param context the operation context
     * @param work the computation
     * @return the future that completes with the computation's result
     */
    static <F, S> CompletableFuture<Result<F, S>> submit(
            final AsyncContext<F> context, final CheckedSupplier<S> work) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(work, "work");

        final CompletableFuture<Result<F, S>> target = new CompletableFuture<>();
        final CancellationToken token = context.cancellation();
        if (token.isCancelled().toBoolean()) {
            target.complete(
                    Result.failure(
                            context.failures()
                                    .fromAnomaly(Anomalies.forCancellation(context, token))));
            return target;
        }
        try {
            context.executor().execute(() -> execute(context, token, work, target));
        } catch (final RuntimeException rejection) {
            target.complete(Result.failure(context.failures().fromException(rejection)));
        }
        return target;
    }

    /**
     * Submits a side effect and returns a future whose success carries no value.
     *
     * @param <F> the failure type
     * @param context the operation context
     * @param work the side effect
     * @return the future that completes with an empty success or a failure
     */
    static <F> CompletableFuture<Result<F, Void>> submitRun(
            final AsyncContext<F> context, final CheckedRunnable work) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(work, "work");

        final CompletableFuture<Result<F, Void>> target = new CompletableFuture<>();
        final CancellationToken token = context.cancellation();
        if (token.isCancelled().toBoolean()) {
            target.complete(
                    Result.failure(
                            context.failures()
                                    .fromAnomaly(Anomalies.forCancellation(context, token))));
            return target;
        }
        try {
            context.executor().execute(() -> executeRun(context, token, work, target));
        } catch (final RuntimeException rejection) {
            target.complete(Result.failure(context.failures().fromException(rejection)));
        }
        return target;
    }

    private static <F> void executeRun(
            final AsyncContext<F> context,
            final CancellationToken token,
            final CheckedRunnable work,
            final CompletableFuture<Result<F, Void>> target) {
        final Thread worker = Thread.currentThread();
        final Runnable deregister =
                token.onCancel(
                        () -> {
                            final boolean cancelled =
                                    target.complete(
                                            Result.failure(
                                                    context.failures()
                                                            .fromAnomaly(
                                                                    Anomalies.forCancellation(
                                                                            context, token))));
                            if (cancelled) {
                                worker.interrupt();
                            }
                        });
        try {
            if (token.isCancelled().toBoolean()) {
                return;
            }
            try {
                work.run();
            } catch (final InterruptedException interrupted) {
                target.complete(
                        Result.failure(
                                context.failures()
                                        .fromAnomaly(
                                                Anomalies.forInterruption(context, interrupted))));
                return;
            } catch (final Error fatal) {
                target.completeExceptionally(fatal);
                throw fatal;
            } catch (final Exception failure) {
                target.complete(Result.failure(context.failures().fromException(failure)));
                return;
            }
            target.complete(Result.success());
        } finally {
            deregister.run();
            Thread.interrupted();
        }
    }

    private static <F, S> void execute(
            final AsyncContext<F> context,
            final CancellationToken token,
            final CheckedSupplier<S> work,
            final CompletableFuture<Result<F, S>> target) {
        final Thread worker = Thread.currentThread();
        final Runnable deregister =
                token.onCancel(
                        () -> {
                            final boolean cancelled =
                                    target.complete(
                                            Result.failure(
                                                    context.failures()
                                                            .fromAnomaly(
                                                                    Anomalies.forCancellation(
                                                                            context, token))));
                            if (cancelled) {
                                worker.interrupt();
                            }
                        });
        try {
            if (token.isCancelled().toBoolean()) {
                return;
            }
            final S value;
            try {
                value = work.get();
            } catch (final InterruptedException interrupted) {
                target.complete(
                        Result.failure(
                                context.failures()
                                        .fromAnomaly(
                                                Anomalies.forInterruption(context, interrupted))));
                return;
            } catch (final Error fatal) {
                target.completeExceptionally(fatal);
                throw fatal;
            } catch (final Exception failure) {
                target.complete(Result.failure(context.failures().fromException(failure)));
                return;
            }
            try {
                target.complete(Result.success(value));
            } catch (final NullPointerException nullSuccess) {
                target.completeExceptionally(nullSuccess);
            }
        } finally {
            deregister.run();
            Thread.interrupted();
        }
    }
}
