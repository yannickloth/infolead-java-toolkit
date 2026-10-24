package eu.infolead.jtk.fp;

import java.util.function.Supplier;

import eu.infolead.jtk.lang.AliasFor;

/**
 * A subclass of {@link Supplier}, whose sole purpose in life is to provide
 * additional useful methods.
 *
 * @param <U> the type of the provided value
 */
@FunctionalInterface
@AliasFor(type = Supplier.class)
public interface Provider<U> extends Supplier<U> {
    /**
     * A supplier has no parameter, while a mapper does, and both return a value.
     * This transforms a supplier into a mapper whose parameter is ignored: the
     * supplier still produces the returned value.
     *
     * @param <T> the type of the mapper's input parameter (which is not used).
     * @return the produced value of type U.
     */
    default <T> Mapper<U, T> asMapper() {
        return ignored -> get();
    }

    /**
     * Creates a provider that always returns the specified value.
     *
     * @param <T>   the value type
     * @param value the value to provide
     * @return a provider of {@code value}
     */
    static <T> Provider<T> of(final T value) {
        return () -> value;
    }

    /**
     * Creates a provider that always returns {@code null}.
     *
     * @param <T> the value type
     * @return a provider of {@code null}
     */
    static <T> Provider<T> ofNull() {
        return () -> null;
    }
}
