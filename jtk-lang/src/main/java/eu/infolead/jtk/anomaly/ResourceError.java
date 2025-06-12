package eu.infolead.jtk.anomaly;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

public record ResourceError(
    String resourceType,
    ResourceErrorType errorType,
    Object[] contextArgs,
    Maybe<Object> actualValue,
    Maybe<String> errorId
) implements SystemError {

    public ResourceError {
        Objects.requireNonNull(resourceType, "resourceType cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
    }

    public ResourceError(String resourceType, ResourceErrorType errorType) {
        this(resourceType, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    @Override public ErrorType getErrorType() { return errorType; }
    @Override public String getMessage() { return String.format("%s: %s", resourceType, errorType.formatMessage(contextArgs)); }
    @Override public Maybe<String> getErrorId() { return errorId; }
    @Override public Object[] getContextArgs() { return contextArgs.clone(); }
    @Override public Maybe<Object> getActualValue() { return actualValue; }

    @Override
    public SystemError withGeneratedId() {
        return new ResourceError(resourceType, errorType, contextArgs, actualValue, 
                               Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(String customErrorId) {
        return new ResourceError(resourceType, errorType, contextArgs, actualValue, 
                               Maybe.ofNullable(customErrorId));
    }

    public static ResourceError notFound(String resourceType, Object resourceId) {
        return new ResourceError(resourceType, ResourceErrorType.NOT_FOUND, 
                               new Object[]{resourceId}, Maybe.none(), Maybe.none());
    }

    public static ResourceError alreadyExists(String resourceType, Object resourceId) {
        return new ResourceError(resourceType, ResourceErrorType.ALREADY_EXISTS, 
                               new Object[]{resourceId}, Maybe.none(), Maybe.none());
    }
}