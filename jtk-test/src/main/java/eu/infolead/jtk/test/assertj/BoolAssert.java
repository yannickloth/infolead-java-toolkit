package eu.infolead.jtk.test.assertj;

import eu.infolead.jtk.logic.Bool;
import java.util.Objects;
import org.assertj.core.api.AbstractBooleanAssert;

/**
 * Assertions for {@link Bool}, bridging the two-valued logic type to AssertJ's boolean assertions
 * so {@code assertThat(bool).isTrue()} reads like the rest of an AssertJ chain. The bridge is
 * total: {@link Bool} has exactly the two constants {@link Bool#TRUE} and {@link Bool#FALSE} and
 * never carries {@code null}.
 */
public final class BoolAssert extends AbstractBooleanAssert<BoolAssert> {

    public BoolAssert(final Bool actual) {
        super(Objects.requireNonNull(actual, "actual").toBoolean(), BoolAssert.class);
    }
}
