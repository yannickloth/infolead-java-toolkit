package eu.infolead.jtk.test.assertj;

import static eu.infolead.jtk.test.JtkAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.infolead.jtk.fp.either.Validation;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Self-tests for {@link ValidationAssert}. */
class ValidationAssertTest {

    @Test
    void validAssertionsPass() {
        assertThat(Validation.<String, Integer>valid(42))
                .isValid()
                .hasValue(42)
                .hasValueSatisfying(value -> assertThat(value).isPositive())
                .extractingValue()
                .isEqualTo(42);
    }

    @Test
    void invalidAssertionsPass() {
        final List<String> errors = List.of("no postal code");
        assertThat(Validation.<List<String>, Integer>invalid(errors))
                .isInvalid()
                .hasInvalid(errors)
                .extractingInvalid()
                .isEqualTo(errors);
    }

    @Test
    void isValidFailsForAnInvalidValidation() {
        assertThatThrownBy(() -> assertThat(Validation.<String, Integer>invalid("bad")).isValid())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("valid")
                .hasMessageContaining("bad");
    }

    @Test
    void hasValueFailsOnAMismatch() {
        assertThatThrownBy(() -> assertThat(Validation.<String, Integer>valid(1)).hasValue(2))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("2");
    }
}
