package eu.infolead.jtk.test.error;

import eu.infolead.jtk.anomaly.RedactionPolicy;
import eu.infolead.jtk.anomaly.SystemError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Test data builder for {@link SystemError}, starting from a canonical fixture (by default {@link
 * TestErrors#nullParameter()}) and applying only the metadata the individual test actually cares
 * about.
 *
 * <p>Use {@link TestErrors} directly when the canonical error is exactly what the test needs; use
 * this builder when the test must vary cause, context, retry metadata or redaction on top of a
 * known-good base.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * SystemError error = ErrorFixture.anError()
 *         .withTenantId("tenant-77")
 *         .withCause(sqlException)
 *         .withRetryable(true)
 *         .build();
 * }</pre>
 */
public final class ErrorFixture {

    private ErrorFixture() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns a builder based on the canonical parameter error fixture.
     *
     * @return a new builder over {@link TestErrors#nullParameter()}
     */
    public static Builder anError() {
        return basedOn(TestErrors.nullParameter());
    }

    /**
     * Returns a builder based on the given error. The base error is not modified; {@link
     * Builder#build()} returns a new instance carrying the requested overrides.
     *
     * @param base the error to start from; MUST NOT be {@code null}
     * @return a new builder over the base error
     */
    public static Builder basedOn(final SystemError base) {
        return new Builder(Objects.requireNonNull(base, "base"));
    }

    /** The fluent builder for error variations. */
    public static final class Builder {

        private final SystemError base;
        private String errorId;
        private String correlationId;
        private String traceId;
        private String requestId;
        private String tenantId;
        private Throwable cause;
        private final List<Throwable> suppressed = new ArrayList<>();
        private Boolean retryable;
        private java.time.Duration retryAfter;
        private RedactionPolicy redactionPolicy;

        private Builder(final SystemError base) {
            this.base = base;
        }

        /**
         * Overrides the error instance ID.
         *
         * @param errorId the pinned ID to use
         * @return this builder
         */
        public Builder withErrorId(final String errorId) {
            this.errorId = Objects.requireNonNull(errorId, "errorId");
            return this;
        }

        /**
         * Overrides the correlation ID.
         *
         * @param correlationId the correlation ID to use
         * @return this builder
         */
        public Builder withCorrelationId(final String correlationId) {
            this.correlationId = Objects.requireNonNull(correlationId, "correlationId");
            return this;
        }

        /**
         * Overrides the trace ID.
         *
         * @param traceId the trace ID to use
         * @return this builder
         */
        public Builder withTraceId(final String traceId) {
            this.traceId = Objects.requireNonNull(traceId, "traceId");
            return this;
        }

        /**
         * Overrides the request ID.
         *
         * @param requestId the request ID to use
         * @return this builder
         */
        public Builder withRequestId(final String requestId) {
            this.requestId = Objects.requireNonNull(requestId, "requestId");
            return this;
        }

        /**
         * Overrides the tenant ID.
         *
         * @param tenantId the tenant ID to use
         * @return this builder
         */
        public Builder withTenantId(final String tenantId) {
            this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
            return this;
        }

        /**
         * Attaches a cause.
         *
         * @param cause the cause to attach
         * @return this builder
         */
        public Builder withCause(final Throwable cause) {
            this.cause = Objects.requireNonNull(cause, "cause");
            return this;
        }

        /**
         * Attaches suppressed errors.
         *
         * @param suppressed the suppressed errors to attach
         * @return this builder
         */
        public Builder withSuppressed(final Throwable... suppressed) {
            this.suppressed.addAll(Arrays.asList(suppressed));
            return this;
        }

        /**
         * Overrides retryability.
         *
         * @param retryable whether a retry may succeed
         * @return this builder
         */
        public Builder withRetryable(final boolean retryable) {
            this.retryable = retryable;
            return this;
        }

        /**
         * Overrides the recommended retry delay.
         *
         * @param retryAfter the delay to recommend
         * @return this builder
         */
        public Builder withRetryAfter(final java.time.Duration retryAfter) {
            this.retryAfter = Objects.requireNonNull(retryAfter, "retryAfter");
            return this;
        }

        /**
         * Overrides the redaction policy.
         *
         * @param redactionPolicy the policy to attach
         * @return this builder
         */
        public Builder withRedaction(final RedactionPolicy redactionPolicy) {
            this.redactionPolicy = Objects.requireNonNull(redactionPolicy, "redactionPolicy");
            return this;
        }

        /**
         * Applies the recorded overrides to a copy of the base error.
         *
         * @return the built error
         */
        public SystemError build() {
            SystemError error = base;
            if (errorId != null) {
                error = error.withErrorId(errorId);
            }
            if (correlationId != null) {
                error = error.withCorrelationId(correlationId);
            }
            if (traceId != null) {
                error = error.withTraceId(traceId);
            }
            if (requestId != null) {
                error = error.withRequestId(requestId);
            }
            if (tenantId != null) {
                error = error.withTenantId(tenantId);
            }
            if (cause != null) {
                error = error.withCause(cause);
            }
            if (!suppressed.isEmpty()) {
                error = error.withSuppressed(suppressed.toArray(new Throwable[0]));
            }
            if (retryable != null) {
                error = error.withRetryable(retryable);
            }
            if (retryAfter != null) {
                error = error.withRetryAfter(retryAfter);
            }
            if (redactionPolicy != null) {
                error = error.withRedaction(redactionPolicy);
            }
            return error;
        }
    }
}
