package eu.infolead.jtk.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.anomaly.SystemError;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AsyncContextTest {

    private ExecutorService executor;
    private ScheduledExecutorService scheduler;

    @BeforeEach
    void setUp() {
        executor = Executors.newSingleThreadExecutor();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
        scheduler.shutdownNow();
    }

    @Test
    void systemErrorsUsesTheDocumentedDefaults() {
        final AsyncContext<SystemError> context = AsyncContext.systemErrors(executor);

        assertEquals(AsyncContext.DEFAULT_NAME, context.name());
        assertSame(executor, context.executor());
        assertFalse(context.cancellation().isCancelled().toBoolean());
        assertSame(FailureStrategy.systemErrors(), context.failures());
    }

    @Test
    void copiesReplaceOneComponentAtATime() {
        final CancellationToken token = CancellationToken.create();
        final FailureStrategy<String> strings =
                new FailureStrategy<>() {
                    @Override
                    public String fromAnomaly(final SystemError anomaly) {
                        return anomaly.getMessage();
                    }

                    @Override
                    public String fromException(final Exception exception) {
                        return exception.getMessage();
                    }

                    @Override
                    public java.util.function.Predicate<? super String> retryable() {
                        return value -> false;
                    }
                };
        final AsyncContext<SystemError> base =
                AsyncContext.of(
                        "base",
                        executor,
                        scheduler,
                        CancellationToken.none(),
                        FailureStrategy.systemErrors());

        final AsyncContext<SystemError> renamed = base.withName("renamed");
        assertEquals("renamed", renamed.name());
        assertSame(executor, renamed.executor());
        assertSame(scheduler, renamed.scheduler());

        final AsyncContext<SystemError> recancelled = base.withCancellation(token);
        assertSame(token, recancelled.cancellation());
        assertSame(executor, recancelled.executor());

        final AsyncContext<String> retyped = base.withFailures(strings);
        assertSame(strings, retyped.failures());
        assertSame(scheduler, retyped.scheduler());

        final ExecutorService other = Executors.newSingleThreadExecutor();
        final ScheduledExecutorService otherScheduler =
                Executors.newSingleThreadScheduledExecutor();
        try {
            assertSame(other, base.withExecutor(other).executor());
            assertSame(scheduler, base.withExecutor(other).scheduler());
            assertSame(otherScheduler, base.withScheduler(otherScheduler).scheduler());
            assertSame(executor, base.withScheduler(otherScheduler).executor());
        } finally {
            other.shutdownNow();
            otherScheduler.shutdownNow();
        }
    }

    @Test
    void aChildCancellationIsAnIndependentScope() {
        final AsyncContext<SystemError> base = AsyncContext.systemErrors(executor);

        final AsyncContext<SystemError> child = base.withChildCancellation();
        child.cancellation().cancel(CancellationReason.CALLER);

        assertTrue(child.cancellation().isCancelled().toBoolean());
        assertFalse(base.cancellation().isCancelled().toBoolean());
    }

    @Test
    void invalidContextsAreRejected() {
        assertThrows(
                NullPointerException.class,
                () ->
                        AsyncContext.of(
                                null,
                                executor,
                                scheduler,
                                CancellationToken.none(),
                                FailureStrategy.systemErrors()));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        AsyncContext.of(
                                "  ",
                                executor,
                                scheduler,
                                CancellationToken.none(),
                                FailureStrategy.systemErrors()));
        assertThrows(
                NullPointerException.class,
                () ->
                        AsyncContext.of(
                                "x",
                                null,
                                scheduler,
                                CancellationToken.none(),
                                FailureStrategy.systemErrors()));
        assertThrows(
                NullPointerException.class,
                () ->
                        AsyncContext.of(
                                "x",
                                executor,
                                null,
                                CancellationToken.none(),
                                FailureStrategy.systemErrors()));
        assertThrows(
                NullPointerException.class,
                () ->
                        AsyncContext.of(
                                "x", executor, scheduler, null, FailureStrategy.systemErrors()));
        assertThrows(
                NullPointerException.class,
                () -> AsyncContext.of("x", executor, scheduler, CancellationToken.none(), null));
    }

    @Test
    void supplyUsesTheContextExecutorAndToken() {
        final AtomicBoolean ran = new AtomicBoolean();
        final AsyncContext<SystemError> context = AsyncContext.systemErrors(executor);

        assertTrue(
                context.supply(
                                () -> {
                                    ran.set(true);
                                    return Thread.currentThread().getName();
                                })
                        .join()
                        .isSuccess()
                        .toBoolean());
        assertTrue(ran.get());
    }
}
