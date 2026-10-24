package eu.infolead.jtk.anomaly.http;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Standard RFC 4918 multi-status problem detail.
 * <p>
 * Serializes as a regular problem detail plus a {@code multistatus} object
 * containing a {@code response} array.
 *
 * <pre>{@code
 * StandardMultiStatusProblemDetail detail = new StandardMultiStatusProblemDetail(
 *         URI.create("about:blank"),
 *         Maybe.some("Multi-Status"),
 *         Maybe.some(207),
 *         Maybe.some("bulk update"),
 *         Maybe.none(),
 *         List.of(new MultiStatusResponseElement(URI.create("/items/1"), 423)));
 * }</pre>
 *
 * <p>The last six components are RFC 7807 extension members used to
 * correlate an occurrence across systems and to tell a client whether a retry
 * is worthwhile; they mirror {@link StandardProblemDetail}.</p>
 *
 * @param problemType       the problem type URI (required)
 * @param title             the problem type title (optional)
 * @param status            the HTTP status code (optional, usually 207)
 * @param detail            the detailed explanation (optional)
 * @param instance          the instance identifier (optional)
 * @param responses         the per-resource responses
 * @param correlationId     the cross-system correlation identifier (optional)
 * @param traceId           the distributed-tracing identifier (optional)
 * @param requestId         the request identifier (optional)
 * @param tenantId          the tenant identifier (optional)
 * @param retryable         whether the failed operation may succeed if
 *                          retried (optional)
 * @param retryAfterSeconds the recommended retry delay in seconds (optional)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StandardMultiStatusProblemDetail(
        @JsonProperty("type") URI problemType,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> title,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<Integer> status,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> detail,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<URI> instance,
        @JsonIgnore List<MultiStatusResponseElement> responses,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> correlationId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> traceId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> requestId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<String> tenantId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<Boolean> retryable,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Maybe<Long> retryAfterSeconds) implements MultiStatusProblemDetail {

    /**
     * Canonical constructor with validation.
     *
     * @param problemType the problem type URI
     * @param title       the problem type title
     * @param status      the HTTP status code; when present it must be 207
     * @param detail      the detailed explanation
     * @param instance          the instance URI
     * @param responses         the per-resource responses
     * @param correlationId     the correlation identifier
     * @param traceId           the trace identifier
     * @param requestId         the request identifier
     * @param tenantId          the tenant identifier
     * @param retryable         the retryability hint
     * @param retryAfterSeconds the recommended retry delay in seconds
     */
    public StandardMultiStatusProblemDetail {
        Objects.requireNonNull(problemType, "problemType cannot be null");
        Objects.requireNonNull(title, "title cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        Objects.requireNonNull(detail, "detail cannot be null");
        Objects.requireNonNull(instance, "instance cannot be null");
        responses = List.copyOf(Objects.requireNonNull(responses, "responses"));
        Objects.requireNonNull(correlationId, "correlationId cannot be null");
        Objects.requireNonNull(traceId, "traceId cannot be null");
        Objects.requireNonNull(requestId, "requestId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(retryable, "retryable cannot be null");
        Objects.requireNonNull(retryAfterSeconds, "retryAfterSeconds cannot be null");
        if (status.isPresent().toBoolean() && status.orNull() != 207) {
            throw new IllegalArgumentException(
                    "A multi-status problem detail must use HTTP 207, was " + status.orNull());
        }
    }

    /**
     * Backwards-compatible constructor without extension members.
     *
     * @param problemType the problem type URI
     * @param title       the problem type title
     * @param status      the HTTP status code
     * @param detail      the detailed explanation
     * @param instance    the instance URI
     * @param responses   the per-resource responses
     */
    public StandardMultiStatusProblemDetail(
            final URI problemType,
            final Maybe<String> title,
            final Maybe<Integer> status,
            final Maybe<String> detail,
            final Maybe<URI> instance,
            final List<MultiStatusResponseElement> responses) {
        this(problemType, title, status, detail, instance, responses,
                Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                Maybe.none(), Maybe.none());
    }

    /**
     * Creates an instance from the nullable JSON representation.
     *
     * @param problemType the problem type URI
     * @param title       the optional title
     * @param status      the optional HTTP status
     * @param detail      the optional detail
     * @param instance    the optional instance URI
     * @param multiStatus       the optional {@code multistatus} wrapper
     * @param correlationId     the optional correlation identifier
     * @param traceId           the optional trace identifier
     * @param requestId         the optional request identifier
     * @param tenantId          the optional tenant identifier
     * @param retryable         the optional retryability hint
     * @param retryAfterSeconds the optional retry delay in seconds
     * @return a new multi-status problem detail
     */
    @JsonCreator
    public static StandardMultiStatusProblemDetail fromJson(
            @JsonProperty("type") final URI problemType,
            @JsonProperty("title") final String title,
            @JsonProperty("status") final Integer status,
            @JsonProperty("detail") final String detail,
            @JsonProperty("instance") final URI instance,
            @JsonProperty("multistatus") final MultiStatusProblemDetail.MultiStatus multiStatus,
            @JsonProperty("correlationId") final String correlationId,
            @JsonProperty("traceId") final String traceId,
            @JsonProperty("requestId") final String requestId,
            @JsonProperty("tenantId") final String tenantId,
            @JsonProperty("retryable") final Boolean retryable,
            @JsonProperty("retryAfter") final Long retryAfterSeconds) {
        return new StandardMultiStatusProblemDetail(
                problemType == null ? URI.create("about:blank") : problemType,
                Maybe.ofNullable(title),
                Maybe.ofNullable(status),
                Maybe.ofNullable(detail),
                Maybe.ofNullable(instance),
                multiStatus == null ? List.of() : multiStatus.response(),
                Maybe.ofNullable(correlationId),
                Maybe.ofNullable(traceId),
                Maybe.ofNullable(requestId),
                Maybe.ofNullable(tenantId),
                Maybe.ofNullable(retryable),
                Maybe.ofNullable(retryAfterSeconds));
    }

    /**
     * Creates an instance from the RFC 7807 core members and the
     * {@code multistatus} payload, without extension members.
     *
     * @param problemType the problem type URI
     * @param title       the optional title
     * @param status      the optional HTTP status
     * @param detail      the optional detail
     * @param instance    the optional instance URI
     * @param multiStatus the optional {@code multistatus} wrapper
     * @return a new multi-status problem detail
     */
    public static StandardMultiStatusProblemDetail fromJson(
            final URI problemType,
            final String title,
            final Integer status,
            final String detail,
            final URI instance,
            final MultiStatusProblemDetail.MultiStatus multiStatus) {
        return fromJson(problemType, title, status, detail, instance, multiStatus,
                null, null, null, null, null, null);
    }

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
     * Legacy accessor retained for source compatibility with the original
     * record component name.
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
     * Legacy accessor retained for source compatibility with the original
     * record component name.
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
     * Legacy accessor retained for source compatibility with the original
     * record component name.
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
     * Legacy accessor retained for source compatibility with the original
     * record component name.
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
    @JsonIgnore
    public List<MultiStatusResponseElement> getResponses() {
        return responses;
    }

    @Override
    @JsonIgnore
    public Maybe<String> getCorrelationId() {
        return correlationId;
    }

    @Override
    @JsonIgnore
    public Maybe<String> getTraceId() {
        return traceId;
    }

    @Override
    @JsonIgnore
    public Maybe<String> getRequestId() {
        return requestId;
    }

    @Override
    @JsonIgnore
    public Maybe<String> getTenantId() {
        return tenantId;
    }

    @Override
    @JsonIgnore
    public Maybe<Boolean> getRetryable() {
        return retryable;
    }

    @Override
    @JsonIgnore
    public Maybe<Long> getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /**
     * Returns a copy with operational metadata attached.
     *
     * <pre>{@code
     * StandardMultiStatusProblemDetail enriched = detail.withExtensions(
     *         Maybe.some("corr-1"), Maybe.none(), Maybe.none(), Maybe.none(),
     *         true, Maybe.of(3L));
     * }</pre>
     *
     * @param correlationId     the correlation identifier
     * @param traceId           the trace identifier
     * @param requestId         the request identifier
     * @param tenantId          the tenant identifier
     * @param retryable         whether a retry may succeed
     * @param retryAfterSeconds the recommended delay in seconds
     * @return a new multi-status problem detail preserving the responses
     */
    public StandardMultiStatusProblemDetail withExtensions(
            final Maybe<String> correlationId,
            final Maybe<String> traceId,
            final Maybe<String> requestId,
            final Maybe<String> tenantId,
            final Maybe<Boolean> retryable,
            final Maybe<Long> retryAfterSeconds) {
        return new StandardMultiStatusProblemDetail(problemType, title, status, detail, instance,
                responses,
                Objects.requireNonNull(correlationId, "correlationId cannot be null"),
                Objects.requireNonNull(traceId, "traceId cannot be null"),
                Objects.requireNonNull(requestId, "requestId cannot be null"),
                Objects.requireNonNull(tenantId, "tenantId cannot be null"),
                Objects.requireNonNull(retryable, "retryable cannot be null"),
                Objects.requireNonNull(retryAfterSeconds, "retryAfterSeconds cannot be null"));
    }

    /**
     * Returns a copy with operational metadata attached.
     *
     * @param correlationId     the correlation identifier
     * @param traceId           the trace identifier
     * @param requestId         the request identifier
     * @param tenantId          the tenant identifier
     * @param retryable         whether a retry may succeed
     * @param retryAfterSeconds the recommended delay in seconds
     * @return a new multi-status problem detail preserving the responses
     */
    public StandardMultiStatusProblemDetail withExtensions(
            final Maybe<String> correlationId,
            final Maybe<String> traceId,
            final Maybe<String> requestId,
            final Maybe<String> tenantId,
            final boolean retryable,
            final Maybe<Long> retryAfterSeconds) {
        return withExtensions(correlationId, traceId, requestId, tenantId,
                Maybe.of(retryable), retryAfterSeconds);
    }

    /**
     * Returns a copy whose title is the localized HTTP status phrase for an
     * {@code about:blank} problem.
     *
     * <p>All core members, the {@code responses} payload and the operational
     * extensions are preserved. Details that do not use {@code about:blank} are
     * returned unchanged.</p>
     *
     * <pre>{@code
     * StandardMultiStatusProblemDetail german = detail.localized(Locale.GERMAN);
     * assertEquals(detail.getResponses(), german.getResponses());
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return a localized multi-status problem detail
     */
    @Override
    public StandardMultiStatusProblemDetail localized(final Locale locale) {
        final Locale resolved = locale == null ? Locale.ENGLISH : locale;
        if (!isAboutBlank()) {
            return this;
        }
        return new StandardMultiStatusProblemDetail(problemType,
                Maybe.ofNullable(getLocalizedTitle(resolved)), status, detail,
                instance, responses, correlationId, traceId, requestId, tenantId,
                retryable, retryAfterSeconds);
    }
}
