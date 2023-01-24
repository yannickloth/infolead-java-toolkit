package eu.infolead.jtk.fp;

public interface Mappable<T> {
    default R map(final Mapper<R, T> mapper) {
        return mapper.map((T) this);
    }
}
