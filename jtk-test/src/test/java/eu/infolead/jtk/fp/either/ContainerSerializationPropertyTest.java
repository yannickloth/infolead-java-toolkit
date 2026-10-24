package eu.infolead.jtk.fp.either;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.infolead.jtk.logic.Bool;
import eu.infolead.jtk.test.JtkAssertions;
import eu.infolead.jtk.test.gen.JtkArbitraries;
import eu.infolead.jtk.test.serial.RoundTrip;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import tools.jackson.databind.json.JsonMapper;

/**
 * Serialization round-trip properties for the container types, covering the two independent
 * serialization contracts of the toolkit: session replication through {@code java.io.Serializable},
 * and the JSON wire envelope, which MUST round-trip with a plain mapper, without registering a
 * module.
 *
 * <p>The singleton values ({@link Bool#TRUE}, {@link Maybe#none()}, the empty success) MUST come
 * back as the <em>same instance</em>; the populated values MUST come back equal.
 */
class ContainerSerializationPropertyTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Provide
    Arbitrary<Result<String, Integer>> results() {
        return JtkArbitraries.results(Arbitraries.strings().ofMaxLength(8), Arbitraries.integers());
    }

    @Provide
    Arbitrary<Maybe<Integer>> maybes() {
        return JtkArbitraries.maybes(Arbitraries.integers());
    }

    @Provide
    Arbitrary<Either<String, Integer>> eithers() {
        return JtkArbitraries.eithers(Arbitraries.strings().ofMaxLength(8), Arbitraries.integers());
    }

    @Provide
    Arbitrary<Validation<String, Integer>> validations() {
        return JtkArbitraries.validations(
                Arbitraries.strings().ofMaxLength(8), Arbitraries.integers());
    }

    @Provide
    Arbitrary<Bool> bools() {
        return JtkArbitraries.bools();
    }

    @Property
    void resultSurvivesJavaSerialization(@ForAll("results") final Result<String, Integer> result) {
        JtkAssertions.assertThat(RoundTrip.java(result)).isEqualTo(result);
    }

    @Property
    void resultHashCodeSurvivesJavaSerialization(
            @ForAll("results") final Result<String, Integer> result) {
        assertThat(RoundTrip.java(result).hashCode()).isEqualTo(result.hashCode());
    }

    @Property
    void maybeSurvivesJavaSerialization(@ForAll("maybes") final Maybe<Integer> maybe) {
        JtkAssertions.assertThat(RoundTrip.java(maybe)).isEqualTo(maybe);
    }

    @Property
    void eitherSurvivesJavaSerialization(@ForAll("eithers") final Either<String, Integer> either) {
        JtkAssertions.assertThat(RoundTrip.java(either)).isEqualTo(either);
    }

    @Property
    void validationSurvivesJavaSerialization(
            @ForAll("validations") final Validation<String, Integer> validation) {
        JtkAssertions.assertThat(RoundTrip.java(validation)).isEqualTo(validation);
    }

    @Property
    void boolSurvivesAsTheSameSingleton(@ForAll("bools") final Bool bool) {
        assertThat(RoundTrip.java(bool)).isSameAs(bool);
    }

    @Example
    void jtkSingletonsSurviveJavaSerializationAsTheSameInstance() {
        assertThat(RoundTrip.java(Result.success())).isSameAs(Result.success());
        assertThat(RoundTrip.java(Maybe.none())).isSameAs(Maybe.none());
        assertThat(RoundTrip.java(Bool.TRUE)).isSameAs(Bool.TRUE);
        assertThat(RoundTrip.java(Bool.FALSE)).isSameAs(Bool.FALSE);
    }

    @Example
    void corruptedStreamsAreRefused() {
        assertThatThrownBy(() -> RoundTrip.deserializeFromJava(new byte[] {1, 2, 3}))
                .isInstanceOf(IllegalStateException.class);
    }

    @Property
    void resultJsonEnvelopeRoundTripsWithAPlainMapper(
            @ForAll("results") final Result<String, Integer> result) {
        JtkAssertions.assertThat(
                        RoundTrip.jackson(
                                MAPPER,
                                result,
                                RoundTrip.envelopeType(
                                        MAPPER, Result.class, String.class, Integer.class)))
                .isEqualTo(result);
    }

    @Property
    void eitherJsonEnvelopeRoundTripsWithAPlainMapper(
            @ForAll("eithers") final Either<String, Integer> either) {
        JtkAssertions.assertThat(
                        RoundTrip.jackson(
                                MAPPER,
                                either,
                                RoundTrip.envelopeType(
                                        MAPPER, Either.class, String.class, Integer.class)))
                .isEqualTo(either);
    }

    @Property
    void maybeJsonEnvelopeRoundTripsWithAPlainMapper(@ForAll("maybes") final Maybe<Integer> maybe) {
        JtkAssertions.assertThat(
                        RoundTrip.jackson(
                                MAPPER,
                                maybe,
                                RoundTrip.envelopeType(MAPPER, Maybe.class, Integer.class)))
                .isEqualTo(maybe);
    }

    @Property
    void validationJsonEnvelopeRoundTripsWithAPlainMapper(
            @ForAll("validations") final Validation<String, Integer> validation) {
        JtkAssertions.assertThat(
                        RoundTrip.jackson(
                                MAPPER,
                                validation,
                                RoundTrip.envelopeType(
                                        MAPPER, Validation.class, String.class, Integer.class)))
                .isEqualTo(validation);
    }
}
