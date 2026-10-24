package eu.infolead.jtk.fp.either;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.fp.validation.ValidationError;
import eu.infolead.jtk.fp.validation.ValidationResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pins the published JSON envelope contract of the {@code Either} hierarchy.
 *
 * <p>The contract is annotation-only: it must hold with a plain {@link JsonMapper} and without
 * registering any module, because consumers of this library configure their own mapper.
 */
class JsonEnvelopeContractTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    /** A consumer-declared holder, the way an API DTO would declare the types. */
    record Holder<L, R>(
            Either<L, R> either,
            Maybe<R> maybe,
            Result<L, R> result,
            Validation<L, R> validation) {}

    private JavaType holderType() {
        return mapper.getTypeFactory()
                .constructParametricType(Holder.class, String.class, Integer.class);
    }

    @Test
    void leftAndRightBranchSerialiseAsSingleMemberEnvelopes() {
        assertEquals("{\"left\":\"boom\"}", mapper.writeValueAsString(Either.left("boom")));
        assertEquals("{\"right\":7}", mapper.writeValueAsString(Either.right(7)));
        assertEquals("{\"right\":7}", mapper.writeValueAsString(Maybe.of(7)));
        assertEquals("{\"left\":null}", mapper.writeValueAsString(Maybe.none()));
        assertEquals("{\"right\":7}", mapper.writeValueAsString(Result.success(7)));
        assertEquals("{\"right\":null}", mapper.writeValueAsString(Result.success()));
        assertEquals("{\"left\":\"boom\"}", mapper.writeValueAsString(Result.failure("boom")));
        assertEquals("{\"right\":7}", mapper.writeValueAsString(Validation.valid(7)));
        assertEquals("{\"left\":\"boom\"}", mapper.writeValueAsString(Validation.invalid("boom")));
    }

    @Test
    void theRawPayloadAccessorIsNeverSerialised() {
        final String json = mapper.writeValueAsString(Maybe.of("value"));
        assertFalse(json.contains("\"value\":"), json);
        assertTrue(json.contains("\"right\""), json);
    }

    @Test
    void genericPayloadTypesComeFromTheDeclaredSignature() {
        final Holder<String, Integer> holder =
                new Holder<>(
                        Either.right(7), Maybe.of(9), Result.success(11), Validation.valid(13));

        final String json = mapper.writeValueAsString(holder);
        final Holder<String, Integer> parsed = mapper.readValue(json, holderType());

        assertEquals(Either.right(7), parsed.either());
        assertEquals(Maybe.of(9), parsed.maybe());
        assertEquals(Result.success(11), parsed.result());
        assertEquals(Validation.valid(13), parsed.validation());
    }

    @Test
    void leftBranchesRoundTripWithTheirGenericPayload() {
        final Holder<String, Integer> holder =
                new Holder<>(
                        Either.left("boom"),
                        Maybe.none(),
                        Result.failure("boom"),
                        Validation.invalid("boom"));

        final Holder<String, Integer> parsed =
                mapper.readValue(mapper.writeValueAsString(holder), holderType());

        assertEquals(Either.left("boom"), parsed.either());
        assertSame(Maybe.none(), parsed.maybe());
        assertEquals(Result.failure("boom"), parsed.result());
        assertEquals(Validation.invalid("boom"), parsed.validation());
    }

    @Test
    void emptyResultSuccessKeepsItsSingletonIdentity() {
        final Holder<String, Integer> holder =
                new Holder<>(Either.right(1), Maybe.of(1), Result.success(), Validation.valid(1));

        final Holder<String, Integer> roundTripped =
                mapper.readValue(mapper.writeValueAsString(holder), holderType());
        final Result<String, Integer> parsed = roundTripped.result();

        assertInstanceOf(Result.EmptySuccess.class, parsed);
        assertSame(Result.success(), parsed);
    }

    @Test
    void absentRightMemberMeansAbsenceForMaybeAndEmptySuccessForResult() {
        assertEquals(Maybe.none(), mapper.readValue("{}", Maybe.class));
        assertInstanceOf(Result.EmptySuccess.class, mapper.readValue("{}", Result.class));
    }

    @Test
    void eitherRejectsAnEmptyEnvelope() {
        assertThrows(Exception.class, () -> mapper.readValue("{}", Either.class));
    }

    @Test
    void validationRejectsAnEmptyEnvelope() {
        assertThrows(Exception.class, () -> mapper.readValue("{}", Validation.class));
    }

    @Test
    void bothBranchesInOneEnvelopeAreRejected() {
        assertThrows(
                Exception.class,
                () -> mapper.readValue("{\"left\":\"a\",\"right\":7}", Either.class));
        assertThrows(
                Exception.class,
                () -> mapper.readValue("{\"left\":\"a\",\"right\":7}", Result.class));
        assertThrows(
                Exception.class,
                () -> mapper.readValue("{\"left\":\"a\",\"right\":7}", Validation.class));
    }

    @Test
    void validationResultAggregationRoundTrips() {
        final ValidationResult<Integer> success = ValidationResult.success(7);
        final ValidationResult<Integer> parsedSuccess =
                mapper.readValue(
                        mapper.writeValueAsString(success),
                        mapper.getTypeFactory()
                                .constructParametricType(ValidationResult.class, Integer.class));
        assertEquals(success, parsedSuccess);

        final ValidationResult<Integer> failure =
                ValidationResult.failure(
                        List.of(
                                ValidationError.required("email"),
                                ValidationError.required("name")));
        final ValidationResult<Integer> parsedFailure =
                mapper.readValue(
                        mapper.writeValueAsString(failure),
                        mapper.getTypeFactory()
                                .constructParametricType(ValidationResult.class, Integer.class));
        assertEquals(failure, parsedFailure);
        assertEquals(2, parsedFailure.getErrors().size());
    }
}
