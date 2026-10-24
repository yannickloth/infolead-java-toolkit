package eu.infolead.jtk.spring.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.anomaly.ResourceError;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

class JtkJacksonAutoConfigurationTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    JacksonAutoConfiguration.class,
                                    JtkJacksonAutoConfiguration.class));

    @Test
    void publishesTheModuleWithoutOverridingUserBeans() {
        runner.run(
                context -> {
                    assertNotNull(context.getBean(eu.infolead.jtk.jackson3.JtkJacksonModule.class));
                    assertNotNull(context.getBean(JsonMapper.class));
                });
    }

    @Test
    void autoConfiguredMapperReadsBothProblemShapes() {
        runner.run(
                context -> {
                    final JsonMapper mapper = context.getBean(JsonMapper.class);
                    final ProblemDetail detail =
                            mapper.readValue(
                                    "{\"type\":\"about:blank\",\"status\":400,\"title\":\"Bad Request\"}",
                                    ProblemDetail.class);
                    assertInstanceOf(StandardProblemDetail.class, detail);
                    assertEquals(400, detail.getStatusValue());
                });
    }

    @Test
    void springBridgeKeepsCoreAndExtensionMembers() {
        final eu.infolead.jtk.anomaly.http.ProblemDetail source =
                StandardProblemDetail.builder(URI.create("https://errors.infolead.eu/problems/x"))
                        .status(429)
                        .title("Too Many Requests")
                        .detail("slow down")
                        .instance(URI.create("/requests/42"))
                        .correlationId("corr-1")
                        .traceId("trace-1")
                        .tenantId("tenant-1")
                        .retryable(true)
                        .retryAfterSeconds(3L)
                        .build();

        final org.springframework.http.ProblemDetail spring = SpringProblemDetails.toSpring(source);
        assertEquals(429, spring.getStatus());
        assertEquals("Too Many Requests", spring.getTitle());
        assertEquals("corr-1", spring.getProperties().get("correlationId"));

        final eu.infolead.jtk.anomaly.http.ProblemDetail back =
                SpringProblemDetails.fromSpring(spring);
        assertEquals(source.getProblemType(), back.getProblemType());
        assertEquals(source.getTitle(), back.getTitle());
        assertEquals(source.getDetail(), back.getDetail());
        assertEquals(source.getInstance(), back.getInstance());
        assertEquals("corr-1", back.getCorrelationId().orNull());
        assertEquals(Boolean.TRUE, back.getRetryable().orNull());
        assertEquals(3L, back.getRetryAfterSeconds().orNull());
    }

    @Test
    void multiStatusIsFlaggedBeforeTranslation() {
        final var multi =
                eu.infolead.jtk.anomaly.http.StandardMultiStatusProblemDetail.fromJson(
                        URI.create("about:blank"),
                        null,
                        207,
                        null,
                        null,
                        new eu.infolead.jtk.anomaly.http.MultiStatusProblemDetail.MultiStatus(
                                java.util.List.of()),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
        assertTrue(SpringProblemDetails.isMultiStatus(multi));
        assertEquals(207, SpringProblemDetails.toSpring(multi).getStatus());
    }

    @Test
    void errorToProblemDocumentStillUsesTheAnnotationContract() {
        final String json =
                eu.infolead.jtk.jackson3.ProblemJson.write(
                        ResourceError.notFound("User", 7), java.util.Locale.ENGLISH);
        assertTrue(json.contains("\"status\":404"), json);
        assertTrue(json.contains("\"type\""), json);
        assertTrue(!json.contains("\"kind\""), json);
    }
}
