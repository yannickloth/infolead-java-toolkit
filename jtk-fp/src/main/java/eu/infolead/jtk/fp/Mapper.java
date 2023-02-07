package eu.infolead.jtk.fp;

@FunctionalInterface
public interface Mapper<U, T> {
    U map(T object);
}
