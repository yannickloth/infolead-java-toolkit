package eu.infolead.jtk.test.error;

import static eu.infolead.jtk.test.JtkAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import eu.infolead.jtk.anomaly.ContextualSystemError;
import eu.infolead.jtk.anomaly.ParameterErrorType;
import eu.infolead.jtk.anomaly.SystemError;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Self-tests for the {@link ErrorFixture} builder. */
class ErrorFixtureTest {

    @Test
    void anErrorBuildsTheCanonicalFixtureUnchanged() {
        assertThat(ErrorFixture.anError().build()).isEqualTo(TestErrors.nullParameter());
    }

    @Test
    void overridesAreApplied() {
        final RuntimeException cause = new RuntimeException("disk full");
        final SystemError error =
                ErrorFixture.anError()
                        .withErrorId("custom-id")
                        .withCorrelationId("corr-9")
                        .withTenantId("tenant-9")
                        .withCause(cause)
                        .withRetryable(true)
                        .withRetryAfter(Duration.ofSeconds(3))
                        .build();

        assertThat(error).hasErrorId("custom-id");
        final ContextualSystemError contextual =
                assertInstanceOf(ContextualSystemError.class, error);
        assertThat(contextual.getErrorContext().correlationId().orNull()).isEqualTo("corr-9");
        assertThat(contextual.getErrorContext().tenantId().orNull()).isEqualTo("tenant-9");
        assertThat(error.getCause().orNull()).isSameAs(cause);
        assertThat(error.isRetryable()).isTrue();
        assertThat(contextual.retryAfter().orNull()).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    void theBaseTypeSurvivesTheOverrides() {
        assertThat(ErrorFixture.anError().withTenantId("tenant-9").build())
                .hasErrorType(ParameterErrorType.NULL_PARAMETER);
    }

    @Test
    void suppressedErrorsAccumulate() {
        final SystemError error =
                ErrorFixture.anError()
                        .withSuppressed(
                                new RuntimeException("first"), new RuntimeException("second"))
                        .build();
        assertThat(assertInstanceOf(ContextualSystemError.class, error).suppressedCount())
                .isEqualTo(2);
    }

    @Test
    void basedOnAcceptsAnyError() {
        assertThat(ErrorFixture.basedOn(TestErrors.unauthorized()).build())
                .isEqualTo(TestErrors.unauthorized());
    }
}
