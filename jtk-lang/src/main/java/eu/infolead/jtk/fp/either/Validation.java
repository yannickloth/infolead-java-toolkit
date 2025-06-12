package eu.infolead.jtk.fp.either;

import java.util.function.Function;
import java.util.function.Predicate;
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

    /**
     * Converts this Validation to a ValidationResult for error accumulation.
     * 
     * @param errorMapper function to convert the invalid value to eu.infolead.jtk.fp.validation.ValidationError
     * @return a eu.infolead.jtk.fp.validation.ValidationResult
     */
    default eu.infolead.jtk.fp.validation.ValidationResult<V> toValidationResult(Function<I, eu.infolead.jtk.fp.validation.ValidationError> errorMapper) {
        return fold(
            invalid -> eu.infolead.jtk.fp.validation.ValidationResult.failure(errorMapper.apply(invalid)),
            eu.infolead.jtk.fp.validation.ValidationResult::success
        );
    }

    /**
     * Creates a validation that checks a predicate.
     * 
     * @param <T> the value type
     * @param value the value to validate
     * @param predicate the validation predicate
     * @param error the error if predicate fails
     * @return a Validation result
     */
    static <T> Validation<eu.infolead.jtk.fp.validation.ValidationError, T> check(T value, Predicate<T> predicate, eu.infolead.jtk.fp.validation.ValidationError error) {
        return predicate.test(value) ? valid(value) : invalid(error);
    }

    /**
     * Creates a validation for a required (non-null) value.
     * 
     * @param <T> the value type
     * @param fieldPath the field path
     * @param value the value to check
     * @return a Validation result
     */
    static <T> Validation<eu.infolead.jtk.fp.validation.ValidationError, T> required(String fieldPath, T value) {
        return value != null ? valid(value) : invalid(eu.infolead.jtk.fp.validation.ValidationError.required(fieldPath));
    }

    /**
     * Creates a validation for string length constraints.
     * 
     * @param fieldPath the field path
     * @param value the string value
     * @param minLength minimum length (inclusive)
     * @param maxLength maximum length (inclusive)
     * @return a Validation result
     */
    static Validation<eu.infolead.jtk.fp.validation.ValidationError, String> lengthBetween(String fieldPath, String value, 
                                                            int minLength, int maxLength) {
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
     * @param <T> the numeric type
     * @param fieldPath the field path
     * @param value the numeric value
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return a Validation result
     */
    static <T extends Comparable<T>> Validation<eu.infolead.jtk.fp.validation.ValidationError, T> rangeBetween(String fieldPath, T value, 
                                                                                T min, T max) {
        if (value == null) {
            return invalid(eu.infolead.jtk.fp.validation.ValidationError.required(fieldPath));
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            return invalid(eu.infolead.jtk.fp.validation.ValidationError.outOfRange(fieldPath, value, min, max));
        }
        return valid(value);
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
