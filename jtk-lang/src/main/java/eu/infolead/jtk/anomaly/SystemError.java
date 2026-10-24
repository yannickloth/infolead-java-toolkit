package eu.infolead.jtk.anomaly;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardMultiStatusProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Base interface for all system errors with comprehensive ProblemDetail and HTTP status mapping.
 *
 * <p>This hierarchy provides a principled approach to error classification based on what the error
 * represents rather than who caused it:
 *
 * <ul>
 *   <li>{@link ParameterError} - Invalid arguments passed to methods
 *   <li>{@link PreconditionError} - System not in correct state for operation
 *   <li>{@link PostconditionError} - System failed to reach expected state
 *   <li>{@link BusinessRuleError} - Domain-specific constraint violations
 *   <li>{@link AuthorizationError} - Permission and access control violations
 *   <li>{@link ResourceError} - Resource availability or state issues
 *   <li>{@link InvariantError} - Object or system consistency violations
 * </ul>
 *
 * <p>Each error type maps to appropriate HTTP status codes and provides RFC 7807 compliant
 * ProblemDetail representations.
 *
 * <h2>Operational metadata</h2>
 *
 * <p>The concrete records stay focused on domain attributes. Cause chaining, correlation context,
 * retry hints and redaction are attached through the {@code with...} methods on this interface and
 * stored in a {@link ContextualSystemError} decorator.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * SystemError error = PostconditionError.persistenceFailed("Order", 42)
 *         .withCause(sqlException)
 *         .withSuppressedException(retryFailure)
 *         .withCorrelationId("corr-123")
 *         .withTraceId("trace-456")
 *         .withTenantId("tenant-a")
 *         .withRedaction(RedactionPolicy.standard());
 *
 * ProblemDetail problem = error.toProblemDetail(
 *         Maybe.none(),
 *         ErrorLocalizer.acceptLanguage(request.getHeader("Accept-Language")));
 *
 * RuntimeException exception = error.toException(); // cause + suppressed preserved
 * }</pre>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "kind",
        visible = false)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ParameterError.class, name = "parameter"),
    @JsonSubTypes.Type(value = PreconditionError.class, name = "precondition"),
    @JsonSubTypes.Type(value = PostconditionError.class, name = "postcondition"),
    @JsonSubTypes.Type(value = BusinessRuleError.class, name = "business-rule"),
    @JsonSubTypes.Type(value = AuthorizationError.class, name = "authorization"),
    @JsonSubTypes.Type(value = ResourceError.class, name = "resource"),
    @JsonSubTypes.Type(value = InvariantError.class, name = "invariant"),
    @JsonSubTypes.Type(value = ContextualSystemError.class, name = "contextual")
})
public sealed interface SystemError extends Serializable
        permits ParameterError,
                PreconditionError,
                PostconditionError,
                BusinessRuleError,
                AuthorizationError,
                ResourceError,
                InvariantError,
                ContextualSystemError {

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
     * Returns the error message localized for the requested language.
     *
     * <p>Implementations that know their domain prefix (a parameter name, a business rule name, and
     * so on) keep it untranslated and localize only the error-type message.
     *
     * <pre>{@code
     * ParameterError.nullParameter("userId").getLocalizedMessage(Locale.FRENCH);
     * // "userId: Paramètre nul"
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return the localized error message
     */
    default String getLocalizedMessage(final Locale locale) {
        return getMessage();
    }

    /**
     * Returns the unique error ID if assigned.
     *
     * @return the error ID or Maybe.none() if not set
     */
    Maybe<String> getErrorId();

    /**
     * Returns a defensive copy of the contextual information used for error message formatting.
     *
     * @return a copy of the context arguments
     */
    Object[] getContextArgs();

    /**
     * Returns the actual value that caused the error, if applicable.
     *
     * @return the actual value or Maybe.none() if not relevant
     */
    Maybe<Object> getActualValue();

    /**
     * Returns the underlying failure, when this error was caused by another exception.
     *
     * @return the cause, or {@link Maybe#none()}
     */
    @JsonIgnore
    default Maybe<Throwable> getCause() {
        return Maybe.none();
    }

    /**
     * Returns failures suppressed by this error.
     *
     * @return an immutable list of suppressed throwables, never {@code null}
     */
    @JsonIgnore
    default List<Throwable> getSuppressed() {
        return List.of();
    }

    /**
     * Returns the correlation and tenant metadata attached to this error.
     *
     * @return the error context, never {@code null}
     */
    @JsonIgnore
    default ErrorContext getErrorContext() {
        return ErrorContext.empty();
    }

    /**
     * Returns the redaction policy applied to values read from this error.
     *
     * @return the redaction policy, never {@code null}
     */
    @JsonIgnore
    default RedactionPolicy getRedactionPolicy() {
        return RedactionPolicy.NONE;
    }

    /**
     * Indicates whether the failed operation may succeed if retried.
     *
     * <p>Server errors and HTTP 429 responses are retryable; a concrete error can override this
     * with {@link #withRetryable(boolean)}.
     *
     * <pre>{@code
     * PreconditionError.rateLimitExceeded("100/min").isRetryable(); // true
     * ParameterError.nullParameter("id").isRetryable();              // false
     * }</pre>
     *
     * @return {@code true} when a retry is worth attempting
     */
    @JsonIgnore
    default boolean isRetryable() {
        return getErrorType().isRetryable();
    }

    /**
     * Returns the recommended delay before retrying.
     *
     * <pre>{@code
     * PreconditionError.rateLimitExceeded("100/min").retryAfter();
     * // Maybe.of(Duration.ofSeconds(1))
     * }</pre>
     *
     * @return the retry delay, or {@link Maybe#none()} when not applicable
     */
    @JsonIgnore
    default Maybe<Duration> retryAfter() {
        return getErrorType().retryAfter();
    }

    /**
     * Returns the retry delay in whole seconds, rounded up and never below one second. This
     * prevents an HTTP {@code Retry-After: 0} header from causing a tight retry loop for sub-second
     * delays.
     *
     * <pre>{@code
     * PostconditionError.sideEffectFailed("email").retryAfterSeconds();
     * // Maybe.of(5L) on HTTP 503
     * }</pre>
     *
     * @return the delay in seconds, or {@link Maybe#none()}
     */
    @JsonIgnore
    default Maybe<Long> retryAfterSeconds() {
        return retryAfter()
                .map(
                        delay -> {
                            long seconds = delay.getSeconds();
                            if (delay.getNano() > 0) {
                                seconds++;
                            }
                            return Math.max(1L, seconds);
                        });
    }

    /**
     * Returns the value for an HTTP {@code Retry-After} header.
     *
     * <pre>{@code
     * PreconditionError.rateLimitExceeded("100/min")
     *         .retryAfterHeader(); // Maybe.of("1")
     * }</pre>
     *
     * @return the delay in seconds, or {@link Maybe#none()}
     */
    @JsonIgnore
    default Maybe<String> retryAfterHeader() {
        return retryAfterSeconds().map(String::valueOf);
    }

    /**
     * Returns the message contributed by this error's type, without the domain-specific prefix
     * (parameter name, rule name, and so on).
     *
     * <pre>{@code
     * ParameterError.outOfRange("age", 42, 18, 65).getTypeMessage();
     * // "Value must be between 18 and 65"
     * }</pre>
     *
     * @return the error-type message
     */
    @JsonIgnore
    default String getTypeMessage() {
        final Object[] args = getContextArgs();
        return args.length == 0
                ? getErrorType().messageWithoutArgs()
                : getErrorType().formatMessage(args);
    }

    /**
     * Returns the localized message contributed by this error's type, without the domain-specific
     * prefix (parameter name, rule name, and so on).
     *
     * <pre>{@code
     * ParameterError.outOfRange("age", 42, 18, 65)
     *         .getLocalizedTypeMessage(Locale.GERMAN);
     * // "Der Wert muss zwischen 18 und 65 liegen"
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return the localized error-type message
     */
    default String getLocalizedTypeMessage(final Locale locale) {
        final Object[] args = getContextArgs();
        return args.length == 0
                ? getErrorType().localizedMessageWithoutArgs(locale)
                : getErrorType().formatLocalizedMessage(locale, args);
    }

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
     * @return a ProblemDetail representing this error; its {@code detail} includes the error ID
     *     when one is assigned
     */
    default ProblemDetail toProblemDetail(final Maybe<URI> instance) {
        return toProblemDetail(instance, ErrorLocalizer.english());
    }

    /**
     * Converts this error to a ProblemDetail localized for the requested language.
     *
     * <pre>{@code
     * ProblemDetail problem = ParameterError.nullParameter("userId")
     *         .toLocalizedProblemDetail(Locale.FRENCH);
     * assertEquals("Paramètre nul", problem.getTitle());
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return a localized ProblemDetail
     */
    default ProblemDetail toLocalizedProblemDetail(final Locale locale) {
        return toProblemDetail(Maybe.none(), ErrorLocalizer.of(locale));
    }

    /**
     * Converts this error to a ProblemDetail with optional instance URI and a locale resolver.
     *
     * <p>Correlation metadata, tenant, retryability and retry delay are exposed as RFC 7807
     * extension members on {@link StandardProblemDetail} and {@link
     * StandardMultiStatusProblemDetail}. A custom {@link ProblemDetail} implementation returned by
     * an overridden {@link ErrorType#toProblemDetail(Maybe, Maybe, ErrorLocalizer)} is returned
     * unchanged, so such an implementation is responsible for carrying the extensions itself.
     *
     * <pre>{@code
     * ProblemDetail problem = error.toProblemDetail(
     *         Maybe.some(URI.create("/orders/42")),
     *         ErrorLocalizer.acceptLanguage(request.getHeader("Accept-Language")));
     *
     * assertEquals("corr-123", problem.getCorrelationId().orNull());
     * }</pre>
     *
     * @param instance optional instance URI for this specific error occurrence
     * @param localizer the resolver for title and message translation
     * @return a ProblemDetail representing this error
     */
    default ProblemDetail toProblemDetail(
            final Maybe<URI> instance, final ErrorLocalizer localizer) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(localizer, "localizer");
        final String localizedMessage = getLocalizedMessage(localizer.locale());
        final Maybe<String> detail =
                getErrorId()
                        .fold(
                                () -> Maybe.some(localizedMessage),
                                id -> Maybe.some(localizer.detailWithId(localizedMessage, id)));
        final ProblemDetail problem = getErrorType().toProblemDetail(detail, instance, localizer);
        final ErrorContext context = getErrorContext();
        if (problem instanceof StandardProblemDetail standard) {
            return standard.withExtensions(
                    context.correlationId(),
                    context.traceId(),
                    context.requestId(),
                    context.tenantId(),
                    Maybe.of(isRetryable()),
                    retryAfterSeconds());
        }
        if (problem instanceof StandardMultiStatusProblemDetail multiStatus) {
            return multiStatus.withExtensions(
                    context.correlationId(),
                    context.traceId(),
                    context.requestId(),
                    context.tenantId(),
                    Maybe.of(isRetryable()),
                    retryAfterSeconds());
        }
        return problem;
    }

    /**
     * Converts this error to a RuntimeException for exception-based handling.
     *
     * <p>The exception type is chosen from the error category. A cause attached with {@link
     * #withCause(Throwable)} becomes the exception cause, and failures registered with {@link
     * #withSuppressed(Throwable...)} become Java suppressed exceptions, so stack traces remain
     * complete.
     *
     * @return a RuntimeException representing this error
     */
    default RuntimeException toException() {
        final Throwable cause = getCause().orNull();
        final String message = format();
        final RuntimeException exception =
                switch (getCategory()) {
                    case VALIDATION -> new IllegalArgumentException(message, cause);
                    case CONTRACT -> new IllegalStateException(message, cause);
                    case SECURITY -> new SecurityException(message, cause);
                    case BUSINESS_LOGIC, RESOURCE -> new IllegalStateException(message, cause);
                    case SYSTEM, EXTERNAL_SERVICE, RATE_LIMIT, CONFIGURATION ->
                            new RuntimeException(message, cause);
                };
        for (final Throwable suppressed : getSuppressed()) {
            if (suppressed != null && suppressed != exception && suppressed != cause) {
                exception.addSuppressed(suppressed);
            }
        }
        return exception;
    }

    /**
     * Attaches the underlying failure to this error.
     *
     * <pre>{@code
     * SystemError error = PostconditionError.persistenceFailed("Order", 42)
     *         .withCause(new SQLException("connection lost"));
     *
     * assert error.toException().getCause() instanceof SQLException;
     * }</pre>
     *
     * @param cause the cause; {@code null} clears the cause
     * @return a contextual error carrying the cause
     */
    default SystemError withCause(final Throwable cause) {
        return ContextualSystemError.of(this).withCause(cause);
    }

    /**
     * Replaces the suppressed failures attached to this error.
     *
     * @param suppressed the suppressed failures
     * @return a contextual error carrying the suppressed failures
     */
    default SystemError withSuppressed(final Throwable... suppressed) {
        return ContextualSystemError.of(this).withSuppressed(suppressed);
    }

    /**
     * Adds one suppressed failure to this error.
     *
     * @param suppressed the suppressed failure
     * @return a contextual error carrying the suppressed failure
     */
    default SystemError withSuppressedException(final Throwable suppressed) {
        return ContextualSystemError.of(this).withSuppressedException(suppressed);
    }

    /**
     * Attaches correlation and tenant metadata.
     *
     * @param context the context
     * @return a contextual error carrying the metadata
     */
    default SystemError withErrorContext(final ErrorContext context) {
        return ContextualSystemError.of(this).withErrorContext(context);
    }

    /**
     * Attaches a cross-system correlation identifier.
     *
     * @param correlationId the correlation identifier; {@code null} clears it
     * @return a contextual error carrying the identifier
     */
    default SystemError withCorrelationId(final String correlationId) {
        return withErrorContext(getErrorContext().withCorrelationId(correlationId));
    }

    /**
     * Attaches a distributed-tracing identifier.
     *
     * @param traceId the trace identifier; {@code null} clears it
     * @return a contextual error carrying the identifier
     */
    default SystemError withTraceId(final String traceId) {
        return withErrorContext(getErrorContext().withTraceId(traceId));
    }

    /**
     * Attaches a request identifier.
     *
     * @param requestId the request identifier; {@code null} clears it
     * @return a contextual error carrying the identifier
     */
    default SystemError withRequestId(final String requestId) {
        return withErrorContext(getErrorContext().withRequestId(requestId));
    }

    /**
     * Attaches a tenant identifier.
     *
     * @param tenantId the tenant identifier; {@code null} clears it
     * @return a contextual error carrying the identifier
     */
    default SystemError withTenantId(final String tenantId) {
        return withErrorContext(getErrorContext().withTenantId(tenantId));
    }

    /**
     * Attaches a redaction policy.
     *
     * <pre>{@code
     * SystemError safe = ParameterError.invalidFormat("payload", credentials)
     *         .withRedaction(RedactionPolicy.standard());
     * }</pre>
     *
     * @param policy the policy
     * @return a contextual error applying the policy
     */
    default SystemError withRedaction(final RedactionPolicy policy) {
        return ContextualSystemError.of(this).withRedaction(policy);
    }

    /**
     * Overrides the retryability of this error.
     *
     * @param retryable whether a retry may succeed
     * @return a contextual error carrying the override
     */
    default SystemError withRetryable(final boolean retryable) {
        return ContextualSystemError.of(this).withRetryable(retryable);
    }

    /**
     * Overrides the recommended retry delay.
     *
     * @param delay the delay; {@code null} clears it
     * @return a contextual error carrying the delay
     */
    default SystemError withRetryAfter(final Duration delay) {
        return ContextualSystemError.of(this).withRetryAfter(delay);
    }

    /**
     * Renders a value for logging without trusting its {@code toString()} implementation.
     *
     * <p>Redaction is unconditional: {@link Sensitive} wrappers and credential-shaped map keys are
     * always masked, regardless of any {@link RedactionPolicy} attached to the error, and even when
     * a policy would leave the value untouched. Output is bounded to 256 characters (259 including
     * the ellipsis), arrays and collections to 16 elements and nesting to three levels, so a large
     * payload cannot exhaust memory or leak wholesale into a log line.
     *
     * <pre>{@code
     * SystemError.renderValue(new Object[] { "alice", Sensitive.of("hunter2") });
     * // "[alice, [REDACTED]]"
     * }</pre>
     *
     * @param value the value to render
     * @return a bounded, null-safe, redacted representation
     */
    static String renderValue(final Object value) {
        return renderValue(value, 0);
    }

    private static String renderValue(final Object value, final int depth) {
        if (value == null) {
            return "null";
        }
        try {
            if (value instanceof Sensitive) {
                return Sensitive.REDACTED;
            }
            if (value.getClass().isArray()) {
                if (depth > 2) {
                    return "...";
                }
                final int length = Array.getLength(value);
                final StringBuilder rendered = new StringBuilder("[");
                for (int index = 0; index < length && index < 16; index++) {
                    if (rendered.length() > 256) {
                        rendered.append(", ...");
                        break;
                    }
                    if (index > 0) {
                        rendered.append(", ");
                    }
                    rendered.append(renderValue(Array.get(value, index), depth + 1));
                }
                if (length > 16 && !rendered.toString().endsWith(", ...")) {
                    rendered.append(", ...");
                }
                return bounded(rendered.append(']').toString());
            }
            if (value instanceof Map<?, ?> map) {
                return renderMap(map, depth);
            }
            if (value instanceof Collection<?> collection) {
                return renderCollection(collection, depth);
            }
            return bounded(String.valueOf(value));
        } catch (final RuntimeException | StackOverflowError throwable) {
            return "<unrenderable " + value.getClass().getName() + ">";
        }
    }

    private static String renderMap(final Map<?, ?> map, final int depth) {
        if (depth > 2) {
            return "...";
        }
        final StringBuilder rendered = new StringBuilder("{");
        int index = 0;
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            if (index >= 16 || rendered.length() > 256) {
                rendered.append(", ...");
                break;
            }
            if (index > 0) {
                rendered.append(", ");
            }
            final Object key = entry.getKey();
            rendered.append(renderValue(key, depth + 1)).append('=');
            if (key != null && RedactionPolicy.isSensitiveKey(String.valueOf(key))) {
                rendered.append(Sensitive.REDACTED);
            } else {
                rendered.append(renderValue(entry.getValue(), depth + 1));
            }
            index++;
        }
        return bounded(rendered.append('}').toString());
    }

    private static String renderCollection(final Collection<?> collection, final int depth) {
        if (depth > 2) {
            return "...";
        }
        final StringBuilder rendered = new StringBuilder("[");
        int index = 0;
        for (final Object element : collection) {
            if (index >= 16 || rendered.length() > 256) {
                rendered.append(", ...");
                break;
            }
            if (index > 0) {
                rendered.append(", ");
            }
            rendered.append(renderValue(element, depth + 1));
            index++;
        }
        return bounded(rendered.append(']').toString());
    }

    private static String bounded(final String rendered) {
        return rendered.length() <= 256 ? rendered : rendered.substring(0, 256) + "...";
    }

    /**
     * Returns a formatted string representation suitable for logging or display.
     *
     * @return formatted error description
     */
    default String format() {
        final String baseFormat =
                getErrorId()
                        .fold(
                                () ->
                                        String.format(
                                                "[%s] %s", getErrorType().getCode(), getMessage()),
                                id ->
                                        String.format(
                                                "[%s|%s] %s",
                                                getErrorType().getCode(), id, getMessage()));

        return getActualValue()
                .fold(
                        () -> baseFormat,
                        value ->
                                baseFormat
                                        + String.format(" (actual value: %s)", renderValue(value)));
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
     * @throws NullPointerException if {@code errorId} is {@code null}
     */
    SystemError withErrorId(String errorId);
}
