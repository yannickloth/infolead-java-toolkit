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
 * Law conformance suite for {@link Maybe}: the functor and monad laws over generated some/none
 * values, using the {@code Maybe}-typed bind ({@code ifSomeDo}) and the inherited {@link
 * Either#flatMap} bind.
 */
class MaybeLawsPropertyTest {

    @Provide
    Arbitrary<Maybe<Integer>> maybes() {
        return JtkArbitraries.maybes(Arbitraries.integers());
    }

    @Property
    void functorIdentity(@ForAll("maybes") final Maybe<Integer> maybe) {
        JtkAssertions.assertThat(maybe.map(value -> value)).isEqualTo(maybe);
    }

    @Property
    void functorComposition(
            @ForAll("maybes") final Maybe<Integer> maybe,
            @ForAll final int first,
            @ForAll final int second) {
        final Mapper<Integer, Integer> firstMapper = value -> value + first;
        final Mapper<String, Integer> secondMapper = value -> "v=" + value;
        final Mapper<String, Integer> composed =
                value -> secondMapper.apply(firstMapper.apply(value));
        JtkAssertions.assertThat(maybe.map(firstMapper).map(secondMapper))
                .isEqualTo(maybe.map(composed));
    }

    @Property
    void monadLeftIdentity(@ForAll final int value, @ForAll final int offset) {
        final Mapper<Maybe<Integer>, Integer> step = operand -> Maybe.of(operand + offset);
        JtkAssertions.assertThat(Maybe.of(value).flatMap(step)).isEqualTo(step.apply(value));
    }

    @Property
    void monadRightIdentity(@ForAll("maybes") final Maybe<Integer> maybe) {
        final Mapper<Maybe<Integer>, Integer> pure = Maybe::of;
        JtkAssertions.assertThat(maybe.flatMap(pure)).isEqualTo(maybe);
    }

    @Property
    void monadRightIdentityThroughIfSomeDo(@ForAll("maybes") final Maybe<Integer> maybe) {
        JtkAssertions.assertThat(maybe.ifSomeDo(() -> Maybe.of(maybe.orNull()))).isEqualTo(maybe);
    }

    @Property
    void filterWithAnAlwaysTruePredicateKeepsTheMaybe(
            @ForAll("maybes") final Maybe<Integer> maybe) {
        JtkAssertions.assertThat(maybe.filter(value -> true)).isEqualTo(maybe);
    }

    @Property
    void filterWithAnAlwaysFalsePredicateEmptiesTheMaybe(
            @ForAll("maybes") final Maybe<Integer> maybe) {
        JtkAssertions.assertThat(maybe.filter(value -> false)).isEqualTo(Maybe.none());
    }

    @Property
    void isPresentAndIsEmptyAreExclusive(@ForAll("maybes") final Maybe<Integer> maybe) {
        assertThat(maybe.isPresent().toBoolean()).isNotEqualTo(maybe.isEmpty().toBoolean());
    }
}
