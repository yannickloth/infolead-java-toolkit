package eu.infolead.jtk.test.assertj;

import static eu.infolead.jtk.test.JtkAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.infolead.jtk.anomaly.ParameterErrorType;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.test.error.TestErrors;
import org.junit.jupiter.api.Test;

/** Self-tests for {@link SystemErrorAssert}, driven by the canonical fixtures. */
class SystemErrorAssertTest {

    @Test
    void theCanonicalFixtureSatisfiesItsContract() {
        assertThat(TestErrors.nullParameter())
                .hasErrorType(ParameterErrorType.NULL_PARAMETER)
                .hasErrorId(TestErrors.NULL_PARAMETER_ERROR_ID)
                .hasHttpStatus(400)
                .problemDetailSatisfying(
                        problem -> {
                            assertThat(problem.getProblemType())
                                    .isEqualTo(ParameterErrorType.NULL_PARAMETER.getProblemType());
                            assertThat(problem.getHttpStatus().orNull()).isEqualTo(400);
                        });
    }

    @Test
    void hasErrorTypeFailsOnAMismatch() {
        assertThatThrownBy(
                        () ->
                                assertThat(TestErrors.resourceNotFound())
                                        .hasErrorType(ParameterErrorType.NULL_PARAMETER))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void hasErrorIdFailsOnAMismatch() {
        assertThatThrownBy(() -> assertThat(TestErrors.nullParameter()).hasErrorId("another-id"))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void extractingProblemDetailGivesTheFullVocabulary() {
        assertThat(TestErrors.unauthorized())
                .extractingProblemDetail()
                .extracting(ProblemDetail::getHttpStatus)
                .satisfies(status -> assertThat(status.orNull()).isEqualTo(401));
    }
}
