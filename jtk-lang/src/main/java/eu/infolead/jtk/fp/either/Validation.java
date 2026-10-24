package eu.infolead.jtk.fp.either;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.validation.ValidationError;
import eu.infolead.jtk.fp.validation.ValidationResult;
import eu.infolead.jtk.logic.Bool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A validation result that is either {@link Valid} or {@link Invalid}.
 *
 * <p>Unlike {@link Result}, a validation is not necessarily tied to an anomaly type; callers choose
 * the invalid value type.
 *
 * @param <I> the type of the invalid value
 * @param <V> the type of the valid value
 */
public sealed interface Validation<I, V> extends Either<I, V>
        permits Validation.Valid, Validation.Invalid {

    /**
     * Reads the {@code left}/{@code right} JSON envelope.
     *
     * <p>An invalid validation is written as {@code {"left": error}}; a valid one as {@code
     * {"right": value}}. Both payloads are required to be non-null, so an envelope without a branch
     * is rejected instead of being guessed.
     *
     * @param <I> the invalid-value type
     * @param <V> the valid-value type
     * @param invalid the invalid value, or {@code null} for a valid validation
     * @param valid the valid value, or {@code null} for an invalid validation
     * @return the envelope as a {@code Validation}
     * @throws IllegalArgumentException if no branch, or both branches, are present
     */
    @JsonCreator
    static <I, V> Validation<I, V> fromJson(
            @JsonProperty("left") final I invalid, @JsonProperty("right") final V valid) {
        if (invalid != null) {
            if (valid != null) {
                throw new IllegalArgumentException(
                        "Validation JSON envelope must contain exactly one of 'left' and 'right'");
            }
            return Validation.invalid(invalid);
        }
        if (valid != null) {
            return Validation.valid(valid);
        }
        throw new IllegalArgumentException(
                "Validation JSON envelope must contain 'left' or 'right'");
    }

    /**
     * Creates a valid validation.
     *
     * @param <E> the invalid type
     * @param <S> the valid type
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
     * @param <E> the invalid type
     * @param <S> the valid type
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
     * @param errorMapper function to convert the invalid value to {@link
     *     eu.infolead.jtk.fp.validation.ValidationError}
     * @return a {@link eu.infolead.jtk.fp.validation.ValidationResult}
     */
    default eu.infolead.jtk.fp.validation.ValidationResult<V> toValidationResult(
            final Function<I, eu.infolead.jtk.fp.validation.ValidationError> errorMapper) {
        Objects.requireNonNull(errorMapper, "errorMapper");
        return fold(
                invalid ->
                        eu.infolead.jtk.fp.validation.ValidationResult.failure(
                                errorMapper.apply(invalid)),
                eu.infolead.jtk.fp.validation.ValidationResult::success);
    }

    /**
     * Applies this validation and another one applicatively, combining their values and
     * accumulating every invalid value instead of short-circuiting.
     *
     * <p>Both validations are always evaluated. The result holds the combined value when both are
     * valid, or the list of all invalid values.
     *
     * <pre>{@code
     * Validation<List<String>, String> city =
     *         Validation.valid("Ghent")
     *                 .zip(Validation.<String, Integer>invalid("no postal code"),
     *                         (name, code) -> name + " " + code);
     * city.isInvalid(); // true
     * city.fold(errors -> errors, value -> List.of()); // ["no postal code"]
     * }</pre>
     *
     * @param <U> the other value type
     * @param <R> the combined value type
     * @param other the other validation
     * @param combiner function combining both values
     * @return a validation holding the combined value or all invalid values
     */
    default <U, R> Validation<List<I>, R> zip(
            final Validation<I, U> other,
            final BiFunction<? super V, ? super U, ? extends R> combiner) {
        Objects.requireNonNull(other, "other");
        Objects.requireNonNull(combiner, "combiner");
        return fold(
                leftError ->
                        other.fold(
                                rightError ->
                                        Validation.<List<I>, R>invalid(
                                                List.of(leftError, rightError)),
                                rightValue -> Validation.<List<I>, R>invalid(List.of(leftError))),
                leftValue ->
                        other.fold(
                                rightError -> Validation.<List<I>, R>invalid(List.of(rightError)),
                                rightValue ->
                                        Validation.<List<I>, R>valid(
                                                combiner.apply(leftValue, rightValue))));
    }

    /**
     * Applies this validation and another one applicatively, accumulating all invalid values.
     *
     * <p>This is an alias for {@link #zip(Validation, BiFunction)}.
     *
     * @param <U> the other value type
     * @param <R> the combined value type
     * @param other the other validation
     * @param combiner function combining both values
     * @return a validation holding the combined value or all invalid values
     */
    default <U, R> Validation<List<I>, R> combine(
            final Validation<I, U> other,
            final BiFunction<? super V, ? super U, ? extends R> combiner) {
        return zip(other, combiner);
    }

    /**
     * Applies this validation and another one, keeping this value when both are valid and
     * accumulating every invalid value otherwise.
     *
     * <p>Use this for validation-only chains where the other validation guards an additional
     * condition and its value is not needed.
     *
     * <pre>{@code
     * Validation<List<String>, String> checked =
     *         Validation.valid("user").and(Validation.<String, Integer>invalid("age"));
     * checked.fold(errors -> errors, value -> List.of()); // ["age"]
     * }</pre>
     *
     * @param <U> the other value type
     * @param other the other validation
     * @return a validation holding this value or all invalid values
     */
    default <U> Validation<List<I>, V> and(final Validation<I, U> other) {
        Objects.requireNonNull(other, "other");
        return fold(
                leftError ->
                        other.fold(
                                rightError ->
                                        Validation.<List<I>, V>invalid(
                                                List.of(leftError, rightError)),
                                rightValue -> Validation.<List<I>, V>invalid(List.of(leftError))),
                leftValue ->
                        other.fold(
                                rightError -> Validation.<List<I>, V>invalid(List.of(rightError)),
                                rightValue -> Validation.<List<I>, V>valid(leftValue)));
    }

    /**
     * Applies two {@link ValidationError}-based validations and accumulates both errors into a
     * {@link ValidationResult}.
     *
     * <p>This is the applicative entry point for the validation API: unlike {@link Either#flatMap},
     * which stops at the first error, both validations are evaluated and every error is reported.
     *
     * <pre>{@code
     * ValidationResult<Registration> result = Validation.zip(
     *         Validation.required("email", email),
     *         Validation.lengthBetween("name", name, 2, 50),
     *         Registration::new);
     * if (result.isFailure()) {
     *     List<ValidationError> all = result.getErrors();
     * }
     * }</pre>
     *
     * @param <A> the left value type
     * @param <B> the right value type
     * @param <R> the combined value type
     * @param left the first validation
     * @param right the second validation
     * @param combiner function combining both values
     * @return a successful result with the combined value, or a failure holding every error
     */
    static <A, B, R> ValidationResult<R> zip(
            final Validation<ValidationError, A> left,
            final Validation<ValidationError, B> right,
            final BiFunction<? super A, ? super B, ? extends R> combiner) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        Objects.requireNonNull(combiner, "combiner");
        final List<ValidationError> errors = new ArrayList<>(2);
        final Maybe<A> leftValue =
                left.fold(
                        error -> {
                            errors.add(error);
                            return Maybe.<A>none();
                        },
                        Maybe::of);
        final Maybe<B> rightValue =
                right.fold(
                        error -> {
                            errors.add(error);
                            return Maybe.<B>none();
                        },
                        Maybe::of);
        if (!errors.isEmpty()) {
            return ValidationResult.failure(errors);
        }
        return ValidationResult.success(combiner.apply(leftValue.orNull(), rightValue.orNull()));
    }

    /**
     * Applies two {@link ValidationError}-based validations and accumulates both errors into a
     * {@link ValidationResult}.
     *
     * <p>This is an alias for {@link #zip(Validation, Validation, BiFunction)}.
     *
     * @param <A> the left value type
     * @param <B> the right value type
     * @param <R> the combined value type
     * @param left the first validation
     * @param right the second validation
     * @param combiner function combining both values
     * @return a successful result with the combined value, or a failure holding every error
     */
    static <A, B, R> ValidationResult<R> combine(
            final Validation<ValidationError, A> left,
            final Validation<ValidationError, B> right,
            final BiFunction<? super A, ? super B, ? extends R> combiner) {
        return zip(left, right, combiner);
    }

    /**
     * Applies two {@link ValidationError}-based validations, keeping the left value when both are
     * valid and accumulating every error otherwise.
     *
     * @param <A> the left value type
     * @param <B> the right value type
     * @param left the first validation
     * @param right the second validation
     * @return a successful result with the left value, or a failure holding every error
     */
    static <A, B> ValidationResult<A> and(
            final Validation<ValidationError, A> left, final Validation<ValidationError, B> right) {
        return zip(left, right, (leftValue, rightValue) -> leftValue);
    }

    /**
     * Creates a validation that checks a predicate.
     *
     * @param <T> the value type
     * @param value the value to validate
     * @param predicate the validation predicate
     * @param error the error if predicate fails
     * @return a Validation result
     * @throws NullPointerException if {@code value} is {@code null}; use {@link #required(String,
     *     Object)} to report a missing value instead
     */
    static <T> Validation<eu.infolead.jtk.fp.validation.ValidationError, T> check(
            final T value,
            final Predicate<T> predicate,
            final eu.infolead.jtk.fp.validation.ValidationError error) {
        Objects.requireNonNull(
                value, "value must not be null; a Validation cannot hold a null valid value");
        Objects.requireNonNull(predicate, "predicate");
        Objects.requireNonNull(error, "error");
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
    static <T> Validation<eu.infolead.jtk.fp.validation.ValidationError, T> required(
            final String fieldPath, final T value) {
        Objects.requireNonNull(fieldPath, "fieldPath");
        return value != null
                ? valid(value)
                : invalid(eu.infolead.jtk.fp.validation.ValidationError.required(fieldPath));
    }

    /**
     * Creates a validation for string length constraints.
     *
     * @param fieldPath the field path
     * @param value the string value
     * @param minLength minimum length (inclusive)
     * @param maxLength maximum length (inclusive)
     * @return a Validation result
     * @throws IllegalArgumentException if {@code minLength} is negative or {@code maxLength} is
     *     smaller than {@code minLength}
     */
    static Validation<eu.infolead.jtk.fp.validation.ValidationError, String> lengthBetween(
            final String fieldPath, final String value, final int minLength, final int maxLength) {
        Objects.requireNonNull(fieldPath, "fieldPath");
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be non-negative, was " + minLength);
        }
        if (maxLength < minLength) {
            throw new IllegalArgumentException(
                    "maxLength ("
                            + maxLength
                            + ") must be greater than or equal to minLength ("
                            + minLength
                            + ")");
        }
        if (value == null) {
            return invalid(eu.infolead.jtk.fp.validation.ValidationError.required(fieldPath));
        }
        if (value.length() < minLength) {
            return invalid(
                    eu.infolead.jtk.fp.validation.ValidationError.tooShort(
                            fieldPath, value, minLength));
        }
        if (value.length() > maxLength) {
            return invalid(
                    eu.infolead.jtk.fp.validation.ValidationError.tooLong(
                            fieldPath, value, maxLength));
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
     * @throws IllegalArgumentException if {@code max} is smaller than {@code min}
     */
    static <T extends Comparable<T>>
            Validation<eu.infolead.jtk.fp.validation.ValidationError, T> rangeBetween(
                    final String fieldPath, final T value, final T min, final T max) {
        Objects.requireNonNull(fieldPath, "fieldPath");
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException(
                    "max (" + max + ") must be greater than or equal to min (" + min + ")");
        }
        if (value == null) {
            return invalid(eu.infolead.jtk.fp.validation.ValidationError.required(fieldPath));
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            return invalid(
                    eu.infolead.jtk.fp.validation.ValidationError.outOfRange(
                            fieldPath, value, min, max));
        }
        return valid(value);
    }

    final class Invalid<E, T> extends AbstractLeft<E, T> implements Validation<E, T> {
        private static final long serialVersionUID = 1L;

        @JsonCreator
        public Invalid(@JsonProperty("left") final E value) {
            super(Objects.requireNonNull(value, "error"));
        }

        private void readObject(final ObjectInputStream input)
                throws IOException, ClassNotFoundException {
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

        @JsonCreator
        public Valid(@JsonProperty("right") final T value) {
            super(Objects.requireNonNull(value, "value"));
        }

        private void readObject(final ObjectInputStream input)
                throws IOException, ClassNotFoundException {
            input.defaultReadObject();
            Objects.requireNonNull(value(), "deserialized Valid value must not be null");
        }

        @Override
        public <X extends Either<T, E>> X swap() {
            throw new UnsupportedOperationException("Swapping a Validation does not make sense.");
        }
    }
}
