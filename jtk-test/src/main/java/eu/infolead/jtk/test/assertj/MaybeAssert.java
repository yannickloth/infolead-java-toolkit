package eu.infolead.jtk.test.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import eu.infolead.jtk.fp.either.Maybe;
import java.util.Objects;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.ObjectAssert;

/**
 * Assertions for {@link Maybe}.
 *
 * @param <T> the value type
 */
public final class MaybeAssert<T> extends AbstractObjectAssert<MaybeAssert<T>, Maybe<T>> {

    public MaybeAssert(final Maybe<T> actual) {
        super(actual, MaybeAssert.class);
    }

    /**
     * Verifies that the actual maybe is present.
     *
     * @return this assertion object
     */
    public MaybeAssert<T> isPresent() {
        isNotNull();
        if (actual.isEmpty().toBoolean()) {
            failWithMessage("Expected maybe to be present but was empty");
        }
        return myself;
    }

    /**
     * Verifies that the actual maybe is empty.
     *
     * @return this assertion object
     */
    public MaybeAssert<T> isEmpty() {
        isNotNull();
        if (actual.isPresent().toBoolean()) {
            failWithMessage(
                    "Expected maybe to be empty but was present with value <%s>", valueOrNull());
        }
        return myself;
    }

    /**
     * Verifies that the actual maybe is present and carries exactly the expected value, as
     * determined by value equality.
     *
     * @param expected the expected value
     * @return this assertion object
     */
    public MaybeAssert<T> hasValue(final T expected) {
        isPresent();
        objects.assertEqual(info, valueOrNull(), expected);
        return myself;
    }

    /**
     * Verifies that the actual maybe is present and its value satisfies the given requirements.
     *
     * @param requirement the requirements the value must satisfy; MUST NOT be {@code null}
     * @return this assertion object
     */
    public MaybeAssert<T> hasValueSatisfying(final Consumer<? super T> requirement) {
        Objects.requireNonNull(requirement, "requirement");
        isPresent();
        requirement.accept(valueOrNull());
        return myself;
    }

    /**
     * Verifies that the actual maybe is present and returns an object assertion over its value, as
     * an escape hatch to the full AssertJ vocabulary.
     *
     * @return an object assertion over the value
     */
    public ObjectAssert<T> extractingValue() {
        isPresent();
        return assertThat(valueOrNull());
    }

    private T valueOrNull() {
        return actual.orNull();
    }
}
