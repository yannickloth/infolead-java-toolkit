package eu.infolead.jtk.async;

import static eu.infolead.jtk.async.AsyncTestSupport.await;
import static eu.infolead.jtk.async.AsyncTestSupport.failureValue;
import static eu.infolead.jtk.async.AsyncTestSupport.newExecutor;
import static eu.infolead.jtk.async.AsyncTestSupport.shutdown;
import static eu.infolead.jtk.async.AsyncTestSupport.successValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.anomaly.ParameterError;
import eu.infolead.jtk.anomaly.PostconditionErrorType;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Result;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParallelTest {

    private ExecutorService executor;
    private AsyncContext<SystemError> context;

    @BeforeEach
    void setUp() {
        executor = newExecutor(4);
        context = AsyncContext.systemErrors(executor).withName("parallel-test");
    }

    @AfterEach
    void tearDown() {
        shutdown(executor);
    }

    @Test
    void resultsPreserveInputOrder() throws Exception {
        final List<Integer> items = IntStream.range(0, 20).boxed().collect(Collectors.toList());

        final Result<SystemError, List<String>> result =
                await(
                        Parallel.traverse(
                                context,
                                Parallelism.of(4),
                                items,
                                (item, scoped) ->
                                        Async.supply(
                                                scoped,
                                                () -> {
                                                    Thread.sleep(1 + (item % 5) * 3L);
                                                    return "v" + item;
                                                })));

        final List<String> expected =
                items.stream().map(item -> "v" + item).collect(Collectors.toList());
        assertEquals(expected, successValue(result));
    }

    @Test
    void theConcurrencyBoundIsNeverExceeded() throws Exception {
        final AtomicInteger running = new AtomicInteger();
        final AtomicInteger maximum = new AtomicInteger();
        final List<Integer> items = IntStream.range(0, 12).boxed().collect(Collectors.toList());

        final Result<SystemError, List<Integer>> result =
                await(
                        Parallel.traverse(
                                context,
                                Parallelism.of(3),
                                items,
                                (item, scoped) ->
                                        Async.supply(
                                                scoped,
                                                () -> {
                                                    final int current = running.incrementAndGet();
                                                    maximum.accumulateAndGet(current, Math::max);
                                                    try {
                                                        Thread.sleep(40);
                                                        return item;
                                                    } finally {
                                                        running.decrementAndGet();
                                                    }
                                                })));

        assertEquals(items, successValue(result));
        assertTrue(maximum.get() <= 3, "observed " + maximum.get() + " concurrent tasks");
        assertTrue(maximum.get() > 1, "the traversal did not run anything in parallel");
    }

    @Test
    void anEmptyInputCompletesWithAnEmptySuccess() throws Exception {
        final Result<SystemError, List<String>> result =
                await(
                        Parallel.traverse(
                                context,
                                Parallelism.of(4),
                                List.of(),
                                (item, scoped) -> Async.success("never")));

        assertTrue(successValue(result).isEmpty());
    }

    @Test
    void failFastReturnsTheFailureAndStopsLaunching() throws Exception {
        final AtomicInteger launched = new AtomicInteger();
        final List<Integer> items = IntStream.range(0, 10).boxed().collect(Collectors.toList());

        final Result<SystemError, List<Integer>> result =
                await(
                        Parallel.traverse(
                                context,
                                Parallelism.of(2),
                                items,
                                (item, scoped) -> {
                                    launched.incrementAndGet();
                                    if (item == 1) {
                                        return CompletableFuture.completedFuture(
                                                Result.<SystemError, Integer>failure(
                                                        ParameterError.nullParameter("item")));
                                    }
                                    return Async.supply(
                                            scoped,
                                            () -> {
                                                Thread.sleep(50);
                                                return item;
                                            });
                                }));

        assertEquals(400, failureValue(result).getHttpStatus());
        assertTrue(
                launched.get() <= 3,
                "no new item may start after the failure, launched " + launched.get());
    }

    @Test
    void failFastCancelsInFlightItems() throws Exception {
        final CountDownLatch blocked = new CountDownLatch(3);
        final CountDownLatch interrupted = new CountDownLatch(3);
        final List<Integer> items = IntStream.range(0, 6).boxed().collect(Collectors.toList());

        final CompletableFuture<Result<SystemError, List<Integer>>> future =
                Parallel.traverse(
                        context,
                        Parallelism.of(4),
                        items,
                        (item, scoped) -> {
                            if (item == 3) {
                                final CompletableFuture<Result<SystemError, Integer>> delayed =
                                        new CompletableFuture<>();
                                context.scheduler()
                                        .schedule(
                                                () ->
                                                        delayed.complete(
                                                                Result.failure(
                                                                        ParameterError
                                                                                .nullParameter(
                                                                                        "item"))),
                                                50,
                                                TimeUnit.MILLISECONDS);
                                return delayed;
                            }
                            return Async.supply(
                                    scoped,
                                    () -> {
                                        blocked.countDown();
                                        try {
                                            new CountDownLatch(1).await();
                                        } catch (final InterruptedException stop) {
                                            interrupted.countDown();
                                            throw stop;
                                        }
                                        return item;
                                    });
                        });

        assertTrue(blocked.await(5, TimeUnit.SECONDS), "items must be in flight");
        final SystemError error = failureValue(await(future));
        assertEquals(400, error.getHttpStatus());
        assertTrue(
                interrupted.await(5, TimeUnit.SECONDS),
                "in-flight items must be interrupted on fail-fast");
    }

    @Test
    void traverseAllKeepsEveryOutcomeInInputOrder() throws Exception {
        final List<Integer> items = IntStream.range(0, 6).boxed().collect(Collectors.toList());

        final Partition<SystemError, Integer> partition =
                await(
                        Parallel.traverseAll(
                                context,
                                Parallelism.of(3),
                                items,
                                (item, scoped) -> {
                                    if (item % 2 == 0) {
                                        return CompletableFuture.completedFuture(
                                                Result.<SystemError, Integer>failure(
                                                        ParameterError.nullParameter(
                                                                "item" + item)));
                                    }
                                    return Async.supply(scoped, () -> item);
                                }));

        assertEquals(List.of(1, 3, 5), partition.successes());
        assertEquals(3, partition.failures().size());
        assertFalse(partition.isCompleteSuccess().toBoolean());
        assertEquals(400, partition.firstFailure().orNull().getHttpStatus());
        assertEquals(400, failureValue(partition.toResult()).getHttpStatus());
    }

    @Test
    void sequenceAwaitsExistingFutures() throws Exception {
        final List<CompletionStage<Result<SystemError, String>>> stages =
                List.of(Async.success("a"), Async.success("b"), Async.success("c"));

        final Result<SystemError, List<String>> result =
                await(Parallel.sequence(context, Parallelism.of(2), stages));

        assertEquals(List.of("a", "b", "c"), successValue(result));
    }

    @Test
    void aRejectedSubmissionBecomesAFailureValue() throws Exception {
        final java.util.concurrent.Executor rejecting =
                command -> {
                    throw new RejectedExecutionException("saturated");
                };
        final AsyncContext<SystemError> rejectedContext =
                AsyncContext.systemErrors(rejecting).withName("rejected");

        final Result<SystemError, String> result =
                await(Async.supply(rejectedContext, () -> "never"));

        final SystemError error = failureValue(result);
        assertEquals(503, error.getHttpStatus());
        assertTrue(error.getMessage().contains("saturated"));
    }

    @Test
    void aThrowingMapperBecomesAFailure() throws Exception {
        final Result<SystemError, List<Integer>> result =
                await(
                        Parallel.traverse(
                                context,
                                Parallelism.of(2),
                                List.of(1, 2),
                                (item, scoped) -> {
                                    throw new IllegalStateException("boom");
                                }));

        assertEquals(500, failureValue(result).getHttpStatus());
    }

    @Test
    void anExceptionalStageBecomesAFailure() throws Exception {
        final CompletableFuture<Result<SystemError, Integer>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IOException("reset"));

        final Result<SystemError, List<Integer>> result =
                await(
                        Parallel.traverse(
                                context,
                                Parallelism.of(2),
                                List.of(1, 2),
                                (item, scoped) -> failed));

        final SystemError error = failureValue(result);
        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, error.getErrorType());
        assertTrue(error.isRetryable());
    }

    @Test
    void anErrorPropagatesThroughTheTraversal() {
        final CompletableFuture<Result<SystemError, Integer>> fatal = new CompletableFuture<>();
        fatal.completeExceptionally(new AssertionError("fatal"));
        final CompletableFuture<Result<SystemError, List<Integer>>> future =
                Parallel.traverse(context, Parallelism.of(2), List.of(1), (item, scoped) -> fatal);

        final ExecutionException thrown =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(AssertionError.class, thrown.getCause());
    }

    @Test
    void aNullStageIsAProgrammingError() throws Exception {
        final SystemError error =
                failureValue(
                        await(
                                Parallel.traverse(
                                        context,
                                        Parallelism.of(2),
                                        List.of(1),
                                        (item, scoped) -> null)));

        assertInstanceOf(NullPointerException.class, error.toException().getCause());
    }

    @Test
    void callerCancellationStopsTheBatch() throws Exception {
        final CancellationToken token = CancellationToken.create();
        final AsyncContext<SystemError> cancellable = context.withCancellation(token);
        final CountDownLatch started = new CountDownLatch(1);
        final List<Integer> items = IntStream.range(0, 6).boxed().collect(Collectors.toList());

        final CompletableFuture<Result<SystemError, List<Integer>>> future =
                Parallel.traverse(
                        cancellable,
                        Parallelism.of(2),
                        items,
                        (item, scoped) ->
                                Async.supply(
                                        scoped,
                                        () -> {
                                            started.countDown();
                                            Thread.sleep(TimeUnit.SECONDS.toMillis(30));
                                            return item;
                                        }));

        assertTrue(started.await(5, TimeUnit.SECONDS), "items must start");
        token.cancel(CancellationReason.CALLER);

        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED,
                failureValue(await(future)).getErrorType());
    }

    @Test
    void sequencePropagatesAFailure() throws Exception {
        final List<CompletionStage<Result<SystemError, String>>> stages =
                List.of(Async.success("a"), Async.failure(ParameterError.nullParameter("id")));

        assertEquals(
                400,
                failureValue(await(Parallel.sequence(context, Parallelism.of(2), stages)))
                        .getHttpStatus());
    }

    @Test
    void aStageWithNoResultIsAProgrammingError() {
        final CompletableFuture<Result<SystemError, Integer>> empty = new CompletableFuture<>();
        empty.complete(null);

        final CompletableFuture<Result<SystemError, List<Integer>>> future =
                Parallel.traverse(context, Parallelism.of(2), List.of(1), (item, scoped) -> empty);

        final ExecutionException thrown =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(NullPointerException.class, thrown.getCause());
    }

    @Test
    void theBoundIsValidated() {
        assertThrows(IllegalArgumentException.class, () -> Parallelism.of(0));
        assertThrows(IllegalArgumentException.class, () -> Parallelism.of(-3));
        assertTrue(Parallelism.of(2).isConcurrent());
        assertFalse(Parallelism.of(1).isConcurrent());
    }
}
