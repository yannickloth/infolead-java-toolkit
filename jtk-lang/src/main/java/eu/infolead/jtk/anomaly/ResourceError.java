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
 * Represents resource availability or state errors.
 *
 * @param resourceType the type of the affected resource
 * @param errorType the specific resource error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual value that caused the failure (optional)
 * @param errorId unique identifier for this error instance (optional)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResourceError(
        String resourceType,
        ResourceErrorType errorType,
        Object[] contextArgs,
        @JsonIgnore Maybe<Object> actualValue,
        @JsonIgnore Maybe<String> errorId)
        implements SystemError {
    private static final long serialVersionUID = 1L;

    /**
     * Canonical constructor with validation.
     *
     * @param resourceType the type of the affected resource
     * @param errorType the specific resource error type
     * @param contextArgs additional arguments for message formatting
     * @param actualValue the rejected value, if available
     * @param errorId the unique error instance identifier, if assigned
     */
    public ResourceError {
        Objects.requireNonNull(resourceType, "resourceType cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     *
     * @param resourceType the type of the affected resource
     * @param errorType the specific resource error type
     */
    public ResourceError(final String resourceType, final ResourceErrorType errorType) {
        this(resourceType, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return String.format("%s: %s", resourceType, getTypeMessage());
    }

    /**
     * Returns the message with the error-type text localized for the requested language; the
     * resource type stays untranslated.
     *
     * <pre>{@code
     * ResourceError.notFound("User", 7)
     *         .getLocalizedMessage(Locale.GERMAN);
     * // "User: Die angeforderte Ressource wurde nicht gefunden"
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return the localized error message
     */
    @Override
    public String getLocalizedMessage(final Locale locale) {
        return String.format("%s: %s", resourceType, getLocalizedTypeMessage(locale));
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
        return new ResourceError(
                resourceType,
                errorType,
                contextArgs,
                actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new ResourceError(
                resourceType,
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
        return "ResourceError[resourceType="
                + resourceType
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
     * Creates an error for a missing resource.
     *
     * @param resourceType the resource type
     * @param resourceId the missing resource identifier
     * @return the resource error
     */
    public static ResourceError notFound(final String resourceType, final Object resourceId) {
        return new ResourceError(
                resourceType,
                ResourceErrorType.NOT_FOUND,
                new Object[] {resourceId},
                Maybe.ofNullable(resourceId),
                Maybe.none());
    }

    /**
     * Creates an error for a resource that already exists.
     *
     * @param resourceType the resource type
     * @param resourceId the existing resource identifier
     * @return the resource error
     */
    public static ResourceError alreadyExists(final String resourceType, final Object resourceId) {
        return new ResourceError(
                resourceType,
                ResourceErrorType.ALREADY_EXISTS,
                new Object[] {resourceId},
                Maybe.ofNullable(resourceId),
                Maybe.none());
    }

    /**
     * Restores the record from its JSON representation.
     *
     * <p>The operational members ({@code actualValue}, {@code errorId}) are not part of the wire
     * contract and are restored as {@link Maybe#none()}. They are reattached by the application
     * when an error crosses a process boundary.
     *
     * @param resourceType the resource type
     * @param errorType the error type
     * @param contextArgs the message-format arguments
     * @return the restored error
     */
    @JsonCreator
    public static ResourceError fromJson(
            @JsonProperty("resourceType") final String resourceType,
            @JsonProperty("errorType") final ResourceErrorType errorType,
            @JsonProperty("contextArgs") final Object[] contextArgs) {
        return new ResourceError(
                resourceType,
                errorType,
                contextArgs == null ? new Object[0] : contextArgs,
                Maybe.none(),
                Maybe.none());
    }
}
