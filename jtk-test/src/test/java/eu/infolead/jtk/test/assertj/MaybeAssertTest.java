package eu.infolead.jtk.test.assertj;

import static eu.infolead.jtk.test.JtkAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.infolead.jtk.fp.either.Maybe;
import org.junit.jupiter.api.Test;

/** Self-tests for {@link MaybeAssert}. */
class MaybeAssertTest {

    @Test
    void presentAssertionsPass() {
        assertThat(Maybe.of(42))
                .isPresent()
                .hasValue(42)
                .hasValueSatisfying(value -> assertThat(value).isPositive())
                .extractingValue()
                .isEqualTo(42);
    }

    @Test
    void emptyAssertionsPass() {
        assertThat(Maybe.none()).isEmpty();
    }

    @Test
    void isPresentFailsForNone() {
        assertThatThrownBy(() -> assertThat(Maybe.none()).isPresent())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("present");
    }

    @Test
    void isEmptyFailsForSome() {
        assertThatThrownBy(() -> assertThat(Maybe.of(42)).isEmpty())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("42");
    }

    @Test
    void hasValueFailsOnAMismatch() {
        assertThatThrownBy(() -> assertThat(Maybe.of(1)).hasValue(2))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("2");
    }
}
