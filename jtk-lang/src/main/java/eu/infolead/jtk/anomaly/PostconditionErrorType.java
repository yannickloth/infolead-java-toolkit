package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;

import java.net.URI;

/**
 * Error types for postcondition failures - system failed to achieve expected state.
 * These typically map to HTTP 5xx status codes (server errors).
 */
public enum PostconditionErrorType implements ErrorType {

    EXPECTED_STATE_NOT_REACHED(
        3001,
        "EXPECTED_STATE_NOT_REACHED",
        "Expected state '%s' was not reached",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Expected State Not Reached",
        ErrorCategory.SYSTEM
    ),

    INVARIANT_VIOLATED(
        3002,
        "INVARIANT_VIOLATED",
        "System invariant violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Invariant Violation",
        ErrorCategory.CONTRACT
    ),

    SIDE_EFFECT_FAILED(
        3003,
        "SIDE_EFFECT_FAILED",
        "Required side effect failed: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Side Effect Failed",
        ErrorCategory.SYSTEM
    ),

    PERSISTENCE_FAILED(
        3004,
        "PERSISTENCE_FAILED",
        "Failed to persist %s with ID %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Persistence Failed",
        ErrorCategory.SYSTEM
    ),

    NOTIFICATION_FAILED(
        3005,
        "NOTIFICATION_FAILED",
        "Failed to send %s notification to %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Notification Failed",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    CONSISTENCY_VIOLATED(
        3006,
        "CONSISTENCY_VIOLATED",
        "Data consistency rule violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Data Consistency Violated",
        ErrorCategory.SYSTEM
    ),

    INTEGRATION_FAILED(
        3007,
        "INTEGRATION_FAILED",
        "Integration with %s failed for operation %s",
            HttpStatus.BAD_GATEWAY.code(),  // Bad Gateway
        "https://tools.ietf.org/html/rfc7231#section-6.6.3",
        "Bad Gateway - External Integration Failed",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    TRANSACTION_ROLLBACK(
        3008,
        "TRANSACTION_ROLLBACK",
        "Transaction was rolled back: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Transaction Rollback",
        ErrorCategory.SYSTEM
    ),

    ASYNC_OPERATION_FAILED(
        3009,
        "ASYNC_OPERATION_FAILED",
        "Asynchronous operation failed: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Async Operation Failed",
        ErrorCategory.SYSTEM
    ),

    RESOURCE_CLEANUP_FAILED(
        3010,
        "RESOURCE_CLEANUP_FAILED",
        "Failed to clean up resources: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "https://tools.ietf.org/html/rfc7231#section-6.6.1",
        "Internal Server Error - Resource Cleanup Failed",
        ErrorCategory.SYSTEM
    );

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    PostconditionErrorType(int typeNumber, String code, String messageTemplate, int httpStatus,
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
        // Postcondition failures are typically critical as they indicate system malfunction
        return ErrorSeverity.CRITICAL;
    }
}