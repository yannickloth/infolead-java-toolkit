package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;

import java.net.URI;

/**
 * Error types for precondition failures - system not in correct state for operation.
 * These typically map to HTTP 4xx status codes (client errors).
 *
 * <pre>{@code
 * PreconditionError error = PreconditionError.rateLimitExceeded("100/min");
 * boolean retryable = error.isRetryable();       // true: HTTP 429
 * Maybe<String> header = error.retryAfterHeader(); // Maybe.of("1")
 * }</pre>
 *
 * @see PreconditionError
 * @see ErrorType
 */
public enum PreconditionErrorType implements ErrorType {

    /** The system is not ready for the operation. */
    SYSTEM_NOT_READY(
        2001,
        "SYSTEM_NOT_READY",
        "System is not ready: %s",
            HttpStatus.SERVICE_UNAVAILABLE.code(),  // Service Unavailable
        "Service Unavailable - System Not Ready",
        ErrorCategory.SYSTEM
    ),

    /** The operation conflicts with the current state. */
    CONFLICTING_STATE(
        2002,
        "CONFLICTING_STATE",
        "Operation conflicts with current state: %s",
            HttpStatus.CONFLICT.code(),  // Conflict
        "Conflict - Conflicting State",
        ErrorCategory.BUSINESS_LOGIC
    ),

    /** The caller lacks a required permission. */
    INSUFFICIENT_PERMISSIONS(
        2003,
        "INSUFFICIENT_PERMISSIONS",
        "Insufficient permissions: %s required",
            HttpStatus.FORBIDDEN.code(),  // Forbidden
        "Forbidden - Insufficient Permissions",
        ErrorCategory.SECURITY
    ),

    /** The resource is currently locked. */
    RESOURCE_LOCKED(
        2004,
        "RESOURCE_LOCKED",
        "%s %s is currently locked",
            HttpStatus.LOCKED.code(),  // Locked
        "Locked - Resource Unavailable",
        ErrorCategory.RESOURCE
    ),

    /** The operation is not allowed in the current state. */
    OPERATION_NOT_ALLOWED(
        2005,
        "OPERATION_NOT_ALLOWED",
        "Operation '%s' not allowed in state '%s'",
            HttpStatus.METHOD_NOT_ALLOWED.code(),  // Method Not Allowed
        "Method Not Allowed - Operation Not Permitted",
        ErrorCategory.BUSINESS_LOGIC
    ),

    /** A rate limit was exceeded. */
    RATE_LIMIT_EXCEEDED(
        2006,
        "RATE_LIMIT_EXCEEDED",
        "Rate limit exceeded: %s",
            HttpStatus.TOO_MANY_REQUESTS.code(),  // Too Many Requests
        "Too Many Requests - Rate Limit Exceeded",
        ErrorCategory.RATE_LIMIT
    ),

    /** The system is in maintenance mode. */
    MAINTENANCE_MODE(
        2007,
        "MAINTENANCE_MODE",
        "System in maintenance mode (estimated duration: %s)",
            HttpStatus.SERVICE_UNAVAILABLE.code(),  // Service Unavailable
        "Service Unavailable - Maintenance Mode",
        ErrorCategory.SYSTEM
    ),

    /** A required dependency is unavailable. */
    DEPENDENCY_UNAVAILABLE(
        2008,
        "DEPENDENCY_UNAVAILABLE",
        "Required dependency unavailable: %s",
            HttpStatus.BAD_GATEWAY.code(),  // Bad Gateway
        "Bad Gateway - Dependency Unavailable",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    /** A precondition failed. */
    PRECONDITION_FAILED(
        2009,
        "PRECONDITION_FAILED",
        "Precondition failed: %s",
            HttpStatus.PRECONDITION_FAILED.code(),  // Precondition Failed
        "Precondition Failed",
        ErrorCategory.BUSINESS_LOGIC
    );

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    PreconditionErrorType(int typeNumber, String code, String messageTemplate, int httpStatus,
                         String problemTitle, ErrorCategory category) {
        this.typeNumber = typeNumber;
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
        this.problemType = ErrorType.problemType(category, "precondition", code);
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