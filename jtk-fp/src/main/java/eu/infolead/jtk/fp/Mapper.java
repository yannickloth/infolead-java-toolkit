package eu.infolead.jtk.fp;

@FunctionalInterface
public interface Mapper<R, T> {
    public R map(T object);
}
