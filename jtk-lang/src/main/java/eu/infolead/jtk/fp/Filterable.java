package eu.infolead.jtk.fp;

import java.util.function.Predicate;
import java.util.stream.Stream;

import eu.infolead.jtk.lang.CompilerWarning;

public interface Filterable<F extends Filterable<F>> {
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default Stream<F> filter(final Predicate<F> p) {
        return Stream.of((F) this).filter(p);
    }
}
