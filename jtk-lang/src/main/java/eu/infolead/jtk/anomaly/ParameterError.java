package eu.infolead.jtk.anomaly;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents parameter validation errors - invalid arguments passed to methods.
 * These typically map to HTTP 400 Bad Request status codes.
 *
 * @param parameterName the name of the invalid parameter
 * @param errorType     the specific parameter error type
 * @param contextArgs   additional arguments for error message formatting
 * @param actualValue   the actual parameter value that was invalid (optional)
 * @param errorId       unique identifier for this error instance (optional)
 */
public record ParameterError(
        String parameterName,
        ParameterErrorType errorType,
        Object[] contextArgs,
        @JsonIgnore Maybe<Object> actualValue,
        @JsonIgnore Maybe<String> errorId) implements SystemError {

    /**
     * Canonical constructor with validation. The context array is copied so that
     * later modifications by the caller cannot change this error.
     *
     * @param parameterName the name of the invalid parameter
     * @param errorType     the specific parameter error type
     * @param contextArgs   additional arguments for message formatting
     * @param actualValue   the rejected value, if available
     * @param errorId       the unique error instance identifier, if assigned
     */
    public ParameterError {
        Objects.requireNonNull(parameterName, "parameterName cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     *
     * @param parameterName the name of the invalid parameter
     * @param errorType     the specific parameter error type
     */
    public ParameterError(final String parameterName, final ParameterErrorType errorType) {
        this(parameterName, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with context args but no actual value or error ID.
     * <p>
     * Every argument is a message-format argument. Use the canonical constructor
     * when the third argument is the rejected value itself.
     *
     * @param parameterName the name of the invalid parameter
     * @param errorType     the specific parameter error type
     * @param contextArgs   arguments substituted into the message template
     */
    public ParameterError(final String parameterName, final ParameterErrorType errorType,
            final Object... contextArgs) {
        this(parameterName, errorType, contextArgs, Maybe.none(), Maybe.none());
    }

    /**
     * Creates an error carrying an actual value rather than context arguments.
     *
     * @param parameterName the parameter name
     * @param errorType the specific parameter error type
     * @param actualValue the value that violated the parameter contract
     * @return the parameter error
     */
    public static ParameterError withActualValue(final String parameterName,
            final ParameterErrorType errorType, final Object actualValue) {
        return new ParameterError(parameterName, errorType, new Object[0],
                Maybe.ofNullable(actualValue), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return String.format("%s: %s", parameterName, getTypeMessage());
    }

    /**
     * Returns the message with the error-type text localized for the requested
     * language; the parameter name stays untranslated.
     *
     * <pre>{@code
     * ParameterError.nullParameter("userId")
     *         .getLocalizedMessage(Locale.FRENCH);
     * // "userId: Le paramètre ne peut pas être nul"
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return the localized error message
     */
    @Override
    public String getLocalizedMessage(final Locale locale) {
        return String.format("%s: %s", parameterName, getLocalizedTypeMessage(locale));
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
        return new ParameterError(parameterName, errorType, contextArgs, actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new ParameterError(parameterName, errorType, contextArgs, actualValue,
                Maybe.some(Objects.requireNonNull(customErrorId, "customErrorId")));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ParameterError that)) {
            return false;
        }
        return parameterName.equals(that.parameterName)
                && errorType == that.errorType
                && actualValue.equals(that.actualValue)
                && errorId.equals(that.errorId)
                && Arrays.equals(contextArgs, that.contextArgs);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(parameterName, errorType, actualValue, errorId)
                + Arrays.hashCode(contextArgs);
    }

    @Override
    public String toString() {
        return "ParameterError[parameterName=" + parameterName
                + ", errorType=" + errorType
                + ", contextArgs=" + SystemError.renderValue(contextArgs)
                + ", actualValue=" + actualValue.fold(() -> "none", SystemError::renderValue)
                + ", errorId=" + errorId + "]";
    }

    // Factory methods for common parameter errors

    /**
     * Creates a parameter error for a null parameter.
     *
     * @param parameterName the name of the null parameter
     * @return the parameter error
     */
    public static ParameterError nullParameter(final String parameterName) {
        return new ParameterError(parameterName, ParameterErrorType.NULL_PARAMETER);
    }

    /**
     * Creates a parameter error for an out-of-range value.
     *
     * @param parameterName the parameter name
     * @param actualValue   the rejected value
     * @param min           the inclusive minimum
     * @param max           the inclusive maximum
     * @return the parameter error
     */
    public static ParameterError outOfRange(final String parameterName, final Object actualValue,
            final Object min, final Object max) {
        return new ParameterError(parameterName, ParameterErrorType.OUT_OF_RANGE,
                new Object[] { min, max }, Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Creates a parameter error for an invalid format.
     *
     * @param parameterName the parameter name
     * @param actualValue   the rejected value
     * @return the parameter error
     */
    public static ParameterError invalidFormat(final String parameterName, final Object actualValue) {
        return new ParameterError(parameterName, ParameterErrorType.INVALID_FORMAT,
                new Object[0], Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Creates a parameter error for a value that's too long.
     *
     * @param parameterName the parameter name
     * @param actualValue   the rejected value
     * @param maxLength     the maximum permitted length
     * @return the parameter error
     */
    public static ParameterError tooLong(final String parameterName, final Object actualValue,
            final int maxLength) {
        return new ParameterError(parameterName, ParameterErrorType.TOO_LONG,
                new Object[] { maxLength }, Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Creates a parameter error for a value that's too short.
     *
     * @param parameterName the parameter name
     * @param actualValue   the rejected value
     * @param minLength     the minimum permitted length
     * @return the parameter error
     */
    public static ParameterError tooShort(final String parameterName, final Object actualValue,
            final int minLength) {
        return new ParameterError(parameterName, ParameterErrorType.TOO_SHORT,
                new Object[] { minLength }, Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Creates a parameter error for an empty collection/array when non-empty
     * required.
     *
     * @param parameterName the parameter name
     * @return the parameter error
     */
    public static ParameterError empty(final String parameterName) {
        return new ParameterError(parameterName, ParameterErrorType.EMPTY);
    }

    /**
     * Creates a parameter error for an unsupported value.
     *
     * @param parameterName the parameter name
     * @param actualValue   the unsupported value
     * @return the parameter error
     */
    public static ParameterError unsupportedValue(final String parameterName, final Object actualValue) {
        return new ParameterError(parameterName, ParameterErrorType.UNSUPPORTED_VALUE,
                new Object[0], Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Creates a parameter error for an unsupported value with a custom message.
     *
     * @param parameterName the parameter name
     * @param actualValue   the unsupported value
     * @param message       the message to include as a context argument
     * @return the parameter error
     */
    public static ParameterError unsupportedValue(final String parameterName, final Object actualValue,
            final String message) {
        return new ParameterError(parameterName, ParameterErrorType.UNSUPPORTED_VALUE,
                new Object[] { message }, Maybe.ofNullable(actualValue), Maybe.none());
    }
}
