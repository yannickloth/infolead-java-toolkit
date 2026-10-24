package eu.infolead.jtk.test.error;

import eu.infolead.jtk.anomaly.AuthorizationError;
import eu.infolead.jtk.anomaly.BusinessRuleError;
import eu.infolead.jtk.anomaly.InvariantError;
import eu.infolead.jtk.anomaly.ParameterError;
import eu.infolead.jtk.anomaly.PostconditionError;
import eu.infolead.jtk.anomaly.PreconditionError;
import eu.infolead.jtk.anomaly.ResourceError;
import eu.infolead.jtk.anomaly.SystemError;
import java.util.List;

/**
 * Canonical, deterministic {@link SystemError} fixtures, one per anomaly category of the unified
 * error system.
 *
 * <p>The toolkit generates error instance IDs from a process-local sequence with a random
 * per-process token, so freshly built errors are never byte-stable. Every fixture in this class
 * pins its error ID (and its operational context) to a constant, which makes the fixtures usable
 * for equality assertions and RFC&nbsp;9457 problem+json snapshot tests. Use the {@link
 * ErrorFixture} builder when a test needs a <em>variation</em> of a canonical error rather than the
 * canonical error itself.
 */
public final class TestErrors {

    /** Pinned correlation ID carried by every fixture. */
    public static final String CORRELATION_ID = "test-correlation-id";

    /** Pinned trace ID carried by every fixture. */
    public static final String TRACE_ID = "test-trace-id";

    /** Pinned request ID carried by every fixture. */
    public static final String REQUEST_ID = "test-request-id";

    /** Pinned tenant ID carried by every fixture. */
    public static final String TENANT_ID = "test-tenant-id";

    /** Pinned error ID of {@link #nullParameter()}. */
    public static final String NULL_PARAMETER_ERROR_ID = "test-error-0001";

    /** Pinned error ID of {@link #resourceNotFound()}. */
    public static final String RESOURCE_NOT_FOUND_ERROR_ID = "test-error-0002";

    /** Pinned error ID of {@link #businessRuleViolation()}. */
    public static final String BUSINESS_RULE_ERROR_ID = "test-error-0003";

    /** Pinned error ID of {@link #unauthorized()}. */
    public static final String UNAUTHORIZED_ERROR_ID = "test-error-0004";

    /** Pinned error ID of {@link #conflictingState()}. */
    public static final String CONFLICTING_STATE_ERROR_ID = "test-error-0005";

    /** Pinned error ID of {@link #persistenceFailed()}. */
    public static final String PERSISTENCE_FAILED_ERROR_ID = "test-error-0006";

    /** Pinned error ID of {@link #invariantViolation()}. */
    public static final String INVARIANT_VIOLATION_ERROR_ID = "test-error-0007";

    private TestErrors() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns a pinned {@code NULL_PARAMETER} parameter error.
     *
     * @return the canonical parameter error
     */
    public static SystemError nullParameter() {
        return withOperationalContext(
                ParameterError.nullParameter("orderId"), NULL_PARAMETER_ERROR_ID);
    }

    /**
     * Returns a pinned resource-not-found error.
     *
     * @return the canonical resource error
     */
    public static SystemError resourceNotFound() {
        return withOperationalContext(
                ResourceError.notFound("Order", "order-42"), RESOURCE_NOT_FOUND_ERROR_ID);
    }

    /**
     * Returns a pinned business-rule violation (currency mismatch).
     *
     * @return the canonical business rule error
     */
    public static SystemError businessRuleViolation() {
        return withOperationalContext(
                BusinessRuleError.currencyMismatch("EUR", "USD"), BUSINESS_RULE_ERROR_ID);
    }

    /**
     * Returns a pinned not-authenticated authorization error.
     *
     * @return the canonical authorization error
     */
    public static SystemError unauthorized() {
        return withOperationalContext(AuthorizationError.notAuthenticated(), UNAUTHORIZED_ERROR_ID);
    }

    /**
     * Returns a pinned conflicting-state precondition error.
     *
     * @return the canonical precondition error
     */
    public static SystemError conflictingState() {
        return withOperationalContext(
                PreconditionError.conflictingState("order already shipped"),
                CONFLICTING_STATE_ERROR_ID);
    }

    /**
     * Returns a pinned persistence-failed postcondition error.
     *
     * @return the canonical postcondition error
     */
    public static SystemError persistenceFailed() {
        return withOperationalContext(
                PostconditionError.persistenceFailed("Order", 42), PERSISTENCE_FAILED_ERROR_ID);
    }

    /**
     * Returns a pinned object-consistency invariant error.
     *
     * @return the canonical invariant error
     */
    public static SystemError invariantViolation() {
        return withOperationalContext(
                InvariantError.objectConsistency(
                        "order total must equal the sum of its line totals"),
                INVARIANT_VIOLATION_ERROR_ID);
    }

    /**
     * Returns every canonical fixture, one per anomaly category. Useful for exhaustive tests over
     * the anomaly model.
     *
     * @return an immutable list of all fixtures
     */
    public static List<SystemError> all() {
        return List.of(
                nullParameter(),
                resourceNotFound(),
                businessRuleViolation(),
                unauthorized(),
                conflictingState(),
                persistenceFailed(),
                invariantViolation());
    }

    private static SystemError withOperationalContext(
            final SystemError error, final String errorId) {
        return error.withErrorId(errorId)
                .withCorrelationId(CORRELATION_ID)
                .withTraceId(TRACE_ID)
                .withRequestId(REQUEST_ID)
                .withTenantId(TENANT_ID);
    }
}
