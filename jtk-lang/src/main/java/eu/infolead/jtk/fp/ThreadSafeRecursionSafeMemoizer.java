package eu.infolead.jtk.fp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import eu.infolead.jtk.fp.either.Maybe;
import jakarta.annotation.Nonnull;

/**
 * Thread-safe memoization function.
 *
 * <p>
 * By default, contents are memoized forever into a {@link HashMap}: values have
 * an infinite lifetime and the map is not bounded.
 *
 * <p>
 * The lock only protects access to the cache. The memoized function is invoked
 * outside the lock, so a slow computation does not block reads of already
 * cached values. If two threads race on the same uncached input, the function
 * may be invoked more than once; the first computed result is stored and
 * returned consistently to all subsequent callers.
 *
 * <p>
 * Recursive calls with <em>different</em> keys are supported. Re-entering the
 * memoized function with the same key on the same thread is rejected with an
 * {@link IllegalStateException} instead of recursing until a stack overflow.
 * A {@code null} result is returned to the caller but is not cached.
 */
public final class ThreadSafeRecursionSafeMemoizer implements Memoizer {

    @Override
    public <I, O> Function<I, O> memoize(@Nonnull final Maybe<Supplier<Map<I, O>>> mapFactoryMaybe,
            @Nonnull final Function<I, O> function) {
        Objects.requireNonNull(mapFactoryMaybe, "mapFactoryMaybe");
        Objects.requireNonNull(function, "function");
        final Map<I, O> lookup = mapFactoryMaybe.isPresent().toBoolean()
                ? Objects.requireNonNull(mapFactoryMaybe.orNull().get(), "cache supplier returned null")
                : new HashMap<>();
        final ReentrantLock lock = new ReentrantLock();
        final ThreadLocal<Set<I>> inProgressKeys = ThreadLocal.withInitial(HashSet::new);
        return input -> {
            lock.lock();
            try {
                if (lookup.containsKey(input)) {
                    return lookup.get(input);
                }
            } finally {
                lock.unlock();
            }

            final Set<I> inProgress = inProgressKeys.get();
            if (!inProgress.add(input)) {
                throw new IllegalStateException(
                        "Recursive memoization of the same key on one thread: " + input);
            }
            final O computed;
            try {
                computed = function.apply(input);
            } finally {
                inProgress.remove(input);
                if (inProgress.isEmpty()) {
                    inProgressKeys.remove();
                }
            }

            if (computed == null) {
                return null;
            }

            lock.lock();
            try {
                if (lookup.containsKey(input)) {
                    return lookup.get(input);
                }
                lookup.put(input, computed);
                return computed;
            } finally {
                lock.unlock();
            }
        };
    }
}
