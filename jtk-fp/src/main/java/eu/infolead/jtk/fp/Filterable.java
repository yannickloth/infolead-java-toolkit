package eu.infolead.jtk.fp;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Filterable<T extends Filterable<T>>  {
    default Stream<Filterable<T>> filter(Predicate<Filterable<T>> p) {
        return Stream.of(this).filter(p);
    }
}
