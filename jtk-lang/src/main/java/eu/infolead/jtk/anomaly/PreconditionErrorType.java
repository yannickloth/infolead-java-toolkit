package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;

import java.net.URI;

/**
 * Error types for precondition failures - system not in correct state for operation.
 * These typically map to HTTP 4xx status codes (client errors).
 */
public enum PreconditionErrorType implements ErrorType {

    SYSTEM_NOT_READY(
        2001,
        "SYSTEM_NOT_READY",
        "System is not ready: %s",
            HttpStatus.SERVICE_UNAVAILABLE.code(),  // Service Unavailable
        "https://tools.ietf.org/html/rfc7231#section-6.6.4",
        "Service Unavailable - System Not Ready",
        ErrorCategory.SYSTEM
    ),

    CONFLICTING_STATE(
        2002,
        "CONFLICTING_STATE",
        "Operation conflicts with current state: %s",
            HttpStatus.CONFLICT.code(),  // Conflict
        "https://tools.ietf.org/html/rfc7231#section-6.5.8",
        "Conflict - Conflicting State",
        ErrorCategory.BUSINESS_LOGIC
    ),

    INSUFFICIENT_PERMISSIONS(
        2003,
        "INSUFFICIENT_PERMISSIONS",
        "Insufficient permissions: %s required",
            HttpStatus.FORBIDDEN.code(),  // Forbidden
        "https://tools.ietf.org/html/rfc7231#section-6.5.3",
        "Forbidden - Insufficient Permissions",
        ErrorCategory.SECURITY
    ),

    RESOURCE_LOCKED(
        2004,
        "RESOURCE_LOCKED",
        "%s %s is currently locked",
            HttpStatus.LOCKED.code(),  // Locked
        "https://tools.ietf.org/html/rfc4918#section-11.3",
        "Locked - Resource Unavailable",
        ErrorCategory.RESOURCE
    ),

    OPERATION_NOT_ALLOWED(
        2005,
        "OPERATION_NOT_ALLOWED",
        "Operation '%s' not allowed in state '%s'",
            HttpStatus.METHOD_NOT_ALLOWED.code(),  // Method Not Allowed
        "https://tools.ietf.org/html/rfc7231#section-6.5.5",
        "Method Not Allowed - Operation Not Permitted",
        ErrorCategory.BUSINESS_LOGIC
    ),

    RATE_LIMIT_EXCEEDED(
        2006,
        "RATE_LIMIT_EXCEEDED",
        "Rate limit exceeded: %s",
            HttpStatus.TOO_MANY_REQUESTS.code(),  // Too Many Requests
        "https://tools.ietf.org/html/rfc6585#section-4",
        "Too Many Requests - Rate Limit Exceeded",
        ErrorCategory.RATE_LIMIT
    ),

    MAINTENANCE_MODE(
        2007,
        "MAINTENANCE_MODE",
        "System in maintenance mode (estimated duration: %s)",
            HttpStatus.SERVICE_UNAVAILABLE.code(),  // Service Unavailable
        "https://tools.ietf.org/html/rfc7231#section-6.6.4",
        "Service Unavailable - Maintenance Mode",
        ErrorCategory.SYSTEM
    ),

    DEPENDENCY_UNAVAILABLE(
        2008,
        "DEPENDENCY_UNAVAILABLE",
        "Required dependency unavailable: %s",
            HttpStatus.BAD_GATEWAY.code(),  // Bad Gateway
        "https://tools.ietf.org/html/rfc7231#section-6.6.3",
        "Bad Gateway - Dependency Unavailable",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    PRECONDITION_FAILED(
        2009,
        "PRECONDITION_FAILED",
        "Precondition failed: %s",
            HttpStatus.PRECONDITION_FAILED.code(),  // Precondition Failed
        "https://tools.ietf.org/html/rfc7232#section-4.2",
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