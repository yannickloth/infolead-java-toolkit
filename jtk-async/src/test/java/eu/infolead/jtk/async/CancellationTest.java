package eu.infolead.jtk.async;

import static eu.infolead.jtk.async.AsyncTestSupport.await;
import static eu.infolead.jtk.async.AsyncTestSupport.failureValue;
import static eu.infolead.jtk.async.AsyncTestSupport.newExecutor;
import static eu.infolead.jtk.async.AsyncTestSupport.shutdown;
import static eu.infolead.jtk.async.AsyncTestSupport.successValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.anomaly.PostconditionErrorType;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Result;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CancellationTest {

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = newExecutor(2);
    }

    @AfterEach
    void tearDown() {
        shutdown(executor);
    }

    private AsyncContext<SystemError> context(final CancellationToken token) {
        return AsyncContext.systemErrors(executor).withName("cancel-test").withCancellation(token);
    }

    @Test
    void cancellationInterruptsTheWorkerAndPublishesTheAnomaly() throws Exception {
        final CancellationToken token = CancellationToken.create();
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);

        final CompletableFuture<Result<SystemError, String>> future =
                Async.supply(
                        context(token),
                        () -> {
                            started.countDown();
                            try {
                                new CountDownLatch(1).await();
                            } catch (final InterruptedException stop) {
                                interrupted.countDown();
                                throw stop;
                            }
                            return "unreachable";
                        });

        assertTrue(started.await(5, TimeUnit.SECONDS), "task must start");
        token.cancel(CancellationReason.CALLER);

        assertTrue(interrupted.await(5, TimeUnit.SECONDS), "worker must observe the interrupt");
        final SystemError error = failureValue(await(future));
        assertEquals(PostconditionErrorType.OPERATION_CANCELLED, error.getErrorType());
        assertFalse(error.isRetryable());
        assertInstanceOf(CancellationException.class, error.toException().getCause());
    }

    @Test
    void aRunningSideEffectIsInterruptedOnCancellation() throws Exception {
        final CancellationToken token = CancellationToken.create();
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);

        final CompletableFuture<Result<SystemError, Void>> future =
                Async.run(
                        context(token),
                        () -> {
                            started.countDown();
                            try {
                                new CountDownLatch(1).await();
                            } catch (final InterruptedException stop) {
                                interrupted.countDown();
                                throw stop;
                            }
                        });

        assertTrue(started.await(5, TimeUnit.SECONDS), "side effect must start");
        token.cancel(CancellationReason.CALLER);

        assertTrue(interrupted.await(5, TimeUnit.SECONDS), "side effect must be interrupted");
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED,
                failureValue(await(future)).getErrorType());
    }

    @Test
    void aTaskCancelledBeforeTheExecutorRunsItDoesNotStart() throws Exception {
        final CancellationToken token = CancellationToken.create();
        final AtomicReference<Runnable> pending = new AtomicReference<>();
        final AsyncContext<SystemError> deferred =
                AsyncContext.systemErrors(pending::set).withCancellation(token);
        final AtomicBoolean ran = new AtomicBoolean();

        final CompletableFuture<Result<SystemError, String>> future =
                Async.supply(
                        deferred,
                        () -> {
                            ran.set(true);
                            return "value";
                        });
        token.cancel(CancellationReason.CALLER);
        pending.get().run();

        assertFalse(ran.get());
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED,
                failureValue(await(future)).getErrorType());
    }

    @Test
    void aSideEffectCancelledBeforeTheExecutorRunsItDoesNotStart() throws Exception {
        final CancellationToken token = CancellationToken.create();
        final AtomicReference<Runnable> pending = new AtomicReference<>();
        final AsyncContext<SystemError> deferred =
                AsyncContext.systemErrors(pending::set).withCancellation(token);
        final AtomicBoolean ran = new AtomicBoolean();

        final CompletableFuture<Result<SystemError, Void>> future =
                Async.run(deferred, () -> ran.set(true));
        token.cancel(CancellationReason.CALLER);
        pending.get().run();

        assertFalse(ran.get());
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED,
                failureValue(await(future)).getErrorType());
    }

    @Test
    void aCancelledTaskDoesNotRunAtAll() throws Exception {
        final CancellationToken token = CancellationToken.create();
        token.cancel(CancellationReason.CALLER);
        final AtomicBoolean ran = new AtomicBoolean();

        final Result<SystemError, String> result =
                await(
                        Async.supply(
                                context(token),
                                () -> {
                                    ran.set(true);
                                    return "value";
                                }));

        assertFalse(ran.get(), "the computation must not start");
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED, failureValue(result).getErrorType());
    }

    @Test
    void theFutureCompletesAtCancellationTime() throws Exception {
        final CancellationToken token = CancellationToken.create();
        final CountDownLatch started = new CountDownLatch(1);
        final CompletableFuture<Result<SystemError, String>> future =
                Async.supply(
                        context(token),
                        () -> {
                            started.countDown();
                            // Ignores interruption on purpose: completion must not wait for it.
                            for (int spin = 0;
                                    spin < 100 && !Thread.currentThread().isInterrupted();
                                    spin++) {
                                Thread.onSpinWait();
                            }
                            Thread.sleep(50);
                            return "late";
                        });

        assertTrue(started.await(5, TimeUnit.SECONDS), "task must start");
        token.cancel(CancellationReason.CALLER);

        assertTrue(future.isDone(), "cancellation must publish the outcome immediately");
        assertEquals(
                PostconditionErrorType.OPERATION_CANCELLED,
                failureValue(await(future)).getErrorType());
    }

    @Test
    void parentCancellationReachesEveryDescendant() {
        final CancellationToken root = CancellationToken.create();
        final CancellationToken child = root.child();
        final CancellationToken grandchild = child.child();

        root.cancel(CancellationReason.SHUTDOWN);

        assertTrue(child.isCancelled().toBoolean());
        assertTrue(grandchild.isCancelled().toBoolean());
        assertEquals(CancellationReason.SHUTDOWN, child.reason().orNull());
        assertEquals(CancellationReason.SHUTDOWN, grandchild.reason().orNull());
    }

    @Test
    void childCancellationLeavesTheParentActive() {
        final CancellationToken root = CancellationToken.create();
        final CancellationToken child = root.child();

        child.cancel(CancellationReason.CALLER);

        assertFalse(root.isCancelled().toBoolean());
        assertTrue(child.isCancelled().toBoolean());
    }

    @Test
    void childrenCreatedAfterCancellationStartCancelled() {
        final CancellationToken root = CancellationToken.create();
        root.cancel(CancellationReason.CALLER);

        final CancellationToken late = root.child();

        assertTrue(late.isCancelled().toBoolean());
        assertEquals(CancellationReason.CALLER, late.reason().orNull());
    }

    @Test
    void theNonCancellableTokenIgnoresCancellationButChildrenAreScopes() {
        final CancellationToken none = CancellationToken.none();
        none.cancel(CancellationReason.CALLER);

        assertFalse(none.isCancelled().toBoolean());

        final CancellationToken child = none.child();
        assertFalse(child.isCancelled().toBoolean());
        child.cancel(CancellationReason.CALLER);
        assertTrue(
                child.isCancelled().toBoolean(),
                "a child of the non-cancellable token is an independent scope");
        assertFalse(none.isCancelled().toBoolean());
    }

    @Test
    void theFirstReasonWinsAndHooksRunOnce() {
        final CancellationToken token = CancellationToken.create();
        final AtomicInteger runs = new AtomicInteger();
        token.onCancel(runs::incrementAndGet);

        token.cancel(CancellationReason.CALLER);
        token.cancel(CancellationReason.DEADLINE);

        assertEquals(CancellationReason.CALLER, token.reason().orNull());
        assertEquals(1, runs.get());
    }

    @Test
    void aHookRegisteredAfterCancellationRunsImmediately() {
        final CancellationToken token = CancellationToken.create();
        token.cancel(CancellationReason.CALLER);
        final AtomicInteger runs = new AtomicInteger();

        token.onCancel(runs::incrementAndGet);

        assertEquals(1, runs.get());
    }

    @Test
    void aFailingHookDoesNotStopPropagation() {
        final CancellationToken token = CancellationToken.create();
        token.onCancel(
                () -> {
                    throw new IllegalStateException("hook failure");
                });
        final CancellationToken child = token.child();

        token.cancel(CancellationReason.CALLER);

        assertTrue(token.isCancelled().toBoolean());
        assertTrue(child.isCancelled().toBoolean());
    }

    @Test
    void aRemovedHookDoesNotRun() {
        final CancellationToken token = CancellationToken.create();
        final AtomicInteger runs = new AtomicInteger();
        final Runnable registration = token.onCancel(runs::incrementAndGet);

        registration.run();
        token.cancel(CancellationReason.CALLER);

        assertEquals(0, runs.get());
    }

    @Test
    void anExternalInterruptBecomesARejectedCancellation() throws Exception {
        final SystemError error =
                failureValue(
                        await(
                                Async.supply(
                                        context(CancellationToken.none()),
                                        () -> {
                                            Thread.currentThread().interrupt();
                                            throw new InterruptedException("external");
                                        })));

        assertEquals(PostconditionErrorType.OPERATION_CANCELLED, error.getErrorType());
        assertFalse(error.isRetryable());
        assertInstanceOf(InterruptedException.class, error.toException().getCause());
    }

    @Test
    void aCancelledWorkerDoesNotPoisonThePoolThread() throws Exception {
        final ExecutorService singleThread = newExecutor(1);
        try {
            final CancellationToken token = CancellationToken.create();
            final AsyncContext<SystemError> cancellable =
                    AsyncContext.systemErrors(singleThread)
                            .withName("cancel-test")
                            .withCancellation(token);
            final CountDownLatch started = new CountDownLatch(1);
            final CompletableFuture<Result<SystemError, String>> cancelled =
                    Async.supply(
                            cancellable,
                            () -> {
                                started.countDown();
                                Thread.sleep(TimeUnit.SECONDS.toMillis(30));
                                return "never";
                            });

            assertTrue(started.await(5, TimeUnit.SECONDS), "task must start");
            token.cancel(CancellationReason.CALLER);
            failureValue(await(cancelled));

            // The same pooled thread must not keep the interrupt flag.
            final AsyncContext<SystemError> probeContext =
                    AsyncContext.systemErrors(singleThread).withName("probe");
            final Result<SystemError, Boolean> probe =
                    await(Async.supply(probeContext, () -> Thread.currentThread().isInterrupted()));
            assertFalse(successValue(probe), "interrupt status leaked into the pool");
        } finally {
            shutdown(singleThread);
        }
    }
}
