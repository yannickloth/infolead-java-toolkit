package eu.infolead.jtk.async;

import eu.infolead.jtk.anomaly.PostconditionError;
import eu.infolead.jtk.anomaly.SystemError;
import java.util.function.Predicate;

/**
 * Adapter between a caller's failure type {@code F} and the toolkit anomaly model.
 *
 * <p>An asynchronous operation can fail for reasons that the operation itself never produced: a
 * deadline elapses, a caller cancels, or the executor rejects the task. Those failures must still
 * be expressed as an {@code F}, which only the caller can construct. A strategy supplies that
 * construction once, instead of a mapper on every call.
 *
 * <p>For code whose failures are {@link SystemError} values, use {@link #systemErrors()}; it maps
 * captured exceptions to {@link PostconditionError#asyncOperationFailed(String)} and consults
 * {@link SystemError#isRetryable()} for the retry decision.
 *
 * <p>An application with its own failure type supplies three functions:
 *
 * <pre>{@code
 * public record ApiError(String code, String message) {}
 *
 * FailureStrategy<ApiError> apiErrors = new FailureStrategy<>() {
 *     @Override
 *     public ApiError fromAnomaly(SystemError anomaly) {
 *         return new ApiError("async", anomaly.getMessage());
 *     }
 *
 *     @Override
 *     public ApiError fromException(Exception exception) {
 *         return new ApiError("internal", exception.getMessage());
 *     }
 *
 *     @Override
 *     public Predicate<? super ApiError> retryable() {
 *         return error -> error.code().equals("unavailable");
 *     }
 * };
 *
 * AsyncContext<ApiError> context = AsyncContext.systemErrors(pool).withFailures(apiErrors);
 * }</pre>
 *
 * @param <F> the failure type used by the application
 */
public interface FailureStrategy<F> {

    /**
     * Converts a synthesized anomaly, such as a deadline or a cancellation, into the application
     * failure type.
     *
     * @param anomaly the synthesized anomaly
     * @return the equivalent application failure
     */
    F fromAnomaly(SystemError anomaly);

    /**
     * Converts an exception thrown by a computation into the application failure type.
     *
     * <p>{@link Error} instances are never passed to this method; they propagate to the caller as
     * JVM failures.
     *
     * @param exception the captured exception
     * @return the equivalent application failure
     */
    F fromException(Exception exception);

    /**
     * Decides whether a failure may be retried.
     *
     * <p>This predicate is necessary but not sufficient: a retry engine also refuses to retry a
     * cancellation, regardless of what this predicate returns.
     *
     * @return the retry predicate for this failure type
     */
    Predicate<? super F> retryable();

    /**
     * Returns the strategy for applications that use {@link SystemError} as their failure type.
     *
     * <p>Captured exceptions become {@link PostconditionError#asyncOperationFailed(String)} values
     * with the exception attached as the cause. They are considered retryable only when they are
     * transient I/O or timeout failures; every other exception is treated as deterministic.
     *
     * @return the system-error strategy
     */
    static FailureStrategy<SystemError> systemErrors() {
        return SystemErrorStrategy.INSTANCE;
    }
}
