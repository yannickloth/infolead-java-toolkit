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
import eu.infolead.jtk.anomaly.PostconditionError;
import eu.infolead.jtk.anomaly.PostconditionErrorType;
import eu.infolead.jtk.anomaly.PreconditionError;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetryTest {

    private ExecutorService executor;
    private AsyncContext<SystemError> context;

    @BeforeEach
    void setUp() {
        executor = newExecutor(2);
        context = AsyncContext.systemErrors(executor).withName("retry-test");
    }

    @AfterEach
    void tearDown() {
        shutdown(executor);
    }

    private static SystemError transientFailure() {
        return PostconditionError.asyncOperationFailed("dial failed");
    }

    @Test
    void retriesUntilSuccess() throws Exception {
        final AtomicInteger attempts = new AtomicInteger();
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5))),
                        attempt -> {
                            if (attempts.incrementAndGet() < 3) {
                                return CompletableFuture.completedFuture(
                                        Result.<SystemError, String>failure(transientFailure()));
                            }
                            return Async.supply(attempt, () -> "ok");
                        });

        assertEquals("ok", successValue(await(future)));
        assertEquals(3, attempts.get());
    }

    @Test
    void deterministicFailuresAreNotRetried() throws Exception {
        final AtomicInteger attempts = new AtomicInteger();
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(5, Backoff.fixed(Duration.ofMillis(5))),
                        attempt -> {
                            attempts.incrementAndGet();
                            return CompletableFuture.completedFuture(
                                    Result.<SystemError, String>failure(
                                            ParameterError.nullParameter("id")));
                        });

        assertEquals(400, failureValue(await(future)).getHttpStatus());
        assertEquals(1, attempts.get(), "a 400 must never be retried");
    }

    @Test
    void cancellationIsNeverRetried() throws Exception {
        final AtomicInteger attempts = new AtomicInteger();
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(5, Backoff.fixed(Duration.ofMillis(5))),
                        attempt -> {
                            attempts.incrementAndGet();
                            return CompletableFuture.completedFuture(
                                    Result.<SystemError, String>failure(
                                            PostconditionError.operationCancelled("op")));
                        });

        assertEquals(1, attempts.get());
        assertTrue(
                failureValue(await(future)).getMessage().contains("Operation 'op' was cancelled"));
    }

    @Test
    void exhaustionReturnsTheLastFailureWithItsHistory() throws Exception {
        final AtomicInteger attempts = new AtomicInteger();
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5))),
                        attempt -> {
                            attempts.incrementAndGet();
                            return CompletableFuture.completedFuture(
                                    Result.<SystemError, String>failure(transientFailure()));
                        });

        final SystemError error = failureValue(await(future));
        assertEquals(3, attempts.get());
        assertTrue(error.getMessage().contains("dial failed"));
        assertEquals(
                2,
                error.getSuppressed().size(),
                "earlier failures must be attached as suppressed history");
    }

    @Test
    void retryAfterActsAsALowerBound() throws Exception {
        final AtomicInteger attempts = new AtomicInteger();
        final long started = System.nanoTime();
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5))),
                        attempt -> {
                            if (attempts.incrementAndGet() == 1) {
                                return CompletableFuture.completedFuture(
                                        Result.<SystemError, String>failure(
                                                PreconditionError.rateLimitExceeded("100/min")
                                                        .withRetryAfter(Duration.ofMillis(120))));
                            }
                            return CompletableFuture.completedFuture(Result.success("ok"));
                        });

        assertEquals("ok", successValue(await(future)));
        final long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        assertTrue(
                elapsedMillis >= 100,
                "Retry-After must be honoured as a floor, elapsed was " + elapsedMillis + "ms");
    }

    @Test
    void cancellationDuringBackoffStopsTheRunImmediately() throws Exception {
        final CancellationToken token = CancellationToken.create();
        final AsyncContext<SystemError> cancellable = context.withCancellation(token);
        final AtomicInteger attempts = new AtomicInteger();

        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        cancellable,
                        RetryPolicy.of(5, Backoff.fixed(Duration.ofSeconds(10))),
                        attempt -> {
                            attempts.incrementAndGet();
                            return CompletableFuture.completedFuture(
                                    Result.<SystemError, String>failure(transientFailure()));
                        });

        token.cancel(CancellationReason.CALLER);

        final SystemError error = failureValue(await(future));
        assertEquals(PostconditionErrorType.OPERATION_CANCELLED, error.getErrorType());
        assertEquals(1, attempts.get(), "no attempt may start after cancellation");
    }

    @Test
    void theOverallDeadlineStopsRetrying() throws Exception {
        final AtomicInteger attempts = new AtomicInteger();
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(10, Backoff.fixed(Duration.ofMillis(200)))
                                .withOverallDeadline(Duration.ofMillis(250)),
                        attempt -> {
                            attempts.incrementAndGet();
                            return CompletableFuture.completedFuture(
                                    Result.<SystemError, String>failure(transientFailure()));
                        });

        assertTrue(failureValue(await(future)).getMessage().contains("dial failed"));
        assertTrue(attempts.get() <= 2, "attempts were " + attempts.get());
    }

    @Test
    void theOverallDeadlineInterruptsAnInFlightAttempt() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);

        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(10)))
                                .withOverallDeadline(Duration.ofMillis(80)),
                        attempt ->
                                Async.supply(
                                        attempt,
                                        () -> {
                                            started.countDown();
                                            try {
                                                new CountDownLatch(1).await();
                                            } catch (final InterruptedException stop) {
                                                interrupted.countDown();
                                                throw stop;
                                            }
                                            return "never";
                                        }));

        assertTrue(started.await(5, TimeUnit.SECONDS), "attempt must start");
        assertTrue(
                interrupted.await(5, TimeUnit.SECONDS),
                "the overall deadline must interrupt the attempt");
        assertEquals(
                PostconditionErrorType.OPERATION_TIMED_OUT,
                failureValue(await(future)).getErrorType());
    }

    @Test
    void aTransientExceptionalStageIsRetried() throws Exception {
        final AtomicInteger attempts = new AtomicInteger();
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5))),
                        attempt ->
                                attempts.incrementAndGet() == 1
                                        ? CompletableFuture.failedFuture(new IOException("reset"))
                                        : CompletableFuture.completedFuture(Result.success("ok")));

        assertEquals("ok", successValue(await(future)));
        assertEquals(2, attempts.get());
    }

    @Test
    void anErrorPropagatesOutOfTheRetry() {
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5))),
                        attempt -> CompletableFuture.failedFuture(new AssertionError("fatal")));

        final ExecutionException thrown =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(AssertionError.class, thrown.getCause());
    }

    @Test
    void aZeroAttemptTimeoutTimesOutBeforeRunning() throws Exception {
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(1, Backoff.fixed(Duration.ofMillis(5)))
                                .withAttemptTimeout(Duration.ZERO),
                        attempt ->
                                Async.supply(
                                        attempt,
                                        () -> {
                                            new CountDownLatch(1).await();
                                            return "never";
                                        }));

        assertEquals(
                PostconditionErrorType.OPERATION_TIMED_OUT,
                failureValue(await(future)).getErrorType());
    }

    @Test
    void aRejectedSchedulerStillReturnsTheLastFailure() throws Exception {
        final ScheduledExecutorService dead = Executors.newSingleThreadScheduledExecutor();
        dead.shutdownNow();
        try {
            final AsyncContext<SystemError> noTimer =
                    AsyncContext.of(
                            "retry-test",
                            executor,
                            dead,
                            CancellationToken.none(),
                            FailureStrategy.systemErrors());

            final CompletableFuture<Result<SystemError, String>> future =
                    Retries.run(
                            noTimer,
                            RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5)))
                                    .withOverallDeadline(Duration.ofSeconds(1)),
                            attempt ->
                                    CompletableFuture.completedFuture(
                                            Result.<SystemError, String>failure(
                                                    transientFailure())));

            assertTrue(failureValue(await(future)).getMessage().contains("dial failed"));
        } finally {
            dead.shutdownNow();
        }
    }

    @Test
    void cancellationAfterSuccessDoesNotChangeTheResult() throws Exception {
        final CancellationToken token = CancellationToken.create();
        final AsyncContext<SystemError> cancellable = context.withCancellation(token);
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        cancellable,
                        RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5))),
                        attempt -> Async.supply(attempt, () -> "ok"));

        assertEquals("ok", successValue(await(future)));
        token.cancel(CancellationReason.CALLER);

        assertEquals("ok", successValue(await(future)));
    }

    @Test
    void aThrowingAttemptMapperIsNotRetried() throws Exception {
        final AtomicInteger attempts = new AtomicInteger();

        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5))),
                        attempt -> {
                            attempts.incrementAndGet();
                            throw new IllegalStateException("boom");
                        });

        assertEquals(
                PostconditionErrorType.ASYNC_OPERATION_FAILED,
                failureValue(await(future)).getErrorType());
        assertEquals(1, attempts.get());
    }

    @Test
    void aStageWithNoResultIsAProgrammingError() {
        final CompletableFuture<Result<SystemError, String>> empty = new CompletableFuture<>();
        empty.complete(null);

        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(1, Backoff.fixed(Duration.ofMillis(5))),
                        attempt -> empty);

        final ExecutionException thrown =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(NullPointerException.class, thrown.getCause());
    }

    @Test
    void anOverflowingDelayStopsRetrying() throws Exception {
        final AtomicInteger attempts = new AtomicInteger();

        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(5, Backoff.fixed(Duration.ofDays(1_000_000))),
                        attempt -> {
                            attempts.incrementAndGet();
                            return CompletableFuture.completedFuture(
                                    Result.<SystemError, String>failure(transientFailure()));
                        });

        assertTrue(failureValue(await(future)).getMessage().contains("dial failed"));
        assertEquals(1, attempts.get());
    }

    @Test
    void aZeroOverallDeadlineTimesOutBeforeTheFirstAttempt() throws Exception {
        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(1, Backoff.fixed(Duration.ofMillis(5)))
                                .withOverallDeadline(Duration.ZERO),
                        attempt -> Async.supply(attempt, () -> "never"));

        assertEquals(
                PostconditionErrorType.OPERATION_TIMED_OUT,
                failureValue(await(future)).getErrorType());
    }

    @Test
    void aRejectedAttemptTimeoutFailsFast() throws Exception {
        final ScheduledExecutorService dead = Executors.newSingleThreadScheduledExecutor();
        dead.shutdownNow();
        try {
            final AsyncContext<SystemError> noTimer =
                    AsyncContext.of(
                            "retry-test",
                            executor,
                            dead,
                            CancellationToken.none(),
                            FailureStrategy.systemErrors());

            final CompletableFuture<Result<SystemError, String>> future =
                    Retries.run(
                            noTimer,
                            RetryPolicy.of(1, Backoff.fixed(Duration.ofMillis(5)))
                                    .withAttemptTimeout(Duration.ofMillis(50)),
                            attempt ->
                                    Async.supply(
                                            attempt,
                                            () -> {
                                                new CountDownLatch(1).await();
                                                return "never";
                                            }));

            assertEquals(
                    PostconditionErrorType.OPERATION_TIMED_OUT,
                    failureValue(await(future)).getErrorType());
        } finally {
            dead.shutdownNow();
        }
    }

    @Test
    void aPreCancelledContextFailsWithoutRunning() throws Exception {
        final CancellationToken token = CancellationToken.create();
        token.cancel(CancellationReason.CALLER);
        final AtomicBoolean ran = new AtomicBoolean();

        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context.withCancellation(token),
                        RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5))),
                        attempt ->
                                Async.supply(
                                        attempt,
                                        () -> {
                                            ran.set(true);
                                            return "value";
                                        }));

        assertFalse(ran.get());
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED,
                failureValue(await(future)).getErrorType());
    }

    @Test
    void anExpiredDeadlineWithAnUnavailableTimerTimesOut() throws Exception {
        final ScheduledExecutorService dead = Executors.newSingleThreadScheduledExecutor();
        dead.shutdownNow();
        try {
            final AsyncContext<SystemError> noTimer =
                    AsyncContext.of(
                            "retry-test",
                            executor,
                            dead,
                            CancellationToken.none(),
                            FailureStrategy.systemErrors());

            final CompletableFuture<Result<SystemError, String>> future =
                    Retries.run(
                            noTimer,
                            RetryPolicy.of(3, Backoff.fixed(Duration.ofMillis(5)))
                                    .withOverallDeadline(Duration.ZERO),
                            attempt -> Async.supply(attempt, () -> "never"));

            assertEquals(
                    PostconditionErrorType.OPERATION_TIMED_OUT,
                    failureValue(await(future)).getErrorType());
        } finally {
            dead.shutdownNow();
        }
    }

    @Test
    void theAttemptTimeoutInterruptsAndIsRetryable() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);

        final CompletableFuture<Result<SystemError, String>> future =
                Retries.run(
                        context,
                        RetryPolicy.of(1, Backoff.fixed(Duration.ofMillis(5)))
                                .withAttemptTimeout(Duration.ofMillis(50)),
                        attempt ->
                                Async.supply(
                                        attempt,
                                        () -> {
                                            started.countDown();
                                            try {
                                                new CountDownLatch(1).await();
                                            } catch (final InterruptedException stop) {
                                                interrupted.countDown();
                                                throw stop;
                                            }
                                            return "never";
                                        }));

        assertTrue(started.await(5, TimeUnit.SECONDS), "attempt must start");
        assertTrue(interrupted.await(5, TimeUnit.SECONDS), "attempt must be interrupted");
        assertEquals(
                PostconditionErrorType.OPERATION_TIMED_OUT,
                failureValue(await(future)).getErrorType());
    }
}
