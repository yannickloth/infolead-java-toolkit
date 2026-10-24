package eu.infolead.jtk.async;

/**
 * Why an asynchronous operation was cancelled.
 *
 * <p>The reason decides which anomaly the cancelled operation completes with, so callers can
 * distinguish an explicit cancellation from a deadline or from a sibling failure in a fail-fast
 * traversal.
 */
public enum CancellationReason {

    /** The owner of the operation cancelled it explicitly. */
    CALLER,

    /** A deadline elapsed before the operation completed. */
    DEADLINE,

    /** A sibling operation failed and the surrounding traversal stopped early. */
    FAIL_FAST,

    /** The owning executor or application is shutting down. */
    SHUTDOWN,

    /**
     * The operation's thread was interrupted by code that does not own a cancellation token. The
     * interrupt status is restored before the failure value is returned.
     */
    EXTERNAL_INTERRUPT
}
