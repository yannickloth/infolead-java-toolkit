package eu.infolead.jtk.anomaly.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.infolead.jtk.fp.either.Maybe;

class ProblemDetailJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void standardProblemDetailRoundTripsAndToleratesExtensions() throws Exception {
        final StandardProblemDetail detail = new StandardProblemDetail(
                URI.create("https://errors.infolead.eu/problems/validation/parameter"),
                Maybe.some("Bad Request"),
                Maybe.some(400),
                Maybe.some("parameter is invalid"),
                Maybe.none());

        final String json = mapper.writeValueAsString(detail);
        assertTrue(json.contains("\"type\""));
        assertTrue(json.contains("\"status\":400"));

        final StandardProblemDetail parsed = mapper.readValue(
                "{\"title\":\"Bad Request\",\"status\":400,\"detail\":\"invalid\",\"extension\":true}",
                StandardProblemDetail.class);
        assertEquals(URI.create("about:blank"), parsed.getProblemType());
    }

    @Test
    void multiStatusUsesTheWrappedResponseShape() throws Exception {
        final StandardMultiStatusProblemDetail detail = new StandardMultiStatusProblemDetail(
                URI.create("about:blank"),
                Maybe.none(),
                Maybe.some(207),
                Maybe.none(),
                Maybe.none(),
                List.of(new MultiStatusResponseElement(URI.create("/resource"), 423)));

        final String json = mapper.writeValueAsString(detail);
        assertTrue(json.contains("\"multistatus\""));
        assertTrue(json.contains("\"response\""));
        assertFalse(json.contains("\"responses\""));

        final StandardMultiStatusProblemDetail parsed =
                mapper.readValue(json, StandardMultiStatusProblemDetail.class);
        assertEquals(1, parsed.getResponses().size());
    }

    @Test
    void multiStatusResponseIgnoresPropstatExtensions() throws Exception {
        final MultiStatusResponseElement response = mapper.readValue(
                "{\"href\":\"/resource\",\"propstat\":{\"status\":\"HTTP/1.1 200 OK\"}}",
                MultiStatusResponseElement.class);
        assertEquals(URI.create("/resource"), response.href());
    }
}
