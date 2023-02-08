package eu.infolead.jtk.fp;

import java.util.function.Predicate;

public interface Testable<T extends Testable<T>> {
    @SuppressWarnings("unchecked")
    default Bool test(final Predicate<T> p) {
        return Bool.of(p.test((T) this));
    }
}
