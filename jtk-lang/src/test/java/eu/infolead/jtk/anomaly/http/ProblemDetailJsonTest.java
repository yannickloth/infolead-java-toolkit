package eu.infolead.jtk.anomaly.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.anomaly.Sensitive;
import eu.infolead.jtk.fp.either.Maybe;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ProblemDetailJsonTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void standardProblemDetailRoundTripsAndToleratesExtensions() throws Exception {
        final StandardProblemDetail detail =
                new StandardProblemDetail(
                        URI.create("https://errors.infolead.eu/problems/validation/parameter"),
                        Maybe.some("Bad Request"),
                        Maybe.some(400),
                        Maybe.some("parameter is invalid"),
                        Maybe.none());

        final String json = mapper.writeValueAsString(detail);
        assertTrue(json.contains("\"type\""));
        assertTrue(json.contains("\"status\":400"));

        final StandardProblemDetail parsed =
                mapper.readValue(
                        "{\"title\":\"Bad Request\",\"status\":400,\"detail\":\"invalid\",\"extension\":true}",
                        StandardProblemDetail.class);
        assertEquals(URI.create("about:blank"), parsed.getProblemType());
    }

    @Test
    void operationalExtensionsRoundTripThroughJson() throws Exception {
        final StandardProblemDetail detail =
                StandardProblemDetail.builder(URI.create("about:blank"))
                        .title("Too Many Requests")
                        .status(429)
                        .detail("slow down")
                        .correlationId("corr-1")
                        .traceId("trace-1")
                        .requestId("req-1")
                        .tenantId("tenant-1")
                        .retryable(true)
                        .retryAfterSeconds(3L)
                        .build();

        final String json = mapper.writeValueAsString(detail);
        assertTrue(json.contains("\"correlationId\":\"corr-1\""));
        assertTrue(json.contains("\"traceId\":\"trace-1\""));
        assertTrue(json.contains("\"requestId\":\"req-1\""));
        assertTrue(json.contains("\"tenantId\":\"tenant-1\""));
        assertTrue(json.contains("\"retryable\":true"));
        assertTrue(json.contains("\"retryAfter\":3"));

        final StandardProblemDetail parsed = mapper.readValue(json, StandardProblemDetail.class);
        assertEquals("corr-1", parsed.getCorrelationId().orNull());
        assertEquals("trace-1", parsed.getTraceId().orNull());
        assertEquals("req-1", parsed.getRequestId().orNull());
        assertEquals("tenant-1", parsed.getTenantId().orNull());
        assertEquals(true, parsed.getRetryable().orNull());
        assertEquals(3L, parsed.getRetryAfterSeconds().orNull());
    }

    @Test
    void sensitiveValuesSerializeAsRedactedStrings() throws Exception {
        assertEquals("\"[REDACTED]\"", mapper.writeValueAsString(Sensitive.of("hunter2")));
        final String json = mapper.writeValueAsString(Map.of("password", Sensitive.of("hunter2")));
        assertFalse(json.contains("hunter2"));
        assertTrue(json.contains("[REDACTED]"));
    }

    @Test
    void coreMembersSerializeAndRoundTripThroughJson() throws Exception {
        final StandardProblemDetail detail =
                StandardProblemDetail.builder(
                                URI.create(
                                        "https://errors.infolead.eu/problems/validation/standard"))
                        .title("Bad Request")
                        .status(400)
                        .detail("field userId is invalid")
                        .instance(URI.create("/requests/42"))
                        .build();

        final String json = mapper.writeValueAsString(detail);
        assertTrue(json.contains("\"title\":\"Bad Request\""), json);
        assertTrue(json.contains("\"detail\":\"field userId is invalid\""), json);
        assertTrue(json.contains("\"instance\":\"/requests/42\""), json);

        final StandardProblemDetail parsed = mapper.readValue(json, StandardProblemDetail.class);
        assertEquals("Bad Request", parsed.getTitle());
        assertEquals("field userId is invalid", parsed.getDetail());
        assertEquals(URI.create("/requests/42"), parsed.getInstance());
        assertEquals(400, parsed.getStatusValue());
    }

    @Test
    void emptyCoreMembersAreOmittedFromJson() throws Exception {
        final String json =
                mapper.writeValueAsString(
                        StandardProblemDetail.aboutBlank(503, java.util.Locale.ENGLISH));
        assertTrue(json.contains("\"title\""));
        assertFalse(json.contains("\"detail\""));
        assertFalse(json.contains("\"instance\""));
        assertFalse(json.contains("\"correlationId\""));
    }

    @Test
    void multiStatusUsesTheWrappedResponseShape() throws Exception {
        final StandardMultiStatusProblemDetail detail =
                new StandardMultiStatusProblemDetail(
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
        assertEquals(URI.create("about:blank"), parsed.getProblemType());
        assertEquals(207, parsed.getHttpStatus().orNull());
    }

    @Test
    void multiStatusCarriesTheOperationalExtensions() throws Exception {
        final StandardMultiStatusProblemDetail detail =
                new StandardMultiStatusProblemDetail(
                                URI.create("about:blank"),
                                Maybe.some("Multi-Status"),
                                Maybe.some(207),
                                Maybe.none(),
                                Maybe.none(),
                                List.of(
                                        new MultiStatusResponseElement(
                                                URI.create("/resource"), 423)))
                        .withExtensions(
                                Maybe.some("corr-1"),
                                Maybe.some("trace-1"),
                                Maybe.some("req-1"),
                                Maybe.some("tenant-1"),
                                true,
                                Maybe.of(3L));

        final String json = mapper.writeValueAsString(detail);
        assertTrue(json.contains("\"correlationId\":\"corr-1\""), json);
        assertTrue(json.contains("\"retryAfter\":3"), json);
        assertTrue(json.contains("\"multistatus\""));

        final StandardMultiStatusProblemDetail parsed =
                mapper.readValue(json, StandardMultiStatusProblemDetail.class);
        assertEquals("corr-1", parsed.getCorrelationId().orNull());
        assertEquals("trace-1", parsed.getTraceId().orNull());
        assertEquals("req-1", parsed.getRequestId().orNull());
        assertEquals("tenant-1", parsed.getTenantId().orNull());
        assertEquals(true, parsed.getRetryable().orNull());
        assertEquals(3L, parsed.getRetryAfterSeconds().orNull());
        assertEquals(1, parsed.getResponses().size());

        final StandardMultiStatusProblemDetail localized =
                parsed.localized(java.util.Locale.GERMAN);
        assertEquals(1, localized.getResponses().size());
        assertEquals("corr-1", localized.getCorrelationId().orNull());
        assertEquals(HttpStatusMessages.title(207, java.util.Locale.GERMAN), localized.getTitle());
    }

    @Test
    void multiStatusResponseIgnoresPropstatExtensions() throws Exception {
        final MultiStatusResponseElement response =
                mapper.readValue(
                        "{\"href\":\"/resource\",\"propstat\":{\"status\":\"HTTP/1.1 200 OK\"}}",
                        MultiStatusResponseElement.class);
        assertEquals(URI.create("/resource"), response.href());
    }
}
