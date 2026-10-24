package eu.infolead.jtk.fp.either;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.test.JtkAssertions;
import eu.infolead.jtk.test.gen.JtkArbitraries;
import java.util.concurrent.atomic.AtomicInteger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Law conformance suite for {@link Result}: the functor and monad laws over generated successes,
 * failures and empty successes, plus the corner-case behavior of the {@link Result.EmptySuccess}
 * singleton.
 *
 * <p>This suite lives in {@code jtk-test} (not {@code jtk-lang}) because {@code jtk-test} depends
 * on {@code jtk-lang}, never the other way around; it is the toolkit's internal TCK.
 */
class ResultLawsPropertyTest {

    private static final int OFFSET = 7;

    @Provide
    Arbitrary<Result<String, Integer>> results() {
        return JtkArbitraries.results(Arbitraries.strings().ofMaxLength(8), Arbitraries.integers());
    }

    @Provide
    Arbitrary<Result<String, Integer>> populatedResults() {
        return JtkArbitraries.populatedResults(
                Arbitraries.strings().ofMaxLength(8), Arbitraries.integers());
    }

    @Property
    void functorIdentity(@ForAll("results") final Result<String, Integer> result) {
        JtkAssertions.assertThat(result.map(value -> value)).isEqualTo(result);
    }

    @Property
    void functorComposition(
            @ForAll("results") final Result<String, Integer> result,
            @ForAll final int first,
            @ForAll final int second) {
        final Mapper<Integer, Integer> firstMapper = value -> value + first;
        final Mapper<String, Integer> secondMapper = value -> "v=" + value;
        final Mapper<String, Integer> composed =
                value -> secondMapper.apply(firstMapper.apply(value));
        JtkAssertions.assertThat(result.map(firstMapper).map(secondMapper))
                .isEqualTo(result.map(composed));
    }

    @Property
    void monadLeftIdentity(@ForAll final int value, @ForAll final int offset) {
        final Mapper<Result<String, Integer>, Integer> step =
                operand -> Result.success(operand + offset);
        JtkAssertions.assertThat(Result.<String, Integer>success(value).flatMap(step))
                .isEqualTo(step.apply(value));
    }

    @Property
    void monadRightIdentity(@ForAll("results") final Result<String, Integer> result) {
        final Mapper<Result<String, Integer>, Integer> pure = operand -> Result.success(operand);
        JtkAssertions.assertThat(result.flatMap(pure)).isEqualTo(result);
    }

    @Property
    void swapInvolution(@ForAll("populatedResults") final Result<String, Integer> result) {
        final Either<String, Integer> reswapped = result.swap().swap();
        JtkAssertions.assertThat(reswapped).isEqualTo(result);
    }

    @Property
    void isSuccessAndIsFailureAreExclusive(
            @ForAll("results") final Result<String, Integer> result) {
        assertThat(result.isSuccess().toBoolean()).isNotEqualTo(result.isFailure().toBoolean());
    }

    @Example
    void emptySuccessIgnoresTheMapper() {
        final AtomicInteger invocations = new AtomicInteger();
        final Result<String, Integer> mapped =
                Result.<String, Integer>success()
                        .map(
                                value -> {
                                    invocations.incrementAndGet();
                                    return value;
                                });
        JtkAssertions.assertThat(mapped).isSuccess().isEmptySuccess();
        assertThat(invocations.get()).isEqualTo(0);
    }

    @Example
    void emptySuccessRefusesSwap() {
        assertThatThrownBy(() -> Result.<String, Integer>success().swap())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
