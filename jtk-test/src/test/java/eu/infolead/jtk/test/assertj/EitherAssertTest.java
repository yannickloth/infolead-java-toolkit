package eu.infolead.jtk.test.assertj;

import static eu.infolead.jtk.test.JtkAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.infolead.jtk.fp.either.Either;
import org.junit.jupiter.api.Test;

/** Self-tests for {@link EitherAssert}. */
class EitherAssertTest {

    @Test
    void rightAssertionsPass() {
        assertThat(Either.right(42)).isRight().hasRight(42).extractingRight().isEqualTo(42);
    }

    @Test
    void leftAssertionsPass() {
        assertThat(Either.left("bad")).isLeft().hasLeft("bad").extractingLeft().isEqualTo("bad");
    }

    @Test
    void isRightFailsForALeft() {
        assertThatThrownBy(() -> assertThat(Either.<String, Integer>left("bad")).isRight())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("right")
                .hasMessageContaining("bad");
    }

    @Test
    void hasLeftFailsForARight() {
        assertThatThrownBy(() -> assertThat(Either.<String, Integer>right(42)).hasLeft("bad"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("left");
    }
}
