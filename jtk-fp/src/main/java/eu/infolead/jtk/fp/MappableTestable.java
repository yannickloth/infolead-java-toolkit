package eu.infolead.jtk.fp;

public interface MappableTestable<T extends Mappable<T> & Testable<T>> extends Mappable<T>, Testable<T> {
}
