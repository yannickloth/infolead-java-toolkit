package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;

import java.net.URI;

/**
 * Standard error types covering common validation and application errors.
 * These can be used directly or serve as examples for custom error type enums.
 */
public enum StandardErrorType implements ErrorType {

    // Validation Errors (400-series)
    REQUIRED(
        1001,
        "REQUIRED",
        "Required field is missing or null",
            HttpStatus.BAD_REQUEST.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Required Field Missing",
        ErrorCategory.VALIDATION
    ),

    INVALID_FORMAT(
        1002,
        "INVALID_FORMAT", 
        "Field has invalid format",
            HttpStatus.BAD_REQUEST.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Invalid Format",
        ErrorCategory.VALIDATION
    ),

    TOO_LONG(
        1003,
        "TOO_LONG",
        "Field exceeds maximum length of %d characters",
            HttpStatus.BAD_REQUEST.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.5.1", 
        "Bad Request - Value Too Long",
        ErrorCategory.VALIDATION
    ),

    TOO_SHORT(
        1004,
        "TOO_SHORT",
        "Field is shorter than minimum length of %d characters",
            HttpStatus.BAD_REQUEST.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Value Too Short", 
        ErrorCategory.VALIDATION
    ),

    OUT_OF_RANGE(
        1005,
        "OUT_OF_RANGE",
        "Value must be between %s and %s",
            HttpStatus.BAD_REQUEST.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Value Out Of Range",
        ErrorCategory.VALIDATION
    ),

    INVALID_EMAIL(
        1006,
        "INVALID_EMAIL",
        "Invalid email address format",
            HttpStatus.BAD_REQUEST.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.5.1",
        "Bad Request - Invalid Email",
        ErrorCategory.VALIDATION
    ),

    // Resource Errors
    NOT_FOUND(
        2001,
        "NOT_FOUND",
        "Requested resource was not found",
            HttpStatus.NOT_FOUND.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.5.4",
        "Not Found",
        ErrorCategory.RESOURCE
    ),

    ALREADY_EXISTS(
        2002,
        "ALREADY_EXISTS",
        "Resource already exists",
            HttpStatus.CONFLICT.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.5.8",
        "Conflict - Resource Already Exists",
        ErrorCategory.BUSINESS_LOGIC
    ),

    // Security Errors
    UNAUTHORIZED(
        3001,
        "UNAUTHORIZED",
        "Authentication required",
            HttpStatus.UNAUTHORIZED.code(),
        "https://tools.ietf.org/html/rfc7235#section-3.1",
        "Unauthorized",
        ErrorCategory.SECURITY
    ),

    FORBIDDEN(
        3002,
        "FORBIDDEN",
        "Access denied",
        403,
        "https://tools.ietf.org/html/rfc7231#section-6.5.3",
        "Forbidden",
        ErrorCategory.SECURITY
    ),

    // Rate Limiting
    TOO_MANY_REQUESTS(
        4001,
        "TOO_MANY_REQUESTS",
        "Rate limit exceeded",
            HttpStatus.TOO_MANY_REQUESTS.code(),
        "https://tools.ietf.org/html/rfc6585#section-4",
        "Too Many Requests",
        ErrorCategory.RATE_LIMIT
    ),

    // Server Errors (500-series)
    INTERNAL_ERROR(
        5001,
        "INTERNAL_ERROR",
        "An internal server error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error",
        ErrorCategory.SYSTEM
    ),

    SERVICE_UNAVAILABLE(
        5002,
        "SERVICE_UNAVAILABLE",
        "Service is temporarily unavailable",
            HttpStatus.SERVICE_UNAVAILABLE.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.6.4",
        "Service Unavailable",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    TIMEOUT(
        5003,
        "TIMEOUT",
        "Request timed out",
            HttpStatus.GATEWAY_TIMEOUT.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.6.5",
        "Gateway Timeout",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    // Business Logic Errors
    BUSINESS_RULE_VIOLATION(
        6001,
        "BUSINESS_RULE_VIOLATION",
        "Business rule violation: %s",
            HttpStatus.UNPROCESSABLE_CONTENT.code(),
        "https://tools.ietf.org/html/rfc4918#section-11.2", 
        "Unprocessable Entity - Business Rule Violation",
        ErrorCategory.BUSINESS_LOGIC
    ),

    INSUFFICIENT_FUNDS(
        6002,
        "INSUFFICIENT_FUNDS",
        "Insufficient funds for this operation",
            HttpStatus.UNPROCESSABLE_CONTENT.code(),
        "https://tools.ietf.org/html/rfc4918#section-11.2",
        "Unprocessable Entity - Insufficient Funds",
        ErrorCategory.BUSINESS_LOGIC
    ),

    // Contract Violations (Programming Errors - typically 500 series)
    PRECONDITION_VIOLATION(
        9001,
        "PRECONDITION_VIOLATION",
        "Method precondition violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Precondition Violation",
        ErrorCategory.CONTRACT
    ),

    POSTCONDITION_VIOLATION(
        9002,
        "POSTCONDITION_VIOLATION", 
        "Method postcondition violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Postcondition Violation",
        ErrorCategory.CONTRACT
    ),

    INVARIANT_VIOLATION(
        9003,
        "INVARIANT_VIOLATION",
        "Object invariant violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Invariant Violation",
        ErrorCategory.CONTRACT
    ),

    NULL_POINTER_VIOLATION(
        9004,
        "NULL_POINTER_VIOLATION",
        "Null value not allowed: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
        "https://tools.ietf.org/html/rfc7231#section-6.6.1", 
        "Internal Server Error - Null Pointer Violation",
        ErrorCategory.CONTRACT
    );

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    StandardErrorType(int typeNumber, String code, String messageTemplate, int httpStatus, 
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

    @Override
    public ErrorSeverity getSeverity() {
        return switch (this) {
            case PRECONDITION_VIOLATION, POSTCONDITION_VIOLATION, 
                 INVARIANT_VIOLATION, NULL_POINTER_VIOLATION -> ErrorSeverity.CRITICAL;
            case INTERNAL_ERROR, SERVICE_UNAVAILABLE, TIMEOUT -> ErrorSeverity.ERROR;
            default -> ErrorSeverity.WARNING;
        };
    }
}