package eu.infolead.jtk.fp;

import java.util.function.Predicate;

public interface Testable<T> {
    default boolean test(final Predicate<T> p){
        return p.test((T)this);
    }
}
