package eu.infolead.jtk.anomaly;

import java.util.Arrays;
import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents resource availability or state errors.
 *
 * @param resourceType the type of the affected resource
 * @param errorType    the specific resource error type
 * @param contextArgs  additional arguments for error message formatting
 * @param actualValue  the actual value that caused the failure (optional)
 * @param errorId      unique identifier for this error instance (optional)
 */
public record ResourceError(
        String resourceType,
        ResourceErrorType errorType,
        Object[] contextArgs,
        Maybe<Object> actualValue,
        Maybe<String> errorId) implements SystemError {

    public ResourceError {
        Objects.requireNonNull(resourceType, "resourceType cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    public ResourceError(final String resourceType, final ResourceErrorType errorType) {
        this(resourceType, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        final String baseMessage = contextArgs.length == 0
                ? errorType.messageWithoutArgs()
                : errorType.formatMessage(contextArgs);
        return String.format("%s: %s", resourceType, baseMessage);
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
        return new ResourceError(resourceType, errorType, contextArgs, actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new ResourceError(resourceType, errorType, contextArgs, actualValue,
                Maybe.some(Objects.requireNonNull(customErrorId, "customErrorId")));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResourceError that)) {
            return false;
        }
        return resourceType.equals(that.resourceType)
                && errorType == that.errorType
                && actualValue.equals(that.actualValue)
                && errorId.equals(that.errorId)
                && Arrays.equals(contextArgs, that.contextArgs);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(resourceType, errorType, actualValue, errorId)
                + Arrays.hashCode(contextArgs);
    }

    @Override
    public String toString() {
        return "ResourceError[resourceType=" + resourceType
                + ", errorType=" + errorType
                + ", contextArgs=" + SystemError.renderValue(contextArgs)
                + ", actualValue=" + actualValue.fold(() -> "none", SystemError::renderValue)
                + ", errorId=" + errorId + "]";
    }

    public static ResourceError notFound(final String resourceType, final Object resourceId) {
        return new ResourceError(resourceType, ResourceErrorType.NOT_FOUND,
                new Object[] { resourceId }, Maybe.ofNullable(resourceId), Maybe.none());
    }

    public static ResourceError alreadyExists(final String resourceType, final Object resourceId) {
        return new ResourceError(resourceType, ResourceErrorType.ALREADY_EXISTS,
                new Object[] { resourceId }, Maybe.ofNullable(resourceId), Maybe.none());
    }
}
