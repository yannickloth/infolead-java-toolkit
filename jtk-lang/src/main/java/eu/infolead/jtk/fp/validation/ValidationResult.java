package eu.infolead.jtk.fp.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Validation;
import eu.infolead.jtk.logic.Bool;

/**
 * A validation result that can accumulate multiple validation errors.
 * Unlike the basic Validation interface which short-circuits on first error,
 * ValidationResult collects all validation errors for comprehensive reporting.
 * 
 * @param <T> the type of the valid value
 */
public sealed interface ValidationResult<T> permits ValidationResult.Success, ValidationResult.Failure {

    /**
     * Creates a successful validation result.
     * 
     * @param <T> the type of the valid value
     * @param value the valid value
     * @return a successful ValidationResult
     */
    static <T> ValidationResult<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed validation result with a single error.
     * 
     * @param <T> the type that would be valid
     * @param error the validation error
     * @return a failed ValidationResult
     */
    static <T> ValidationResult<T> failure(ValidationError error) {
        return new Failure<>(List.of(error));
    }

    /**
     * Creates a failed validation result with multiple errors.
     * 
     * @param <T> the type that would be valid
     * @param errors the validation errors
     * @return a failed ValidationResult
     */
    static <T> ValidationResult<T> failure(List<ValidationError> errors) {
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("Cannot create failure with empty error list");
        }
        return new Failure<>(new ArrayList<>(errors));
    }

    /**
     * Creates a failed validation result with multiple errors.
     * 
     * @param <T> the type that would be valid
     * @param errors the validation errors
     * @return a failed ValidationResult
     */
    @SafeVarargs
    static <T> ValidationResult<T> failure(ValidationError... errors) {
        return failure(Arrays.asList(errors));
    }

    /**
     * Converts a simple Validation to a ValidationResult.
     * 
     * @param <T> the type of the valid value
     * @param validation the validation to convert
     * @param errorMapper function to convert the error to ValidationError
     * @return a ValidationResult
     */
    static <E, T> ValidationResult<T> from(Validation<E, T> validation, 
                                          Function<E, ValidationError> errorMapper) {
        return validation.fold(
            error -> failure(errorMapper.apply(error)),
            ValidationResult::success
        );
    }

    /**
     * Checks if this result is successful.
     * 
     * @return true if successful, false otherwise
     */
    Bool isSuccess();

    /**
     * Checks if this result is a failure.
     * 
     * @return true if failure, false otherwise
     */
    default Bool isFailure() {
        return isSuccess().negate();
    }

    /**
     * Returns the valid value if successful, or null if failed.
     * 
     * @return the valid value or null
     */
    Maybe<T> getValue();

    /**
     * Returns the validation errors if failed, or empty list if successful.
     * 
     * @return list of validation errors
     */
    List<ValidationError> getErrors();

    /**
     * Transforms the value if successful, preserving errors if failed.
     * 
     * @param <U> the new value type
     * @param mapper the transformation function
     * @return a new ValidationResult with transformed value
     */
    <U> ValidationResult<U> map(Function<T, U> mapper);

    /**
     * Applies a validation function that may add more errors.
     * This allows chaining validations while accumulating all errors.
     * 
     * @param <U> the new value type
     * @param validator the validation function
     * @return a new ValidationResult with accumulated errors
     */
    <U> ValidationResult<U> flatMap(Function<T, ValidationResult<U>> validator);

    /**
     * Combines this result with another, accumulating both values and errors.
     * If both are successful, combines values using the combiner function.
     * If either fails, accumulates all errors.
     * 
     * @param <U> the type of the other value
     * @param <R> the type of the combined result
     * @param other the other validation result
     * @param combiner function to combine successful values
     * @return combined ValidationResult
     */
    <U, R> ValidationResult<R> combine(ValidationResult<U> other, 
                                      Function<T, Function<U, R>> combiner);

    /**
     * Applies additional validation if this result is successful.
     * 
     * @param validator the additional validation
     * @return this result with additional validation applied
     */
    ValidationResult<T> validate(Function<T, ValidationResult<T>> validator);

    /**
     * Applies a predicate validation with custom error.
     * 
     * @param predicate the validation predicate
     * @param error the error to add if predicate fails
     * @return this result with predicate validation applied
     */
    ValidationResult<T> validateThat(Predicate<T> predicate, ValidationError error);

    /**
     * Converts to a simple Either with first error or aggregated error message.
     * 
     * @return Either with error string or valid value
     */
    Either<String, T> toEither();

    /**
     * Converts to a basic Validation with first error.
     * 
     * @return Validation with first error or valid value
     */
    Validation<ValidationError, T> toValidation();

    /**
     * Converts validation errors to a ProblemDetail for HTTP API responses.
     * 
     * @param problemType the problem type URI
     * @param title the problem title
     * @param status the HTTP status code
     * @return a ProblemDetail representing the validation errors
     */
    default ProblemDetail toProblemDetail(java.net.URI problemType, String title, Integer status) {
        if (isSuccess().toBoolean()) {
            throw new IllegalStateException("Cannot convert successful result to ProblemDetail");
        }
        
        List<ValidationError> errors = getErrors();
        String detail = errors.stream()
            .map(ValidationError::format)
            .collect(java.util.stream.Collectors.joining("; "));
            
        return StandardProblemDetail.builder(problemType)
            .title(title)
            .status(status)
            .detail(detail)
            .build();
    }

    /**
     * Converts validation errors to a ProblemDetail with default validation error type.
     * 
     * @param status the HTTP status code (typically 400 for validation errors)
     * @return a ProblemDetail representing the validation errors
     */
    default ProblemDetail toProblemDetail(Integer status) {
        return toProblemDetail(
            java.net.URI.create("https://tools.ietf.org/html/rfc7231#section-6.5.1"),
            "Validation Error", 
            status
        );
    }

    /**
     * Converts validation errors to a ProblemDetail with 400 Bad Request status.
     * 
     * @return a ProblemDetail representing the validation errors
     */
    default ProblemDetail toProblemDetail() {
        return toProblemDetail(400);
    }

    /**
     * Converts validation errors to a ProblemDetail using the first error's type information.
     * This preserves the HTTP status and problem type from the ErrorType enum.
     * 
     * @return a ProblemDetail representing the validation errors
     */
    default ProblemDetail toProblemDetailFromErrorType() {
        if (isSuccess().toBoolean()) {
            throw new IllegalStateException("Cannot convert successful result to ProblemDetail");
        }
        
        List<ValidationError> errors = getErrors();
        if (errors.isEmpty()) {
            throw new IllegalStateException("Cannot convert empty error list to ProblemDetail");
        }
        
        ValidationError firstError = errors.get(0);
        String detail = errors.stream()
            .map(ValidationError::format)
            .collect(java.util.stream.Collectors.joining("; "));
            
        return firstError.errorType().toProblemDetail(detail);
    }

    /**
     * Groups validation errors by their HTTP status codes.
     * Useful when multiple error types with different status codes are present.
     * 
     * @return a map of HTTP status codes to lists of validation errors
     */
    default java.util.Map<Integer, List<ValidationError>> groupByHttpStatus() {
        return getErrors().stream()
            .collect(java.util.stream.Collectors.groupingBy(ValidationError::getHttpStatus));
    }

    /**
     * Checks if all validation errors are client errors (4xx status codes).
     * 
     * @return true if all errors are client errors, false otherwise
     */
    default boolean areAllClientErrors() {
        return getErrors().stream()
            .allMatch(error -> error.errorType().isClientError());
    }

    /**
     * Checks if any validation errors are server errors (5xx status codes).
     * 
     * @return true if any error is a server error, false otherwise
     */
    default boolean hasServerErrors() {
        return getErrors().stream()
            .anyMatch(error -> error.errorType().isServerError());
    }

    /**
     * Successful validation result.
     */
    record Success<T>(T value) implements ValidationResult<T> {
        public Success {
            Objects.requireNonNull(value, "value cannot be null");
        }

        @Override
        public Bool isSuccess() {
            return Bool.TRUE;
        }

        @Override
        public Maybe<T> getValue() {
            return Maybe.of(value);
        }

        @Override
        public List<ValidationError> getErrors() {
            return List.of();
        }

        @Override
        public <U> ValidationResult<U> map(Function<T, U> mapper) {
            return success(mapper.apply(value));
        }

        @Override
        public <U> ValidationResult<U> flatMap(Function<T, ValidationResult<U>> validator) {
            return validator.apply(value);
        }

        @Override
        public <U, R> ValidationResult<R> combine(ValidationResult<U> other, 
                                                 Function<T, Function<U, R>> combiner) {
            return other.map(otherValue -> combiner.apply(value).apply(otherValue));
        }

        @Override
        public ValidationResult<T> validate(Function<T, ValidationResult<T>> validator) {
            return validator.apply(value);
        }

        @Override
        public ValidationResult<T> validateThat(Predicate<T> predicate, ValidationError error) {
            return predicate.test(value) ? this : failure(error);
        }

        @Override
        public Either<String, T> toEither() {
            return Either.right(value);
        }

        @Override
        public Validation<ValidationError, T> toValidation() {
            return Validation.valid(value);
        }
    }

    /**
     * Failed validation result with accumulated errors.
     */
    record Failure<T>(List<ValidationError> errors) implements ValidationResult<T> {
        public Failure {
            Objects.requireNonNull(errors, "errors cannot be null");
            if (errors.isEmpty()) {
                throw new IllegalArgumentException("errors cannot be empty");
            }
            errors = new ArrayList<>(errors); // defensive copy
        }

        @Override
        public Bool isSuccess() {
            return Bool.FALSE;
        }

        @Override
        public Maybe<T> getValue() {
            return Maybe.none();
        }

        @Override
        public List<ValidationError> getErrors() {
            return new ArrayList<>(errors); // defensive copy
        }

        @Override
        public <U> ValidationResult<U> map(Function<T, U> mapper) {
            return new Failure<>(errors);
        }

        @Override
        public <U> ValidationResult<U> flatMap(Function<T, ValidationResult<U>> validator) {
            return new Failure<>(errors);
        }

        @Override
        public <U, R> ValidationResult<R> combine(ValidationResult<U> other, 
                                                 Function<T, Function<U, R>> combiner) {
            List<ValidationError> combinedErrors = new ArrayList<>(errors);
            combinedErrors.addAll(other.getErrors());
            return new Failure<>(combinedErrors);
        }

        @Override
        public ValidationResult<T> validate(Function<T, ValidationResult<T>> validator) {
            return this;
        }

        @Override
        public ValidationResult<T> validateThat(Predicate<T> predicate, ValidationError error) {
            return this;
        }

        @Override
        public Either<String, T> toEither() {
            String errorMessage = errors.stream()
                .map(ValidationError::format)
                .collect(Collectors.joining("; "));
            return Either.left(errorMessage);
        }

        @Override
        public Validation<ValidationError, T> toValidation() {
            return Validation.invalid(errors.get(0)); // Return first error
        }
    }

    /**
     * Combines multiple ValidationResults, accumulating all errors.
     * 
     * @param <T> the type of the results
     * @param results the results to combine
     * @return a list of all successful values, or accumulated errors
     */
    static <T> ValidationResult<List<T>> sequence(Collection<ValidationResult<T>> results) {
        List<ValidationError> allErrors = new ArrayList<>();
        List<T> allValues = new ArrayList<>();

        for (ValidationResult<T> result : results) {
            if (result.isSuccess().toBoolean()) {
                result.getValue().apply(() -> {}, allValues::add);
            } else {
                allErrors.addAll(result.getErrors());
            }
        }

        return allErrors.isEmpty() ? success(allValues) : failure(allErrors);
    }

    /**
     * Validates all items in a collection, accumulating errors.
     * 
     * @param <T> the item type
     * @param <U> the validated type
     * @param items the items to validate
     * @param validator the validation function
     * @return ValidationResult with all validated items or accumulated errors
     */
    static <T, U> ValidationResult<List<U>> validateAll(Collection<T> items, 
                                                       Function<T, ValidationResult<U>> validator) {
        List<ValidationResult<U>> results = items.stream()
            .map(validator)
            .collect(Collectors.toList());
        return sequence(results);
    }
}