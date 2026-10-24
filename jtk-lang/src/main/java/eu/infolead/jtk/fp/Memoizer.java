package eu.infolead.jtk.fp;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import eu.infolead.jtk.fp.either.Maybe;
import jakarta.annotation.Nonnull;

/**
 * Produces memoizing functions.
 */
public interface Memoizer {
    /**
     * Returns a function that caches the results of {@code function} per input.
     * <p>
     * Implementations must be thread-safe. Concurrent callers of the same
     * uncached input may cause {@code function} to be invoked more than once;
     * the first stored result is returned consistently to all callers. A
     * {@code null} result is not cached.
     *
     * @param <I>              the input (key) type
     * @param <O>              the output (value) type
     * @param mapFactoryMaybe an optional supplier of the map used as cache; when
     *                         empty, an implementation-defined default map is used
     * @param function         the function to memoize
     * @return a memoizing function
     */
    <I, O> Function<I, O> memoize(@Nonnull Maybe<Supplier<Map<I, O>>> mapFactoryMaybe,
            @Nonnull Function<I, O> function);
}
