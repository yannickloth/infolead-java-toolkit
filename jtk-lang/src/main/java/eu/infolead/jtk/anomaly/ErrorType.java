package eu.infolead.jtk.anomaly;

import java.net.URI;

import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;

/**
 * Interface for error types that can be mapped to HTTP responses and ProblemDetail.
 * Implementing enums should provide all necessary metadata for error handling.
 * 
 * <p>This interface allows different projects to define their own error types
 * while maintaining consistency in error handling and HTTP response generation.</p>
 */
public interface ErrorType {

    /**
     * Returns the unique error code for this error type.
     * Should be stable across versions and suitable for programmatic handling.
     * 
     * @return the error code (e.g., "VALIDATION_FAILED", "RESOURCE_NOT_FOUND")
     */
    String getCode();

    /**
     * Returns the numeric type identifier for this error type.
     * Used to construct error IDs and should be unique within the error type enum.
     * 
     * @return the error type number (e.g., 1001, 2003, 5001)
     */
    int getTypeNumber();

    /**
     * Returns a human-readable error message template.
     * May contain placeholders for dynamic values.
     * 
     * @return the error message template
     */
    String getMessageTemplate();

    /**
     * Returns the HTTP status code associated with this error type.
     * 
     * @return the HTTP status code (e.g., 400, 404, 500)
     * @TODO use the HttpStatusCode type instead of an int.
     */
    int getHttpStatus();

    /**
     * Returns the problem type URI for RFC 7807 compliance.
     * Should uniquely identify this error type.
     * 
     * @return the problem type URI
     */
    URI getProblemType();

    /**
     * Returns the problem title for RFC 7807 compliance.
     * Should be a short, human-readable summary.
     * 
     * @return the problem title
     */
    String getProblemTitle();

    /**
     * Indicates whether this is a client error (4xx) or server error (5xx).
     * 
     * @return true for client errors, false for server errors
     */
    default boolean isClientError() {
        return getHttpStatus() >= 400 && getHttpStatus() < 500;
    }

    /**
     * Indicates whether this is a server error (5xx).
     * 
     * @return true for server errors, false for client errors
     */
    default boolean isServerError() {
        return getHttpStatus() >= 500;
    }

    /**
     * Indicates whether this error type should be retried.
     * Typically true for server errors, false for client errors.
     * 
     * @return true if the operation should be retried
     */
    default boolean isRetryable() {
        return isServerError();
    }

    /**
     * Returns the error category for grouping similar errors.
     * 
     * @return the error category (e.g., "VALIDATION", "AUTHENTICATION", "BUSINESS_LOGIC")
     */
    ErrorCategory getCategory();

    /**
     * Returns additional metadata about this error type.
     * Can be used for logging, monitoring, or custom error handling.
     * 
     * @return a Maybe containing additional metadata
     */
    default Maybe<String> getMetadata() {
        return Maybe.none();
    }

    /**
     * Formats the error message with the provided arguments.
     * Default implementation returns the message template as-is.
     * 
     * @param args arguments to substitute in the message template
     * @return the formatted error message
     */
    default String formatMessage(Object... args) {
        if (args.length == 0) {
            return getMessageTemplate();
        }
        try {
            return String.format(getMessageTemplate(), args);
        } catch (Exception e) {
            return getMessageTemplate();
        }
    }

    /**
     * Creates a StandardProblemDetail from this error type.
     * 
     * @param detail optional detailed explanation
     * @param instance optional instance URI
     * @return a ProblemDetail representing this error
     */
    default ProblemDetail toProblemDetail(Maybe<String> detail, Maybe<URI> instance) {
        return StandardProblemDetail.builder(getProblemType())
            .title(getProblemTitle())
            .status(getHttpStatus())
            .detail(detail.orNull())
            .instance(instance.orNull())
            .build();
    }

    /**
     * Creates a StandardProblemDetail from this error type with detail message.
     * 
     * @param detail detailed explanation
     * @return a ProblemDetail representing this error
     */
    default ProblemDetail toProblemDetail(String detail) {
        return toProblemDetail(Maybe.ofNullable(detail), Maybe.none());
    }

    /**
     * Creates a StandardProblemDetail from this error type without additional details.
     * 
     * @return a ProblemDetail representing this error
     */
    default ProblemDetail toProblemDetail() {
        return toProblemDetail(Maybe.none(), Maybe.none());
    }

    /**
     * Returns the severity level of this error type.
     * Used to determine how the error should be handled and logged.
     * 
     * @return the error severity level
     */
    default ErrorSeverity getSeverity() {
        return isServerError() ? ErrorSeverity.ERROR : ErrorSeverity.WARNING;
    }

    /**
     * Generates a unique error ID combining the type number and instance identifier.
     * Format: {typeNumber}-{instanceId} (e.g., "1001-12345", "2003-67890")
     * 
     * @param instanceId the unique instance identifier for this specific error occurrence
     * @return the formatted error ID
     */
    default String generateErrorId(long instanceId) {
        return getTypeNumber() + "-" + instanceId;
    }

    /**
     * Generates a unique error ID with a timestamp-based instance identifier.
     * Uses current system time in milliseconds as the instance ID.
     * 
     * @return the formatted error ID with timestamp instance
     */
    default String generateErrorId() {
        return generateErrorId(System.currentTimeMillis());
    }

    /**
     * Error categories for grouping similar error types.
     */
    enum ErrorCategory {
        /** Input validation errors - expected user input issues */
        VALIDATION,
        
        /** Authentication and authorization errors */
        SECURITY,
        
        /** Business logic constraint violations */
        BUSINESS_LOGIC,
        
        /** Resource not found or access errors */
        RESOURCE,
        
        /** External service communication errors */
        EXTERNAL_SERVICE,
        
        /** Internal system errors */
        SYSTEM,
        
        /** Rate limiting and throttling errors */
        RATE_LIMIT,
        
        /** Configuration or setup errors */
        CONFIGURATION,
        
        /** Design-by-contract violations - programming errors */
        CONTRACT
    }

    /**
     * Error severity levels indicating how errors should be handled.
     */
    enum ErrorSeverity {
        /** Informational - for logging/monitoring only */
        INFO,
        
        /** Warning - handled gracefully, user notified */
        WARNING,
        
        /** Error - serious issue, operation failed */
        ERROR,
        
        /** Critical - system integrity at risk, immediate action required */
        CRITICAL
    }
}