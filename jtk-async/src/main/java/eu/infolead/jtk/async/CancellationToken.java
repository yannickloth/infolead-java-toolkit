package eu.infolead.jtk.async;

import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.logic.Bool;

/**
 * Cooperative cancellation for asynchronous operations.
 *
 * <p>{@link java.util.concurrent.CompletableFuture#cancel(boolean)} does not interrupt the task
 * that is computing the value, so a future cannot be used as its own cancellation mechanism. A
 * token is passed to the task instead: cancelling it interrupts the worker and completes the
 * operation with the anomaly that matches the {@linkplain CancellationReason reason}.
 *
 * <p>Cancellation is <strong>best effort</strong>. It never rolls back a side effect that already
 * happened, and it cannot stop a task that ignores thread interruption. A cancelled operation
 * completes promptly with a failure value; the worker may still be unwinding after that.
 *
 * <p>Tokens form a tree: cancelling a parent cancels all of its children, while cancelling a child
 * affects only that child. The first reason wins; later cancellations are ignored.
 *
 * <p>Tokens are runtime handles and are not serializable. A token should scope one operation or one
 * request, not the lifetime of a whole service.
 *
 * <pre>{@code
 * CancellationToken token = CancellationToken.create();
 * AsyncContext<SystemError> context = AsyncContext.systemErrors(pool).withCancellation(token);
 *
 * CompletableFuture<Result<SystemError, Report>> report =
 *         Async.supply(context, () -> service.generate());
 *
 * // From a request-abort handler, a shutdown hook, or a timer:
 * token.cancel(CancellationReason.SHUTDOWN);
 * // report completes with a failure value and the worker is interrupted.
 * }</pre>
 */
public interface CancellationToken {

    /**
     * Returns whether this token has been cancelled.
     *
     * @return {@link Bool#TRUE} once cancellation has been requested
     */
    Bool isCancelled();

    /**
     * Returns the reason why this token was cancelled, if it was.
     *
     * @return the first cancellation reason, or {@link Maybe#none()}
     */
    Maybe<CancellationReason> reason();

    /**
     * Cancels this token and all of its descendants.
     *
     * <p>The call is idempotent: only the first reason is retained. Hooks registered with {@link
     * #onCancel(Runnable)} run on the calling thread before this method returns, and a failing hook
     * never stops the remaining hooks or the propagation to children.
     *
     * @param reason why the operation is cancelled
     * @throws NullPointerException if {@code reason} is {@code null}
     */
    void cancel(CancellationReason reason);

    /**
     * Registers an action to run when this token is cancelled.
     *
     * <p>If the token is already cancelled the action runs immediately on the calling thread. A
     * failing action is ignored, because cancellation must always reach every registered hook and
     * every child token.
     *
     * @param hook the action to run; must not be {@code null}
     * @return a handle that removes the hook; calling it after the hook ran has no effect
     * @throws NullPointerException if {@code hook} is {@code null}
     */
    Runnable onCancel(Runnable hook);

    /**
     * Creates a child token that is cancelled when this token is cancelled.
     *
     * <p>A child is a new cancellation scope. Cancelling the child leaves this token active. A
     * child of the {@linkplain #none() non-cancellable} token is an independent, cancellable scope,
     * so deadlines and fail-fast traversals work on contexts that nobody owns.
     *
     * @return a new child token, already cancelled if this token is
     */
    CancellationToken child();

    /**
     * Creates a root token that can be cancelled by its owner.
     *
     * @return a new, active token
     */
    static CancellationToken create() {
        return new DefaultCancellationToken(null, false);
    }

    /**
     * Returns a token that can never be cancelled.
     *
     * <p>Use it when an operation must run to completion regardless of its caller, or as the
     * neutral value of a token parameter. A child of this token is an independent cancellable
     * scope: deadlines and fail-fast traversals derive one automatically, so they work even on a
     * context that nobody owns.
     *
     * @return the shared non-cancellable token
     */
    static CancellationToken none() {
        return DefaultCancellationToken.NONE;
    }
}
