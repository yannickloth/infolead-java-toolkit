package eu.infolead.jtk.anomaly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.either.Maybe;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Decorator that adds operational metadata to a {@link SystemError} without changing the concrete
 * error record.
 *
 * <p>This keeps the seven error records focused on their domain attributes while cause chaining,
 * correlation context, retry hints and redaction live in one place. Instances are created through
 * the {@code with...} methods declared by {@link SystemError}; callers normally never construct
 * this record directly.
 *
 * <pre>{@code
 * SystemError error = ResourceError.notFound("User", 7)
 *         .withCause(new IOException("missing"))
 *         .withCorrelationId("corr-123");
 *
 * // error is a ContextualSystemError wrapping the ResourceError; the
 * // original domain attributes remain available through getErrorType().
 * }</pre>
 *
 * @param delegate the wrapped error
 * @param cause the underlying failure (optional)
 * @param suppressed additional failures suppressed by this one
 * @param context correlation and tenant metadata
 * @param redaction the redaction policy applied to values
 * @param retryable an explicit retryability override (optional)
 * @param retryAfter an explicit retry delay (optional)
 * @param retryAfterOverridden whether {@code retryAfter} was explicitly set, including an explicit
 *     clear with {@code null}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContextualSystemError(
        SystemError delegate,
        @JsonIgnore Maybe<Throwable> cause,
        @JsonIgnore List<Throwable> suppressed,
        @JsonIgnore ErrorContext context,
        @JsonIgnore RedactionPolicy redaction,
        @JsonIgnore Maybe<Boolean> retryable,
        @JsonIgnore Maybe<Duration> retryAfter,
        @JsonIgnore boolean retryAfterOverridden)
        implements SystemError {
    private static final long serialVersionUID = 1L;

    /**
     * Canonical constructor with validation.
     *
     * @param delegate the wrapped error
     * @param cause the underlying failure
     * @param suppressed additional suppressed failures
     * @param context correlation and tenant metadata
     * @param redaction the redaction policy
     * @param retryable an explicit retryability override
     * @param retryAfter an explicit retry delay
     * @param retryAfterOverridden whether the retry delay was explicitly set
     */
    /**
     * Restores a contextual error from its JSON representation.
     *
     * <p>Only the delegate is part of the wire contract. Cause chaining, correlation context,
     * redaction and retry overrides are operational metadata; they are deliberately not serialized
     * because they contain throwables, tenant identifiers or strategy objects that must not cross a
     * trust boundary. They are restored empty.
     *
     * @param delegate the wrapped error
     * @return the restored contextual error
     */
    @JsonCreator
    public static ContextualSystemError fromJson(
            @JsonProperty("delegate") final SystemError delegate) {
        return new ContextualSystemError(
                delegate,
                Maybe.none(),
                List.of(),
                ErrorContext.empty(),
                RedactionPolicy.NONE,
                Maybe.none(),
                Maybe.none(),
                false);
    }

    public ContextualSystemError {
        Objects.requireNonNull(delegate, "delegate cannot be null");
        Objects.requireNonNull(cause, "cause cannot be null");
        Objects.requireNonNull(suppressed, "suppressed cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(redaction, "redaction cannot be null");
        Objects.requireNonNull(retryAfter, "retryAfter cannot be null");
        Objects.requireNonNull(retryable, "retryable cannot be null");
        suppressed = List.copyOf(suppressed);
    }

    /**
     * Wraps an error with empty metadata, or returns it unchanged when it is already contextual.
     *
     * @param error the error to wrap
     * @return a contextual error
     */
    static SystemError of(final SystemError error) {
        Objects.requireNonNull(error, "error cannot be null");
        if (error instanceof ContextualSystemError) {
            return error;
        }
        return new ContextualSystemError(
                error,
                Maybe.none(),
                List.of(),
                ErrorContext.empty(),
                RedactionPolicy.NONE,
                Maybe.none(),
                Maybe.none(),
                false);
    }

    @Override
    public ErrorType getErrorType() {
        return delegate.getErrorType();
    }

    @Override
    public String getMessage() {
        return redaction.redactMessage(delegate.getMessage(), delegate.getContextArgs());
    }

    @Override
    public String getLocalizedMessage(final Locale locale) {
        return redaction.redactMessage(
                delegate.getLocalizedMessage(locale), delegate.getContextArgs());
    }

    @Override
    public Maybe<String> getErrorId() {
        return delegate.getErrorId();
    }

    @Override
    public Object[] getContextArgs() {
        return redaction.redactAll(delegate.getContextArgs());
    }

    @Override
    public Maybe<Object> getActualValue() {
        return delegate.getActualValue().map(redaction::redact);
    }

    @Override
    public Maybe<Throwable> getCause() {
        return cause;
    }

    @Override
    public List<Throwable> getSuppressed() {
        return suppressed;
    }

    @Override
    public ErrorContext getErrorContext() {
        return context;
    }

    @Override
    public RedactionPolicy getRedactionPolicy() {
        return redaction;
    }

    @Override
    public boolean isRetryable() {
        return retryable.fold(delegate::isRetryable, Boolean::booleanValue);
    }

    @Override
    public Maybe<Duration> retryAfter() {
        return retryAfterOverridden ? retryAfter : delegate.retryAfter();
    }

    @Override
    public SystemError withGeneratedId() {
        return copyWith(
                delegate.withGeneratedId(),
                cause,
                suppressed,
                context,
                redaction,
                retryable,
                retryAfter,
                retryAfterOverridden);
    }

    @Override
    public SystemError withErrorId(final String errorId) {
        return copyWith(
                delegate.withErrorId(errorId),
                cause,
                suppressed,
                context,
                redaction,
                retryable,
                retryAfter,
                retryAfterOverridden);
    }

    @Override
    public SystemError withCause(final Throwable newCause) {
        return new ContextualSystemError(
                delegate,
                Maybe.ofNullable(newCause),
                suppressed,
                context,
                redaction,
                retryable,
                retryAfter,
                retryAfterOverridden);
    }

    @Override
    public SystemError withSuppressed(final Throwable... suppressedErrors) {
        Objects.requireNonNull(suppressedErrors, "suppressedErrors cannot be null");
        return new ContextualSystemError(
                delegate,
                cause,
                List.of(suppressedErrors),
                context,
                redaction,
                retryable,
                retryAfter,
                retryAfterOverridden);
    }

    @Override
    public SystemError withSuppressedException(final Throwable suppressedError) {
        Objects.requireNonNull(suppressedError, "suppressedError cannot be null");
        final List<Throwable> combined = new ArrayList<>(suppressed.size() + 1);
        combined.addAll(suppressed);
        combined.add(suppressedError);
        return new ContextualSystemError(
                delegate,
                cause,
                combined,
                context,
                redaction,
                retryable,
                retryAfter,
                retryAfterOverridden);
    }

    @Override
    public SystemError withErrorContext(final ErrorContext newContext) {
        return new ContextualSystemError(
                delegate,
                cause,
                suppressed,
                Objects.requireNonNull(newContext, "newContext cannot be null"),
                redaction,
                retryable,
                retryAfter,
                retryAfterOverridden);
    }

    @Override
    public SystemError withRedaction(final RedactionPolicy policy) {
        return new ContextualSystemError(
                delegate,
                cause,
                suppressed,
                context,
                Objects.requireNonNull(policy, "policy cannot be null"),
                retryable,
                retryAfter,
                retryAfterOverridden);
    }

    @Override
    public SystemError withRetryable(final boolean retry) {
        return new ContextualSystemError(
                delegate,
                cause,
                suppressed,
                context,
                redaction,
                Maybe.of(retry),
                retryAfter,
                retryAfterOverridden);
    }

    @Override
    public SystemError withRetryAfter(final Duration delay) {
        return new ContextualSystemError(
                delegate,
                cause,
                suppressed,
                context,
                redaction,
                retryable,
                Maybe.ofNullable(delay),
                true);
    }

    private static SystemError copyWith(
            final SystemError newDelegate,
            final Maybe<Throwable> cause,
            final List<Throwable> suppressed,
            final ErrorContext context,
            final RedactionPolicy redaction,
            final Maybe<Boolean> retryable,
            final Maybe<Duration> retryAfter,
            final boolean retryAfterOverridden) {
        return new ContextualSystemError(
                newDelegate,
                cause,
                suppressed,
                context,
                redaction,
                retryable,
                retryAfter,
                retryAfterOverridden);
    }

    @Override
    public String toString() {
        return "ContextualSystemError[message="
                + getMessage()
                + ", cause="
                + cause.fold(() -> "none", throwable -> throwable.getClass().getName())
                + ", suppressed="
                + suppressed.size()
                + ", context="
                + context
                + ", retryable="
                + isRetryable()
                + ", retryAfter="
                + retryAfter().fold(() -> "none", Duration::toString)
                + "]";
    }

    /**
     * Returns a debug count of suppressed throwables without rendering the throwables themselves,
     * which could contain sensitive data.
     *
     * @return the suppression count
     */
    public int suppressedCount() {
        return suppressed.size();
    }
}
