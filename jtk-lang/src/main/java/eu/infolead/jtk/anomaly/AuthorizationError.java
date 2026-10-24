package eu.infolead.jtk.anomaly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.either.Maybe;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents authentication and authorization failures.
 *
 * @param requirement the access requirement that was not met
 * @param errorType the specific authorization error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual value that caused the failure (optional)
 * @param errorId unique identifier for this error instance (optional)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorizationError(
        String requirement,
        AuthorizationErrorType errorType,
        Object[] contextArgs,
        @JsonIgnore Maybe<Object> actualValue,
        @JsonIgnore Maybe<String> errorId)
        implements SystemError {
    private static final long serialVersionUID = 1L;

    /**
     * Canonical constructor with validation.
     *
     * @param requirement the access requirement that was not met
     * @param errorType the specific authorization error type
     * @param contextArgs additional arguments for message formatting
     * @param actualValue the actual value, if available
     * @param errorId the unique error instance identifier, if assigned
     */
    public AuthorizationError {
        Objects.requireNonNull(requirement, "requirement cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     *
     * @param requirement the access requirement that was not met
     * @param errorType the specific authorization error type
     */
    public AuthorizationError(final String requirement, final AuthorizationErrorType errorType) {
        this(requirement, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with one message-format argument.
     *
     * @param requirement the access requirement that was not met
     * @param errorType the specific authorization error type
     * @param contextArg the argument substituted into the message template
     */
    public AuthorizationError(
            final String requirement,
            final AuthorizationErrorType errorType,
            final String contextArg) {
        this(requirement, errorType, new Object[] {contextArg}, Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return String.format("Authorization: %s", getTypeMessage());
    }

    /**
     * Returns the message with the error-type text localized for the requested language; the
     * authorization label is localized too, while the requirement itself stays untranslated.
     *
     * <pre>{@code
     * AuthorizationError.notAuthenticated()
     *         .getLocalizedMessage(Locale.FRENCH);
     * // "Autorisation: Authentification requise"
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return the localized error message
     */
    @Override
    public String getLocalizedMessage(final Locale locale) {
        final String label =
                ErrorLocalizer.of(locale).label("error.label.authorization", "Authorization");
        return String.format("%s: %s", label, getLocalizedTypeMessage(locale));
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
        return new AuthorizationError(
                requirement,
                errorType,
                contextArgs,
                actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new AuthorizationError(
                requirement,
                errorType,
                contextArgs,
                actualValue,
                Maybe.some(Objects.requireNonNull(customErrorId, "customErrorId")));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AuthorizationError that)) {
            return false;
        }
        return requirement.equals(that.requirement)
                && errorType == that.errorType
                && actualValue.equals(that.actualValue)
                && errorId.equals(that.errorId)
                && Arrays.equals(contextArgs, that.contextArgs);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(requirement, errorType, actualValue, errorId)
                + Arrays.hashCode(contextArgs);
    }

    @Override
    public String toString() {
        return "AuthorizationError[requirement="
                + requirement
                + ", errorType="
                + errorType
                + ", contextArgs="
                + SystemError.renderValue(contextArgs)
                + ", actualValue="
                + actualValue.fold(() -> "none", SystemError::renderValue)
                + ", errorId="
                + errorId
                + "]";
    }

    /**
     * Creates an error for a missing authentication.
     *
     * @return the authorization error
     */
    public static AuthorizationError notAuthenticated() {
        return new AuthorizationError(
                "Authentication required", AuthorizationErrorType.NOT_AUTHENTICATED);
    }

    /**
     * Creates an error for insufficient permissions.
     *
     * @param requiredRole the role required by the operation
     * @return the authorization error
     */
    public static AuthorizationError insufficientPermissions(final String requiredRole) {
        return new AuthorizationError(
                "Insufficient permissions",
                AuthorizationErrorType.INSUFFICIENT_PERMISSIONS,
                requiredRole);
    }

    /**
     * Restores the record from its JSON representation.
     *
     * <p>The operational members ({@code actualValue}, {@code errorId}) are not part of the wire
     * contract and are restored as {@link Maybe#none()}. They are reattached by the application
     * when an error crosses a process boundary.
     *
     * @param requirement the access requirement
     * @param errorType the error type
     * @param contextArgs the message-format arguments
     * @return the restored error
     */
    @JsonCreator
    public static AuthorizationError fromJson(
            @JsonProperty("requirement") final String requirement,
            @JsonProperty("errorType") final AuthorizationErrorType errorType,
            @JsonProperty("contextArgs") final Object[] contextArgs) {
        return new AuthorizationError(
                requirement,
                errorType,
                contextArgs == null ? new Object[0] : contextArgs,
                Maybe.none(),
                Maybe.none());
    }
}
