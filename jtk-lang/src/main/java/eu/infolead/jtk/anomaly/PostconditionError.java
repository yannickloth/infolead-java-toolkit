package eu.infolead.jtk.anomaly;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents postcondition errors - system failed to achieve expected state after operation.
 * These typically map to HTTP 5xx status codes (server errors) as the system
 * did not work correctly to reach the expected state.
 * 
 * @param condition the postcondition that was violated
 * @param errorType the specific postcondition error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual system state that violated the postcondition (optional)
 * @param errorId unique identifier for this error instance (optional)
 */
public record PostconditionError(
    String condition,
    PostconditionErrorType errorType,
    Object[] contextArgs,
    Maybe<Object> actualValue,
    Maybe<String> errorId
) implements SystemError {

    /**
     * Canonical constructor with validation.
     */
    public PostconditionError {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     */
    public PostconditionError(String condition, PostconditionErrorType errorType) {
        this(condition, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with actual value but no context args or error ID.
     */
    public PostconditionError(String condition, PostconditionErrorType errorType, Object actualValue) {
        this(condition, errorType, new Object[0], Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Convenience constructor with context args but no actual value or error ID.
     */
    public PostconditionError(String condition, PostconditionErrorType errorType, Object... contextArgs) {
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
        return new PostconditionError(condition, errorType, contextArgs, actualValue, 
                                    Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(String customErrorId) {
        return new PostconditionError(condition, errorType, contextArgs, actualValue, 
                                    Maybe.ofNullable(customErrorId));
    }

    // Factory methods for common postcondition errors

    /**
     * Creates a postcondition error for when expected state was not reached.
     */
    public static PostconditionError expectedStateNotReached(String expectedState, Object actualState) {
        return new PostconditionError("Expected state not reached", 
                                    PostconditionErrorType.EXPECTED_STATE_NOT_REACHED, 
                                    new Object[]{expectedState}, Maybe.ofNullable(actualState), Maybe.none());
    }

    /**
     * Creates a postcondition error for when an invariant was violated.
     */
    public static PostconditionError invariantViolated(String invariantDescription) {
        return new PostconditionError("Invariant violated", 
                                    PostconditionErrorType.INVARIANT_VIOLATED, invariantDescription);
    }

    /**
     * Creates a postcondition error for when a side effect failed.
     */
    public static PostconditionError sideEffectFailed(String sideEffectDescription) {
        return new PostconditionError("Side effect failed", 
                                    PostconditionErrorType.SIDE_EFFECT_FAILED, sideEffectDescription);
    }

    /**
     * Creates a postcondition error for when data persistence failed.
     */
    public static PostconditionError persistenceFailed(String entityType, Object entityId) {
        return new PostconditionError("Persistence failed", 
                                    PostconditionErrorType.PERSISTENCE_FAILED, 
                                    new Object[]{entityType, entityId}, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a postcondition error for when notification delivery failed.
     */
    public static PostconditionError notificationFailed(String notificationType, String recipient) {
        return new PostconditionError("Notification failed", 
                                    PostconditionErrorType.NOTIFICATION_FAILED, 
                                    new Object[]{notificationType, recipient}, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a postcondition error for when data consistency was compromised.
     */
    public static PostconditionError consistencyViolated(String consistencyRule) {
        return new PostconditionError("Data consistency violated", 
                                    PostconditionErrorType.CONSISTENCY_VIOLATED, consistencyRule);
    }

    /**
     * Creates a postcondition error for when external system integration failed.
     */
    public static PostconditionError integrationFailed(String externalSystem, String operation) {
        return new PostconditionError("External integration failed", 
                                    PostconditionErrorType.INTEGRATION_FAILED, 
                                    new Object[]{externalSystem, operation}, Maybe.none(), Maybe.none());
    }
}