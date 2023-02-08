package eu.infolead.jtk.fp;

import java.util.function.Predicate;

import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;

public interface Testable<T extends Testable<T>> {
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default Bool test(final Predicate<T> p) {
        return Bool.of(p.test((T) this));
    }
}
