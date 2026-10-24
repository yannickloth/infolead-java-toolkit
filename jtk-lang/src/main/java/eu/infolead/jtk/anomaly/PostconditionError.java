package eu.infolead.jtk.anomaly;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents postcondition errors - system failed to achieve expected state
 * after an operation. These typically map to HTTP 5xx status codes (server
 * errors) as the system did not work correctly to reach the expected state.
 *
 * @param condition   the postcondition that was violated
 * @param errorType   the specific postcondition error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual system state that violated the postcondition
 *                    (optional)
 * @param errorId     unique identifier for this error instance (optional)
 */
public record PostconditionError(
        String condition,
        PostconditionErrorType errorType,
        Object[] contextArgs,
        @JsonIgnore Maybe<Object> actualValue,
        @JsonIgnore Maybe<String> errorId) implements SystemError {

    /**
     * Canonical constructor with validation.
     *
     * @param condition   the postcondition that was violated
     * @param errorType   the specific postcondition error type
     * @param contextArgs additional arguments for message formatting
     * @param actualValue the actual system state, if available
     * @param errorId     the unique error instance identifier, if assigned
     */
    public PostconditionError {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     *
     * @param condition the postcondition that was violated
     * @param errorType the specific postcondition error type
     */
    public PostconditionError(final String condition, final PostconditionErrorType errorType) {
        this(condition, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with context args but no actual value or error ID.
     * <p>
     * Every argument is a message-format argument. Use the canonical constructor
     * when the third argument is the rejected value itself.
     *
     * @param condition   the postcondition that was violated
     * @param errorType   the specific postcondition error type
     * @param contextArgs arguments substituted into the message template
     */
    public PostconditionError(final String condition, final PostconditionErrorType errorType,
            final Object... contextArgs) {
        this(condition, errorType, contextArgs, Maybe.none(), Maybe.none());
    }

    /**
     * Creates an error carrying an actual value rather than context arguments.
     *
     * @param condition the postcondition that was violated
     * @param errorType the specific postcondition error type
     * @param actualValue the value that violated the postcondition
     * @return the postcondition error
     */
    public static PostconditionError withActualValue(final String condition,
            final PostconditionErrorType errorType, final Object actualValue) {
        return new PostconditionError(condition, errorType, new Object[0],
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
                : condition + ": " + getTypeMessage();
    }

    /**
     * Returns the message with the error-type text localized for the requested
     * language.
     *
     * <p>When no context arguments are present, the caller-supplied
     * {@code condition} is the whole message and is returned untranslated;
     * there is no error-type text to localize in that case.</p>
     *
     * <pre>{@code
     * PostconditionError.expectedStateNotReached("READY", "BUSY")
     *         .getLocalizedMessage(Locale.GERMAN);
     * // "Expected state not reached: Erwarteter Zustand 'READY' wurde nicht erreicht"
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return the localized error message
     */
    @Override
    public String getLocalizedMessage(final Locale locale) {
        return contextArgs.length == 0
                ? condition
                : condition + ": " + getLocalizedTypeMessage(locale);
    }

    @Override
    public Maybe<String> getErrorId() {
        return errorId;
    }

    /**
     * Returns a defensive copy of the message-format arguments.
     *
     * @return a copy of the context arguments
     */
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
        return new PostconditionError(condition, errorType, contextArgs, actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new PostconditionError(condition, errorType, contextArgs, actualValue,
                Maybe.some(Objects.requireNonNull(customErrorId, "customErrorId")));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PostconditionError that)) {
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
        return "PostconditionError[condition=" + condition
                + ", errorType=" + errorType
                + ", contextArgs=" + SystemError.renderValue(contextArgs)
                + ", actualValue=" + actualValue.fold(() -> "none", SystemError::renderValue)
                + ", errorId=" + errorId + "]";
    }

    // Factory methods for common postcondition errors

    /**
     * Creates a postcondition error for when expected state was not reached.
     *
     * @param expectedState the state that was expected
     * @param actualState   the state that was observed
     * @return the postcondition error
     */
    public static PostconditionError expectedStateNotReached(final String expectedState, final Object actualState) {
        return new PostconditionError("Expected state not reached",
                PostconditionErrorType.EXPECTED_STATE_NOT_REACHED,
                new Object[] { expectedState }, Maybe.ofNullable(actualState), Maybe.none());
    }

    /**
     * Creates a postcondition error for when an invariant was violated.
     *
     * @param invariantDescription the violated invariant
     * @return the postcondition error
     */
    public static PostconditionError invariantViolated(final String invariantDescription) {
        return new PostconditionError("Invariant violated",
                PostconditionErrorType.INVARIANT_VIOLATED,
                new Object[] { invariantDescription }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a postcondition error for when a side effect failed.
     *
     * @param sideEffectDescription the failed side effect
     * @return the postcondition error
     */
    public static PostconditionError sideEffectFailed(final String sideEffectDescription) {
        return new PostconditionError("Side effect failed",
                PostconditionErrorType.SIDE_EFFECT_FAILED,
                new Object[] { sideEffectDescription }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a postcondition error for when data persistence failed.
     *
     * @param entityType the entity type
     * @param entityId   the entity identifier
     * @return the postcondition error
     */
    public static PostconditionError persistenceFailed(final String entityType, final Object entityId) {
        return new PostconditionError("Persistence failed",
                PostconditionErrorType.PERSISTENCE_FAILED,
                new Object[] { entityType, entityId }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a postcondition error for when notification delivery failed.
     *
     * @param notificationType the notification type
     * @param recipient        the intended recipient
     * @return the postcondition error
     */
    public static PostconditionError notificationFailed(final String notificationType, final String recipient) {
        return new PostconditionError("Notification failed",
                PostconditionErrorType.NOTIFICATION_FAILED,
                new Object[] { notificationType, recipient }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a postcondition error for when data consistency was compromised.
     *
     * @param consistencyRule the violated consistency rule
     * @return the postcondition error
     */
    public static PostconditionError consistencyViolated(final String consistencyRule) {
        return new PostconditionError("Data consistency violated",
                PostconditionErrorType.CONSISTENCY_VIOLATED,
                new Object[] { consistencyRule }, Maybe.none(), Maybe.none());
    }

    /**
     * Creates a postcondition error for when external system integration failed.
     *
     * @param externalSystem the external system
     * @param operation      the failed operation
     * @return the postcondition error
     */
    public static PostconditionError integrationFailed(final String externalSystem, final String operation) {
        return new PostconditionError("External integration failed",
                PostconditionErrorType.INTEGRATION_FAILED,
                new Object[] { externalSystem, operation }, Maybe.none(), Maybe.none());
    }
}
