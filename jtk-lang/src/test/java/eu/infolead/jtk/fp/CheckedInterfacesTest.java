package eu.infolead.jtk.fp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class CheckedInterfacesTest {

    @Test
    void checkedSupplierAcceptsNonThrowingLambdas() throws Exception {
        final CheckedSupplier<Integer> nonThrowing = () -> 42;

        assertEquals(Integer.valueOf(42), nonThrowing.get());
    }

    @Test
    void checkedSupplierUncheckedWrapsCheckedExceptions() {
        final CheckedSupplier<String> checked = () -> {
            throw new IOException("file missing");
        };

        final Throwable thrown = assertThrows(UncheckedException.class, () -> checked.unchecked().get());
        assertTrue(thrown.getCause() instanceof IOException);
    }

    @Test
    void checkedSupplierUncheckedRethrowsRuntimeExceptionsUnchanged() {
        final IllegalStateException original = new IllegalStateException("broken");
        final CheckedSupplier<String> checked = () -> {
            throw original;
        };

        assertSame(original, assertThrows(IllegalStateException.class, () -> checked.unchecked().get()));
    }

    @Test
    void checkedFunctionUncheckedProducesTheValue() {
        final CheckedFunction<String, Integer> checked = Integer::parseInt;

        assertEquals(Integer.valueOf(42), checked.unchecked().apply("42"));
    }

    @Test
    void checkedFunctionUncheckedWrapsCheckedExceptions() {
        final CheckedFunction<String, String> checked = value -> {
            throw new IOException("boom for " + value);
        };

        final Throwable thrown = assertThrows(UncheckedException.class, () -> checked.unchecked().apply("x"));
        assertTrue(thrown.getCause() instanceof IOException);
    }

    @Test
    void checkedRunnableUncheckedRunsTheComputation() {
        final AtomicBoolean ran = new AtomicBoolean();
        final CheckedRunnable checked = () -> ran.set(true);

        checked.unchecked().run();
        assertTrue(ran.get());
    }

    @Test
    void checkedRunnableUncheckedWrapsCheckedExceptions() {
        final CheckedRunnable checked = () -> {
            throw new IOException("boom");
        };

        final Throwable thrown = assertThrows(UncheckedException.class, checked.unchecked()::run);
        assertTrue(thrown.getCause() instanceof IOException);
    }

    @Test
    void checkedSupplierUncheckedRestoresTheInterruptStatus() {
        final CheckedSupplier<String> checked = () -> {
            throw new InterruptedException("cancelled");
        };

        try {
            final Throwable thrown = assertThrows(UncheckedException.class, () -> checked.unchecked().get());
            assertTrue(thrown.getCause() instanceof InterruptedException);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // clear the flag so other tests are unaffected
        }
    }

    @Test
    void checkedFunctionUncheckedRestoresTheInterruptStatus() {
        final CheckedFunction<String, String> checked = value -> {
            throw new InterruptedException("cancelled");
        };

        try {
            final Throwable thrown = assertThrows(UncheckedException.class, () -> checked.unchecked().apply("x"));
            assertTrue(thrown.getCause() instanceof InterruptedException);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // clear the flag so other tests are unaffected
        }
    }

    @Test
    void checkedRunnableUncheckedRestoresTheInterruptStatus() {
        final CheckedRunnable checked = () -> {
            throw new InterruptedException("cancelled");
        };

        try {
            final Throwable thrown = assertThrows(UncheckedException.class, checked.unchecked()::run);
            assertTrue(thrown.getCause() instanceof InterruptedException);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // clear the flag so other tests are unaffected
        }
    }
}
