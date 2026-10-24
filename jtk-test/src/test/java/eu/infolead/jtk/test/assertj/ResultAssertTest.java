package eu.infolead.jtk.test.assertj;

import static eu.infolead.jtk.test.JtkAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.infolead.jtk.fp.either.Result;
import org.junit.jupiter.api.Test;

/**
 * Self-tests for {@link ResultAssert}, covering the passing paths and every failure branch, because
 * an assertion library that cannot fail loudly is worse than none.
 */
class ResultAssertTest {

    @Test
    void successAssertionsPass() {
        assertThat(Result.success(42))
                .isSuccess()
                .hasValue(42)
                .hasValueSatisfying(value -> assertThat(value).isPositive())
                .extractingValue()
                .isEqualTo(42);
    }

    @Test
    void emptySuccessIsASuccessWithoutValue() {
        final Result<String, Integer> empty = Result.success();
        assertThat(empty).isSuccess().isEmptySuccess();
        assertThatThrownBy(() -> assertThat(empty).hasValue(42))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("empty success");
        assertThatThrownBy(
                        () ->
                                assertThat(empty)
                                        .hasValueSatisfying(
                                                value -> assertThat(value).isPositive()))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("empty success");
    }

    @Test
    void failureAssertionsPass() {
        assertThat(Result.failure("boom"))
                .isFailure()
                .hasFailure("boom")
                .hasFailureSatisfying(anomaly -> assertThat(anomaly).startsWith("bo"))
                .extractingFailure()
                .isEqualTo("boom");
    }

    @Test
    void isSuccessFailsForAFailure() {
        assertThatThrownBy(() -> assertThat(Result.<String, Integer>failure("boom")).isSuccess())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("success")
                .hasMessageContaining("boom");
    }

    @Test
    void isFailureFailsForASuccess() {
        assertThatThrownBy(() -> assertThat(Result.success(42)).isFailure())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("failure")
                .hasMessageContaining("42");
    }

    @Test
    void isFailureFailsForAnEmptySuccess() {
        assertThatThrownBy(() -> assertThat(Result.<String, Integer>success()).isFailure())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("empty success");
    }

    @Test
    void hasValueFailsOnAMismatch() {
        assertThatThrownBy(() -> assertThat(Result.success(1)).hasValue(2))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("2");
    }

    @Test
    void hasFailureFailsForASuccess() {
        assertThatThrownBy(() -> assertThat(Result.<String, Integer>success(1)).hasFailure("boom"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("failure");
    }

    @Test
    void isEmptySuccessFailsForAPopulatedResult() {
        assertThatThrownBy(() -> assertThat(Result.success(42)).isEmptySuccess())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("empty success");
    }
}
