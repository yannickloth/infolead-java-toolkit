package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;

import java.net.URI;

/**
 * Standard error types covering common validation and application errors.
 * These can be used directly or serve as examples for custom error type enums.
 *
 * <pre>{@code
 * ProblemDetail problem = StandardErrorType.TOO_MANY_REQUESTS.toProblemDetail(
 *         Maybe.some("rate limit exceeded"),
 *         Maybe.none(),
 *         ErrorLocalizer.acceptLanguage("fr-FR"));
 * assertEquals(429, problem.getStatusValue());
 * }</pre>
 *
 * @see ErrorType
 */
public enum StandardErrorType implements ErrorType {

    // Validation Errors (400-series)
    /** A required field is missing. */
    REQUIRED(
        1001,
        "REQUIRED",
        "Required field is missing or null",
            HttpStatus.BAD_REQUEST.code(),
        "Bad Request - Required Field Missing",
        ErrorCategory.VALIDATION
    ),

    /** A field has an invalid format. */
    INVALID_FORMAT(
        1002,
        "INVALID_FORMAT", 
        "Field has invalid format",
            HttpStatus.BAD_REQUEST.code(),
        "Bad Request - Invalid Format",
        ErrorCategory.VALIDATION
    ),

    /** A field exceeds its maximum length. */
    TOO_LONG(
        1003,
        "TOO_LONG",
        "Field exceeds maximum length of %d characters",
            HttpStatus.BAD_REQUEST.code(), 
        "Bad Request - Value Too Long",
        ErrorCategory.VALIDATION
    ),

    /** A field is shorter than its minimum length. */
    TOO_SHORT(
        1004,
        "TOO_SHORT",
        "Field is shorter than minimum length of %d characters",
            HttpStatus.BAD_REQUEST.code(),
        "Bad Request - Value Too Short", 
        ErrorCategory.VALIDATION
    ),

    /** A value is outside its permitted range. */
    OUT_OF_RANGE(
        1005,
        "OUT_OF_RANGE",
        "Value must be between %s and %s",
            HttpStatus.BAD_REQUEST.code(),
        "Bad Request - Value Out Of Range",
        ErrorCategory.VALIDATION
    ),

    /** An email address has an invalid format. */
    INVALID_EMAIL(
        1006,
        "INVALID_EMAIL",
        "Invalid email address format",
            HttpStatus.BAD_REQUEST.code(),
        "Bad Request - Invalid Email",
        ErrorCategory.VALIDATION
    ),

    CROSS_FIELD_VIOLATION(
        1007,
        "CROSS_FIELD_VIOLATION",
        "Fields violate a cross-field constraint: %s",
            HttpStatus.BAD_REQUEST.code(),
        "Bad Request - Cross-Field Violation",
        ErrorCategory.VALIDATION
    ),

    // Resource Errors
    /** A requested resource was not found. */
    NOT_FOUND(
        2001,
        "NOT_FOUND",
        "Requested resource was not found",
            HttpStatus.NOT_FOUND.code(),
        "Not Found",
        ErrorCategory.RESOURCE
    ),

    /** A resource already exists. */
    ALREADY_EXISTS(
        2002,
        "ALREADY_EXISTS",
        "Resource already exists",
            HttpStatus.CONFLICT.code(),
        "Conflict - Resource Already Exists",
        ErrorCategory.BUSINESS_LOGIC
    ),

    // Security Errors
    /** Authentication is required. */
    UNAUTHORIZED(
        3001,
        "UNAUTHORIZED",
        "Authentication required",
            HttpStatus.UNAUTHORIZED.code(),
        "Unauthorized",
        ErrorCategory.SECURITY
    ),

    /** Access is denied. */
    FORBIDDEN(
        3002,
        "FORBIDDEN",
        "Access denied",
        403,
        "Forbidden",
        ErrorCategory.SECURITY
    ),

    // Rate Limiting
    /** A rate limit was exceeded. */
    TOO_MANY_REQUESTS(
        4001,
        "TOO_MANY_REQUESTS",
        "Rate limit exceeded",
            HttpStatus.TOO_MANY_REQUESTS.code(),
        "Too Many Requests",
        ErrorCategory.RATE_LIMIT
    ),

    // Server Errors (500-series)
    /** An internal server error occurred. */
    INTERNAL_ERROR(
        5001,
        "INTERNAL_ERROR",
        "An internal server error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
        "Internal Server Error",
        ErrorCategory.SYSTEM
    ),

    /** A service is temporarily unavailable. */
    SERVICE_UNAVAILABLE(
        5002,
        "SERVICE_UNAVAILABLE",
        "Service is temporarily unavailable",
            HttpStatus.SERVICE_UNAVAILABLE.code(),
        "Service Unavailable",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    /** A downstream request timed out. */
    TIMEOUT(
        5003,
        "TIMEOUT",
        "Request timed out",
            HttpStatus.GATEWAY_TIMEOUT.code(),
        "Gateway Timeout",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    // Business Logic Errors
    /** A domain business rule was violated. */
    BUSINESS_RULE_VIOLATION(
        6001,
        "BUSINESS_RULE_VIOLATION",
        "Business rule violation: %s",
            HttpStatus.UNPROCESSABLE_CONTENT.code(), 
        "Unprocessable Content - Business Rule Violation",
        ErrorCategory.BUSINESS_LOGIC
    ),

    /** An account has insufficient funds. */
    INSUFFICIENT_FUNDS(
        6002,
        "INSUFFICIENT_FUNDS",
        "Insufficient funds for this operation",
            HttpStatus.UNPROCESSABLE_CONTENT.code(),
        "Unprocessable Content - Insufficient Funds",
        ErrorCategory.BUSINESS_LOGIC
    ),

    // Contract Violations (Programming Errors - typically 500 series)
    /** A method precondition was violated. */
    PRECONDITION_VIOLATION(
        9001,
        "PRECONDITION_VIOLATION",
        "Method precondition violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
        "Internal Server Error - Precondition Violation",
        ErrorCategory.CONTRACT
    ),

    /** A method postcondition was violated. */
    POSTCONDITION_VIOLATION(
        9002,
        "POSTCONDITION_VIOLATION", 
        "Method postcondition violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
        "Internal Server Error - Postcondition Violation",
        ErrorCategory.CONTRACT
    ),

    /** An object invariant was violated. */
    INVARIANT_VIOLATION(
        9003,
        "INVARIANT_VIOLATION",
        "Object invariant violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
        "Internal Server Error - Invariant Violation",
        ErrorCategory.CONTRACT
    ),

    /** A null value was supplied where forbidden. */
    NULL_POINTER_VIOLATION(
        9004,
        "NULL_POINTER_VIOLATION",
        "Null value not allowed: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(), 
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
                     String problemTitle, ErrorCategory category) {
        this.typeNumber = typeNumber;
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
        this.problemType = ErrorType.problemType(category, "standard", code);
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
            case REQUIRED, INVALID_FORMAT, TOO_LONG, TOO_SHORT,
                 OUT_OF_RANGE, INVALID_EMAIL, CROSS_FIELD_VIOLATION, NOT_FOUND, ALREADY_EXISTS,
                 UNAUTHORIZED, FORBIDDEN, TOO_MANY_REQUESTS, BUSINESS_RULE_VIOLATION,
                 INSUFFICIENT_FUNDS -> ErrorSeverity.WARNING;
        };
    }
}