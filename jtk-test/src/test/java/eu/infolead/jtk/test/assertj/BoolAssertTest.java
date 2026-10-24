package eu.infolead.jtk.test.assertj;

import static eu.infolead.jtk.test.JtkAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.infolead.jtk.logic.Bool;
import org.junit.jupiter.api.Test;

/** Self-tests for the {@link Bool} bridge to AssertJ's boolean assertions. */
class BoolAssertTest {

    @Test
    void trueAssertsAsTrue() {
        assertThat(Bool.TRUE).isTrue();
        assertThat(Bool.of(true)).isTrue();
    }

    @Test
    void falseAssertsAsFalse() {
        assertThat(Bool.FALSE).isFalse();
        assertThat(Bool.of(false)).isFalse();
    }

    @Test
    void isTrueFailsForFalse() {
        assertThatThrownBy(() -> assertThat(Bool.FALSE).isTrue())
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void boolsComposeWithTheFluentVocabulary() {
        assertThat(Bool.TRUE.and(Bool.TRUE)).isTrue();
        assertThat(Bool.FALSE.or(Bool.TRUE)).isTrue();
        assertThat(Bool.TRUE.negate()).isFalse();
    }
}
