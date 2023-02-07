package eu.infolead.jtk.fp;

public interface Mappable<T extends Mappable<T>> {
    @SuppressWarnings("unchecked")
    default <R> R map(final Mapper<R, T> mapper) {
        return mapper.map((T) this);
    }
}
