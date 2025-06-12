package eu.infolead.jtk.anomaly;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents precondition errors - system not in correct state for the requested operation.
 * These typically map to HTTP 4xx status codes (client errors) as the client requested
 * an operation that the system cannot perform at this time.
 * 
 * @param condition the precondition that was violated
 * @param errorType the specific precondition error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual system state that violated the precondition (optional)
 * @param errorId unique identifier for this error instance (optional)
 */
public record PreconditionError(
    String condition,
    PreconditionErrorType errorType,
    Object[] contextArgs,
    Maybe<Object> actualValue,
    Maybe<String> errorId
) implements SystemError {

    /**
     * Canonical constructor with validation.
     */
    public PreconditionError {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     */
    public PreconditionError(String condition, PreconditionErrorType errorType) {
        this(condition, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with actual value but no context args or error ID.
     */
    public PreconditionError(String condition, PreconditionErrorType errorType, Object actualValue) {
        this(condition, errorType, new Object[0], Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Convenience constructor with context args but no actual value or error ID.
     */
    public PreconditionError(String condition, PreconditionErrorType errorType, Object... contextArgs) {
        this(condition, errorType, contextArgs, Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        String baseMessage = errorType.formatMessage(contextArgs);
        return String.format("%s: %s", condition, baseMessage);
    }

    @Override
    public Maybe<String> getErrorId() {
        return errorId;
    }

    @Override
    public Object[] getContextArgs() {
        return contextArgs.clone();
    }

    @Override
    public Maybe<Object> getActualValue() {
        return actualValue;
    }

    @Override
    public SystemError withGeneratedId() {
        return new PreconditionError(condition, errorType, contextArgs, actualValue, 
                                   Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(String customErrorId) {
        return new PreconditionError(condition, errorType, contextArgs, actualValue, 
                                   Maybe.ofNullable(customErrorId));
    }

    // Factory methods for common precondition errors

    /**
     * Creates a precondition error for when system is not ready for operation.
     */
    public static PreconditionError systemNotReady(String reason) {
        return new PreconditionError("System not ready", PreconditionErrorType.SYSTEM_NOT_READY, reason);
    }

    /**
     * Creates a precondition error for conflicting system state.
     */
    public static PreconditionError conflictingState(String description) {
        return new PreconditionError("Conflicting state", PreconditionErrorType.CONFLICTING_STATE, description);
    }

    /**
     * Creates a precondition error for insufficient permissions.
     */
    public static PreconditionError insufficientPermissions(String requirement) {
        return new PreconditionError("Insufficient permissions", PreconditionErrorType.INSUFFICIENT_PERMISSIONS, requirement);
    }

    /**
     * Creates a precondition error for resource being locked.
     */
    public static PreconditionError resourceLocked(String resourceType, Object resourceId) {
        return new PreconditionError("Resource locked", PreconditionErrorType.RESOURCE_LOCKED, 
                                   new Object[]{resourceType, resourceId}, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a precondition error for operation not allowed in current state.
     */
    public static PreconditionError operationNotAllowed(String operation, String currentState) {
        return new PreconditionError("Operation not allowed", PreconditionErrorType.OPERATION_NOT_ALLOWED, 
                                   new Object[]{operation, currentState}, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a precondition error for rate limiting.
     */
    public static PreconditionError rateLimitExceeded(String limit) {
        return new PreconditionError("Rate limit exceeded", PreconditionErrorType.RATE_LIMIT_EXCEEDED, limit);
    }

    /**
     * Creates a precondition error for maintenance mode.
     */
    public static PreconditionError maintenanceMode(String estimatedDuration) {
        return new PreconditionError("System in maintenance", PreconditionErrorType.MAINTENANCE_MODE, estimatedDuration);
    }
}