package eu.infolead.jtk.jackson2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.anomaly.ParameterError;
import eu.infolead.jtk.anomaly.ResourceError;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.anomaly.http.MultiStatusResponseElement;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardMultiStatusProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Result;
import eu.infolead.jtk.fp.validation.ValidationResult;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Mirror of the Jackson 3 contract test, proving the bridge carries the same mechanics on the
 * Jackson 2 API.
 */
@SuppressWarnings("deprecation")
class JtkJacksonContractTest {

    @Test
    void problemDetailInterfaceReadsBothPermittedShapes() {
        final ProblemDetail standard =
                ProblemJson.read(
                        "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400}");
        assertInstanceOf(StandardProblemDetail.class, standard);
        assertEquals("Bad Request", standard.getTitle());
        assertEquals(400, standard.getStatusValue());

        final ProblemDetail multi =
                ProblemJson.read(
                        "{\"type\":\"about:blank\",\"status\":207,"
                                + "\"multistatus\":{\"response\":[{\"href\":\"/r\",\"status\":423}]}}");
        assertInstanceOf(StandardMultiStatusProblemDetail.class, multi);
        assertEquals(207, multi.getStatusValue());
    }

    @Test
    void multiStatusShapeIsWrittenAsMultistatus() {
        final StandardMultiStatusProblemDetail detail =
                new StandardMultiStatusProblemDetail(
                        URI.create("about:blank"),
                        Maybe.some("Multi-Status"),
                        Maybe.some(207),
                        Maybe.none(),
                        Maybe.none(),
                        List.of(new MultiStatusResponseElement(URI.create("/r"), 423)));

        final String json = ProblemJson.write(detail);
        assertTrue(json.contains("\"multistatus\""), json);
        assertInstanceOf(StandardMultiStatusProblemDetail.class, ProblemJson.read(json));
    }

    @Test
    void problemMediaTypeIgnoresParametersAndCase() {
        assertTrue(ProblemJson.isProblemJson("application/problem+json"));
        assertTrue(ProblemJson.isProblemJson("Application/Problem+Json; charset=utf-8"));
        assertFalse(ProblemJson.isProblemJson("application/json"));
        assertFalse(ProblemJson.isProblemJson(null));
    }

    @Test
    void errorBecomesAProblemDocument() {
        final SystemError error = ParameterError.nullParameter("userId");
        final String json = ProblemJson.write(error, Locale.FRENCH);
        assertTrue(json.contains("\"status\":400"), json);
        assertTrue(json.contains("userId"), json);
        assertFalse(json.contains("\"kind\""), json);
    }

    @Test
    void functionalEnvelopesStillRoundTripWithTheModuleRegistered() throws Exception {
        final var mapper = JtkJackson.mapper();

        final Either<String, Integer> right =
                mapper.readValue(
                        "{\"right\":7}",
                        mapper.getTypeFactory()
                                .constructParametricType(
                                        Either.class, String.class, Integer.class));
        assertEquals(Either.right(7), right);

        final Result<String, Integer> empty =
                mapper.readValue(
                        "{\"right\":null}",
                        mapper.getTypeFactory()
                                .constructParametricType(
                                        Result.class, String.class, Integer.class));
        assertInstanceOf(Result.EmptySuccess.class, empty);

        final ValidationResult<Integer> result =
                mapper.readValue(
                        "{\"right\":7}",
                        mapper.getTypeFactory()
                                .constructParametricType(ValidationResult.class, Integer.class));
        assertEquals(ValidationResult.success(7), result);
    }

    @Test
    void malformedProblemDocumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ProblemJson.read("[]"));
    }

    @Test
    void errorsKeepTheirOwnContractWhenTheModuleIsRegistered() {
        final var mapper = JtkJackson.mapper();
        final String json;
        try {
            json = mapper.writeValueAsString(ResourceError.notFound("User", 7));
        } catch (final com.fasterxml.jackson.core.JsonProcessingException failure) {
            throw new AssertionError(failure);
        }
        assertTrue(json.contains("\"kind\":\"resource\""), json);
        try {
            assertEquals(ResourceError.class, mapper.readValue(json, SystemError.class).getClass());
        } catch (final com.fasterxml.jackson.core.JsonProcessingException failure) {
            throw new AssertionError(failure);
        }
    }
}
