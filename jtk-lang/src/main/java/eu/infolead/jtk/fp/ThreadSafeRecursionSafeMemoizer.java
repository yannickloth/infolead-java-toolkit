package eu.infolead.jtk.fp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import eu.infolead.jtk.fp.either.Maybe;
import jakarta.annotation.Nonnull;

/**
 * Thread-safe and recursion-safe memoization function.
 * 
 * <p>
 * By default, contents are memoized forever into a {@link HashMap}: values have
 * an infinite lifetime, the map is not bounded.
 * 
 * @param f the function that maps the input to its corresponding result.
 * @return the value corresponding to the specified key when applying the
 *         specified function {@code f} to the specified key.
 * @param <I>         the type of the input (key).
 * @param <O>         the type of the output/result (value)
 * @param mapSupplier A function that supplies the map used as the cache. This
 *                    allows to specify different types of maps with different
 *                    behaviors, for example to influence the lifetime of
 *                    stored values. If this is {@code null}, a {@link HashMap}
 *                    is used.
 */
public final class ThreadSafeRecursionSafeMemoizer implements Memoizer {

    public <I, O> Function<I, O> memoize(@Nonnull final Maybe<Supplier<Map<I, O>>> mapSupplierMaybe,
            @Nonnull final Function<I, O> f) {
        final Map<I, O> lookup = mapSupplierMaybe.fold(() -> new HashMap<I, O>(), Supplier::get);
        final var lock = new ReentrantLock();
        return input -> {
            lock.lock();
            try {
                return lookup.computeIfAbsent(input, f);
            } finally {
                lock.unlock();
            }
        };
    }
}
