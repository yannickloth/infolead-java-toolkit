package eu.infolead.jtk.async;

import eu.infolead.jtk.fp.Fn;
import eu.infolead.jtk.fp.either.Result;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coordinated fan-out with a concurrency bound.
 *
 * <p>The coordinator never blocks and never calls {@code join()}: a completion callback launches
 * the next item, so sharing the executor between coordination and work cannot deadlock.
 *
 * <p>Every launched item receives its own child token. A fail-fast traversal cancels the batch,
 * which cancels all item tokens and therefore interrupts their workers. An item that completed with
 * a genuine result before the cancellation is kept; an item that only completed because it was
 * cancelled is excluded from the partition.
 *
 * @param <F> the failure type
 * @param <T> the input element type
 * @param <S> the success type
 */
final class FanOut<F, T, S> {

    private final AsyncContext<F> context;
    private final int maxConcurrency;
    private final List<T> items;
    private final Fn.FN2<? extends CompletionStage<Result<F, S>>, ? super T, AsyncContext<F>>
            mapper;
    private final boolean failFast;
    private final CancellationToken batch;

    private final CompletableFuture<Partition<F, S>> target = new CompletableFuture<>();
    private final Object lock = new Object();
    private final AtomicBoolean pumping = new AtomicBoolean();
    private final Object[] values;
    private final Object[] failures;
    private final boolean[] settled;
    private final boolean[] cancelled;

    private int next;
    private int active;
    private boolean stopped;
    private boolean finished;

    FanOut(
            final AsyncContext<F> context,
            final Parallelism parallelism,
            final List<T> items,
            final Fn.FN2<? extends CompletionStage<Result<F, S>>, ? super T, AsyncContext<F>>
                    mapper,
            final boolean failFast) {
        this.context = Objects.requireNonNull(context, "context");
        this.maxConcurrency = Objects.requireNonNull(parallelism, "parallelism").maxConcurrency();
        this.items = Objects.requireNonNull(items, "items");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.failFast = failFast;
        this.batch = context.cancellation().child();
        this.values = new Object[items.size()];
        this.failures = new Object[items.size()];
        this.settled = new boolean[items.size()];
        this.cancelled = new boolean[items.size()];
    }

    /**
     * Starts the traversal.
     *
     * @return the future completing with the ordered partition
     */
    CompletableFuture<Partition<F, S>> run() {
        pump();
        return target;
    }

    private void pump() {
        if (!pumping.compareAndSet(false, true)) {
            return;
        }
        try {
            while (true) {
                final int index;
                synchronized (lock) {
                    if (stopped || next >= items.size() || active >= maxConcurrency) {
                        break;
                    }
                    index = next++;
                    active++;
                }
                launch(index);
            }
        } finally {
            pumping.set(false);
        }
        completeIfSettled();
    }

    private void launch(final int index) {
        final CancellationToken itemToken = batch.child();
        final AsyncContext<F> itemContext = context.withCancellation(itemToken);
        final CompletableFuture<Result<F, S>> task;
        try {
            final CompletionStage<Result<F, S>> stage = mapper.apply(items.get(index), itemContext);
            task = Objects.requireNonNull(stage, "mapper returned null").toCompletableFuture();
        } catch (final RuntimeException failure) {
            settle(index, Result.failure(context.failures().fromException(failure)), itemToken);
            return;
        }
        task.whenComplete(
                (result, error) -> {
                    if (result != null) {
                        settle(index, result, itemToken);
                        return;
                    }
                    if (error == null) {
                        target.completeExceptionally(
                                new NullPointerException("stage produced no result"));
                        return;
                    }
                    final Throwable cause = Anomalies.unwrap(error);
                    if (cause instanceof Error fatal) {
                        target.completeExceptionally(fatal);
                        return;
                    }
                    settle(
                            index,
                            Result.failure(
                                    context.failures().fromException(Anomalies.asException(cause))),
                            itemToken);
                });
    }

    private void settle(
            final int index, final Result<F, S> result, final CancellationToken itemToken) {
        boolean cancelBatch = false;
        synchronized (lock) {
            if (!settled[index]) {
                settled[index] = true;
                if (result.isSuccess().toBoolean()) {
                    values[index] = Anomalies.rightOf(result);
                } else {
                    final F failure = Anomalies.leftOf(result);
                    final boolean siblingCancellation =
                            failFast
                                    && stopped
                                    && Anomalies.isCancellation(failure)
                                    && itemToken.reason().or(CancellationReason.CALLER)
                                            == CancellationReason.FAIL_FAST;
                    if (siblingCancellation) {
                        cancelled[index] = true;
                    } else {
                        failures[index] = failure;
                        if (failFast && !stopped) {
                            stopped = true;
                            cancelBatch = true;
                        }
                    }
                }
            }
            active--;
        }
        DefaultCancellationToken.detach(itemToken);
        if (cancelBatch) {
            batch.cancel(CancellationReason.FAIL_FAST);
        }
        pump();
    }

    private void completeIfSettled() {
        final boolean done;
        synchronized (lock) {
            done = !finished && active == 0 && (stopped || next >= items.size());
            if (done) {
                finished = true;
            }
        }
        if (!done) {
            return;
        }
        target.complete(partition());
        DefaultCancellationToken.detach(batch);
    }

    private Partition<F, S> partition() {
        final List<F> failureList = new ArrayList<>();
        final List<S> successList = new ArrayList<>();
        synchronized (lock) {
            for (int index = 0; index < items.size(); index++) {
                if (!settled[index] || cancelled[index]) {
                    continue;
                }
                if (failures[index] != null) {
                    failureList.add(castFailure(failures[index]));
                } else {
                    successList.add(castSuccess(values[index]));
                }
            }
        }
        return new Partition<>(failureList, successList);
    }

    @SuppressWarnings("unchecked")
    private F castFailure(final Object failure) {
        return (F) failure;
    }

    @SuppressWarnings("unchecked")
    private S castSuccess(final Object value) {
        return (S) value;
    }
}
