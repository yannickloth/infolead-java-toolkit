package eu.infolead.jtk.anomaly;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents parameter validation errors - invalid arguments passed to methods.
 * These typically map to HTTP 400 Bad Request status codes.
 * 
 * @param parameterName the name of the invalid parameter
 * @param errorType the specific parameter error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual parameter value that was invalid (optional)
 * @param errorId unique identifier for this error instance (optional)
 */
public record ParameterError(
    String parameterName,
    ParameterErrorType errorType,
    Object[] contextArgs,
    Maybe<Object> actualValue,
    Maybe<String> errorId
) implements SystemError {

    /**
     * Canonical constructor with validation.
     */
    public ParameterError {
        Objects.requireNonNull(parameterName, "parameterName cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     */
    public ParameterError(String parameterName, ParameterErrorType errorType) {
        this(parameterName, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with actual value but no context args or error ID.
     */
    public ParameterError(String parameterName, ParameterErrorType errorType, Object actualValue) {
        this(parameterName, errorType, new Object[0], Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Convenience constructor with context args but no actual value or error ID.
     */
    public ParameterError(String parameterName, ParameterErrorType errorType, Object... contextArgs) {
        this(parameterName, errorType, contextArgs, Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        String baseMessage = errorType.formatMessage(contextArgs);
        return String.format("%s: %s", parameterName, baseMessage);
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
        return new ParameterError(parameterName, errorType, contextArgs, actualValue, 
                                 Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(String customErrorId) {
        return new ParameterError(parameterName, errorType, contextArgs, actualValue, 
                                 Maybe.ofNullable(customErrorId));
    }

    // Factory methods for common parameter errors

    /**
     * Creates a parameter error for a null parameter.
     */
    public static ParameterError nullParameter(String parameterName) {
        return new ParameterError(parameterName, ParameterErrorType.NULL_PARAMETER);
    }

    /**
     * Creates a parameter error for an out-of-range value.
     */
    public static ParameterError outOfRange(String parameterName, Object actualValue, Object min, Object max) {
        return new ParameterError(parameterName, ParameterErrorType.OUT_OF_RANGE, 
                                 new Object[]{min, max}, Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Creates a parameter error for an invalid format.
     */
    public static ParameterError invalidFormat(String parameterName, Object actualValue) {
        return new ParameterError(parameterName, ParameterErrorType.INVALID_FORMAT, actualValue);
    }

    /**
     * Creates a parameter error for a value that's too long.
     */
    public static ParameterError tooLong(String parameterName, Object actualValue, int maxLength) {
        return new ParameterError(parameterName, ParameterErrorType.TOO_LONG, 
                                 new Object[]{maxLength}, Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Creates a parameter error for a value that's too short.
     */
    public static ParameterError tooShort(String parameterName, Object actualValue, int minLength) {
        return new ParameterError(parameterName, ParameterErrorType.TOO_SHORT, 
                                 new Object[]{minLength}, Maybe.ofNullable(actualValue), Maybe.none());
    }

    /**
     * Creates a parameter error for an empty collection/array when non-empty required.
     */
    public static ParameterError empty(String parameterName) {
        return new ParameterError(parameterName, ParameterErrorType.EMPTY);
    }

    /**
     * Creates a parameter error for an unsupported value.
     */
    public static ParameterError unsupportedValue(String parameterName, Object actualValue) {
        return new ParameterError(parameterName, ParameterErrorType.UNSUPPORTED_VALUE, actualValue);
    }
}