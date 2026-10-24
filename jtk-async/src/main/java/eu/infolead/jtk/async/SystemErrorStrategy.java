package eu.infolead.jtk.async;

import eu.infolead.jtk.anomaly.PostconditionError;
import eu.infolead.jtk.anomaly.PreconditionError;
import eu.infolead.jtk.anomaly.SystemError;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * Canonical {@link FailureStrategy} for applications that use {@link SystemError} as their failure
 * type.
 */
enum SystemErrorStrategy implements FailureStrategy<SystemError> {

    /** The shared instance. */
    INSTANCE;

    @Override
    public SystemError fromAnomaly(final SystemError anomaly) {
        return Objects.requireNonNull(anomaly, "anomaly");
    }

    @Override
    public SystemError fromException(final Exception exception) {
        Objects.requireNonNull(exception, "exception");
        if (exception instanceof RejectedExecutionException) {
            return PreconditionError.systemNotReady(describe(exception)).withCause(exception);
        }
        return PostconditionError.asyncOperationFailed(describe(exception))
                .withCause(exception)
                .withRetryable(isTransient(exception));
    }

    @Override
    public Predicate<? super SystemError> retryable() {
        return SystemError::isRetryable;
    }

    private static String describe(final Exception exception) {
        final String message = exception.getMessage();
        final String type = exception.getClass().getSimpleName();
        return message == null || message.isBlank() ? type : type + ": " + message;
    }

    private static boolean isTransient(final Exception exception) {
        return exception instanceof IOException
                || exception instanceof UncheckedIOException
                || exception instanceof TimeoutException;
    }
}
