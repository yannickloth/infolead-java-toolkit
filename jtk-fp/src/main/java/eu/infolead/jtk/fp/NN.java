package eu.infolead.jtk.fp;

import java.util.Objects;

/**
 * This class explicitly indicates that any variable, method argument or returned value of this type must not be {@code null}. The value it wraps is never {@code null}.
 * <p>"NN" stands for "not null", or "never null".</p>
 *
 * <p>In Java, by default, any reference may be {@code null}. Using this container class, the intent of the developer to never manipulate a reference that may be {@code null}.</p>
 *
 * @param <T> the type of the contained value
 */
public final class NN<T> {
    private final T value;

    private NN(final T value) {
        this.value = Objects.requireNonNull(value, "The specified value must not be null.");
    }

    public static <T> Result<NN<T>> of(final T value) {
        try {
            return R.success(new NN<>(value));
        } catch (final NullPointerException npe) {
            return R.failure();
        }
    }

    public T get() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final NN<?> nn = (NN<?>) o;
        if (value == nn.value) return true;
        if (value.getClass() != nn.value.getClass()) return false;
        return value.equals(nn.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
