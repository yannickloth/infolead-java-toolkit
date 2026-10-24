package eu.infolead.jtk.test.gen;

import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Result;
import eu.infolead.jtk.fp.either.Validation;
import eu.infolead.jtk.logic.Bool;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Tuple;

/**
 * jqwik {@link Arbitrary} factories for the toolkit's container types, so property tests can
 * generate {@code Result}, {@code Maybe}, {@code Either}, {@code Validation} and {@code Bool}
 * values over arbitrary payloads.
 *
 * <p>The factories are deliberately biased towards the informative variants: {@link
 * #results(Arbitrary, Arbitrary)} generates successes slightly more often than failures and still
 * produces the {@link Result#success() empty success}, because that variant is the easiest to
 * forget in hand-written tests.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * @Property
 * void mappingPreservesTheVariant(@ForAll("results") final Result<String, Integer> result) {
 *     assertThat(result.map(value -> value)).isEqualTo(result);
 * }
 *
 * @Provide
 * Arbitrary<Result<String, Integer>> results() {
 *     return JtkArbitraries.results(Arbitraries.strings().ofMaxLength(8), Arbitraries.integers());
 * }
 * }</pre>
 */
public final class JtkArbitraries {

    private JtkArbitraries() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Generates {@link Maybe} values: mostly {@code Some} over the given value arbitrary,
     * occasionally {@code None}.
     *
     * @param <T> the value type
     * @param values the arbitrary for present values
     * @return a maybe arbitrary
     */
    public static <T> Arbitrary<Maybe<T>> maybes(final Arbitrary<? extends T> values) {
        return Arbitraries.frequencyOf(
                Tuple.of(4, values.map(Maybe::of)), Tuple.of(1, Arbitraries.just(Maybe.none())));
    }

    /**
     * Generates {@link Result} values over the three variants: successes and failures with
     * comparable frequency, plus the {@link Result#success() empty success}.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param failures the arbitrary for failure anomalies
     * @param successes the arbitrary for success values
     * @return a result arbitrary
     */
    public static <F, S> Arbitrary<Result<F, S>> results(
            final Arbitrary<? extends F> failures, final Arbitrary<? extends S> successes) {
        return Arbitraries.frequencyOf(
                Tuple.of(5, successes.map(Result::success)),
                Tuple.of(4, failures.map(Result::failure)),
                Tuple.of(1, Arbitraries.just(Result.success())));
    }

    /**
     * Generates {@link Result} values restricted to the two populated variants {@code Success} and
     * {@code Failure}, for laws that the empty success refuses (such as swap involution).
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param failures the arbitrary for failure anomalies
     * @param successes the arbitrary for success values
     * @return a result arbitrary without the empty success
     */
    public static <F, S> Arbitrary<Result<F, S>> populatedResults(
            final Arbitrary<? extends F> failures, final Arbitrary<? extends S> successes) {
        return Arbitraries.oneOf(successes.map(Result::success), failures.map(Result::failure));
    }

    /**
     * Generates plain {@link Either} values over both branches.
     *
     * @param <L> the left type
     * @param <R> the right type
     * @param lefts the arbitrary for left values
     * @param rights the arbitrary for right values
     * @return an either arbitrary
     */
    public static <L, R> Arbitrary<Either<L, R>> eithers(
            final Arbitrary<? extends L> lefts, final Arbitrary<? extends R> rights) {
        return Arbitraries.oneOf(lefts.map(Either::left), rights.map(Either::right));
    }

    /**
     * Generates {@link Validation} values over both branches.
     *
     * @param <I> the invalid-value type
     * @param <V> the valid-value type
     * @param invalids the arbitrary for invalid values
     * @param valids the arbitrary for valid values
     * @return a validation arbitrary
     */
    public static <I, V> Arbitrary<Validation<I, V>> validations(
            final Arbitrary<? extends I> invalids, final Arbitrary<? extends V> valids) {
        return Arbitraries.oneOf(invalids.map(Validation::invalid), valids.map(Validation::valid));
    }

    /**
     * Generates both {@link Bool} constants with equal frequency.
     *
     * @return a bool arbitrary
     */
    public static Arbitrary<Bool> bools() {
        return Arbitraries.of(Bool.TRUE, Bool.FALSE);
    }
}
