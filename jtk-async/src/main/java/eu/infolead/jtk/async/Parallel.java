package eu.infolead.jtk.async;

import eu.infolead.jtk.fp.Fn;
import eu.infolead.jtk.fp.either.Result;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Bounded, non-blocking traversal of a collection into the railway.
 *
 * <p>Results preserve input order regardless of completion order. Coordination is
 * completion-driven: each finished item launches the next one, so there are never more than the
 * requested number of in-flight operations and no thread ever waits for another task on the same
 * executor.
 *
 * <h2>Choosing a variant</h2>
 *
 * <ul>
 *   <li>{@link #traverse} is fail-fast: the first genuine failure stops new launches, cancels the
 *       in-flight items, and is returned. Among the failures observed, the one with the lowest
 *       input index wins, so the outcome is reproducible.
 *   <li>{@link #traverseAll} runs every item and returns successes and failures side by side, in
 *       input order.
 *   <li>{@link #sequence} is {@code traverse} over futures that are already running.
 * </ul>
 *
 * <p>An empty collection completes immediately with an empty success. A rejected executor
 * submission becomes a failure value, never an exceptional completion.
 *
 * <pre>{@code
 * CompletableFuture<Result<SystemError, List<Invoice>>> invoices =
 *         Parallel.traverse(context, Parallelism.of(8), customerIds,
 *                 (id, scoped) -> Async.supply(scoped,
 *                         () -> billingClient.invoicesFor(id)));
 * }</pre>
 */
public final class Parallel {

    private Parallel() {
        // Utility class
    }

    /**
     * Runs the mapper for every item, stopping at the first failure.
     *
     * @param <F> the failure type
     * @param <T> the input element type
     * @param <S> the success type
     * @param context the operation context
     * @param parallelism the concurrency bound
     * @param items the items to process; must not contain {@code null}
     * @param mapper the operation, receiving the item and its scoped context
     * @return a future with all successes in input order, or the lowest-index failure
     */
    public static <F, T, S> CompletableFuture<Result<F, List<S>>> traverse(
            final AsyncContext<F> context,
            final Parallelism parallelism,
            final Collection<? extends T> items,
            final Fn.FN2<? extends CompletionStage<Result<F, S>>, ? super T, AsyncContext<F>>
                    mapper) {
        return fanOut(context, parallelism, items, mapper, true).thenApply(Partition::toResult);
    }

    /**
     * Runs the mapper for every item and keeps every outcome.
     *
     * @param <F> the failure type
     * @param <T> the input element type
     * @param <S> the success type
     * @param context the operation context
     * @param parallelism the concurrency bound
     * @param items the items to process; must not contain {@code null}
     * @param mapper the operation, receiving the item and its scoped context
     * @return a future with the ordered partition of failures and successes
     */
    public static <F, T, S> CompletableFuture<Partition<F, S>> traverseAll(
            final AsyncContext<F> context,
            final Parallelism parallelism,
            final Collection<? extends T> items,
            final Fn.FN2<? extends CompletionStage<Result<F, S>>, ? super T, AsyncContext<F>>
                    mapper) {
        return fanOut(context, parallelism, items, mapper, false);
    }

    /**
     * Awaits a collection of already started futures with a concurrency bound.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param context the operation context
     * @param parallelism the concurrency bound
     * @param stages the futures to await; must not contain {@code null}
     * @return a future with all successes in input order, or the lowest-index failure
     */
    public static <F, S> CompletableFuture<Result<F, List<S>>> sequence(
            final AsyncContext<F> context,
            final Parallelism parallelism,
            final Collection<? extends CompletionStage<Result<F, S>>> stages) {
        return traverse(context, parallelism, stages, (stage, scoped) -> stage);
    }

    private static <F, T, S> CompletableFuture<Partition<F, S>> fanOut(
            final AsyncContext<F> context,
            final Parallelism parallelism,
            final Collection<? extends T> items,
            final Fn.FN2<? extends CompletionStage<Result<F, S>>, ? super T, AsyncContext<F>>
                    mapper,
            final boolean failFast) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(parallelism, "parallelism");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(mapper, "mapper");
        final List<T> snapshot = List.copyOf(items);
        return new FanOut<>(context, parallelism, snapshot, mapper, failFast).run();
    }
}
