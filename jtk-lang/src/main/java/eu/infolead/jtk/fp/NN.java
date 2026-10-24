package eu.infolead.jtk.fp;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Result;

/**
 * This class explicitly indicates that any variable, method argument or
 * returned value of this type must not be {@code null}. The value it wraps is
 * never {@code null}.
 * <p>
 * "NN" stands for "not null", or "never null".
 * </p>
 *
 * <p>
 * In Java, by default, any reference may be {@code null}. Using this container
 * class communicates the intent of the developer to never manipulate a
 * reference that may be {@code null}.
 *
 * <p>
 * To rephrase: the reference to an {@link NN} MUST NOT (ever) be {@code null}
 * and the contained value MUST NOT (ever) be {@code null}.
 * </p>
 *
 * @param <T> the type of the contained value
 */
public final class NN<T> implements Filterable<NN<T>>, Mappable<NN<T>>, Testable<NN<T>> {
    private final T value;

    private NN(final T value) {
        this.value = Objects.requireNonNull(value, "The specified value must not be null.");
    }

    /**
     * Wraps the specified value, or returns a failed result explaining why the
     * value is not acceptable.
     *
     * @param <V>   the value type
     * @param value the value to wrap
     * @return a successful result holding the non-null value, or a failure
     *         carrying a diagnostic message
     */
    public static <V> Result<String, NN<V>> of(final V value) {
        if (value == null) {
            return Result.failure("The specified value must not be null.");
        }
        return Result.success(new NN<>(value));
    }

    public T get() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NN<?> nn = (NN<?>) o;
        return value.equals(nn.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
