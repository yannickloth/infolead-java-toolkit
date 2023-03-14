package eu.infolead.jtk.fp;

import eu.infolead.jtk.lang.CompilerWarning;

public interface Mappable<T> {
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <R> R map(final Mapper<R, T> mapper) {
        return mapper.map((T) this);
    }
}
