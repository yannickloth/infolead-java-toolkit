package eu.infolead.jtk.anomaly;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.http.HttpStatusCode;

/**
 * Interface for error types that can be mapped to HTTP responses and ProblemDetail.
 * Implementing enums should provide all necessary metadata for error handling.
 * 
 * <p>This interface allows different projects to define their own error types
 * while maintaining consistency in error handling and HTTP response generation.</p>
 */
public interface ErrorType {

    /** Base URL of all RFC 7807 problem type identifiers. */
    String PROBLEM_BASE = "https://errors.infolead.eu/problems/";

    /**
     * Builds an RFC 7807 problem type URI for an error type. The namespace
     * should be the enum that declares the type so that equal codes in
     * different enums do not collide.
     *
     * @param category  the error category
     * @param namespace the declaring type
     * @param code      the stable error code
     * @return a URI uniquely identifying the error type
     */
    static URI problemType(final ErrorCategory category, final String namespace, final String code) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(code, "code");
        return URI.create(PROBLEM_BASE
                + category.name().toLowerCase(Locale.ROOT).replace('_', '-')
                + "/" + namespace.toLowerCase(Locale.ROOT).replace('_', '-')
                + "/" + code.toLowerCase(Locale.ROOT).replace('_', '-'));
    }

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
     * <p>
     * When no arguments are supplied, the raw template is returned. If the
     * template cannot be formatted with the supplied arguments, the raw template
     * is returned instead: error reporting must not mask the original failure by
     * throwing from a logging or response-building path.
     *
     * @param args arguments to substitute in the message template
     * @return the formatted error message, or the raw template when formatting
     *         fails
     */
    default String formatMessage(final Object... args) {
        Objects.requireNonNull(args, "args");
        if (args.length == 0) {
            return getMessageTemplate();
        }
        try {
            return String.format(getMessageTemplate(), args);
        } catch (final RuntimeException exception) {
            return getMessageTemplate();
        }
    }

    /**
     * Returns a human-readable message for the case where no context arguments
     * are available. Templates containing format specifiers fall back to the
     * problem title so that raw placeholders are not exposed.
     *
     * @return a message without unresolved format specifiers
     */
    default String messageWithoutArgs() {
        final String template = getMessageTemplate();
        if (template == null) {
            return getProblemTitle();
        }
        return template.matches("(?s).*%[a-zA-Z%].*") ? getProblemTitle() : template;
    }

    /**
     * Creates a StandardProblemDetail from this error type.
     * 
     * @param detail optional detailed explanation
     * @param instance optional instance URI
     * @return a ProblemDetail representing this error
     */
    default ProblemDetail toProblemDetail(final Maybe<String> detail, final Maybe<URI> instance) {
        Objects.requireNonNull(detail, "detail");
        Objects.requireNonNull(instance, "instance");
        if (!HttpStatusCode.isValid(getHttpStatus()).toBoolean()) {
            throw new IllegalStateException("Invalid HTTP status " + getHttpStatus() + " for error type " + getCode());
        }
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
     * Generates a unique error ID with a process token and a process-local,
     * monotonically increasing instance identifier. The process token keeps IDs
     * distinct across restarts and hosts.
     *
     * @return the formatted error ID
     */
    default String generateErrorId() {
        return getTypeNumber() + "-" + ErrorIdSequence.next();
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