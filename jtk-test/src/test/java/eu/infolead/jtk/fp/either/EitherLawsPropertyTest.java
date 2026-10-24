package eu.infolead.jtk.fp.either;

import static org.assertj.core.api.Assertions.assertThat;

import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.test.JtkAssertions;
import eu.infolead.jtk.test.gen.JtkArbitraries;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Law conformance suite for the plain {@link Either}: the functor and monad laws over generated
 * left/right values, and swap involution.
 */
class EitherLawsPropertyTest {

    @Provide
    Arbitrary<Either<String, Integer>> eithers() {
        return JtkArbitraries.eithers(Arbitraries.strings().ofMaxLength(8), Arbitraries.integers());
    }

    @Property
    void functorIdentity(@ForAll("eithers") final Either<String, Integer> either) {
        JtkAssertions.assertThat(either.map(value -> value)).isEqualTo(either);
    }

    @Property
    void functorComposition(
            @ForAll("eithers") final Either<String, Integer> either,
            @ForAll final int first,
            @ForAll final int second) {
        final Mapper<Integer, Integer> firstMapper = value -> value + first;
        final Mapper<String, Integer> secondMapper = value -> "v=" + value;
        final Mapper<String, Integer> composed =
                value -> secondMapper.apply(firstMapper.apply(value));
        JtkAssertions.assertThat(either.map(firstMapper).map(secondMapper))
                .isEqualTo(either.map(composed));
    }

    @Property
    void monadLeftIdentity(@ForAll final int value, @ForAll final int offset) {
        final Mapper<Either<String, Integer>, Integer> step =
                operand -> Either.right(operand + offset);
        JtkAssertions.assertThat(Either.<String, Integer>right(value).flatMap(step))
                .isEqualTo(step.apply(value));
    }

    @Property
    void monadRightIdentity(@ForAll("eithers") final Either<String, Integer> either) {
        final Mapper<Either<String, Integer>, Integer> pure = Either::right;
        JtkAssertions.assertThat(either.flatMap(pure)).isEqualTo(either);
    }

    @Property
    void swapInvolution(@ForAll("eithers") final Either<String, Integer> either) {
        final Either<String, Integer> reswapped = either.swap().swap();
        JtkAssertions.assertThat(reswapped).isEqualTo(either);
    }

    @Property
    void isRightAndIsLeftAreExclusive(@ForAll("eithers") final Either<String, Integer> either) {
        assertThat(either.isRight().toBoolean()).isNotEqualTo(either.isLeft().toBoolean());
    }

    @Property
    void mapLeftLeavesTheRightBranchUntouched(
            @ForAll("eithers") final Either<String, Integer> either, @ForAll final int offset) {
        final Mapper<Integer, Integer> rightMapper = value -> value + offset;
        JtkAssertions.assertThat(either.bimap(left -> "left:" + left, rightMapper))
                .isEqualTo(either.map(rightMapper).mapLeft(left -> "left:" + left));
    }
}
