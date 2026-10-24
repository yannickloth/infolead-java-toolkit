package eu.infolead.jtk.anomaly.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.either.Maybe;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * A standard implementation of {@link ProblemDetail} as defined by RFC 7807. This record provides a
 * simple, immutable implementation of the Problem Details format.
 *
 * <p>The {@code Maybe} components are never serialized directly: nullable bean-style getters expose
 * the RFC 7807 members, and {@link #fromJson} supports deserialization.
 *
 * <p>The last six components are RFC 7807 extension members used to correlate an error occurrence
 * across systems and to tell a client whether a retry is worthwhile.
 *
 * <pre>{@code
 * StandardProblemDetail problem = StandardProblemDetail
 *         .builder(URI.create("about:blank"))
 *         .title("Not Found")
 *         .status(404)
 *         .detail("User 7 does not exist")
 *         .correlationId("corr-123")
 *         .traceId("trace-456")
 *         .tenantId("tenant-a")
 *         .retryable(false)
 *         .build();
 * }</pre>
 *
 * @param problemType the problem type URI (required)
 * @param title the problem type title (optional)
 * @param status the HTTP status code (optional)
 * @param detail the detailed explanation (optional)
 * @param instance the instance identifier (optional)
 * @param correlationId the cross-system correlation identifier (optional)
 * @param traceId the distributed-tracing identifier (optional)
 * @param requestId the request identifier (optional)
 * @param tenantId the tenant identifier (optional)
 * @param retryable whether the failed operation may succeed if retried (optional)
 * @param retryAfter the recommended retry delay in seconds (optional)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StandardProblemDetail(
        @JsonProperty("type") URI problemType,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> title,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<Integer> status,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> detail,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<URI> instance,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> correlationId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> traceId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> requestId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> tenantId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<Boolean> retryable,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<Long> retryAfter)
        implements ProblemDetail {

    /**
     * Canonical constructor with validation.
     *
     * <p>The component names deliberately match the RFC 7807 wire names so that Jackson can
     * associate the nullable {@link #fromJson} creator parameters with this record without exposing
     * the {@code Maybe} wrappers.
     *
     * @param problemType the problem type URI
     * @param title the problem type title
     * @param status the HTTP status code
     * @param detail the detailed explanation
     * @param instance the instance URI
     * @param correlationId the cross-system correlation identifier
     * @param traceId the distributed-tracing identifier
     * @param requestId the request identifier
     * @param tenantId the tenant identifier
     * @param retryable whether a retry may succeed
     * @param retryAfter the recommended retry delay in seconds
     */
    public StandardProblemDetail {
        Objects.requireNonNull(problemType, "problemType cannot be null");
        Objects.requireNonNull(title, "title cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        Objects.requireNonNull(detail, "detail cannot be null");
        Objects.requireNonNull(instance, "instance cannot be null");
        Objects.requireNonNull(correlationId, "correlationId cannot be null");
        Objects.requireNonNull(traceId, "traceId cannot be null");
        Objects.requireNonNull(requestId, "requestId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(retryable, "retryable cannot be null");
        Objects.requireNonNull(retryAfter, "retryAfter cannot be null");
    }

    /**
     * Backwards-compatible constructor without extension members.
     *
     * @param problemType the problem type URI
     * @param problemTypeTitle the optional title
     * @param httpStatus the optional HTTP status
     * @param fullDetail the optional detail
     * @param problemInstanceId the optional instance URI
     */
    public StandardProblemDetail(
            final URI problemType,
            final Maybe<String> problemTypeTitle,
            final Maybe<Integer> httpStatus,
            final Maybe<String> fullDetail,
            final Maybe<URI> problemInstanceId) {
        this(
                problemType,
                problemTypeTitle,
                httpStatus,
                fullDetail,
                problemInstanceId,
                Maybe.none(),
                Maybe.none(),
                Maybe.none(),
                Maybe.none(),
                Maybe.none(),
                Maybe.none());
    }

    /**
     * Creates an instance from the nullable JSON representation.
     *
     * @param problemType the problem type URI
     * @param title the optional title
     * @param status the optional HTTP status
     * @param detail the optional detail
     * @param instance the optional instance URI
     * @param correlationId the optional correlation identifier
     * @param traceId the optional trace identifier
     * @param requestId the optional request identifier
     * @param tenantId the optional tenant identifier
     * @param retryable the optional retryability hint
     * @param retryAfterSeconds the optional retry delay in seconds
     * @return a new problem detail
     */
    @JsonCreator
    public static StandardProblemDetail fromJson(
            @JsonProperty("type") final URI problemType,
            @JsonProperty("title") final String title,
            @JsonProperty("status") final Integer status,
            @JsonProperty("detail") final String detail,
            @JsonProperty("instance") final URI instance,
            @JsonProperty("correlationId") final String correlationId,
            @JsonProperty("traceId") final String traceId,
            @JsonProperty("requestId") final String requestId,
            @JsonProperty("tenantId") final String tenantId,
            @JsonProperty("retryable") final Boolean retryable,
            @JsonProperty("retryAfter") final Long retryAfterSeconds) {
        return new StandardProblemDetail(
                problemType == null ? URI.create("about:blank") : problemType,
                Maybe.ofNullable(title),
                Maybe.ofNullable(status),
                Maybe.ofNullable(detail),
                Maybe.ofNullable(instance),
                Maybe.ofNullable(correlationId),
                Maybe.ofNullable(traceId),
                Maybe.ofNullable(requestId),
                Maybe.ofNullable(tenantId),
                Maybe.ofNullable(retryable),
                Maybe.ofNullable(retryAfterSeconds));
    }

    /**
     * Creates an instance from the RFC 7807 core members only.
     *
     * @param problemType the problem type URI
     * @param title the optional title
     * @param status the optional HTTP status
     * @param detail the optional detail
     * @param instance the optional instance URI
     * @return a new problem detail
     */
    public static StandardProblemDetail fromJson(
            final URI problemType,
            final String title,
            final Integer status,
            final String detail,
            final URI instance) {
        return fromJson(
                problemType, title, status, detail, instance, null, null, null, null, null, null);
    }

    /**
     * Convenience constructor for minimal problem details.
     *
     * @param problemType the problem type URI
     */
    public StandardProblemDetail(final URI problemType) {
        this(problemType, Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with type and title.
     *
     * @param problemType the problem type URI
     * @param title the problem type title
     */
    public StandardProblemDetail(final URI problemType, final String title) {
        this(problemType, Maybe.ofNullable(title), Maybe.none(), Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with type, title, and status.
     *
     * @param problemType the problem type URI
     * @param title the problem type title
     * @param status the HTTP status code
     */
    public StandardProblemDetail(final URI problemType, final String title, final Integer status) {
        this(
                problemType,
                Maybe.ofNullable(title),
                Maybe.ofNullable(status),
                Maybe.none(),
                Maybe.none());
    }

    // Interface implementation methods

    @Override
    public URI getProblemType() {
        return problemType;
    }

    @Override
    @JsonIgnore
    public Maybe<String> getProblemTypeTitle() {
        return title;
    }

    @Override
    @JsonIgnore
    public Maybe<Integer> getHttpStatus() {
        return status;
    }

    @Override
    @JsonIgnore
    public Maybe<String> getFullDetail() {
        return detail;
    }

    @Override
    @JsonIgnore
    public Maybe<URI> getProblemInstanceId() {
        return instance;
    }

    /**
     * Legacy accessor retained for source compatibility with the original record component name.
     *
     * @return the title
     * @deprecated use {@link #getProblemTypeTitle()}
     */
    @Deprecated
    @JsonIgnore
    public Maybe<String> problemTypeTitle() {
        return title;
    }

    /**
     * Legacy accessor retained for source compatibility with the original record component name.
     *
     * @return the HTTP status
     * @deprecated use {@link #getHttpStatus()}
     */
    @Deprecated
    @JsonIgnore
    public Maybe<Integer> httpStatus() {
        return status;
    }

    /**
     * Legacy accessor retained for source compatibility with the original record component name.
     *
     * @return the detail
     * @deprecated use {@link #getFullDetail()}
     */
    @Deprecated
    @JsonIgnore
    public Maybe<String> fullDetail() {
        return detail;
    }

    /**
     * Legacy accessor retained for source compatibility with the original record component name.
     *
     * @return the instance URI
     * @deprecated use {@link #getProblemInstanceId()}
     */
    @Deprecated
    @JsonIgnore
    public Maybe<URI> problemInstanceId() {
        return instance;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Maybe<String> getCorrelationId() {
        return correlationId;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Maybe<String> getTraceId() {
        return traceId;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Maybe<String> getRequestId() {
        return requestId;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Maybe<String> getTenantId() {
        return tenantId;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Maybe<Boolean> getRetryable() {
        return retryable;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Maybe<Long> getRetryAfterSeconds() {
        return retryAfter;
    }

    /**
     * Returns a copy whose title is the localized HTTP status phrase for an {@code about:blank}
     * problem.
     *
     * <p>Core members and the operational extension members are preserved through {@link
     * #toBuilder()}; details that do not use {@code about:blank} are returned unchanged.
     *
     * <pre>{@code
     * StandardProblemDetail german = StandardProblemDetail.aboutBlank("Not Found", 404)
     *         .localized(Locale.GERMAN);
     * assertEquals("Nicht gefunden", german.getTitle());
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return a localized problem detail of the same type
     */
    @Override
    public StandardProblemDetail localized(final Locale locale) {
        final Locale resolved = locale == null ? Locale.ENGLISH : locale;
        if (!isAboutBlank()) {
            return this;
        }
        return toBuilder().title(getLocalizedTitle(resolved)).build();
    }

    /**
     * Returns a copy with operational metadata attached.
     *
     * <pre>{@code
     * StandardProblemDetail detail = StandardProblemDetail.aboutBlank("Too Many Requests", 429)
     *         .withExtensions(
     *                 Maybe.some("corr-123"),
     *                 Maybe.some("trace-456"),
     *                 Maybe.none(),
     *                 Maybe.some("tenant-a"),
     *                 Maybe.some(true),
     *                 Maybe.some(3L));
     * }</pre>
     *
     * @param correlationId the correlation identifier
     * @param traceId the trace identifier
     * @param requestId the request identifier
     * @param tenantId the tenant identifier
     * @param retryable the retryability hint; {@link Maybe#none()} leaves it unknown
     * @param retryAfterSeconds the recommended delay in seconds
     * @return a new problem detail preserving the RFC 7807 core members
     */
    public StandardProblemDetail withExtensions(
            final Maybe<String> correlationId,
            final Maybe<String> traceId,
            final Maybe<String> requestId,
            final Maybe<String> tenantId,
            final Maybe<Boolean> retryable,
            final Maybe<Long> retryAfterSeconds) {
        return new StandardProblemDetail(
                problemType,
                title,
                status,
                detail,
                instance,
                Objects.requireNonNull(correlationId, "correlationId cannot be null"),
                Objects.requireNonNull(traceId, "traceId cannot be null"),
                Objects.requireNonNull(requestId, "requestId cannot be null"),
                Objects.requireNonNull(tenantId, "tenantId cannot be null"),
                Objects.requireNonNull(retryable, "retryable cannot be null"),
                Objects.requireNonNull(retryAfterSeconds, "retryAfterSeconds cannot be null"));
    }

    /**
     * Returns a copy with operational metadata attached, using a definite retryability value.
     *
     * @param correlationId the correlation identifier
     * @param traceId the trace identifier
     * @param requestId the request identifier
     * @param tenantId the tenant identifier
     * @param retryable whether a retry may succeed
     * @param retryAfterSeconds the recommended delay in seconds
     * @return a new problem detail preserving the RFC 7807 core members
     */
    public StandardProblemDetail withExtensions(
            final Maybe<String> correlationId,
            final Maybe<String> traceId,
            final Maybe<String> requestId,
            final Maybe<String> tenantId,
            final boolean retryable,
            final Maybe<Long> retryAfterSeconds) {
        return withExtensions(
                correlationId,
                traceId,
                requestId,
                tenantId,
                Maybe.of(retryable),
                retryAfterSeconds);
    }

    /**
     * Creates a new Builder for constructing StandardProblemDetail instances.
     *
     * @param problemType the required problem type URI
     * @return a new Builder instance
     */
    public static Builder builder(final URI problemType) {
        return new Builder(problemType);
    }

    /**
     * Returns a builder pre-populated with this instance's values.
     *
     * @return a new Builder instance
     */
    public Builder toBuilder() {
        return new Builder(problemType)
                .title(title.orNull())
                .status(status.orNull())
                .detail(detail.orNull())
                .instance(instance.orNull())
                .correlationId(correlationId.orNull())
                .traceId(traceId.orNull())
                .requestId(requestId.orNull())
                .tenantId(tenantId.orNull())
                .retryable(retryable.orNull())
                .retryAfterSeconds(retryAfter.orNull());
    }

    /**
     * Factory method for the "about:blank" problem type.
     *
     * @return a StandardProblemDetail with "about:blank" type
     */
    public static StandardProblemDetail aboutBlank() {
        return new StandardProblemDetail(URI.create("about:blank"));
    }

    /**
     * Factory method for the "about:blank" problem type with a title.
     *
     * @param title the problem title
     * @return a StandardProblemDetail with "about:blank" type and title
     */
    public static StandardProblemDetail aboutBlank(final String title) {
        return new StandardProblemDetail(URI.create("about:blank"), title);
    }

    /**
     * Factory method for the "about:blank" problem type with title and status.
     *
     * @param title the problem title
     * @param status the HTTP status code
     * @return a StandardProblemDetail with "about:blank" type, title, and status
     */
    public static StandardProblemDetail aboutBlank(final String title, final Integer status) {
        return new StandardProblemDetail(URI.create("about:blank"), title, status);
    }

    /**
     * Factory method for an {@code about:blank} problem whose title is the localized HTTP reason
     * phrase.
     *
     * <pre>{@code
     * StandardProblemDetail.aboutBlank(404, Locale.FRENCH).getTitle();
     * // "Introuvable"
     * }</pre>
     *
     * @param status the HTTP status code
     * @param locale the requested locale; {@code null} means English
     * @return a StandardProblemDetail with "about:blank" type, localized title, and status
     */
    public static StandardProblemDetail aboutBlank(final int status, final Locale locale) {
        return new StandardProblemDetail(
                URI.create("about:blank"),
                Maybe.of(HttpStatusMessages.title(status, locale)),
                Maybe.of(status),
                Maybe.none(),
                Maybe.none());
    }

    /** Builder pattern for constructing StandardProblemDetail instances. */
    public static final class Builder {
        private final URI problemType;
        private Maybe<String> problemTypeTitle = Maybe.none();
        private Maybe<Integer> httpStatus = Maybe.none();
        private Maybe<String> fullDetail = Maybe.none();
        private Maybe<URI> problemInstanceId = Maybe.none();
        private Maybe<String> correlationId = Maybe.none();
        private Maybe<String> traceId = Maybe.none();
        private Maybe<String> requestId = Maybe.none();
        private Maybe<String> tenantId = Maybe.none();
        private Maybe<Boolean> retryable = Maybe.none();
        private Maybe<Long> retryAfterSeconds = Maybe.none();

        private Builder(final URI problemType) {
            this.problemType = Objects.requireNonNull(problemType, "problemType cannot be null");
        }

        /**
         * Sets the problem type title.
         *
         * @param title the problem type title
         * @return this builder
         */
        public Builder title(final String title) {
            this.problemTypeTitle = Maybe.ofNullable(title);
            return this;
        }

        /**
         * Sets the HTTP status code.
         *
         * @param status the HTTP status code
         * @return this builder
         */
        public Builder status(final Integer status) {
            this.httpStatus = Maybe.ofNullable(status);
            return this;
        }

        /**
         * Sets the detailed explanation.
         *
         * @param detail the detailed explanation
         * @return this builder
         */
        public Builder detail(final String detail) {
            this.fullDetail = Maybe.ofNullable(detail);
            return this;
        }

        /**
         * Sets the problem instance identifier.
         *
         * @param instance the instance identifier URI
         * @return this builder
         */
        public Builder instance(final URI instance) {
            this.problemInstanceId = Maybe.ofNullable(instance);
            return this;
        }

        /**
         * Sets the cross-system correlation identifier.
         *
         * @param correlationId the correlation identifier
         * @return this builder
         */
        public Builder correlationId(final String correlationId) {
            this.correlationId = Maybe.ofNullable(correlationId);
            return this;
        }

        /**
         * Sets the distributed-tracing identifier.
         *
         * @param traceId the trace identifier
         * @return this builder
         */
        public Builder traceId(final String traceId) {
            this.traceId = Maybe.ofNullable(traceId);
            return this;
        }

        /**
         * Sets the request identifier.
         *
         * @param requestId the request identifier
         * @return this builder
         */
        public Builder requestId(final String requestId) {
            this.requestId = Maybe.ofNullable(requestId);
            return this;
        }

        /**
         * Sets the tenant identifier.
         *
         * @param tenantId the tenant identifier
         * @return this builder
         */
        public Builder tenantId(final String tenantId) {
            this.tenantId = Maybe.ofNullable(tenantId);
            return this;
        }

        /**
         * Sets the retryability hint.
         *
         * @param retryable the hint
         * @return this builder
         */
        public Builder retryable(final Boolean retryable) {
            this.retryable = Maybe.ofNullable(retryable);
            return this;
        }

        /**
         * Sets the recommended retry delay.
         *
         * @param retryAfterSeconds the delay in seconds
         * @return this builder
         */
        public Builder retryAfterSeconds(final Long retryAfterSeconds) {
            this.retryAfterSeconds = Maybe.ofNullable(retryAfterSeconds);
            return this;
        }

        /**
         * Builds the StandardProblemDetail instance.
         *
         * @return a new StandardProblemDetail instance
         */
        public StandardProblemDetail build() {
            return new StandardProblemDetail(
                    problemType,
                    problemTypeTitle,
                    httpStatus,
                    fullDetail,
                    problemInstanceId,
                    correlationId,
                    traceId,
                    requestId,
                    tenantId,
                    retryable,
                    retryAfterSeconds);
        }
    }
}
