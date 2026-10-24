package eu.infolead.jtk.fp;

import java.util.Objects;
import java.util.function.Predicate;

import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;

/**
 * A type that can test itself against a predicate.
 *
 * @param <T> the concrete self type
 */
public interface Testable<T extends Testable<T>> {
    /**
     * Tests this instance against the predicate.
     *
     * @param predicate the predicate to apply
     * @return {@link Bool#TRUE} when the predicate matches, {@link Bool#FALSE}
     *         otherwise
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default Bool test(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return Bool.of(predicate.test((T) this));
    }
}
