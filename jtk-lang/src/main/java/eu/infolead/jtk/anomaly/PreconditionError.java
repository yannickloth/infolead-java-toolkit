package eu.infolead.jtk.anomaly;

import java.util.Arrays;
import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents precondition errors - system not in correct state for the requested
 * operation. These typically map to HTTP 4xx status codes (client errors) as the
 * client requested an operation that the system cannot perform at this time.
 *
 * @param condition   the precondition that was violated
 * @param errorType   the specific precondition error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual system state that violated the precondition
 *                    (optional)
 * @param errorId     unique identifier for this error instance (optional)
 */
public record PreconditionError(
        String condition,
        PreconditionErrorType errorType,
        Object[] contextArgs,
        Maybe<Object> actualValue,
        Maybe<String> errorId) implements SystemError {

    /**
     * Canonical constructor with validation.
     */
    public PreconditionError {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     */
    public PreconditionError(final String condition, final PreconditionErrorType errorType) {
        this(condition, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with context args but no actual value or error ID.
     * <p>
     * Every argument is a message-format argument. Use the canonical constructor
     * when the third argument is the rejected value itself.
     */
    public PreconditionError(final String condition, final PreconditionErrorType errorType,
            final Object... contextArgs) {
        this(condition, errorType, contextArgs, Maybe.none(), Maybe.none());
    }

    /**
     * Creates an error carrying an actual value rather than context arguments.
     *
     * @param condition the precondition that was violated
     * @param errorType the specific precondition error type
     * @param actualValue the value that violated the precondition
     * @return the precondition error
     */
    public static PreconditionError withActualValue(final String condition,
            final PreconditionErrorType errorType, final Object actualValue) {
        return new PreconditionError(condition, errorType, new Object[0],
                Maybe.ofNullable(actualValue), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return contextArgs.length == 0
                ? condition
                : condition + ": " + errorType.formatMessage(contextArgs);
    }

    @Override
    public Maybe<String> getErrorId() {
        return errorId;
    }

    @Override
    public Object[] contextArgs() {
        return contextArgs.clone();
    }

    @Override
    public Object[] getContextArgs() {
        return contextArgs();
    }

    @Override
    public Maybe<Object> getActualValue() {
        return actualValue;
    }

    @Override
    public SystemError withGeneratedId() {
        if (errorId.isPresent().toBoolean()) {
            return this;
        }
        return new PreconditionError(condition, errorType, contextArgs, actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new PreconditionError(condition, errorType, contextArgs, actualValue,
                Maybe.some(Objects.requireNonNull(customErrorId, "customErrorId")));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PreconditionError that)) {
            return false;
        }
        return condition.equals(that.condition)
                && errorType == that.errorType
                && actualValue.equals(that.actualValue)
                && errorId.equals(that.errorId)
                && Arrays.equals(contextArgs, that.contextArgs);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(condition, errorType, actualValue, errorId)
                + Arrays.hashCode(contextArgs);
    }

    @Override
    public String toString() {
        return "PreconditionError[condition=" + condition
                + ", errorType=" + errorType
                + ", contextArgs=" + SystemError.renderValue(contextArgs)
                + ", actualValue=" + actualValue.fold(() -> "none", SystemError::renderValue)
                + ", errorId=" + errorId + "]";
    }

    // Factory methods for common precondition errors

    /**
     * Creates a precondition error for when system is not ready for operation.
     */
    public static PreconditionError systemNotReady(final String reason) {
        return new PreconditionError("System not ready", PreconditionErrorType.SYSTEM_NOT_READY,
                new Object[] { reason }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a precondition error for conflicting system state.
     */
    public static PreconditionError conflictingState(final String description) {
        return new PreconditionError("Conflicting state", PreconditionErrorType.CONFLICTING_STATE,
                new Object[] { description }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a precondition error for insufficient permissions.
     */
    public static PreconditionError insufficientPermissions(final String requirement) {
        return new PreconditionError("Insufficient permissions", PreconditionErrorType.INSUFFICIENT_PERMISSIONS,
                new Object[] { requirement }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a precondition error for resource being locked.
     */
    public static PreconditionError resourceLocked(final String resourceType, final Object resourceId) {
        return new PreconditionError("Resource locked", PreconditionErrorType.RESOURCE_LOCKED,
                new Object[] { resourceType, resourceId }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a precondition error for operation not allowed in current state.
     */
    public static PreconditionError operationNotAllowed(final String operation, final String currentState) {
        return new PreconditionError("Operation not allowed", PreconditionErrorType.OPERATION_NOT_ALLOWED,
                new Object[] { operation, currentState }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a precondition error for rate limiting.
     */
    public static PreconditionError rateLimitExceeded(final String limit) {
        return new PreconditionError("Rate limit exceeded", PreconditionErrorType.RATE_LIMIT_EXCEEDED,
                new Object[] { limit }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a precondition error for maintenance mode.
     */
    public static PreconditionError maintenanceMode(final String estimatedDuration) {
        return new PreconditionError("System in maintenance", PreconditionErrorType.MAINTENANCE_MODE,
                new Object[] { estimatedDuration }, Maybe.none(), Maybe.none());
    }
}
