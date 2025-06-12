package eu.infolead.jtk.anomaly;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

public record AuthorizationError(
    String requirement,
    AuthorizationErrorType errorType,
    Object[] contextArgs,
    Maybe<Object> actualValue,
    Maybe<String> errorId
) implements SystemError {

    public AuthorizationError {
        Objects.requireNonNull(requirement, "requirement cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
    }

    public AuthorizationError(String requirement, AuthorizationErrorType errorType) {
        this(requirement, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    public AuthorizationError(String requirement, AuthorizationErrorType errorType, String contextArg) {
        this(requirement, errorType, new Object[]{contextArg}, Maybe.none(), Maybe.none());
    }

    @Override public ErrorType getErrorType() { return errorType; }
    @Override public String getMessage() { return String.format("Authorization: %s", errorType.formatMessage(contextArgs)); }
    @Override public Maybe<String> getErrorId() { return errorId; }
    @Override public Object[] getContextArgs() { return contextArgs.clone(); }
    @Override public Maybe<Object> getActualValue() { return actualValue; }

    @Override
    public SystemError withGeneratedId() {
        return new AuthorizationError(requirement, errorType, contextArgs, actualValue, 
                                    Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(String customErrorId) {
        return new AuthorizationError(requirement, errorType, contextArgs, actualValue, 
                                    Maybe.ofNullable(customErrorId));
    }

    public static AuthorizationError notAuthenticated() {
        return new AuthorizationError("Authentication required", AuthorizationErrorType.NOT_AUTHENTICATED);
    }

    public static AuthorizationError insufficientPermissions(String requiredRole) {
        return new AuthorizationError("Insufficient permissions", AuthorizationErrorType.INSUFFICIENT_PERMISSIONS, requiredRole);
    }
}