package eu.infolead.jtk.fp.validation;

import java.util.Objects;

import eu.infolead.jtk.anomaly.ErrorType;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.StandardErrorType;
import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents a structured validation error with field path, error type, and contextual information.
 * Uses ErrorType enums for consistent error handling and HTTP status mapping.
 * 
 * @param fieldPath the path to the field that failed validation (e.g., "user.email", "items[0].name")
 * @param errorType the type of error that occurred (enum implementing ErrorType)
 * @param contextArgs additional arguments for error message formatting
 * @param rejectedValue the value that failed validation (optional)
 * @param errorId unique identifier for this error instance (optional)
 */
public record ValidationError(
    String fieldPath,
    ErrorType errorType,
    Object[] contextArgs,
    Maybe<Object> rejectedValue,
    Maybe<String> errorId
) {
    
    /**
     * Canonical constructor with validation.
     */
    public ValidationError {
        Objects.requireNonNull(fieldPath, "fieldPath cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(rejectedValue, "rejectedValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
    }

    /**
     * Convenience constructor without context args, rejected value, or error ID.
     * 
     * @param fieldPath the field path
     * @param errorType the error type
     */
    public ValidationError(String fieldPath, ErrorType errorType) {
        this(fieldPath, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with context args but no rejected value or error ID.
     * 
     * @param fieldPath the field path
     * @param errorType the error type
     * @param contextArgs arguments for message formatting
     */
    public ValidationError(String fieldPath, ErrorType errorType, Object... contextArgs) {
        this(fieldPath, errorType, contextArgs, Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with rejected value but no context args or error ID.
     * 
     * @param fieldPath the field path
     * @param errorType the error type
     * @param rejectedValue the rejected value
     */
    public ValidationError(String fieldPath, ErrorType errorType, Object rejectedValue) {
        this(fieldPath, errorType, new Object[0], Maybe.ofNullable(rejectedValue), Maybe.none());
    }

    /**
     * Convenience constructor with auto-generated error ID.
     * 
     * @param fieldPath the field path
     * @param errorType the error type
     * @param contextArgs arguments for message formatting
     * @param rejectedValue the rejected value
     * @param generateId whether to generate an automatic error ID
     */
    public ValidationError(String fieldPath, ErrorType errorType, Object[] contextArgs, 
                          Maybe<Object> rejectedValue, boolean generateId) {
        this(fieldPath, errorType, contextArgs, rejectedValue, 
             generateId ? Maybe.some(errorType.generateErrorId()) : Maybe.none());
    }

    /**
     * Creates a validation error for a required field.
     * 
     * @param fieldPath the field path
     * @return a ValidationError for a required field
     */
    public static ValidationError required(String fieldPath) {
        return new ValidationError(fieldPath, StandardErrorType.REQUIRED);
    }

    /**
     * Creates a validation error for a required field with generated error ID.
     * 
     * @param fieldPath the field path
     * @return a ValidationError for a required field with error ID
     */
    public static ValidationError requiredWithId(String fieldPath) {
        return new ValidationError(fieldPath, StandardErrorType.REQUIRED, new Object[0], Maybe.none(), true);
    }

    /**
     * Creates a validation error for an invalid format.
     * 
     * @param fieldPath the field path
     * @param value the invalid value
     * @return a ValidationError for invalid format
     */
    public static ValidationError invalidFormat(String fieldPath, Object value) {
        return new ValidationError(fieldPath, StandardErrorType.INVALID_FORMAT, 
            new Object[0], Maybe.ofNullable(value), Maybe.none());
    }

    /**
     * Creates a validation error for an invalid format with generated error ID.
     * 
     * @param fieldPath the field path
     * @param value the invalid value
     * @return a ValidationError for invalid format with error ID
     */
    public static ValidationError invalidFormatWithId(String fieldPath, Object value) {
        return new ValidationError(fieldPath, StandardErrorType.INVALID_FORMAT, 
            new Object[0], Maybe.ofNullable(value), true);
    }

    /**
     * Creates a validation error for a value that's too long.
     * 
     * @param fieldPath the field path
     * @param value the too-long value
     * @param maxLength the maximum allowed length
     * @return a ValidationError for too long value
     */
    public static ValidationError tooLong(String fieldPath, Object value, int maxLength) {
        return new ValidationError(fieldPath, StandardErrorType.TOO_LONG, 
            new Object[]{maxLength}, Maybe.ofNullable(value), Maybe.none());
    }

    /**
     * Creates a validation error for a value that's too long with generated error ID.
     * 
     * @param fieldPath the field path
     * @param value the too-long value
     * @param maxLength the maximum allowed length
     * @return a ValidationError for too long value with error ID
     */
    public static ValidationError tooLongWithId(String fieldPath, Object value, int maxLength) {
        return new ValidationError(fieldPath, StandardErrorType.TOO_LONG, 
            new Object[]{maxLength}, Maybe.ofNullable(value), true);
    }

    /**
     * Creates a validation error for a value that's too short.
     * 
     * @param fieldPath the field path
     * @param value the too-short value
     * @param minLength the minimum required length
     * @return a ValidationError for too short value
     */
    public static ValidationError tooShort(String fieldPath, Object value, int minLength) {
        return new ValidationError(fieldPath, StandardErrorType.TOO_SHORT, 
            new Object[]{minLength}, Maybe.ofNullable(value), Maybe.none());
    }

    /**
     * Creates a validation error for an out-of-range value.
     * 
     * @param fieldPath the field path
     * @param value the out-of-range value
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return a ValidationError for out-of-range value
     */
    public static ValidationError outOfRange(String fieldPath, Object value, Object min, Object max) {
        return new ValidationError(fieldPath, StandardErrorType.OUT_OF_RANGE, 
            new Object[]{min, max}, Maybe.ofNullable(value), Maybe.none());
    }

    /**
     * Returns the error code from the error type.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorType.getCode();
    }

    /**
     * Returns the error type number.
     * 
     * @return the error type number
     */
    public int getErrorTypeNumber() {
        return errorType.getTypeNumber();
    }

    /**
     * Returns the error ID if present.
     * 
     * @return the error ID or Maybe.none() if not set
     */
    public Maybe<String> getErrorId() {
        return errorId;
    }

    /**
     * Creates a new ValidationError with a generated error ID.
     * 
     * @return a new ValidationError with error ID
     */
    public ValidationError withGeneratedId() {
        return new ValidationError(fieldPath, errorType, contextArgs, rejectedValue, 
                                 Maybe.some(errorType.generateErrorId()));
    }

    /**
     * Creates a new ValidationError with a specific error ID.
     * 
     * @param customErrorId the custom error ID
     * @return a new ValidationError with the specified error ID
     */
    public ValidationError withErrorId(String customErrorId) {
        return new ValidationError(fieldPath, errorType, contextArgs, rejectedValue, 
                                 Maybe.ofNullable(customErrorId));
    }

    /**
     * Returns the formatted error message with context arguments.
     * 
     * @return the formatted error message
     */
    public String getMessage() {
        return errorType.formatMessage(contextArgs);
    }

    /**
     * Returns the HTTP status code from the error type.
     * 
     * @return the HTTP status code
     */
    public int getHttpStatus() {
        return errorType.getHttpStatus();
    }

    /**
     * Converts this validation error to a ProblemDetail.
     * 
     * @return a ProblemDetail representing this error
     */
    public ProblemDetail toProblemDetail() {
        String detail = String.format("%s: %s", fieldPath, getMessage());
        return errorType.toProblemDetail(detail);
    }

    /**
     * Returns a formatted string representation suitable for logging or display.
     * 
     * @return formatted error description
     */
    public String format() {
        String baseFormat = errorId.fold(
            () -> String.format("[%s] %s: %s", getErrorCode(), fieldPath, getMessage()),
            id -> String.format("[%s|%s] %s: %s", getErrorCode(), id, fieldPath, getMessage())
        );
        
        return rejectedValue.fold(
            () -> baseFormat,
            value -> baseFormat + String.format(" (rejected value: %s)", value)
        );
    }
}