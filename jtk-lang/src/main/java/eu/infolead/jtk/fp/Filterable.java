package eu.infolead.jtk.fp;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import eu.infolead.jtk.lang.CompilerWarning;

/**
 * A type that can be viewed as a stream containing itself, filtered by a
 * predicate.
 *
 * @param <F> the concrete self type
 */
public interface Filterable<F extends Filterable<F>> {
    /**
     * Returns a stream containing this instance if it matches the predicate, or an
     * empty stream otherwise.
     *
     * @param predicate the predicate to apply
     * @return a stream with this instance, or an empty stream
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default Stream<F> filter(final Predicate<? super F> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return Stream.of((F) this).filter(predicate);
    }
}
