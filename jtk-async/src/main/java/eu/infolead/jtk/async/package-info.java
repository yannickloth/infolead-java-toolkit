/**
 * Asynchronous railway: {@link java.util.concurrent.CompletableFuture} carrying {@link
 * eu.infolead.jtk.fp.either.Result} values.
 *
 * <h2>Why a module</h2>
 *
 * <p>{@code CompletableFuture} is an excellent scheduling primitive and a poor error model. {@link
 * eu.infolead.jtk.fp.either.Result} is an excellent error model and knows nothing about time,
 * threads or cancellation. This module is the seam between them: it never introduces a second
 * result type and never changes the semantics of either side.
 *
 * <h2>The canonical value</h2>
 *
 * <pre>{@code CompletableFuture<Result<F, S>>}</pre>
 *
 * <p>Every operation accepts and returns this shape, so existing railway operators, anomaly
 * formatting, localization and ProblemDetail mapping keep working unchanged. The {@link
 * eu.infolead.jtk.async.Async} class supplies the missing combinators and the two boundaries:
 * throwing code becomes a result, and a result becomes a future.
 *
 * <h2>Failure values, not exceptions</h2>
 *
 * <p>A future produced here completes normally with a failure value for every ordinary problem. It
 * completes exceptionally only for {@link Error} instances, exactly as {@code Result.fromTry}
 * refuses to capture JVM failures. An application that wants {@link
 * eu.infolead.jtk.anomaly.SystemError} failures uses {@link
 * eu.infolead.jtk.async.FailureStrategy#systemErrors()}; an application with its own failure type
 * supplies three functions.
 *
 * <h2>Cancellation</h2>
 *
 * <p>{@link java.util.concurrent.CompletableFuture#cancel(boolean)} does not interrupt the task
 * computing the value. Cancellation therefore travels through {@link
 * eu.infolead.jtk.async.CancellationToken}: cancelling the token completes the operation with the
 * anomaly that matches the {@link eu.infolead.jtk.async.CancellationReason} and interrupts the
 * worker. Tokens form a tree, so cancelling a request cancels every operation derived from it.
 *
 * <p>The mapping from reason to anomaly is fixed:
 *
 * <ul>
 *   <li>{@code DEADLINE} becomes {@link
 *       eu.infolead.jtk.anomaly.PostconditionErrorType#OPERATION_TIMED_OUT} (HTTP 504, retryable by
 *       default);
 *   <li>every other reason becomes {@link
 *       eu.infolead.jtk.anomaly.PostconditionErrorType#OPERATION_CANCELLED} (HTTP 408, never
 *       retried).
 * </ul>
 *
 * <h2>Time and retries</h2>
 *
 * <p>Deadlines use a shared daemon timer and cancel through the same token, so an expired operation
 * is interrupted rather than abandoned. {@link eu.infolead.jtk.async.Retries} waits on that timer
 * too: a retry that is backing off holds no thread. Every attempt consults {@link
 * eu.infolead.jtk.anomaly.SystemError#isRetryable()} (via the failure strategy) and honours {@code
 * Retry-After} as a lower bound.
 *
 * <h2>Bounded fan-out</h2>
 *
 * <p>{@link eu.infolead.jtk.async.Parallel} runs at most {@link
 * eu.infolead.jtk.async.Parallelism#maxConcurrency()} operations at a time, launches the next one
 * as soon as a slot frees up, preserves input order, and completes only after every launched
 * operation has settled. Coordination is non-blocking, so the executor may be shared with the work.
 *
 * <h2>A complete use case</h2>
 *
 * <p>Fetch one invoice per customer with bounded concurrency, three attempts per invoice, a
 * two-second budget per attempt, and a thirty-second budget for the whole batch:
 *
 * <pre>{@code
 * ExecutorService pool = Executors.newFixedThreadPool(16);
 * AsyncContext<SystemError> context = AsyncContext.systemErrors(pool)
 *         .withName("invoice-sync");
 *
 * RetryPolicy policy = RetryPolicy.of(3, Backoff.exponential(
 *                 Duration.ofMillis(100), 2.0d, Duration.ofSeconds(2)))
 *         .withAttemptTimeout(Duration.ofSeconds(2));
 *
 * CompletableFuture<Result<SystemError, List<Invoice>>> batch = Parallel.traverse(
 *         context, Parallelism.of(8), customerIds,
 *         (customerId, scoped) -> Retries.run(scoped, policy,
 *                 attempt -> Async.within(attempt, Duration.ofSeconds(10),
 *                         bounded -> Async.supply(bounded,
 *                                 () -> billingClient.invoicesFor(customerId)))));
 *
 * Result<SystemError, List<Invoice>> outcome =
 *         Async.await(context, batch, Maybe.of(Duration.ofSeconds(30)));
 * }</pre>
 *
 * <p>{@code outcome} is a value in every non-fatal case: the invoice list in input order, or the
 * lowest-index failure (fail-fast) mapped to the application's anomaly model. If the batch deadline
 * elapses, {@code outcome} carries {@code OPERATION_TIMED_OUT}; if the caller cancels {@code
 * context}, it carries {@code OPERATION_CANCELLED}.
 *
 * <h2>Threading rules</h2>
 *
 * <ul>
 *   <li>The executor is always explicit. There is no hidden {@code ForkJoinPool.commonPool()}.
 *   <li>No library code sleeps, joins, or blocks a worker thread.
 *   <li>A worker that observed an interrupt publishes the failure value and clears the interrupt
 *       flag before returning to its pool; the owner of a thread is responsible for the flag.
 *   <li>Futures, tokens and contexts are runtime handles and are not serializable; {@link
 *       eu.infolead.jtk.fp.either.Result} values remain serializable.
 * </ul>
 */
package eu.infolead.jtk.async;
