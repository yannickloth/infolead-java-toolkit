package eu.infolead.jtk.anomaly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.http.HttpStatus;
import eu.infolead.jtk.http.HttpStatusCode;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

/**
 * Interface for error types that can be mapped to HTTP responses and ProblemDetail. Implementing
 * enums should provide all necessary metadata for error handling.
 *
 * <p>This interface allows different projects to define their own error types while maintaining
 * consistency in error handling and HTTP response generation.
 *
 * <h2>Localization and retry metadata</h2>
 *
 * <pre>{@code
 * ErrorType type = StandardErrorType.TOO_MANY_REQUESTS;
 * type.getMessageKey();        // "error.rate-limit.standard.too-many-requests"
 * type.isRetryable();          // true
 * type.retryAfter();           // Maybe.of(Duration.ofSeconds(1))
 *
 * ProblemDetail french = type.toProblemDetail(
 *         Maybe.none(),
 *         Maybe.none(),
 *         ErrorLocalizer.acceptLanguage("fr-FR,fr;q=0.9"));
 * }</pre>
 */
public interface ErrorType {

    /** Base URL of all RFC 7807 problem type identifiers. */
    String PROBLEM_BASE = "https://errors.infolead.eu/problems/";

    /**
     * Builds an RFC 7807 problem type URI for an error type. The namespace should be the enum that
     * declares the type so that equal codes in different enums do not collide.
     *
     * @param category the error category
     * @param namespace the declaring type
     * @param code the stable error code
     * @return a URI uniquely identifying the error type
     */
    static URI problemType(
            final ErrorCategory category, final String namespace, final String code) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(code, "code");
        return URI.create(
                PROBLEM_BASE
                        + category.name().toLowerCase(Locale.ROOT).replace('_', '-')
                        + "/"
                        + namespace.toLowerCase(Locale.ROOT).replace('_', '-')
                        + "/"
                        + code.toLowerCase(Locale.ROOT).replace('_', '-'));
    }

    /**
     * Returns the unique error code for this error type. Should be stable across versions and
     * suitable for programmatic handling.
     *
     * @return the error code (e.g., "VALIDATION_FAILED", "RESOURCE_NOT_FOUND")
     */
    String getCode();

    /**
     * Returns the numeric type identifier for this error type. Used to construct error IDs and
     * should be unique within the error type enum.
     *
     * @return the error type number (e.g., 1001, 2003, 5001)
     */
    int getTypeNumber();

    /**
     * Returns a human-readable error message template. May contain placeholders for dynamic values.
     *
     * @return the error message template
     */
    String getMessageTemplate();

    /**
     * Returns the HTTP status code associated with this error type.
     *
     * <p><em>TODO:</em> Use {@link HttpStatusCode} instead of an {@code int}.
     *
     * @return the HTTP status code (e.g., 400, 404, 500)
     */
    int getHttpStatus();

    /**
     * Returns the problem type URI for RFC 7807 compliance. Should uniquely identify this error
     * type.
     *
     * @return the problem type URI
     */
    URI getProblemType();

    /**
     * Returns the problem title for RFC 7807 compliance. Should be a short, human-readable summary.
     *
     * @return the problem title
     */
    String getProblemTitle();

    /**
     * Returns the stable localization key for this error type.
     *
     * <p>The key is derived from the problem type URI so that equal codes in different namespaces
     * never collide. For example {@code .../problems/validation/parameter/null-parameter} becomes
     * {@code error.validation.parameter.null-parameter}. Translators add {@code <key>.title} and
     * {@code <key>.message} entries to the error resource bundle; missing entries fall back to
     * {@link #getProblemTitle()} and {@link #getMessageTemplate()}.
     *
     * <pre>{@code
     * ParameterErrorType.NULL_PARAMETER.getMessageKey();
     * // "error.validation.parameter.null-parameter"
     * }</pre>
     *
     * @return the stable message key
     */
    /**
     * Resolves an error type from its stable message key.
     *
     * <p>This is the inverse of {@link #getMessageKey()} and the reason every error JSON document
     * is self-describing: the wire form of an error type is its stable key, not the enum constant
     * name. All known implementations are searched; an unknown key is rejected instead of being
     * silently mapped to a default, so a contract drift cannot turn into a wrongly classified
     * error.
     *
     * @param code the stable message key
     * @return the matching error type
     * @throws IllegalArgumentException if no implementation declares the key
     */
    @JsonCreator
    static ErrorType fromJson(final String code) {
        return java.util.stream.Stream.of(
                        StandardErrorType.values(),
                        ParameterErrorType.values(),
                        PreconditionErrorType.values(),
                        PostconditionErrorType.values(),
                        BusinessRuleErrorType.values(),
                        AuthorizationErrorType.values(),
                        ResourceErrorType.values(),
                        InvariantErrorType.values())
                .flatMap(java.util.Arrays::stream)
                .filter(type -> type.getMessageKey().equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Unknown error type code: " + code));
    }

    @JsonValue
    default String getMessageKey() {
        final URI problemType = getProblemType();
        final String path = problemType == null ? null : problemType.getPath();
        final String prefix = "/problems/";
        if (path != null && path.startsWith(prefix) && path.length() > prefix.length()) {
            return "error." + path.substring(prefix.length()).replace('/', '.');
        }
        return "error."
                + getCategory().name().toLowerCase(Locale.ROOT).replace('_', '-')
                + "."
                + getCode().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Returns the localized problem title.
     *
     * <pre>{@code
     * StandardErrorType.REQUIRED.getLocalizedProblemTitle(Locale.FRENCH);
     * // "Champ obligatoire manquant"
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return the localized title, or the built-in English title
     */
    default String getLocalizedProblemTitle(final Locale locale) {
        return ErrorLocalizer.of(locale).title(this);
    }

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
     *
     * <p>Server errors are retryable, as is HTTP {@code 429 Too Many Requests}: the latter is a
     * client error by status class, but the same request can succeed after the indicated delay.
     * Deterministic programming failures ({@link ErrorCategory#CONTRACT}) and critical integrity
     * failures are not retryable, because repeating the request cannot make them succeed.
     *
     * <pre>{@code
     * StandardErrorType.TOO_MANY_REQUESTS.isRetryable(); // true
     * InvariantErrorType.DATA_CORRUPTION.isRetryable();  // false
     * }</pre>
     *
     * @return true if the operation should be retried
     */
    default boolean isRetryable() {
        return (isServerError()
                        && getCategory() != ErrorCategory.CONTRACT
                        && getSeverity() != ErrorSeverity.CRITICAL)
                || getHttpStatus() == HttpStatus.TOO_MANY_REQUESTS.code();
    }

    /**
     * Returns the recommended delay before retrying an operation that failed with this error type.
     *
     * <p>Rate-limited requests suggest one second and unavailable services suggest five seconds.
     * Other error types have no suggestion. A concrete error can override this value with {@link
     * SystemError#withRetryAfter(Duration)}.
     *
     * <pre>{@code
     * PreconditionErrorType.RATE_LIMIT_EXCEEDED.retryAfter();
     * // Maybe.of(Duration.ofSeconds(1))
     * }</pre>
     *
     * @return the suggested retry delay, or {@link Maybe#none()}
     */
    default Maybe<Duration> retryAfter() {
        if (getHttpStatus() == HttpStatus.TOO_MANY_REQUESTS.code()) {
            return Maybe.of(Duration.ofSeconds(1));
        }
        if (getHttpStatus() == HttpStatus.SERVICE_UNAVAILABLE.code()) {
            return Maybe.of(Duration.ofSeconds(5));
        }
        return Maybe.none();
    }

    /**
     * Returns the error category for grouping similar errors.
     *
     * @return the error category (e.g., "VALIDATION", "AUTHENTICATION", "BUSINESS_LOGIC")
     */
    ErrorCategory getCategory();

    /**
     * Returns additional metadata about this error type. Can be used for logging, monitoring, or
     * custom error handling.
     *
     * @return a Maybe containing additional metadata
     */
    default Maybe<String> getMetadata() {
        return Maybe.none();
    }

    /**
     * Formats the error message with the provided arguments.
     *
     * <p>Arguments are passed through {@link RedactionPolicy#standard()}: explicit {@link
     * Sensitive} wrappers and credential-shaped map entries are replaced with {@link
     * Sensitive#REDACTED} before formatting, so a message can never echo a secret. When no
     * arguments are supplied, the raw template is returned.
     *
     * <p>If the redacted arguments cannot be formatted (for example a sensitive number passed to a
     * {@code %d} specifier), the message falls back to {@link #messageWithoutArgs()} rather than
     * returning the raw template: placeholders must not leak into logs or responses.
     *
     * <pre>{@code
     * StandardErrorType.TOO_LONG.formatMessage(70);
     * // "Field exceeds maximum length of 70 characters"
     *
     * ParameterErrorType.INVALID_FORMAT.formatMessage(Sensitive.of("hunter2"));
     * // "Field has invalid format [REDACTED]" (never the raw value)
     * }</pre>
     *
     * @param args arguments to substitute in the message template
     * @return the formatted error message; the raw template when no arguments are given; the
     *     argument-free message when formatting fails
     */
    default String formatMessage(final Object... args) {
        Objects.requireNonNull(args, "args");
        if (args.length == 0) {
            return getMessageTemplate();
        }
        final Object[] redacted = RedactionPolicy.standard().redactAll(args);
        boolean redactionChangedAnArgument = false;
        for (int index = 0; index < args.length; index++) {
            if (args[index] != redacted[index]) {
                redactionChangedAnArgument = true;
                break;
            }
        }
        try {
            return String.format(Locale.ENGLISH, getMessageTemplate(), redacted);
        } catch (final RuntimeException exception) {
            return redactionChangedAnArgument ? messageWithoutArgs() : getMessageTemplate();
        }
    }

    /**
     * Formats the localized message template with the provided arguments.
     *
     * <pre>{@code
     * ParameterErrorType.OUT_OF_RANGE.formatLocalizedMessage(
     *         Locale.FRENCH, 0, 150);
     * // "La valeur du paramètre doit être comprise entre 0 et 150"
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @param args arguments to substitute in the message template
     * @return the localized message, or the localized template when formatting fails
     */
    default String formatLocalizedMessage(final Locale locale, final Object... args) {
        return ErrorLocalizer.of(locale).formatMessage(this, args);
    }

    /**
     * Returns a human-readable message for the case where no context arguments are available.
     * Templates containing format specifiers fall back to the problem title so that raw
     * placeholders are not exposed.
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
     * Returns a localized message for the case where no context arguments are available.
     *
     * @param locale the requested locale; {@code null} means English
     * @return a localized message without unresolved format specifiers
     */
    default String localizedMessageWithoutArgs(final Locale locale) {
        return ErrorLocalizer.of(locale).messageWithoutArgs(this);
    }

    /**
     * Creates a StandardProblemDetail from this error type.
     *
     * @param detail optional detailed explanation
     * @param instance optional instance URI
     * @return a ProblemDetail representing this error
     */
    default ProblemDetail toProblemDetail(final Maybe<String> detail, final Maybe<URI> instance) {
        return toProblemDetail(detail, instance, ErrorLocalizer.english());
    }

    /**
     * Creates a localized StandardProblemDetail from this error type.
     *
     * <pre>{@code
     * ProblemDetail problem = StandardErrorType.REQUIRED.toProblemDetail(
     *         Maybe.some("name is required"),
     *         Maybe.some(URI.create("/requests/42")),
     *         ErrorLocalizer.acceptLanguage("fr"));
     * }</pre>
     *
     * @param detail optional detailed explanation
     * @param instance optional instance URI
     * @param localizer the resolver for title and message translation
     * @return a ProblemDetail representing this error
     */
    default ProblemDetail toProblemDetail(
            final Maybe<String> detail, final Maybe<URI> instance, final ErrorLocalizer localizer) {
        Objects.requireNonNull(detail, "detail");
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(localizer, "localizer");
        if (!HttpStatusCode.isValid(getHttpStatus()).toBoolean()) {
            throw new IllegalStateException(
                    "Invalid HTTP status " + getHttpStatus() + " for error type " + getCode());
        }
        return StandardProblemDetail.builder(getProblemType())
                .title(localizer.title(this))
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
     * Returns the severity level of this error type. Used to determine how the error should be
     * handled and logged.
     *
     * @return the error severity level
     */
    default ErrorSeverity getSeverity() {
        return isServerError() ? ErrorSeverity.ERROR : ErrorSeverity.WARNING;
    }

    /**
     * Generates a unique error ID combining the type number and instance identifier. Format:
     * {typeNumber}-{instanceId} (e.g., "1001-12345", "2003-67890")
     *
     * @param instanceId the unique instance identifier for this specific error occurrence
     * @return the formatted error ID
     */
    default String generateErrorId(long instanceId) {
        return getTypeNumber() + "-" + instanceId;
    }

    /**
     * Generates a unique error ID with a process token and a process-local, monotonically
     * increasing instance identifier. The process token keeps IDs distinct across restarts and
     * hosts.
     *
     * @return the formatted error ID
     */
    default String generateErrorId() {
        return getTypeNumber() + "-" + ErrorIdSequence.next();
    }

    /** Error categories for grouping similar error types. */
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

    /** Error severity levels indicating how errors should be handled. */
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
