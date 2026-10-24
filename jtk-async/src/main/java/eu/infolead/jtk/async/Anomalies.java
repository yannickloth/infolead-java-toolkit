package eu.infolead.jtk.async;

import eu.infolead.jtk.anomaly.PostconditionError;
import eu.infolead.jtk.anomaly.PostconditionErrorType;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Result;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * Construction and classification of the anomalies that the asynchronous layer synthesizes.
 *
 * <p>The mapping is the contract that makes cancellation propagate the right failure value:
 *
 * <ul>
 *   <li>a deadline becomes {@link PostconditionErrorType#OPERATION_TIMED_OUT} (HTTP 504, retryable
 *       by default);
 *   <li>every other cancellation reason becomes {@link PostconditionErrorType#OPERATION_CANCELLED}
 *       (HTTP 408, explicitly marked non-retryable).
 * </ul>
 */
final class Anomalies {

    private Anomalies() {
        // Utility class
    }

    /**
     * Builds the anomaly matching the token's cancellation reason.
     *
     * @param context the operation context, used for the operation name
     * @param token the cancelled token
     * @return the anomaly to publish
     */
    static SystemError forCancellation(
            final AsyncContext<?> context, final CancellationToken token) {
        final CancellationReason reason = token.reason().or(CancellationReason.CALLER);
        if (reason == CancellationReason.DEADLINE) {
            return forDeadline(context);
        }
        return PostconditionError.operationCancelled(context.name())
                .withCause(new CancellationException("cancelled for " + reason))
                .withRetryable(false);
    }

    /**
     * Builds the anomaly for an elapsed deadline.
     *
     * @param context the operation context
     * @return the timeout anomaly
     */
    static SystemError forDeadline(final AsyncContext<?> context) {
        return PostconditionError.operationTimedOut(context.name())
                .withCause(
                        new java.util.concurrent.TimeoutException(
                                "deadline exceeded for " + context.name()));
    }

    /**
     * Builds the anomaly for a future that was cancelled by code that does not use a {@link
     * CancellationToken}.
     *
     * @param context the operation context
     * @param cancelled the foreign cancellation
     * @return the cancellation anomaly
     */
    static SystemError forForeignCancellation(
            final AsyncContext<?> context, final CancellationException cancelled) {
        return PostconditionError.operationCancelled(context.name())
                .withCause(cancelled)
                .withRetryable(false);
    }

    /**
     * Builds the anomaly for a task interrupted by code that did not use the token.
     *
     * @param context the operation context
     * @param interrupted the interrupt that the task observed
     * @return the anomaly to publish
     */
    static SystemError forInterruption(
            final AsyncContext<?> context, final InterruptedException interrupted) {
        return PostconditionError.operationCancelled(context.name())
                .withCause(interrupted)
                .withRetryable(false);
    }

    /**
     * Returns whether a failure value is a cancellation, so a retry engine refuses to repeat it.
     *
     * @param failure the failure value
     * @return true when the value represents a cancelled operation
     */
    static boolean isCancellation(final Object failure) {
        return failure instanceof SystemError error
                && error.getErrorType() == PostconditionErrorType.OPERATION_CANCELLED;
    }

    /**
     * Extracts the failure value of a failed result.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param result the failed result
     * @return the failure value
     */
    @SuppressWarnings("unchecked")
    static <F, S> F leftOf(final Result<F, S> result) {
        return (F) result.fold(value -> value, success -> null);
    }

    /**
     * Extracts the success value of a successful result.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param result the successful result
     * @return the success value
     */
    @SuppressWarnings("unchecked")
    static <F, S> S rightOf(final Result<F, S> result) {
        return (S) result.fold(failure -> null, value -> value);
    }

    /**
     * Unwraps the completion wrappers that {@link java.util.concurrent} adds around a failure.
     *
     * @param error the completion error
     * @return the underlying throwable
     */
    static Throwable unwrap(final Throwable error) {
        Throwable current = Objects.requireNonNull(error, "error");
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    /**
     * Converts an unwrapped throwable into an {@link Exception} for the failure strategy. Errors
     * are rejected: they must propagate.
     *
     * @param error the unwrapped throwable
     * @return the throwable as an exception
     */
    static Exception asException(final Throwable error) {
        if (error instanceof Error fatal) {
            throw fatal;
        }
        if (error instanceof Exception exception) {
            return exception;
        }
        return new IllegalStateException(error);
    }
}
