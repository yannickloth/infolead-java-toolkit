package eu.infolead.jtk.async;

import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.CheckedSupplier;
import eu.infolead.jtk.fp.either.Result;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The infrastructure an asynchronous operation runs on: an executor, a timer, a cancellation token,
 * and the strategy that turns failures into the application's failure type.
 *
 * <p>A context is immutable; the {@code with...} methods return copies. Create one per request or
 * per operation scope and derive narrower contexts from it, for example a child token for one batch
 * of work.
 *
 * <p>Contexts are runtime handles and are not serializable: they hold thread pools and tokens, not
 * application state.
 *
 * <pre>{@code
 * ExecutorService pool = Executors.newFixedThreadPool(8);
 * AsyncContext<SystemError> context = AsyncContext.systemErrors(pool)
 *         .withName("loadOrder")
 *         .withCancellation(CancellationToken.create()); // optional owner
 *
 * CompletableFuture<Result<SystemError, Order>> order =
 *         context.supply(() -> repository.load(orderId));
 * }</pre>
 *
 * @param <F> the failure type
 * @param name the operation name used in synthesized anomaly messages
 * @param executor the executor that runs the operations
 * @param scheduler the scheduler that enforces deadlines and retry delays
 * @param cancellation the token that cancels the operations
 * @param failures the strategy that adapts anomalies to {@code F}
 */
public record AsyncContext<F>(
        String name,
        Executor executor,
        ScheduledExecutorService scheduler,
        CancellationToken cancellation,
        FailureStrategy<F> failures) {

    /** The operation name used when a caller does not supply one. */
    public static final String DEFAULT_NAME = "asynchronous operation";

    /**
     * Canonical constructor with validation.
     *
     * @param name the operation name
     * @param executor the executor
     * @param scheduler the deadline and retry scheduler
     * @param cancellation the cancellation token
     * @param failures the failure strategy
     */
    public AsyncContext {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(cancellation, "cancellation");
        Objects.requireNonNull(failures, "failures");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    /**
     * Creates a context for applications that use {@link SystemError} as their failure type.
     *
     * <p>The context uses the shared daemon timer and a non-cancellable token. Deadlines, fail-fast
     * traversals and retries derive their own child scopes, so they work without further setup. Use
     * {@link #withCancellation(CancellationToken)} or {@link #withChildCancellation()} when an
     * owner must be able to cancel the operation explicitly.
     *
     * @param executor the executor that runs the operations
     * @return a system-error context
     */
    public static AsyncContext<SystemError> systemErrors(final Executor executor) {
        return new AsyncContext<>(
                DEFAULT_NAME,
                executor,
                Timers.shared(),
                CancellationToken.none(),
                FailureStrategy.systemErrors());
    }

    /**
     * Creates a fully specified context.
     *
     * @param <F> the failure type
     * @param name the operation name
     * @param executor the executor
     * @param scheduler the deadline and retry scheduler
     * @param cancellation the cancellation token
     * @param failures the failure strategy
     * @return the context
     */
    public static <F> AsyncContext<F> of(
            final String name,
            final Executor executor,
            final ScheduledExecutorService scheduler,
            final CancellationToken cancellation,
            final FailureStrategy<F> failures) {
        return new AsyncContext<>(name, executor, scheduler, cancellation, failures);
    }

    /**
     * Returns a copy with a different operation name.
     *
     * @param newName the new name
     * @return the copy
     */
    public AsyncContext<F> withName(final String newName) {
        return new AsyncContext<>(newName, executor, scheduler, cancellation, failures);
    }

    /**
     * Returns a copy running on a different executor.
     *
     * @param newExecutor the executor
     * @return the copy
     */
    public AsyncContext<F> withExecutor(final Executor newExecutor) {
        return new AsyncContext<>(name, newExecutor, scheduler, cancellation, failures);
    }

    /**
     * Returns a copy using a different scheduler.
     *
     * @param newScheduler the scheduler
     * @return the copy
     */
    public AsyncContext<F> withScheduler(final ScheduledExecutorService newScheduler) {
        return new AsyncContext<>(name, executor, newScheduler, cancellation, failures);
    }

    /**
     * Returns a copy using a different cancellation token.
     *
     * @param newCancellation the token
     * @return the copy
     */
    public AsyncContext<F> withCancellation(final CancellationToken newCancellation) {
        return new AsyncContext<>(name, executor, scheduler, newCancellation, failures);
    }

    /**
     * Returns a copy whose token is a child of this context's token.
     *
     * <p>Cancelling the returned context cancels only its operations; cancelling this context still
     * cancels them all.
     *
     * @return a copy with a child token
     */
    public AsyncContext<F> withChildCancellation() {
        return withCancellation(cancellation.child());
    }

    /**
     * Returns a copy with a different failure strategy.
     *
     * @param <G> the new failure type
     * @param newFailures the strategy
     * @return the copy
     */
    public <G> AsyncContext<G> withFailures(final FailureStrategy<G> newFailures) {
        return new AsyncContext<>(name, executor, scheduler, cancellation, newFailures);
    }

    /**
     * Runs a computation on this context's executor and captures its failure.
     *
     * <p>This is the primary way to create an operation that participates in cancellation,
     * deadlines and retries. The returned future completes with a {@link Result}; it completes
     * exceptionally only when the computation throws an {@link Error}.
     *
     * @param <S> the success type
     * @param work the computation, which may throw a checked exception
     * @return the anomaly-complete future
     */
    public <S> CompletableFuture<Result<F, S>> supply(final CheckedSupplier<S> work) {
        return TaskRunner.submit(this, work);
    }
}
