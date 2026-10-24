package eu.infolead.jtk.async;

import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.fp.either.Result;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Shared fixtures and assertions for the asynchronous tests. */
final class AsyncTestSupport {

    static final long TIMEOUT_SECONDS = 10L;

    private AsyncTestSupport() {
        // Utility class
    }

    static ExecutorService newExecutor(final int threads) {
        return Executors.newFixedThreadPool(threads);
    }

    static <T> T await(final CompletableFuture<T> future) throws Exception {
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final TimeoutException timedOut) {
            throw new AssertionError(
                    "future did not complete within " + TIMEOUT_SECONDS + "s", timedOut);
        }
    }

    static void shutdown(final ExecutorService executor) {
        executor.shutdownNow();
        try {
            assertTrue(
                    executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "executor did not terminate");
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    static <F, S> S successValue(final Result<F, S> result) {
        assertTrue(result.isSuccess().toBoolean(), () -> "expected a success, got " + result);
        return result.fold(failure -> null, value -> value);
    }

    static <F, S> F failureValue(final Result<F, S> result) {
        assertTrue(result.isFailure().toBoolean(), () -> "expected a failure, got " + result);
        return result.fold(failure -> failure, value -> null);
    }
}
