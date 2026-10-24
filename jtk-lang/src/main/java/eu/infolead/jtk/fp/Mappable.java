package eu.infolead.jtk.fp;

import java.util.Objects;

import eu.infolead.jtk.lang.CompilerWarning;

/**
 * A type that can map itself to a value of another type using a {@link Mapper}.
 *
 * @param <T> the concrete self type
 */
public interface Mappable<T extends Mappable<T>> {
    /**
     * Applies the mapper to this instance.
     *
     * @param <R>    the result type
     * @param mapper the mapper to apply
     * @return the mapped value
     */
    default <R> R map(final Mapper<R, ? super T> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return mapper.map(self());
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    private T self() {
        return (T) this;
    }
}
