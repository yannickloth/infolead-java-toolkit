package eu.infolead.jtk.anomaly;

import java.net.URI;

/**
 * Error types for parameter validation failures.
 * All parameter errors map to HTTP 400 Bad Request status.
 */
public enum ParameterErrorType implements ErrorType {

    NULL_PARAMETER(
        1001,
        "NULL_PARAMETER",
        "Parameter cannot be null",
        400,
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Null Parameter",
        ErrorCategory.VALIDATION
    ),

    OUT_OF_RANGE(
        1002,
        "OUT_OF_RANGE",
        "Parameter value must be between %s and %s",
        400,
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Parameter Out Of Range",
        ErrorCategory.VALIDATION
    ),

    INVALID_FORMAT(
        1003,
        "INVALID_FORMAT",
        "Parameter has invalid format",
        400,
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Invalid Parameter Format",
        ErrorCategory.VALIDATION
    ),

    TOO_LONG(
        1004,
        "TOO_LONG",
        "Parameter exceeds maximum length of %d",
        400,
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Parameter Too Long",
        ErrorCategory.VALIDATION
    ),

    TOO_SHORT(
        1005,
        "TOO_SHORT",
        "Parameter is shorter than minimum length of %d",
        400,
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Parameter Too Short",
        ErrorCategory.VALIDATION
    ),

    EMPTY(
        1006,
        "EMPTY",
        "Parameter cannot be empty",
        400,
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Empty Parameter",
        ErrorCategory.VALIDATION
    ),

    UNSUPPORTED_VALUE(
        1007,
        "UNSUPPORTED_VALUE",
        "Parameter value is not supported",
        400,
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Unsupported Parameter Value",
        ErrorCategory.VALIDATION
    ),

    INVALID_COMBINATION(
        1008,
        "INVALID_COMBINATION",
        "Invalid combination of parameters: %s",
        400,
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Invalid Parameter Combination",
        ErrorCategory.VALIDATION
    );

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    ParameterErrorType(int typeNumber, String code, String messageTemplate, int httpStatus,
                      String problemType, String problemTitle, ErrorCategory category) {
        this.typeNumber = typeNumber;
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
        this.problemType = URI.create(problemType);
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
}