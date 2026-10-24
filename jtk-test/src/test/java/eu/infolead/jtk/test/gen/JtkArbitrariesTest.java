package eu.infolead.jtk.test.gen;

import static org.assertj.core.api.Assertions.assertThat;

import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Result;
import eu.infolead.jtk.fp.either.Validation;
import eu.infolead.jtk.logic.Bool;
import java.util.List;
import java.util.Objects;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Self-tests for {@link JtkArbitraries}: the factories must generate the variants they advertise,
 * including the empty success, because a property suite is only as good as the space its
 * arbitraries explore. The variant classes are package-private, so coverage is asserted through the
 * runtime classes of factory-built instances.
 */
class JtkArbitrariesTest {

    @Example
    void resultArbitraryCoversAllThreeVariants() {
        final List<Result<String, Integer>> samples =
                JtkArbitraries.results(Arbitraries.strings().ofMaxLength(4), Arbitraries.integers())
                        .sampleStream()
                        .limit(500)
                        .toList();
        assertThat(samples)
                .allMatch(Objects::nonNull)
                .anyMatch(sample -> sample.getClass() == Result.success(1).getClass())
                .anyMatch(sample -> sample.getClass() == Result.failure("x").getClass())
                .anyMatch(sample -> sample.getClass() == Result.success().getClass());
    }

    @Example
    void populatedResultArbitraryExcludesTheEmptySuccess() {
        final List<Result<String, Integer>> samples =
                JtkArbitraries.populatedResults(
                                Arbitraries.strings().ofMaxLength(4), Arbitraries.integers())
                        .sampleStream()
                        .limit(500)
                        .toList();
        assertThat(samples).allMatch(sample -> sample.getClass() != Result.success().getClass());
    }

    @Example
    void maybeArbitraryCoversSomeAndNone() {
        final List<Maybe<Integer>> samples =
                JtkArbitraries.maybes(Arbitraries.integers()).sampleStream().limit(200).toList();
        assertThat(samples)
                .anyMatch(sample -> sample.getClass() == Maybe.of(1).getClass())
                .anyMatch(sample -> sample.getClass() == Maybe.none().getClass());
    }

    @Example
    void eitherArbitraryCoversLeftAndRight() {
        final List<Either<String, Integer>> samples =
                JtkArbitraries.eithers(Arbitraries.strings().ofMaxLength(4), Arbitraries.integers())
                        .sampleStream()
                        .limit(200)
                        .toList();
        assertThat(samples)
                .anyMatch(sample -> sample.getClass() == Either.right(1).getClass())
                .anyMatch(sample -> sample.getClass() == Either.left("x").getClass());
    }

    @Example
    void validationArbitraryCoversValidAndInvalid() {
        final List<Validation<String, Integer>> samples =
                JtkArbitraries.validations(
                                Arbitraries.strings().ofMaxLength(4), Arbitraries.integers())
                        .sampleStream()
                        .limit(200)
                        .toList();
        assertThat(samples)
                .anyMatch(sample -> sample.getClass() == Validation.valid(1).getClass())
                .anyMatch(sample -> sample.getClass() == Validation.invalid("x").getClass());
    }

    @Example
    void boolArbitraryYieldsBothConstants() {
        final List<Bool> samples = JtkArbitraries.bools().sampleStream().limit(50).toList();
        assertThat(samples).contains(Bool.TRUE, Bool.FALSE);
    }

    @Property
    void generatedResultsCarryConsistentState(
            @ForAll("results") final Result<String, Integer> result) {
        assertThat(result.isSuccess().toBoolean()).isNotEqualTo(result.isFailure().toBoolean());
        if (result.isFailure().toBoolean()) {
            assertThat(result.orNull()).isNull();
        }
    }

    @Provide
    Arbitrary<Result<String, Integer>> results() {
        return JtkArbitraries.results(Arbitraries.strings().ofMaxLength(4), Arbitraries.integers());
    }
}
