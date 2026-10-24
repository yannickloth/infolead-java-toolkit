package eu.infolead.jtk.test;

import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Result;
import eu.infolead.jtk.fp.either.Validation;
import eu.infolead.jtk.logic.Bool;
import eu.infolead.jtk.test.assertj.BoolAssert;
import eu.infolead.jtk.test.assertj.EitherAssert;
import eu.infolead.jtk.test.assertj.MaybeAssert;
import eu.infolead.jtk.test.assertj.ResultAssert;
import eu.infolead.jtk.test.assertj.SystemErrorAssert;
import eu.infolead.jtk.test.assertj.ValidationAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;

/**
 * Entry point for the jtk-aware AssertJ assertions. Static-import {@code assertThat} (or the
 * BDD-style {@code then}) from this class; the overloads may be combined with AssertJ's own {@code
 * org.assertj.core.api.Assertions#assertThat} because they only accept jtk types.
 *
 * <h2>Naming note</h2>
 *
 * <p>The assertion methods deliberately shadow domain vocabulary: {@code
 * assertThat(result).isSuccess()} reads exactly like the domain call {@code result.isSuccess()},
 * but asserts instead of returning {@link Bool}. This is intentional; the AssertJ methods never
 * leak {@code Bool} instances.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * assertThat(service.place(order))
 *         .isSuccess()
 *         .hasValue(expectedOrder);
 *
 * assertThat(service.load(id))
 *         .isFailure()
 *         .hasFailureSatisfying(error -> assertThat(error.getErrorType())
 *                 .isEqualTo(ParameterErrorType.NULL_PARAMETER));
 * }</pre>
 */
public final class JtkAssertions {

    private JtkAssertions() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates an assertion object for a {@link Result}.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param actual the result to assert; must not be {@code null}
     * @return a new {@link ResultAssert}
     */
    public static <F, S> ResultAssert<F, S> assertThat(final Result<F, S> actual) {
        return new ResultAssert<>(actual);
    }

    /**
     * Creates an assertion object for a {@link Maybe}.
     *
     * @param <T> the value type
     * @param actual the maybe to assert; must not be {@code null}
     * @return a new {@link MaybeAssert}
     */
    public static <T> MaybeAssert<T> assertThat(final Maybe<T> actual) {
        return new MaybeAssert<>(actual);
    }

    /**
     * Creates an assertion object for an {@link Either}.
     *
     * @param <L> the left type
     * @param <R> the right type
     * @param actual the either to assert; must not be {@code null}
     * @return a new {@link EitherAssert}
     */
    public static <L, R> EitherAssert<L, R> assertThat(final Either<L, R> actual) {
        return new EitherAssert<>(actual);
    }

    /**
     * Creates an assertion object for a {@link Validation}.
     *
     * @param <I> the invalid-value type
     * @param <V> the valid-value type
     * @param actual the validation to assert; must not be {@code null}
     * @return a new {@link ValidationAssert}
     */
    public static <I, V> ValidationAssert<I, V> assertThat(final Validation<I, V> actual) {
        return new ValidationAssert<>(actual);
    }

    /**
     * Creates an assertion object for a {@link Bool}, bridging it to AssertJ's boolean assertions.
     * {@code Bool} never carries {@code null}; three-valued checks are the domain of {@link
     * eu.infolead.jtk.logic.NullableBool} and are not covered by this bridge.
     *
     * @param actual the bool to assert; must not be {@code null}
     * @return a new {@link BoolAssert}
     */
    public static BoolAssert assertThat(final Bool actual) {
        return new BoolAssert(actual);
    }

    /**
     * Creates an assertion object for a {@link SystemError}.
     *
     * @param actual the error to assert; must not be {@code null}
     * @return a new {@link SystemErrorAssert}
     */
    public static SystemErrorAssert assertThat(final SystemError actual) {
        return new SystemErrorAssert(actual);
    }

    /**
     * BDD-style alias for {@link #assertThat(Result)}.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param actual the result to assert
     * @return a new {@link ResultAssert}
     */
    public static <F, S> ResultAssert<F, S> then(final Result<F, S> actual) {
        return assertThat(actual);
    }

    /**
     * BDD-style alias for {@link #assertThat(Maybe)}.
     *
     * @param <T> the value type
     * @param actual the maybe to assert
     * @return a new {@link MaybeAssert}
     */
    public static <T> MaybeAssert<T> then(final Maybe<T> actual) {
        return assertThat(actual);
    }

    /**
     * BDD-style alias for {@link #assertThat(Either)}.
     *
     * @param <L> the left type
     * @param <R> the right type
     * @param actual the either to assert
     * @return a new {@link EitherAssert}
     */
    public static <L, R> EitherAssert<L, R> then(final Either<L, R> actual) {
        return assertThat(actual);
    }

    /**
     * BDD-style alias for {@link #assertThat(Validation)}.
     *
     * @param <I> the invalid-value type
     * @param <V> the valid-value type
     * @param actual the validation to assert
     * @return a new {@link ValidationAssert}
     */
    public static <I, V> ValidationAssert<I, V> then(final Validation<I, V> actual) {
        return assertThat(actual);
    }

    /**
     * BDD-style alias for {@link #assertThat(Bool)}.
     *
     * @param actual the bool to assert
     * @return a new {@link BoolAssert}
     */
    public static BoolAssert then(final Bool actual) {
        return assertThat(actual);
    }

    /**
     * BDD-style alias for {@link #assertThat(SystemError)}.
     *
     * @param actual the error to assert
     * @return a new {@link SystemErrorAssert}
     */
    public static SystemErrorAssert then(final SystemError actual) {
        return assertThat(actual);
    }

    /**
     * Delegates to AssertJ for non-jtk values so that {@code JtkAssertions.assertThat} can be
     * static-imported as a single entry point in tests.
     */
    public static <T> AbstractObjectAssert<?, T> assertThat(final T actual) {
        return Assertions.assertThat(actual);
    }

    /** Delegates to AssertJ for primitive booleans. */
    public static AbstractBooleanAssert<?> assertThat(final boolean actual) {
        return Assertions.assertThat(actual);
    }
}
