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

import eu.infolead.jtk.anomaly.PostconditionErrorType;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Result;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeadlineTest {

    private ExecutorService executor;
    private AsyncContext<SystemError> context;

    @BeforeEach
    void setUp() {
        executor = newExecutor(2);
        context = AsyncContext.systemErrors(executor).withName("deadline-test");
    }

    @AfterEach
    void tearDown() {
        shutdown(executor);
    }

    @Test
    void expiryPublishesTimeoutAndInterruptsTheWorker() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);

        final CompletableFuture<Result<SystemError, String>> future =
                Async.within(
                        context,
                        Duration.ofMillis(50),
                        scoped ->
                                Async.supply(
                                        scoped,
                                        () -> {
                                            started.countDown();
                                            try {
                                                new CountDownLatch(1).await();
                                            } catch (final InterruptedException stop) {
                                                interrupted.countDown();
                                                throw stop;
                                            }
                                            return "unreachable";
                                        }));

        assertTrue(started.await(5, TimeUnit.SECONDS), "task must start");
        assertTrue(interrupted.await(5, TimeUnit.SECONDS), "worker must be interrupted");

        final SystemError error = failureValue(await(future));
        assertEquals(PostconditionErrorType.OPERATION_TIMED_OUT, error.getErrorType());
        assertTrue(error.isRetryable());
        assertInstanceOf(TimeoutException.class, error.toException().getCause());
    }

    @Test
    void aForeignStageIsAbandonedAtTheDeadline() throws Exception {
        final CompletableFuture<Result<SystemError, String>> foreign = new CompletableFuture<>();

        final Result<SystemError, String> result =
                await(Async.within(context, Duration.ofMillis(50), scoped -> foreign));

        assertEquals(
                PostconditionErrorType.OPERATION_TIMED_OUT, failureValue(result).getErrorType());
        assertFalse(foreign.isDone(), "a foreign stage cannot be interrupted, only abandoned");
    }

    @Test
    void aTaskThatFinishesInTimeReturnsItsValue() throws Exception {
        final Result<SystemError, String> result =
                await(
                        Async.within(
                                context,
                                Duration.ofSeconds(2),
                                scoped ->
                                        Async.supply(
                                                scoped,
                                                () -> {
                                                    Thread.sleep(10);
                                                    return "done";
                                                })));
        assertEquals("done", successValue(result));
    }

    @Test
    void aZeroDeadlineFailsWithoutRunningTheOperation() throws Exception {
        final AtomicBoolean ran = new AtomicBoolean();
        final Result<SystemError, String> result =
                await(
                        Async.within(
                                context,
                                Duration.ZERO,
                                scoped ->
                                        Async.supply(
                                                scoped,
                                                () -> {
                                                    ran.set(true);
                                                    return "value";
                                                })));

        assertFalse(ran.get());
        assertEquals(
                PostconditionErrorType.OPERATION_TIMED_OUT, failureValue(result).getErrorType());
    }

    @Test
    void aNegativeDeadlineIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Async.within(
                                context, Duration.ofMillis(-1), scoped -> Async.success("value")));
    }

    @Test
    void anAlreadyCancelledParentFailsWithoutRunning() throws Exception {
        final CancellationToken token = CancellationToken.create();
        token.cancel(CancellationReason.CALLER);
        final AtomicBoolean ran = new AtomicBoolean();

        final Result<SystemError, String> result =
                await(
                        Async.within(
                                context.withCancellation(token),
                                Duration.ofSeconds(1),
                                scoped ->
                                        Async.supply(
                                                scoped,
                                                () -> {
                                                    ran.set(true);
                                                    return "value";
                                                })));

        assertFalse(ran.get());
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED, failureValue(result).getErrorType());
    }

    @Test
    void aRejectedSchedulerBecomesAFailure() throws Exception {
        final ScheduledExecutorService dead = Executors.newSingleThreadScheduledExecutor();
        dead.shutdownNow();
        try {
            final AsyncContext<SystemError> noTimer =
                    AsyncContext.of(
                            "deadline-test",
                            executor,
                            dead,
                            CancellationToken.none(),
                            FailureStrategy.systemErrors());

            final SystemError error =
                    failureValue(
                            await(
                                    Async.within(
                                            noTimer,
                                            Duration.ofSeconds(1),
                                            scoped -> Async.success("value"))));

            assertEquals(503, error.getHttpStatus());
        } finally {
            dead.shutdownNow();
        }
    }

    @Test
    void aThrowingScopedMapperBecomesAFailure() throws Exception {
        final SystemError error =
                failureValue(
                        await(
                                Async.within(
                                        context,
                                        Duration.ofSeconds(1),
                                        scoped -> {
                                            throw new IllegalStateException("boom");
                                        })));

        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, error.getErrorType());
    }

    @Test
    void aNullScopedStageIsAProgrammingError() throws Exception {
        final SystemError error =
                failureValue(await(Async.within(context, Duration.ofSeconds(1), scoped -> null)));

        assertInstanceOf(NullPointerException.class, error.toException().getCause());
    }

    @Test
    void anExceptionalScopedStageBecomesAFailure() throws Exception {
        final CompletableFuture<Result<SystemError, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IOException("reset"));

        final SystemError error =
                failureValue(await(Async.within(context, Duration.ofSeconds(1), scoped -> failed)));

        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, error.getErrorType());
    }

    @Test
    void aScopedStageErrorPropagates() {
        final CompletableFuture<Result<SystemError, String>> fatal = new CompletableFuture<>();
        fatal.completeExceptionally(new AssertionError("fatal"));

        final CompletableFuture<Result<SystemError, String>> future =
                Async.within(context, Duration.ofSeconds(1), scoped -> fatal);

        final ExecutionException thrown =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(AssertionError.class, thrown.getCause());
    }

    @Test
    void aScopedStageWithoutAResultIsAProgrammingError() {
        final CompletableFuture<Result<SystemError, String>> empty = new CompletableFuture<>();
        empty.complete(null);

        final CompletableFuture<Result<SystemError, String>> future =
                Async.within(context, Duration.ofSeconds(1), scoped -> empty);

        final ExecutionException thrown =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(NullPointerException.class, thrown.getCause());
    }

    @Test
    void theTimerDoesNotFireAfterCompletion() throws Exception {
        final ExecutorService singleThread = newExecutor(1);
        try {
            final AsyncContext<SystemError> single =
                    AsyncContext.systemErrors(singleThread).withName("timer-test");
            assertEquals(
                    "ok",
                    successValue(
                            await(
                                    Async.within(
                                            single,
                                            Duration.ofMillis(100),
                                            scoped -> Async.supply(scoped, () -> "ok")))));

            Thread.sleep(250);
            final Result<SystemError, Boolean> probe =
                    await(Async.supply(single, () -> Thread.currentThread().isInterrupted()));
            assertFalse(successValue(probe), "a cancelled timer must not interrupt the pool");
        } finally {
            shutdown(singleThread);
        }
    }

    @Test
    void outerCancellationKeepsTheCancellationAnomaly() throws Exception {
        final CancellationToken token = CancellationToken.create();
        final AsyncContext<SystemError> cancellable = context.withCancellation(token);
        final CountDownLatch started = new CountDownLatch(1);

        final CompletableFuture<Result<SystemError, String>> future =
                Async.within(
                        cancellable,
                        Duration.ofSeconds(30),
                        scoped ->
                                Async.supply(
                                        scoped,
                                        () -> {
                                            started.countDown();
                                            Thread.sleep(TimeUnit.SECONDS.toMillis(30));
                                            return "never";
                                        }));

        assertTrue(started.await(5, TimeUnit.SECONDS), "task must start");
        token.cancel(CancellationReason.CALLER);

        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED,
                failureValue(await(future)).getErrorType());
    }
}
