package eu.infolead.jtk.fp;

import java.util.function.Supplier;

import eu.infolead.jtk.lang.AliasFor;
import eu.infolead.jtk.lang.SonarLintWarning;

/**
 * A subclass of {@link Supplier}, whose sole purpose in life is to provide
 * additional useful methods.
 */
@FunctionalInterface
@AliasFor(type = Supplier.class)
public interface Provider<U> extends Supplier<U> {
    /**
     * A supplier has no parameter, while a mapper does, and both return a value.
     * This transforms a supplier
     * into a mapper whose parameter is not used to produce the returned value -
     * it's still the supplier that produces the returned value.
     * 
     * @param <T> the type of the mapper's input parameter (which is not used).
     * @return the produced value of type U.
     */
    default <T> Mapper<U, T> asMapper() {
        return t -> get();
    }

    /**
     * 
     * @param <T>
     * @param value
     * @return
     */
    static <T> Provider<T> of(final T value) {
        return () -> value;
    }

    /**
     * 
     * @param <T>
     * @param value
     * @return
     */
    static <T> Provider<T> ofNull() {
        return Fn::toNull;
    }

    @SuppressWarnings(SonarLintWarning.JAVA_S1172)
    static <T, U> Provider<T> ofNull(final U value) {
        return Fn::toNull;
    }
}
