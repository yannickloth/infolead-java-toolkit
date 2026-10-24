package eu.infolead.jtk.anomaly;

import java.util.Arrays;
import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents authentication and authorization failures.
 *
 * @param requirement the access requirement that was not met
 * @param errorType   the specific authorization error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual value that caused the failure (optional)
 * @param errorId     unique identifier for this error instance (optional)
 */
public record AuthorizationError(
        String requirement,
        AuthorizationErrorType errorType,
        Object[] contextArgs,
        Maybe<Object> actualValue,
        Maybe<String> errorId) implements SystemError {

    public AuthorizationError {
        Objects.requireNonNull(requirement, "requirement cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    public AuthorizationError(final String requirement, final AuthorizationErrorType errorType) {
        this(requirement, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    public AuthorizationError(final String requirement, final AuthorizationErrorType errorType,
            final String contextArg) {
        this(requirement, errorType, new Object[] { contextArg }, Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return String.format("Authorization: %s", errorType.formatMessage(contextArgs));
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
        return new AuthorizationError(requirement, errorType, contextArgs, actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new AuthorizationError(requirement, errorType, contextArgs, actualValue,
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
        return "AuthorizationError[requirement=" + requirement
                + ", errorType=" + errorType
                + ", contextArgs=" + Arrays.toString(contextArgs)
                + ", actualValue=" + actualValue
                + ", errorId=" + errorId + "]";
    }

    public static AuthorizationError notAuthenticated() {
        return new AuthorizationError("Authentication required", AuthorizationErrorType.NOT_AUTHENTICATED);
    }

    public static AuthorizationError insufficientPermissions(final String requiredRole) {
        return new AuthorizationError("Insufficient permissions",
                AuthorizationErrorType.INSUFFICIENT_PERMISSIONS, requiredRole);
    }
}
