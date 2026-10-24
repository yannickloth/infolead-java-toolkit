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
import eu.infolead.jtk.anomaly.ResourceError;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Result;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AsyncBridgeTest {

    private ExecutorService executor;
    private AsyncContext<SystemError> context;

    @BeforeEach
    void setUp() {
        executor = newExecutor(2);
        context = AsyncContext.systemErrors(executor).withName("bridge-test");
    }

    @AfterEach
    void tearDown() {
        shutdown(executor);
    }

    @Test
    void completedValuesRoundTrip() throws Exception {
        assertEquals("value", successValue(await(Async.success("value"))));
        final SystemError error =
                failureValue(
                        await(
                                Async.<SystemError, String>failure(
                                        eu.infolead.jtk.anomaly.ParameterError.nullParameter(
                                                "id"))));
        assertEquals(400, error.getHttpStatus());
        assertEquals("value", successValue(await(Async.completed(Result.success("value")))));
    }

    @Test
    void eitherValuesLiftIntoTheRailway() throws Exception {
        final Either<String, Integer> right = Either.right(7);
        final Either<String, Integer> left = Either.left("boom");

        assertEquals(7, successValue(await(Async.fromEither(right))).intValue());
        assertEquals("boom", failureValue(await(Async.fromEither(left))));
    }

    @Test
    void supplyCapturesCheckedExceptionsAsFailureValues() throws Exception {
        final Result<SystemError, String> result =
                await(
                        Async.supply(
                                context,
                                () -> {
                                    throw new IOException("disk unavailable");
                                }));

        final SystemError error = failureValue(result);
        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, error.getErrorType());
        assertInstanceOf(IOException.class, error.toException().getCause());
        assertTrue(error.isRetryable(), "transient I/O failures should be retryable");
    }

    @Test
    void runProducesAnEmptySuccess() throws Exception {
        final Result<SystemError, Void> result =
                await(
                        Async.run(
                                context,
                                () -> {
                                    // side effect only
                                }));
        assertTrue(result.isSuccess().toBoolean());
        assertTrue(result.isSuccess().toBoolean());
    }

    @Test
    void fromStageMapsExceptionalCompletion() throws Exception {
        final CompletableFuture<String> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IOException("connection reset"));

        final SystemError error = failureValue(await(Async.fromStage(context, failed)));
        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, error.getErrorType());
        assertInstanceOf(IOException.class, error.toException().getCause());
    }

    @Test
    void fromStageMapsForeignCancellation() throws Exception {
        final CompletableFuture<String> cancelled = new CompletableFuture<>();
        cancelled.cancel(true);

        final SystemError error = failureValue(await(Async.fromStage(context, cancelled)));
        assertEquals(PostconditionErrorType.OPERATION_CANCELLED, error.getErrorType());
        assertFalse(error.isRetryable());
        assertInstanceOf(CancellationException.class, error.toException().getCause());
    }

    @Test
    void fromStagePropagatesErrors() {
        final CompletableFuture<String> failed = new CompletableFuture<>();
        failed.completeExceptionally(new AssertionError("jvm failure"));

        // The future completes exceptionally, so awaiting it rethrows the Error.
        final AssertionError thrown =
                assertThrows(
                        AssertionError.class,
                        () -> Async.await(context, Async.fromStage(context, failed), Maybe.none()));
        assertEquals("jvm failure", thrown.getMessage());
    }

    @Test
    void railwayCombinatorsCompose() throws Exception {
        final CompletableFuture<Result<SystemError, String>> base = Async.success("41");

        final int mapped =
                await(
                        Async.fold(
                                Async.map(base, value -> Integer.parseInt(value) + 1),
                                error -> -1,
                                value -> value));
        assertEquals(42, mapped);

        final CompletableFuture<Result<SystemError, String>> chained =
                Async.flatMap(base, value -> Async.success(value + "0"));
        assertEquals("410", successValue(await(chained)));

        final CompletableFuture<Result<SystemError, String>> failed =
                Async.<SystemError, String>failure(
                        eu.infolead.jtk.anomaly.ParameterError.nullParameter("id"));
        assertEquals(
                "recovered",
                successValue(await(Async.recover(failed, error -> Result.success("recovered")))));

        final CompletableFuture<Result<SystemError, String>> mappedFailure =
                Async.mapFailure(
                        failed, error -> eu.infolead.jtk.anomaly.ResourceError.notFound("User", 1));
        assertEquals(404, failureValue(await(mappedFailure)).getHttpStatus());
    }

    @Test
    void fromStageRejectsANullSuccess() {
        final Result<SystemError, String> result =
                Async.await(
                        context,
                        Async.fromStage(context, CompletableFuture.completedFuture(null)),
                        Maybe.none());

        assertInstanceOf(NullPointerException.class, failureValue(result).toException().getCause());
    }

    @Test
    void fromStageMapsInterruptsAndRestoresTheFlag() throws Exception {
        final CompletableFuture<String> interrupted = new CompletableFuture<>();
        interrupted.completeExceptionally(new InterruptedException("stopped"));

        final Result<SystemError, String> result = await(Async.fromStage(context, interrupted));

        assertTrue(Thread.interrupted(), "the interrupt status must be restored");
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED, failureValue(result).getErrorType());
    }

    @Test
    void fromFutureDelegatesToFromStage() throws Exception {
        assertEquals(
                "value",
                successValue(
                        await(
                                Async.fromFuture(
                                        context, CompletableFuture.completedFuture("value")))));
    }

    @Test
    void flatMapDoesNotInvokeTheStepAfterAFailure() throws Exception {
        final AtomicBoolean invoked = new AtomicBoolean();
        final CompletableFuture<Result<SystemError, String>> failed =
                Async.failure(ParameterError.nullParameter("id"));

        final CompletableFuture<Result<SystemError, String>> chained =
                Async.flatMap(
                        failed,
                        value -> {
                            invoked.set(true);
                            return Async.success(value);
                        });

        assertEquals(400, failureValue(await(chained)).getHttpStatus());
        assertFalse(invoked.get());
    }

    @Test
    void recoverCanReplaceAFailureWithAnotherFailure() throws Exception {
        final CompletableFuture<Result<SystemError, String>> failed =
                Async.failure(ParameterError.nullParameter("id"));

        final CompletableFuture<Result<SystemError, String>> recovered =
                Async.recover(failed, error -> Result.failure(ResourceError.notFound("User", 1)));

        assertEquals(404, failureValue(await(recovered)).getHttpStatus());
    }

    @Test
    void mapFailureLeavesASuccessUntouched() throws Exception {
        final AtomicBoolean invoked = new AtomicBoolean();

        final CompletableFuture<Result<SystemError, String>> mapped =
                Async.mapFailure(
                        Async.success("value"),
                        error -> {
                            invoked.set(true);
                            return error;
                        });

        assertEquals("value", successValue(await(mapped)));
        assertFalse(invoked.get());
    }

    @Test
    void foldHandlesBothTracks() throws Exception {
        final CompletableFuture<Result<SystemError, String>> failed =
                Async.failure(ParameterError.nullParameter("id"));

        assertEquals(-1, await(Async.fold(failed, error -> -1, value -> 1)).intValue());
        assertEquals(
                1, await(Async.fold(Async.success("value"), error -> -1, value -> 1)).intValue());
    }

    @Test
    void runCapturesFailures() throws Exception {
        final SystemError error =
                failureValue(
                        await(
                                Async.run(
                                        context,
                                        () -> {
                                            throw new IOException("disk");
                                        })));

        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, error.getErrorType());
        assertTrue(error.isRetryable());
    }

    @Test
    void awaitMapsAForeignCancellation() {
        final CompletableFuture<Result<SystemError, String>> cancelled = new CompletableFuture<>();
        cancelled.cancel(true);

        final SystemError error = failureValue(Async.await(context, cancelled, Maybe.none()));

        assertEquals(PostconditionErrorType.OPERATION_CANCELLED, error.getErrorType());
    }

    @Test
    void awaitMapsAnExceptionalCompletion() {
        final CompletableFuture<Result<SystemError, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("bug"));

        final SystemError error = failureValue(Async.await(context, failed, Maybe.none()));

        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, error.getErrorType());
        assertFalse(error.isRetryable());
    }

    @Test
    void runDoesNotStartWhenTheTokenIsCancelled() throws Exception {
        final CancellationToken token = CancellationToken.create();
        token.cancel(CancellationReason.CALLER);
        final AtomicBoolean ran = new AtomicBoolean();

        final Result<SystemError, Void> result =
                await(Async.run(context.withCancellation(token), () -> ran.set(true)));

        assertFalse(ran.get());
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED, failureValue(result).getErrorType());
    }

    @Test
    void runCapturesAnErrorAsAnExceptionalCompletion() {
        final CompletableFuture<Result<SystemError, Void>> future =
                Async.run(
                        context,
                        () -> {
                            throw new AssertionError("fatal");
                        });

        final ExecutionException thrown =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(AssertionError.class, thrown.getCause());
    }

    @Test
    void runReportsExecutorRejectionAsAFailure() throws Exception {
        final AsyncContext<SystemError> rejected =
                AsyncContext.systemErrors(
                        command -> {
                            throw new RejectedExecutionException("full");
                        });

        final SystemError error =
                failureValue(
                        await(
                                Async.run(
                                        rejected,
                                        () -> {
                                            // never runs
                                        })));

        assertEquals(503, error.getHttpStatus());
    }

    @Test
    void supplyCapturesAnErrorAsAnExceptionalCompletion() {
        final CompletableFuture<Result<SystemError, String>> future =
                Async.supply(
                        context,
                        () -> {
                            throw new AssertionError("fatal");
                        });

        final ExecutionException thrown =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(AssertionError.class, thrown.getCause());
    }

    @Test
    void supplyRejectsANullSuccess() {
        final CompletableFuture<Result<SystemError, String>> future =
                Async.supply(context, () -> null);

        final ExecutionException thrown =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(NullPointerException.class, thrown.getCause());
    }

    @Test
    void aMissingDeadlineWaitsForTheValue() throws Exception {
        assertEquals(
                "value", successValue(Async.await(context, Async.success("value"), Maybe.none())));
    }

    @Test
    void aDeadlineThatIsNotReachedReturnsTheValue() throws Exception {
        final Result<SystemError, String> result =
                Async.await(context, Async.success("value"), Maybe.of(Duration.ofSeconds(1)));

        assertEquals("value", successValue(result));
    }

    @Test
    void awaitReturnsTimeoutAnomalyInsteadOfThrowing() {
        final CompletableFuture<Result<SystemError, String>> never = new CompletableFuture<>();

        final SystemError error =
                failureValue(Async.await(context, never, Maybe.of(Duration.ofMillis(20))));
        assertEquals(PostconditionErrorType.OPERATION_TIMED_OUT, error.getErrorType());
        assertTrue(error.isRetryable());
    }

    @Test
    void awaitRestoresInterruptAndReturnsCancellation() throws Exception {
        final CompletableFuture<Result<SystemError, String>> never = new CompletableFuture<>();
        final AtomicReference<Result<SystemError, String>> observed = new AtomicReference<>();
        final AtomicBoolean interruptedAfterwards = new AtomicBoolean();

        final Thread waiter =
                new Thread(
                        () -> {
                            Thread.currentThread().interrupt();
                            observed.set(Async.await(context, never, Maybe.none()));
                            interruptedAfterwards.set(Thread.currentThread().isInterrupted());
                            Thread.interrupted();
                        });
        waiter.start();
        waiter.join(TimeUnit.SECONDS.toMillis(5));

        assertFalse(waiter.isAlive(), "await must return after an interrupt");
        assertTrue(interruptedAfterwards.get(), "flag must be restored");
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED,
                failureValue(observed.get()).getErrorType());
    }

    @Test
    void awaitRethrowsErrors() {
        final CompletableFuture<Result<SystemError, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new AssertionError("fatal"));

        assertThrows(AssertionError.class, () -> Async.await(context, failed, Maybe.none()));
    }
}
