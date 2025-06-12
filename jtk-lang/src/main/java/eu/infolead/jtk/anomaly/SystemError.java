package eu.infolead.jtk.anomaly;

import java.net.URI;

import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;

/**
 * Base interface for all system errors with comprehensive ProblemDetail and HTTP status mapping.
 * 
 * <p>This hierarchy provides a principled approach to error classification based on
 * what the error represents rather than who caused it:</p>
 * 
 * <ul>
 *   <li>{@link ParameterError} - Invalid arguments passed to methods</li>
 *   <li>{@link PreconditionError} - System not in correct state for operation</li>
 *   <li>{@link PostconditionError} - System failed to reach expected state</li>
 *   <li>{@link BusinessRuleError} - Domain-specific constraint violations</li>
 *   <li>{@link AuthorizationError} - Permission and access control violations</li>
 *   <li>{@link ResourceError} - Resource availability or state issues</li>
 *   <li>{@link InvariantError} - Object or system consistency violations</li>
 * </ul>
 * 
 * <p>Each error type maps to appropriate HTTP status codes and provides
 * RFC 7807 compliant ProblemDetail representations.</p>
 */
public sealed interface SystemError permits 
    ParameterError, PreconditionError, PostconditionError,
    BusinessRuleError, AuthorizationError, ResourceError, InvariantError {

    /**
     * Returns the error type with metadata for HTTP mapping.
     * 
     * @return the error type
     */
    ErrorType getErrorType();

    /**
     * Returns the error message with any formatting applied.
     * 
     * @return the formatted error message
     */
    String getMessage();

    /**
     * Returns the unique error ID if assigned.
     * 
     * @return the error ID or Maybe.none() if not set
     */
    Maybe<String> getErrorId();

    /**
     * Returns contextual information for error message formatting.
     * 
     * @return array of context arguments
     */
    Object[] getContextArgs();

    /**
     * Returns the actual value that caused the error, if applicable.
     * 
     * @return the actual value or Maybe.none() if not relevant
     */
    Maybe<Object> getActualValue();

    /**
     * Returns the HTTP status code for this error.
     * 
     * @return the HTTP status code
     */
    default int getHttpStatus() {
        return getErrorType().getHttpStatus();
    }

    /**
     * Indicates whether this is a client error (4xx).
     * 
     * @return true for client errors
     */
    default boolean isClientError() {
        return getErrorType().isClientError();
    }

    /**
     * Indicates whether this is a server error (5xx).
     * 
     * @return true for server errors
     */
    default boolean isServerError() {
        return getErrorType().isServerError();
    }

    /**
     * Returns the error category for grouping similar errors.
     * 
     * @return the error category
     */
    default ErrorType.ErrorCategory getCategory() {
        return getErrorType().getCategory();
    }

    /**
     * Returns the error severity level.
     * 
     * @return the error severity
     */
    default ErrorType.ErrorSeverity getSeverity() {
        return getErrorType().getSeverity();
    }

    /**
     * Converts this error to a ProblemDetail for RFC 7807 compliance.
     * 
     * @return a ProblemDetail representing this error
     */
    default ProblemDetail toProblemDetail() {
        return toProblemDetail(Maybe.none());
    }

    /**
     * Converts this error to a ProblemDetail with optional instance URI.
     * 
     * @param instance optional instance URI for this specific error occurrence
     * @return a ProblemDetail representing this error
     */
    default ProblemDetail toProblemDetail(Maybe<URI> instance) {
        return getErrorType().toProblemDetail(Maybe.some(getMessage()), instance);
    }

    /**
     * Converts this error to a RuntimeException for exception-based handling.
     * 
     * @return a RuntimeException representing this error
     */
    default RuntimeException toException() {
        return switch (getCategory()) {
            case VALIDATION -> new IllegalArgumentException(format());
            case CONTRACT -> new IllegalStateException(format());
            case SECURITY -> new SecurityException(format());
            case BUSINESS_LOGIC -> new IllegalStateException(format());
            case RESOURCE -> new IllegalStateException(format());
            case SYSTEM -> new RuntimeException(format());
            default -> new RuntimeException(format());
        };
    }

    /**
     * Returns a formatted string representation suitable for logging or display.
     * 
     * @return formatted error description
     */
    default String format() {
        String baseFormat = getErrorId().fold(
            () -> String.format("[%s] %s", getErrorType().getCode(), getMessage()),
            id -> String.format("[%s|%s] %s", getErrorType().getCode(), id, getMessage())
        );
        
        return getActualValue().fold(
            () -> baseFormat,
            value -> baseFormat + String.format(" (actual value: %s)", value)
        );
    }

    /**
     * Creates a new error instance with a generated error ID.
     * 
     * @return a new error instance with error ID
     */
    SystemError withGeneratedId();

    /**
     * Creates a new error instance with a specific error ID.
     * 
     * @param errorId the custom error ID
     * @return a new error instance with the specified error ID
     */
    SystemError withErrorId(String errorId);
}