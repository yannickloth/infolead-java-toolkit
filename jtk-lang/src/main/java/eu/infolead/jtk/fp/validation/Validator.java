package eu.infolead.jtk.fp.validation;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import eu.infolead.jtk.anomaly.ErrorType;
import eu.infolead.jtk.fp.either.Maybe;

/**
 * Utility class for creating and combining validators.
 * Provides a fluent API for building complex validation logic.
 */
public final class Validator {

    private Validator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates a validator builder for a specific field.
     *
     * @param <T>       the type of the value to validate
     * @param fieldPath the field path
     * @return a new ValidatorBuilder
     */
    public static <T> ValidatorBuilder<T> field(final String fieldPath) {
        return new ValidatorBuilder<>(fieldPath);
    }

    /**
     * Creates a validator for an object with multiple fields.
     *
     * @param <T>    the type of the object to validate
     * @param object the object to validate
     * @return a new ObjectValidator
     */
    public static <T> ObjectValidator<T> object(final T object) {
        return new ObjectValidator<>(object);
    }

    /**
     * Builder for creating field-level validators.
     *
     * @param <T> the value type
     */
    public static final class ValidatorBuilder<T> {
        private final String fieldPath;

        private ValidatorBuilder(final String fieldPath) {
            this.fieldPath = Objects.requireNonNull(fieldPath, "fieldPath");
        }

        /**
         * Validates that the value is not null.
         *
         * @param value the value to validate
         * @return a ValidationResult
         */
        public ValidationResult<T> required(final T value) {
            return value != null
                    ? ValidationResult.success(value)
                    : ValidationResult.failure(ValidationError.required(fieldPath));
        }

        /**
         * Validates that the value passes a custom predicate.
         *
         * @param value     the value to validate
         * @param predicate the validation predicate
         * @param errorType the error type if validation fails
         * @return a ValidationResult
         */
        public ValidationResult<T> check(final T value, final Predicate<T> predicate, final ErrorType errorType) {
            Objects.requireNonNull(value, "value must not be null; use required(...) to report a missing value");
            Objects.requireNonNull(predicate, "predicate");
            Objects.requireNonNull(errorType, "errorType");
            return predicate.test(value)
                    ? ValidationResult.success(value)
                    : ValidationResult.failure(new ValidationError(fieldPath, errorType,
                            new Object[0], Maybe.ofNullable(value)));
        }

        /**
         * Validates that the value passes a custom predicate with context args.
         *
         * @param value       the value to validate
         * @param predicate   the validation predicate
         * @param errorType   the error type if validation fails
         * @param contextArgs arguments for error message formatting
         * @return a ValidationResult
         */
        public ValidationResult<T> check(final T value, final Predicate<T> predicate, final ErrorType errorType,
                final Object... contextArgs) {
            Objects.requireNonNull(value, "value must not be null; use required(...) to report a missing value");
            Objects.requireNonNull(predicate, "predicate");
            Objects.requireNonNull(errorType, "errorType");
            return predicate.test(value)
                    ? ValidationResult.success(value)
                    : ValidationResult.failure(new ValidationError(fieldPath, errorType, contextArgs,
                            Maybe.ofNullable(value), Maybe.none()));
        }

        /**
         * Validates string length constraints.
         *
         * @param value     the string value
         * @param minLength minimum length
         * @param maxLength maximum length
         * @return a ValidationResult
         * @throws IllegalArgumentException if the bounds are inconsistent
         */
        public ValidationResult<String> lengthBetween(final String value, final int minLength, final int maxLength) {
            if (minLength < 0) {
                throw new IllegalArgumentException("minLength must be non-negative, was " + minLength);
            }
            if (maxLength < minLength) {
                throw new IllegalArgumentException("maxLength (" + maxLength
                        + ") must be greater than or equal to minLength (" + minLength + ")");
            }
            if (value == null) {
                return ValidationResult.failure(ValidationError.required(fieldPath));
            }
            if (value.length() < minLength) {
                return ValidationResult.failure(ValidationError.tooShort(fieldPath, value, minLength));
            }
            if (value.length() > maxLength) {
                return ValidationResult.failure(ValidationError.tooLong(fieldPath, value, maxLength));
            }
            return ValidationResult.success(value);
        }

        /**
         * Validates numeric range constraints.
         *
         * @param <N>   the numeric type
         * @param value the numeric value
         * @param min   minimum value
         * @param max   maximum value
         * @return a ValidationResult
         * @throws IllegalArgumentException if {@code min} is greater than {@code max}
         */
        public <N extends Comparable<N>> ValidationResult<N> rangeBetween(final N value, final N min, final N max) {
            Objects.requireNonNull(min, "min");
            Objects.requireNonNull(max, "max");
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("max (" + max
                        + ") must be greater than or equal to min (" + min + ")");
            }
            if (value == null) {
                return ValidationResult.failure(ValidationError.required(fieldPath));
            }
            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                return ValidationResult.failure(ValidationError.outOfRange(fieldPath, value, min, max));
            }
            return ValidationResult.success(value);
        }
    }

    /**
     * Builder for validating objects with multiple fields.
     *
     * @param <T> the object type
     */
    public static final class ObjectValidator<T> {
        private final T object;
        private ValidationResult<T> currentResult;

        private ObjectValidator(final T object) {
            this.object = Objects.requireNonNull(object, "object");
            this.currentResult = ValidationResult.success(object);
        }

        /**
         * Validates a field of the object.
         *
         * @param <F>            the field type
         * @param fieldExtractor function to extract the field value
         * @param fieldValidator function to validate the field
         * @return this ObjectValidator for method chaining
         */
        public <F> ObjectValidator<T> validate(final Function<T, F> fieldExtractor,
                final Function<F, ValidationResult<F>> fieldValidator) {
            Objects.requireNonNull(fieldExtractor, "fieldExtractor");
            Objects.requireNonNull(fieldValidator, "fieldValidator");
            if (currentResult.isSuccess().toBoolean()) {
                final F fieldValue = fieldExtractor.apply(object);
                final ValidationResult<F> fieldResult = Objects.requireNonNull(
                        fieldValidator.apply(fieldValue), "fieldValidator returned null");
                if (fieldResult.isFailure().toBoolean()) {
                    currentResult = ValidationResult.failure(fieldResult.getErrors());
                }
            }
            return this;
        }

        /**
         * Validates a field and accumulates errors even if previous validations
         * failed.
         *
         * @param <F>            the field type
         * @param fieldExtractor function to extract the field value
         * @param fieldValidator function to validate the field
         * @return this ObjectValidator for method chaining
         */
        public <F> ObjectValidator<T> validateAndAccumulate(final Function<T, F> fieldExtractor,
                final Function<F, ValidationResult<F>> fieldValidator) {
            Objects.requireNonNull(fieldExtractor, "fieldExtractor");
            Objects.requireNonNull(fieldValidator, "fieldValidator");
            final F fieldValue = fieldExtractor.apply(object);
            final ValidationResult<F> fieldResult = Objects.requireNonNull(
                    fieldValidator.apply(fieldValue), "fieldValidator returned null");

            if (fieldResult.isFailure().toBoolean()) {
                currentResult = currentResult.combine(fieldResult, t -> f -> t);
            }
            return this;
        }

        /**
         * Returns the final validation result.
         *
         * @return the ValidationResult for the entire object
         */
        public ValidationResult<T> result() {
            return currentResult;
        }
    }
}
