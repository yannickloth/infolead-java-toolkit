package eu.infolead.jtk.async;

import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.logic.Bool;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default {@link CancellationToken} implementation: a token tree with first-writer-wins reasons and
 * idempotent, failure-tolerant hooks.
 */
final class DefaultCancellationToken implements CancellationToken {

    /** Shared non-cancellable token. */
    static final DefaultCancellationToken NONE = new DefaultCancellationToken(null, true);

    private final DefaultCancellationToken parent;
    private final boolean nonCancellable;
    private final AtomicReference<CancellationReason> reason = new AtomicReference<>();
    private final Set<Runnable> hooks = ConcurrentHashMap.newKeySet();
    private final Set<DefaultCancellationToken> children = ConcurrentHashMap.newKeySet();

    DefaultCancellationToken(final DefaultCancellationToken parent, final boolean nonCancellable) {
        this.parent = parent;
        this.nonCancellable = nonCancellable;
    }

    @Override
    public Bool isCancelled() {
        return Bool.of(!nonCancellable && reason.get() != null);
    }

    @Override
    public Maybe<CancellationReason> reason() {
        return Maybe.ofNullable(reason.get());
    }

    @Override
    public void cancel(final CancellationReason requested) {
        Objects.requireNonNull(requested, "reason");
        if (nonCancellable || !reason.compareAndSet(null, requested)) {
            return;
        }
        final CancellationReason actual = reason.get();
        for (final Runnable hook : hooks) {
            runQuietly(hook);
        }
        hooks.clear();
        for (final DefaultCancellationToken child : children) {
            child.cancel(actual);
        }
        children.clear();
        detachFromParent();
    }

    @Override
    public Runnable onCancel(final Runnable hook) {
        Objects.requireNonNull(hook, "hook");
        if (nonCancellable) {
            return () -> {
                // Nothing to remove from a token that can never fire.
            };
        }
        final AtomicBoolean ran = new AtomicBoolean();
        final Runnable guarded =
                () -> {
                    if (ran.compareAndSet(false, true)) {
                        runQuietly(hook);
                    }
                };
        hooks.add(guarded);
        if (reason.get() != null && hooks.remove(guarded)) {
            guarded.run();
        }
        return () -> hooks.remove(guarded);
    }

    @Override
    public CancellationToken child() {
        if (nonCancellable) {
            // A child is a new cancellation scope. Even when this token can
            // never be cancelled, work derived from it can be cancelled by its
            // own deadline, by a sibling failure, or by its owner.
            return new DefaultCancellationToken(null, false);
        }
        final DefaultCancellationToken child = new DefaultCancellationToken(this, false);
        final CancellationReason inherited = reason.get();
        if (inherited != null) {
            child.cancel(inherited);
            return child;
        }
        children.add(child);
        // Re-check after publishing: a concurrent cancel may have missed the child.
        final CancellationReason raced = reason.get();
        if (raced != null && children.remove(child)) {
            child.cancel(raced);
        }
        return child;
    }

    /**
     * Releases this token from its parent's registry once the operation that owned it has settled,
     * so long-lived parents do not retain finished children.
     */
    void detach() {
        detachFromParent();
    }

    /**
     * Detaches any default token implementation from its parent.
     *
     * @param token the token to detach, possibly not a default implementation
     */
    static void detach(final CancellationToken token) {
        if (token instanceof DefaultCancellationToken defaultToken) {
            defaultToken.detach();
        }
    }

    private void detachFromParent() {
        if (parent != null) {
            parent.children.remove(this);
        }
    }

    private static void runQuietly(final Runnable hook) {
        try {
            hook.run();
        } catch (final Throwable ignored) {
            // Cancellation must reach every hook and every child.
        }
    }
}
