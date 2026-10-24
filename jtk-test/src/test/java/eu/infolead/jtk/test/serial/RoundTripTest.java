package eu.infolead.jtk.test.serial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import eu.infolead.jtk.anomaly.ContextualSystemError;
import eu.infolead.jtk.anomaly.ParameterError;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.test.JtkAssertions;
import eu.infolead.jtk.test.error.TestErrors;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Self-tests for {@link RoundTrip} beyond the generated property coverage. */
class RoundTripTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Test
    void parameterErrorsRoundTripThroughJavaSerialization() {
        final ParameterError error = ParameterError.nullParameter("orderId");
        assertThat(RoundTrip.java(error)).isEqualTo(error);
    }

    @Test
    void theCanonicalFixtureRoundTripsThroughJavaSerialization() {
        final SystemError fixture = TestErrors.nullParameter();
        final SystemError roundTripped = RoundTrip.java(fixture);
        // Documents the current gap: ContextualSystemError record equality does not survive a
        // Java round trip, because the RedactionPolicy component is a lambda constant that
        // deserializes to a fresh instance. Session replication therefore preserves the semantic
        // fields, which this test pins; switch to strict equality once equality is fixed
        // upstream in jtk-lang.
        assertThat(roundTripped.getMessage()).isEqualTo(fixture.getMessage());
        JtkAssertions.assertThat(roundTripped)
                .hasErrorType(fixture.getErrorType())
                .hasErrorId(TestErrors.NULL_PARAMETER_ERROR_ID)
                .hasHttpStatus(fixture.getHttpStatus());
        final ContextualSystemError contextual =
                assertInstanceOf(ContextualSystemError.class, roundTripped);
        assertThat(contextual.getErrorContext().correlationId().orNull())
                .isEqualTo(TestErrors.CORRELATION_ID);
        assertThat(contextual.getErrorContext().tenantId().orNull())
                .isEqualTo(TestErrors.TENANT_ID);
    }

    @Test
    void problemDetailsRoundTripThroughJackson() {
        final var problem = TestErrors.nullParameter().toProblemDetail();
        assertThat(RoundTrip.jackson(MAPPER, problem)).isEqualTo(problem);
    }

    @Test
    void corruptedStreamsAreRefusedAsIllegalState() {
        assertThatThrownBy(() -> RoundTrip.deserializeFromJava(new byte[] {1, 2, 3}))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deserialization");
    }
}
