package eu.infolead.jtk.spring.autoconfigure;

import eu.infolead.jtk.anomaly.http.MultiStatusProblemDetail;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * Bridges the toolkit's RFC 9457 document to Spring's own {@code ProblemDetail}.
 *
 * <p>The two types model the same standard but evolve for different reasons: the toolkit's type is
 * framework-free and lives with the error taxonomy, while {@code
 * org.springframework.http.ProblemDetail} is driven by Spring releases. Keeping the translation
 * here means neither type has to know about the other.
 *
 * <p>Only the members both models share are bridged. The multi-status {@code responses} payload has
 * no Spring counterpart and is intentionally dropped; use the Jackson module directly if it must
 * cross the boundary.
 */
public final class SpringProblemDetails {

    private static final String CORRELATION_ID = "correlationId";
    private static final String TRACE_ID = "traceId";
    private static final String REQUEST_ID = "requestId";
    private static final String TENANT_ID = "tenantId";
    private static final String RETRYABLE = "retryable";
    private static final String RETRY_AFTER = "retryAfter";

    private SpringProblemDetails() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Translates a toolkit problem document into a Spring one.
     *
     * @param detail the toolkit document
     * @return the Spring document, carrying the core members and the known extension members
     */
    public static org.springframework.http.ProblemDetail toSpring(final ProblemDetail detail) {
        Objects.requireNonNull(detail, "detail");
        final int status = detail.getStatusValue() == null ? 500 : detail.getStatusValue();
        final org.springframework.http.ProblemDetail target =
                org.springframework.http.ProblemDetail.forStatus(status);
        target.setType(detail.getProblemType());
        if (detail.getTitle() != null) {
            target.setTitle(detail.getTitle());
        }
        if (detail.getDetail() != null) {
            target.setDetail(detail.getDetail());
        }
        if (detail.getInstance() != null) {
            target.setInstance(detail.getInstance());
        }
        put(target, CORRELATION_ID, detail.getCorrelationId());
        put(target, TRACE_ID, detail.getTraceId());
        put(target, REQUEST_ID, detail.getRequestId());
        put(target, TENANT_ID, detail.getTenantId());
        put(target, RETRYABLE, detail.getRetryable());
        put(target, RETRY_AFTER, detail.getRetryAfterSeconds());
        return target;
    }

    /**
     * Translates a Spring problem document into a toolkit one.
     *
     * <p>Spring's arbitrary extension map is not copied wholesale: only the members the toolkit
     * models are read. A multi-status payload is not reconstructed, because Spring's type has no
     * such structure.
     *
     * @param detail the Spring document
     * @return the toolkit document
     */
    public static ProblemDetail fromSpring(final org.springframework.http.ProblemDetail detail) {
        Objects.requireNonNull(detail, "detail");
        final Map<String, Object> properties = detail.getProperties();
        final StandardProblemDetail.Builder builder =
                StandardProblemDetail.builder(
                                detail.getType() == null
                                        ? URI.create("about:blank")
                                        : detail.getType())
                        .status(detail.getStatus())
                        .title(detail.getTitle())
                        .detail(detail.getDetail())
                        .instance(detail.getInstance());
        builder.correlationId(stringProperty(properties, CORRELATION_ID));
        builder.traceId(stringProperty(properties, TRACE_ID));
        builder.requestId(stringProperty(properties, REQUEST_ID));
        builder.tenantId(stringProperty(properties, TENANT_ID));
        final Object retryable = properties.get(RETRYABLE);
        if (retryable instanceof Boolean flag) {
            builder.retryable(flag);
        }
        final Object retryAfter = properties.get(RETRY_AFTER);
        if (retryAfter instanceof Number seconds) {
            builder.retryAfterSeconds(seconds.longValue());
        }
        return builder.build();
    }

    private static void put(
            final org.springframework.http.ProblemDetail target,
            final String name,
            final Maybe<?> value) {
        if (value == null) {
            return;
        }
        final Object present = value.orNull();
        if (present != null) {
            target.setProperty(name, present);
        }
    }

    private static String stringProperty(final Map<String, Object> properties, final String name) {
        final Object value = properties.get(name);
        return value == null ? null : value.toString();
    }

    /**
     * Returns whether the document is a multi-status document, for callers that need to reject the
     * lossy translation explicitly.
     *
     * @param detail the toolkit document
     * @return {@code true} for a multi-status document
     */
    public static boolean isMultiStatus(final ProblemDetail detail) {
        return detail instanceof MultiStatusProblemDetail;
    }
}
