package eu.infolead.jtk.fp.validation;

import java.util.function.Function;
import java.util.function.Predicate;

import eu.infolead.jtk.anomaly.ErrorType;

/**
 * Utility class for creating and combining validators.
 * Provides a fluent API for building complex validation logic.
 */
public final class Validator {

    private Validator() {
        // Utility class
    }

    /**
     * Creates a validator builder for a specific field.
     * 
     * @param <T> the type of the value to validate
     * @param fieldPath the field path
     * @return a new ValidatorBuilder
     */
    public static <T> ValidatorBuilder<T> field(String fieldPath) {
        return new ValidatorBuilder<>(fieldPath);
    }

    /**
     * Creates a validator for an object with multiple fields.
     * 
     * @param <T> the type of the object to validate
     * @param object the object to validate
     * @return a new ObjectValidator
     */
    public static <T> ObjectValidator<T> object(T object) {
        return new ObjectValidator<>(object);
    }

    /**
     * Builder for creating field-level validators.
     */
    public static final class ValidatorBuilder<T> {
        private final String fieldPath;

        private ValidatorBuilder(String fieldPath) {
            this.fieldPath = fieldPath;
        }

        /**
         * Validates that the value is not null.
         * 
         * @param value the value to validate
         * @return a ValidationResult
         */
        public ValidationResult<T> required(T value) {
            return value != null 
                ? ValidationResult.success(value)
                : ValidationResult.failure(ValidationError.required(fieldPath));
        }

        /**
         * Validates that the value passes a custom predicate.
         * 
         * @param value the value to validate
         * @param predicate the validation predicate
         * @param errorType the error type if validation fails
         * @return a ValidationResult
         */
        public ValidationResult<T> check(T value, Predicate<T> predicate, ErrorType errorType) {
            return predicate.test(value)
                ? ValidationResult.success(value)
                : ValidationResult.failure(new ValidationError(fieldPath, errorType, value));
        }

        /**
         * Validates that the value passes a custom predicate with context args.
         * 
         * @param value the value to validate
         * @param predicate the validation predicate
         * @param errorType the error type if validation fails
         * @param contextArgs arguments for error message formatting
         * @return a ValidationResult
         */
        public ValidationResult<T> check(T value, Predicate<T> predicate, ErrorType errorType, Object... contextArgs) {
            return predicate.test(value)
                ? ValidationResult.success(value)
                : ValidationResult.failure(new ValidationError(fieldPath, errorType, contextArgs, 
                    eu.infolead.jtk.fp.either.Maybe.ofNullable(value)));
        }

        /**
         * Validates string length constraints.
         * 
         * @param value the string value
         * @param minLength minimum length
         * @param maxLength maximum length
         * @return a ValidationResult
         */
        public ValidationResult<String> lengthBetween(String value, int minLength, int maxLength) {
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
         * @param <N> the numeric type
         * @param value the numeric value
         * @param min minimum value
         * @param max maximum value
         * @return a ValidationResult
         */
        public <N extends Comparable<N>> ValidationResult<N> rangeBetween(N value, N min, N max) {
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
     */
    public static final class ObjectValidator<T> {
        private final T object;
        private ValidationResult<T> currentResult;

        private ObjectValidator(T object) {
            this.object = object;
            this.currentResult = ValidationResult.success(object);
        }

        /**
         * Validates a field of the object.
         * 
         * @param <F> the field type
         * @param fieldExtractor function to extract the field value
         * @param fieldValidator function to validate the field
         * @return this ObjectValidator for method chaining
         */
        public <F> ObjectValidator<T> validate(Function<T, F> fieldExtractor, 
                                              Function<F, ValidationResult<F>> fieldValidator) {
            if (currentResult.isSuccess().toBoolean()) {
                F fieldValue = fieldExtractor.apply(object);
                ValidationResult<F> fieldResult = fieldValidator.apply(fieldValue);
                if (fieldResult.isFailure().toBoolean()) {
                    currentResult = ValidationResult.failure(fieldResult.getErrors());
                }
            }
            return this;
        }

        /**
         * Validates a field and accumulates errors even if previous validations failed.
         * 
         * @param <F> the field type
         * @param fieldExtractor function to extract the field value
         * @param fieldValidator function to validate the field
         * @return this ObjectValidator for method chaining
         */
        public <F> ObjectValidator<T> validateAndAccumulate(Function<T, F> fieldExtractor, 
                                                           Function<F, ValidationResult<F>> fieldValidator) {
            F fieldValue = fieldExtractor.apply(object);
            ValidationResult<F> fieldResult = fieldValidator.apply(fieldValue);
            
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