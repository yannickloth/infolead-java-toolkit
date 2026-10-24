package eu.infolead.jtk.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.anomaly.PostconditionError;
import eu.infolead.jtk.anomaly.PostconditionErrorType;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Result;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class AnomaliesTest {

    private final AsyncContext<SystemError> context = AsyncContext.systemErrors(Runnable::run);

    @Test
    void aDeadlineBecomesARetryableTimeout() {
        final CancellationToken token = CancellationToken.create();
        token.cancel(CancellationReason.DEADLINE);

        final SystemError error = Anomalies.forCancellation(context, token);

        assertEquals(PostconditionErrorType.OPERATION_TIMED_OUT, error.getErrorType());
        assertTrue(error.isRetryable());
        assertInstanceOf(TimeoutException.class, error.toException().getCause());
    }

    @Test
    void callerCancellationIsNotRetryable() {
        final CancellationToken token = CancellationToken.create();
        token.cancel(CancellationReason.CALLER);

        final SystemError error = Anomalies.forCancellation(context, token);

        assertEquals(PostconditionErrorType.OPERATION_CANCELLED, error.getErrorType());
        assertFalse(error.isRetryable());
        assertInstanceOf(CancellationException.class, error.toException().getCause());
    }

    @Test
    void anExternalInterruptBecomesACancellation() {
        final InterruptedException interrupted = new InterruptedException("stopped");

        final SystemError error = Anomalies.forInterruption(context, interrupted);

        assertEquals(PostconditionErrorType.OPERATION_CANCELLED, error.getErrorType());
        assertFalse(error.isRetryable());
        assertSame(interrupted, error.toException().getCause());
    }

    @Test
    void aForeignCancellationKeepsItsCause() {
        final CancellationException cancelled = new CancellationException("foreign");

        final SystemError error = Anomalies.forForeignCancellation(context, cancelled);

        assertEquals(PostconditionErrorType.OPERATION_CANCELLED, error.getErrorType());
        assertSame(cancelled, error.toException().getCause());
    }

    @Test
    void unwrappingRemovesNestedCompletionWrappers() {
        final IllegalStateException cause = new IllegalStateException("root");
        final Throwable nested = new CompletionException(new ExecutionException(cause));

        assertSame(cause, Anomalies.unwrap(nested));
        assertSame(cause, Anomalies.unwrap(cause));
    }

    @Test
    void errorsAreNeverConvertedIntoFailures() {
        assertThrows(
                AssertionError.class, () -> Anomalies.asException(new AssertionError("fatal")));
    }

    @Test
    void resultAccessorsExtractBothTracks() {
        assertEquals("value", Anomalies.rightOf(Result.success("value")));
        assertEquals("failure", Anomalies.leftOf(Result.failure("failure")));
        assertNull(Anomalies.leftOf(Result.success("value")));
        assertNull(Anomalies.rightOf(Result.failure("failure")));
    }

    @Test
    void nonExceptionThrowablesAreWrapped() {
        final Throwable weird = new Throwable("weird");

        final Exception wrapped = Anomalies.asException(weird);

        assertInstanceOf(IllegalStateException.class, wrapped);
        assertSame(weird, wrapped.getCause());
    }

    @Test
    void cancellationDetectionRecognizesTheSynthesizedAnomaly() {
        assertTrue(Anomalies.isCancellation(PostconditionError.operationCancelled("operation")));
        assertFalse(Anomalies.isCancellation(PostconditionError.operationTimedOut("operation")));
        assertFalse(Anomalies.isCancellation("not a system error"));
    }
}
