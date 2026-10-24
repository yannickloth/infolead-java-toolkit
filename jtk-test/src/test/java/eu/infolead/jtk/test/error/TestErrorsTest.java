package eu.infolead.jtk.test.error;

import static eu.infolead.jtk.test.JtkAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import eu.infolead.jtk.anomaly.SystemError;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Verifies the two guarantees the fixtures exist for: determinism (two calls produce equal errors)
 * and pinned error IDs (no process-local sequence leaks into tests).
 */
class TestErrorsTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Test
    void fixturesAreDeterministic() {
        final List<SystemError> first = TestErrors.all();
        final List<SystemError> second = TestErrors.all();
        for (int index = 0; index < first.size(); index++) {
            assertThat(first.get(index)).isEqualTo(second.get(index));
        }
    }

    @Test
    void everyFixtureCarriesAPinnedErrorId() {
        for (final SystemError error : TestErrors.all()) {
            assertThat(error.getErrorId().isPresent()).isTrue();
        }
    }

    @Test
    void pinnedErrorIdsMatchTheirConstants() {
        assertThat(TestErrors.nullParameter().getErrorId().orNull())
                .isEqualTo(TestErrors.NULL_PARAMETER_ERROR_ID);
        assertThat(TestErrors.resourceNotFound().getErrorId().orNull())
                .isEqualTo(TestErrors.RESOURCE_NOT_FOUND_ERROR_ID);
        assertThat(TestErrors.persistenceFailed().getErrorId().orNull())
                .isEqualTo(TestErrors.PERSISTENCE_FAILED_ERROR_ID);
    }

    @Test
    void problemDetailJsonIsStableAcrossCalls() {
        final String first =
                MAPPER.writeValueAsString(TestErrors.nullParameter().toProblemDetail());
        final String second =
                MAPPER.writeValueAsString(TestErrors.nullParameter().toProblemDetail());
        assertThat(first).isEqualTo(second);
    }

    @Test
    void everyFixtureCoversADistinctCategory() {
        final long distinctTypes =
                TestErrors.all().stream()
                        .map(error -> error.getErrorType().getClass())
                        .distinct()
                        .count();
        assertThat(distinctTypes).isEqualTo(TestErrors.all().size());
    }
}
