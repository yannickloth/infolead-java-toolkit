package eu.infolead.jtk.fp;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

record Some<T>(T value) implements Maybe<T>, Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * Constructor.
     * 
     * @param value the value.
     * @throws NullPointerException if {@code value==null}.
     */
    Some(final T value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public <U> U fold(final Supplier<? extends U> emptyMapper,
            final Mapper<? extends U, ? super T> presentMapper) {
        return presentMapper.map(value);
    }

    @Override
    public Maybe<T> apply(final Runnable emptyAction, final Consumer<? super T> presentConsumer) {
        presentConsumer.accept(value);
        return this;
    }

    @Override
    public Bool isPresent() {
        return Bool.TRUE;
    }

    @Override
    public Bool isEmpty() {
        return isPresent().negate();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(value, ((Some<?>) obj).value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
