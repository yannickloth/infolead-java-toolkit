package eu.infolead.jtk.fp.either;

import java.io.Serializable;
import java.util.Objects;

/**
 * Common superclass holding the value of an {@link Either} variant.
 * <p>
 * The class is {@link Serializable} so that all variants (including the ones
 * defined inside {@link Maybe}, {@link Result} and {@link Validation}) keep their
 * payload when serialized. The no-argument constructor is kept for
 * deserialization and for the serializable-subclass contract described by
 * <a target="_blank" href=
 * "https://rules.sonarsource.com/java/RSPEC-2055">RSPEC-2055</a>.
 * <p>
 * The default {@link #toString()} includes the carried value. Do not log values
 * that may contain secrets; variants such as {@code Maybe.Some} override this
 * to avoid exposing their payload.
 *
 * @param <T> the type of the contained value
 */
sealed abstract class ValueContainer<T> implements Serializable permits AbstractEither {
    private static final long serialVersionUID = 1L;

    /**
     * Not final on purpose: the value is restored by the serialization
     * mechanism.
     */
    private T value;

    ValueContainer() {
        // do nothing, necessary for deserialization of Serializable subclasses
    }

    ValueContainer(final T value) {
        this.value = value;
    }

    public final T value() {
        return value;
    }

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return Objects.equals(value, ((ValueContainer<?>) other).value);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + String.valueOf(value) + ")";
    }
}
