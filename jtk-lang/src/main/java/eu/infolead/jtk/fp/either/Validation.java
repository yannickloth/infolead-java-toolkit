package eu.infolead.jtk.fp.either;

import eu.infolead.jtk.logic.Bool;

public sealed interface Validation<I, V> extends Either<I, V> permits Validation.Valid, Validation.Invalid {
    static <E, S> Validation<E, S> valid(final S value) {
        return new Valid<>(value);
    }

    static <E, S> Validation<E, S> invalid(final E error) {
        return new Invalid<>(error);
    }

    default Bool isValid() {
        return isRight();
    }

    default Bool isInvalid() {
        return isLeft();
    }

    final class Invalid<E, T> extends AbstractLeft<E, T> implements Validation<E, T> {
        public Invalid(final E value) {
            super(value);
        }

        @Override
        public <X extends Either<T, E>> X swap() {
            throw new UnsupportedOperationException("Swapping a Validation does not make sense.");
        }

        @Override
        public String toString() {
            return value().toString();
        }
    }

    final class Valid<E, T> extends AbstractRight<E, T> implements Validation<E, T> {
        public Valid(final T value) {
            super(value);
        }

        @Override
        public <X extends Either<T, E>> X swap() {
            throw new UnsupportedOperationException("Swapping a Validation does not make sense.");
        }

        @Override
        public String toString() {
            return value().toString();
        }
    }
}
