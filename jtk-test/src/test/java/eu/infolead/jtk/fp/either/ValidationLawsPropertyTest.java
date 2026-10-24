package eu.infolead.jtk.fp.either;

import static org.assertj.core.api.Assertions.assertThat;

import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.test.JtkAssertions;
import eu.infolead.jtk.test.gen.JtkArbitraries;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Law conformance suite for {@link Validation}. Validation is an applicative functor with error
 * accumulation, not a monad: its {@code zip}/{@code and} combinators accumulate every invalid value
 * instead of short-circuiting.
 *
 * <p>Two properties are deliberately absent:
 *
 * <ul>
 *   <li><strong>Functor identity</strong>: {@code Validation} inherits {@link Either#map}, which
 *       rebuilds plain {@code Right}/{@code Left} variants, so {@code valid(1).map(identity)} is
 *       not equal to {@code valid(1)}. The current behavior is pinned by {@link
 *       #mapKeepsThePlainEitherVariants()}.
 *   <li><strong>Swap involution</strong>: for the same reason, swapping a {@code Valid} twice lands
 *       on the plain {@code Right} class, not back on {@code Valid}.
 * </ul>
 */
class ValidationLawsPropertyTest {

    @Provide
    Arbitrary<Validation<String, Integer>> validations() {
        return JtkArbitraries.validations(
                Arbitraries.strings().ofMaxLength(8), Arbitraries.integers());
    }

    @Property
    void functorComposition(
            @ForAll("validations") final Validation<String, Integer> validation,
            @ForAll final int first,
            @ForAll final int second) {
        final Mapper<Integer, Integer> firstMapper = value -> value + first;
        final Mapper<String, Integer> secondMapper = value -> "v=" + value;
        final Mapper<String, Integer> composed =
                value -> secondMapper.apply(firstMapper.apply(value));
        JtkAssertions.assertThat(validation.map(firstMapper).map(secondMapper))
                .isEqualTo(validation.map(composed));
    }

    @Property
    void isValidAndIsInvalidAreExclusive(
            @ForAll("validations") final Validation<String, Integer> validation) {
        assertThat(validation.isValid().toBoolean())
                .isNotEqualTo(validation.isInvalid().toBoolean());
    }

    @Property
    void zipAccumulatesEveryInvalid(@ForAll final String first, @ForAll final String second) {
        final Validation<List<String>, Integer> combined =
                Validation.<String, Integer>invalid(first)
                        .zip(
                                Validation.<String, Integer>invalid(second),
                                (left, right) -> left + right);
        JtkAssertions.assertThat(combined).isInvalid().hasInvalid(List.of(first, second));
    }

    @Property
    void zipWithOneInvalidReportsOnlyIt(@ForAll final String invalid, @ForAll final int value) {
        final Validation<List<String>, Integer> combined =
                Validation.<String, Integer>valid(value)
                        .zip(
                                Validation.<String, Integer>invalid(invalid),
                                (left, right) -> left + right);
        JtkAssertions.assertThat(combined).isInvalid().hasInvalid(List.of(invalid));
    }

    @Property
    void zipOfValidsCombinesTheValues(@ForAll final int first, @ForAll final int second) {
        final Validation<List<String>, Integer> combined =
                Validation.<String, Integer>valid(first)
                        .zip(
                                Validation.<String, Integer>valid(second),
                                (left, right) -> left + right);
        JtkAssertions.assertThat(combined).isValid().hasValue(first + second);
    }

    @Example
    void mapKeepsThePlainEitherVariants() {
        // Documents the current contract: Validation inherits Either.map, which
        // rebuilds plain Right/Left variants instead of Valid/Invalid. If map
        // ever becomes variant-preserving, update this pin deliberately.
        JtkAssertions.assertThat(Validation.<String, Integer>valid(1).map(value -> value))
                .isEqualTo(Either.right(1));
        JtkAssertions.assertThat(Validation.<String, Integer>invalid("bad").map(value -> value))
                .isEqualTo(Either.left("bad"));
    }
}
