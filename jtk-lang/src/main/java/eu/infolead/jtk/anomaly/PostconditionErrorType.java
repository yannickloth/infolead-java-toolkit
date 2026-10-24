package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;

import java.net.URI;

/**
 * Error types for postcondition failures - system failed to achieve expected state.
 * These typically map to HTTP 5xx status codes (server errors).
 *
 * <pre>{@code
 * PostconditionError error = PostconditionError.persistenceFailed("Order", 42);
 * boolean retryable = error.isRetryable();          // true: transient failure
 * Maybe<Long> delay = error.retryAfterSeconds();    // e.g. Maybe.of(5L) on 503
 * }</pre>
 *
 * @see PostconditionError
 * @see ErrorType
 */
public enum PostconditionErrorType implements ErrorType {

    /** The expected state was not reached. */
    EXPECTED_STATE_NOT_REACHED(
        3001,
        "EXPECTED_STATE_NOT_REACHED",
        "Expected state '%s' was not reached",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "Internal Server Error - Expected State Not Reached",
        ErrorCategory.CONTRACT
    ),

    /** An invariant was violated. */
    INVARIANT_VIOLATED(
        3002,
        "INVARIANT_VIOLATED",
        "System invariant violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "Internal Server Error - Invariant Violation",
        ErrorCategory.CONTRACT
    ),

    /** A required side effect failed. */
    SIDE_EFFECT_FAILED(
        3003,
        "SIDE_EFFECT_FAILED",
        "Required side effect failed: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "Internal Server Error - Side Effect Failed",
        ErrorCategory.SYSTEM
    ),

    /** Data persistence failed. */
    PERSISTENCE_FAILED(
        3004,
        "PERSISTENCE_FAILED",
        "Failed to persist %s with ID %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "Internal Server Error - Persistence Failed",
        ErrorCategory.SYSTEM
    ),

    /** A notification could not be delivered. */
    NOTIFICATION_FAILED(
        3005,
        "NOTIFICATION_FAILED",
        "Failed to send %s notification to %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "Internal Server Error - Notification Failed",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    /** A data consistency rule was violated. */
    CONSISTENCY_VIOLATED(
        3006,
        "CONSISTENCY_VIOLATED",
        "Data consistency rule violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "Internal Server Error - Data Consistency Violated",
        ErrorCategory.CONTRACT
    ),

    /** An external integration failed. */
    INTEGRATION_FAILED(
        3007,
        "INTEGRATION_FAILED",
        "Integration with %s failed for operation %s",
            HttpStatus.BAD_GATEWAY.code(),  // Bad Gateway
        "Bad Gateway - External Integration Failed",
        ErrorCategory.EXTERNAL_SERVICE
    ),

    /** A transaction was rolled back. */
    TRANSACTION_ROLLBACK(
        3008,
        "TRANSACTION_ROLLBACK",
        "Transaction was rolled back: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "Internal Server Error - Transaction Rollback",
        ErrorCategory.SYSTEM
    ),

    /** An asynchronous operation failed. */
    ASYNC_OPERATION_FAILED(
        3009,
        "ASYNC_OPERATION_FAILED",
        "Asynchronous operation failed: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
        "Internal Server Error - Async Operation Failed",
        ErrorCategory.SYSTEM
    ),

    /** Resource cleanup failed. */
    RESOURCE_CLEANUP_FAILED(
        3010,
        "RESOURCE_CLEANUP_FAILED",
        "Failed to clean up resources: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),  // Internal Server Error
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
                          String problemTitle, ErrorCategory category) {
        this.typeNumber = typeNumber;
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
        this.problemType = ErrorType.problemType(category, "postcondition", code);
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
     * Returns the severity of this postcondition failure.
     *
     * <p>Contract failures indicate a programming error and are
     * {@link ErrorSeverity#CRITICAL}. Infrastructure failures (persistence,
     * side effects, notification, integration, async work, cleanup) are
     * {@link ErrorSeverity#ERROR}: the operation failed, but a retry can
     * succeed, which is what {@link ErrorType#isRetryable()} reports.</p>
     *
     * <pre>{@code
     * PostconditionErrorType.PERSISTENCE_FAILED.getSeverity(); // ERROR
     * PostconditionErrorType.INVARIANT_VIOLATED.getSeverity(); // CRITICAL
     * }</pre>
     *
     * @return the severity of this error type
     */
    @Override
    public ErrorSeverity getSeverity() {
        return category == ErrorCategory.CONTRACT
                ? ErrorSeverity.CRITICAL
                : ErrorSeverity.ERROR;
    }
}