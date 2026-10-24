package eu.infolead.jtk.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.anomaly.ParameterError;
import eu.infolead.jtk.anomaly.PostconditionError;
import eu.infolead.jtk.anomaly.PostconditionErrorType;
import eu.infolead.jtk.anomaly.PreconditionErrorType;
import eu.infolead.jtk.anomaly.SystemError;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class FailureStrategyTest {

    private final FailureStrategy<SystemError> strategy = FailureStrategy.systemErrors();

    @Test
    void transientExceptionsAreRetryable() {
        final SystemError io = strategy.fromException(new IOException("reset"));
        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, io.getErrorType());
        assertInstanceOf(IOException.class, io.toException().getCause());
        assertTrue(io.isRetryable());

        assertTrue(strategy.fromException(new TimeoutException("slow")).isRetryable());
        assertTrue(
                strategy.fromException(new UncheckedIOException(new IOException("wrapped")))
                        .isRetryable());
    }

    @Test
    void anExceptionWithoutAMessageStillNamesItsType() {
        final SystemError error = strategy.fromException(new IOException());

        assertTrue(error.getMessage().contains("IOException"));
    }

    @Test
    void deterministicExceptionsAreNotRetryable() {
        final SystemError failure = strategy.fromException(new IllegalStateException("bug"));

        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, failure.getErrorType());
        assertInstanceOf(IllegalStateException.class, failure.toException().getCause());
        assertFalse(failure.isRetryable());
    }

    @Test
    void executorRejectionBecomesServiceUnavailable() {
        final SystemError error = strategy.fromException(new RejectedExecutionException("full"));

        assertEquals(503, error.getHttpStatus());
        assertEquals(PreconditionErrorType.SYSTEM_NOT_READY, error.getErrorType());
        assertTrue(error.isRetryable());
    }

    @Test
    void retryabilityDelegatesToTheAnomalyModel() {
        assertTrue(strategy.retryable().test(PostconditionError.asyncOperationFailed("x")));
        assertFalse(strategy.retryable().test(ParameterError.nullParameter("x")));
    }

    @Test
    void synthesizedAnomaliesPassThroughUnchanged() {
        final SystemError anomaly = PostconditionError.operationTimedOut("operation");

        assertSame(anomaly, strategy.fromAnomaly(anomaly));
    }
}
