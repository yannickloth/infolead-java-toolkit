package eu.infolead.jtk.anomaly;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Correlation metadata that ties an error occurrence to the request and
 * tenant in which it happened.
 *
 * <p>Unlike the generated error ID (which is a process-local sequence), these
 * identifiers are supplied by the surrounding infrastructure and therefore
 * correlate across services, logs and traces.</p>
 *
 * <pre>{@code
 * ErrorContext context = ErrorContext.empty()
 *         .withCorrelationId("corr-123")
 *         .withTraceId("trace-456")
 *         .withRequestId("req-789")
 *         .withTenantId("tenant-a");
 *
 * SystemError error = ResourceError.notFound("User", 7)
 *         .withErrorContext(context);
 * }</pre>
 *
 * @param correlationId the cross-system correlation identifier (optional)
 * @param traceId       the distributed-tracing identifier (optional)
 * @param requestId     the request identifier (optional)
 * @param tenantId      the tenant identifier (optional)
 */
public record ErrorContext(
        Maybe<String> correlationId,
        Maybe<String> traceId,
        Maybe<String> requestId,
        Maybe<String> tenantId) {

    /**
     * Canonical constructor with validation.
     *
     * @param correlationId the cross-system correlation identifier
     * @param traceId       the distributed-tracing identifier
     * @param requestId     the request identifier
     * @param tenantId      the tenant identifier
     */
    public ErrorContext {
        Objects.requireNonNull(correlationId, "correlationId cannot be null");
        Objects.requireNonNull(traceId, "traceId cannot be null");
        Objects.requireNonNull(requestId, "requestId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }

    private static final ErrorContext EMPTY =
            new ErrorContext(Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none());

    /**
     * Returns the empty context.
     *
     * @return a context with no identifiers
     */
    public static ErrorContext empty() {
        return EMPTY;
    }

    /**
     * Returns a new context with the supplied correlation identifier.
     *
     * @param correlationId the correlation identifier; {@code null} clears it
     * @return a new context
     */
    public ErrorContext withCorrelationId(final String correlationId) {
        return new ErrorContext(Maybe.ofNullable(correlationId), traceId, requestId, tenantId);
    }

    /**
     * Returns a new context with the supplied trace identifier.
     *
     * @param traceId the trace identifier; {@code null} clears it
     * @return a new context
     */
    public ErrorContext withTraceId(final String traceId) {
        return new ErrorContext(correlationId, Maybe.ofNullable(traceId), requestId, tenantId);
    }

    /**
     * Returns a new context with the supplied request identifier.
     *
     * @param requestId the request identifier; {@code null} clears it
     * @return a new context
     */
    public ErrorContext withRequestId(final String requestId) {
        return new ErrorContext(correlationId, traceId, Maybe.ofNullable(requestId), tenantId);
    }

    /**
     * Returns a new context with the supplied tenant identifier.
     *
     * @param tenantId the tenant identifier; {@code null} clears it
     * @return a new context
     */
    public ErrorContext withTenantId(final String tenantId) {
        return new ErrorContext(correlationId, traceId, requestId, Maybe.ofNullable(tenantId));
    }

    /**
     * Indicates whether this context carries no identifiers.
     *
     * @return {@code true} when every identifier is absent
     */
    public boolean isEmpty() {
        return correlationId.isEmpty().toBoolean()
                && traceId.isEmpty().toBoolean()
                && requestId.isEmpty().toBoolean()
                && tenantId.isEmpty().toBoolean();
    }
}
