package eu.infolead.jtk.anomaly;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.net.URI;

/**
 * Error types for parameter validation failures. All parameter errors map to HTTP 400 Bad Request
 * status.
 *
 * <pre>{@code
 * ParameterError error = ParameterError.withActualValue(
 *         "age", ParameterErrorType.OUT_OF_RANGE, 42);
 * ProblemDetail problem = error.toProblemDetail(Maybe.some(URI.create("/users/1")));
 * }</pre>
 *
 * @see ParameterError
 * @see ErrorType
 */
public enum ParameterErrorType implements ErrorType {

    /** A required parameter is {@code null}. */
    NULL_PARAMETER(
            1001,
            "NULL_PARAMETER",
            "Parameter cannot be null",
            400,
            "Bad Request - Null Parameter",
            ErrorCategory.VALIDATION),

    /** A parameter value is outside its permitted range. */
    OUT_OF_RANGE(
            1002,
            "OUT_OF_RANGE",
            "Parameter value must be between %s and %s",
            400,
            "Bad Request - Parameter Out Of Range",
            ErrorCategory.VALIDATION),

    /** A parameter does not match its required format. */
    INVALID_FORMAT(
            1003,
            "INVALID_FORMAT",
            "Parameter has invalid format",
            400,
            "Bad Request - Invalid Parameter Format",
            ErrorCategory.VALIDATION),

    /** A parameter exceeds its maximum length. */
    TOO_LONG(
            1004,
            "TOO_LONG",
            "Parameter exceeds maximum length of %d",
            400,
            "Bad Request - Parameter Too Long",
            ErrorCategory.VALIDATION),

    /** A parameter is shorter than its minimum length. */
    TOO_SHORT(
            1005,
            "TOO_SHORT",
            "Parameter is shorter than minimum length of %d",
            400,
            "Bad Request - Parameter Too Short",
            ErrorCategory.VALIDATION),

    /** A required parameter is empty. */
    EMPTY(
            1006,
            "EMPTY",
            "Parameter cannot be empty",
            400,
            "Bad Request - Empty Parameter",
            ErrorCategory.VALIDATION),

    /** A parameter value is not supported. */
    UNSUPPORTED_VALUE(
            1007,
            "UNSUPPORTED_VALUE",
            "Parameter value is not supported",
            400,
            "Bad Request - Unsupported Parameter Value",
            ErrorCategory.VALIDATION),

    /** A combination of parameters is invalid. */
    INVALID_COMBINATION(
            1008,
            "INVALID_COMBINATION",
            "Invalid combination of parameters: %s",
            400,
            "Bad Request - Invalid Parameter Combination",
            ErrorCategory.VALIDATION);

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    ParameterErrorType(
            int typeNumber,
            String code,
            String messageTemplate,
            int httpStatus,
            String problemTitle,
            ErrorCategory category) {
        this.typeNumber = typeNumber;
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
        this.problemType = ErrorType.problemType(category, "parameter", code);
        this.problemTitle = problemTitle;
        this.category = category;
    }

    @Override
    public int getTypeNumber() {
        return typeNumber;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessageTemplate() {
        return messageTemplate;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public URI getProblemType() {
        return problemType;
    }

    @Override
    public String getProblemTitle() {
        return problemTitle;
    }

    @Override
    public ErrorCategory getCategory() {
        return category;
    }

    /**
     * Reads the enum constant from its stable message key, which is also the JSON representation
     * written by {@code ErrorType#getMessageKey()}.
     *
     * @param code the stable message key
     * @return the matching constant
     * @throws IllegalArgumentException if the code is unknown
     */
    @JsonCreator
    static ParameterErrorType fromJson(final String code) {
        for (final ParameterErrorType value : values()) {
            if (value.getMessageKey().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown ParameterErrorType code: " + code);
    }
}
