package eu.infolead.jtk.fp.either;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import eu.infolead.jtk.logic.Bool;

/**
 * A validation result that is either {@link Valid} or {@link Invalid}.
 * <p>
 * Unlike {@link Result}, a validation is not necessarily tied to an anomaly
 * type; callers choose the invalid value type.
 *
 * @param <I> the type of the invalid value
 * @param <V> the type of the valid value
 */
public sealed interface Validation<I, V> extends Either<I, V> permits Validation.Valid, Validation.Invalid {
    /**
     * Creates a valid validation.
     *
     * @param <E>   the invalid type
     * @param <S>   the valid type
     * @param value the valid value
     * @return a valid validation
     * @throws NullPointerException if {@code value} is {@code null}
     */
    static <E, S> Validation<E, S> valid(final S value) {
        return new Valid<>(Objects.requireNonNull(value, "value"));
    }

    /**
     * Creates an invalid validation.
     *
     * @param <E>   the invalid type
     * @param <S>   the valid type
     * @param error the invalid value
     * @return an invalid validation
     * @throws NullPointerException if {@code error} is {@code null}
     */
    static <E, S> Validation<E, S> invalid(final E error) {
        return new Invalid<>(Objects.requireNonNull(error, "error"));
    }

    default Bool isValid() {
        return isRight();
    }

    default Bool isInvalid() {
        return isLeft();
    }

    /**
     * Converts this Validation to a ValidationResult for error accumulation.
     *
     * @param errorMapper function to convert the invalid value to
     *                    {@link eu.infolead.jtk.fp.validation.ValidationError}
     * @return a {@link eu.infolead.jtk.fp.validation.ValidationResult}
     */
    default eu.infolead.jtk.fp.validation.ValidationResult<V> toValidationResult(
            final Function<I, eu.infolead.jtk.fp.validation.ValidationError> errorMapper) {
        Objects.requireNonNull(errorMapper, "errorMapper");
        return fold(
                invalid -> eu.infolead.jtk.fp.validation.ValidationResult.failure(errorMapper.apply(invalid)),
                eu.infolead.jtk.fp.validation.ValidationResult::success);
    }

    /**
     * Creates a validation that checks a predicate.
     *
     * @param <T>       the value type
     * @param value     the value to validate
     * @param predicate the validation predicate
     * @param error     the error if predicate fails
     * @return a Validation result
     * @throws NullPointerException if {@code value} is {@code null}; use
     *                              {@link #required(String, Object)} to report a
     *                              missing value instead
     */
    static <T> Validation<eu.infolead.jtk.fp.validation.ValidationError, T> check(final T value,
            final Predicate<T> predicate, final eu.infolead.jtk.fp.validation.ValidationError error) {
        Objects.requireNonNull(value, "value must not be null; a Validation cannot hold a null valid value");
        Objects.requireNonNull(predicate, "predicate");
        Objects.requireNonNull(error, "error");
        return predicate.test(value) ? valid(value) : invalid(error);
    }

    /**
     * Creates a validation for a required (non-null) value.
     *
     * @param <T>       the value type
     * @param fieldPath the field path
     * @param value     the value to check
     * @return a Validation result
     */
    static <T> Validation<eu.infolead.jtk.fp.validation.ValidationError, T> required(final String fieldPath,
            final T value) {
        Objects.requireNonNull(fieldPath, "fieldPath");
        return value != null ? valid(value)
                : invalid(eu.infolead.jtk.fp.validation.ValidationError.required(fieldPath));
    }

    /**
     * Creates a validation for string length constraints.
     *
     * @param fieldPath the field path
     * @param value     the string value
     * @param minLength minimum length (inclusive)
     * @param maxLength maximum length (inclusive)
     * @return a Validation result
     * @throws IllegalArgumentException if {@code minLength} is negative or
     *                                  {@code maxLength} is smaller than
     *                                  {@code minLength}
     */
    static Validation<eu.infolead.jtk.fp.validation.ValidationError, String> lengthBetween(final String fieldPath,
            final String value, final int minLength, final int maxLength) {
        Objects.requireNonNull(fieldPath, "fieldPath");
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be non-negative, was " + minLength);
        }
        if (maxLength < minLength) {
            throw new IllegalArgumentException(
                    "maxLength (" + maxLength + ") must be greater than or equal to minLength (" + minLength + ")");
        }
        if (value == null) {
            return invalid(eu.infolead.jtk.fp.validation.ValidationError.required(fieldPath));
        }
        if (value.length() < minLength) {
            return invalid(eu.infolead.jtk.fp.validation.ValidationError.tooShort(fieldPath, value, minLength));
        }
        if (value.length() > maxLength) {
            return invalid(eu.infolead.jtk.fp.validation.ValidationError.tooLong(fieldPath, value, maxLength));
        }
        return valid(value);
    }

    /**
     * Creates a validation for numeric range constraints.
     *
     * @param <T>       the numeric type
     * @param fieldPath the field path
     * @param value     the numeric value
     * @param min       minimum value (inclusive)
     * @param max       maximum value (inclusive)
     * @return a Validation result
     * @throws IllegalArgumentException if {@code max} is smaller than {@code min}
     */
    static <T extends Comparable<T>> Validation<eu.infolead.jtk.fp.validation.ValidationError, T> rangeBetween(
            final String fieldPath, final T value, final T min, final T max) {
        Objects.requireNonNull(fieldPath, "fieldPath");
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("max (" + max + ") must be greater than or equal to min (" + min + ")");
        }
        if (value == null) {
            return invalid(eu.infolead.jtk.fp.validation.ValidationError.required(fieldPath));
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            return invalid(eu.infolead.jtk.fp.validation.ValidationError.outOfRange(fieldPath, value, min, max));
        }
        return valid(value);
    }

    final class Invalid<E, T> extends AbstractLeft<E, T> implements Validation<E, T> {
        private static final long serialVersionUID = 1L;

        public Invalid(final E value) {
            super(Objects.requireNonNull(value, "error"));
        }

        private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
            input.defaultReadObject();
            Objects.requireNonNull(value(), "deserialized Invalid error must not be null");
        }

        @Override
        public <X extends Either<T, E>> X swap() {
            throw new UnsupportedOperationException("Swapping a Validation does not make sense.");
        }
    }

    final class Valid<E, T> extends AbstractRight<E, T> implements Validation<E, T> {
        private static final long serialVersionUID = 1L;

        public Valid(final T value) {
            super(Objects.requireNonNull(value, "value"));
        }

        private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
            input.defaultReadObject();
            Objects.requireNonNull(value(), "deserialized Valid value must not be null");
        }

        @Override
        public <X extends Either<T, E>> X swap() {
            throw new UnsupportedOperationException("Swapping a Validation does not make sense.");
        }
    }
}
