package eu.infolead.jtk.test.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import eu.infolead.jtk.fp.either.Validation;
import java.util.Objects;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.ObjectAssert;

/**
 * Assertions for {@link Validation}.
 *
 * @param <I> the invalid-value type
 * @param <V> the valid-value type
 */
public final class ValidationAssert<I, V>
        extends AbstractObjectAssert<ValidationAssert<I, V>, Validation<I, V>> {

    public ValidationAssert(final Validation<I, V> actual) {
        super(actual, ValidationAssert.class);
    }

    /**
     * Verifies that the actual validation is valid.
     *
     * @return this assertion object
     */
    public ValidationAssert<I, V> isValid() {
        isNotNull();
        if (actual.isInvalid().toBoolean()) {
            failWithMessage(
                    "Expected validation to be valid but was invalid with <%s>", invalidValue());
        }
        return myself;
    }

    /**
     * Verifies that the actual validation is invalid.
     *
     * @return this assertion object
     */
    public ValidationAssert<I, V> isInvalid() {
        isNotNull();
        if (actual.isValid().toBoolean()) {
            failWithMessage(
                    "Expected validation to be invalid but was valid with value <%s>",
                    validValue());
        }
        return myself;
    }

    /**
     * Verifies that the actual validation is valid and carries exactly the expected value, as
     * determined by value equality.
     *
     * @param expected the expected valid value
     * @return this assertion object
     */
    public ValidationAssert<I, V> hasValue(final V expected) {
        isValid();
        objects.assertEqual(info, validValue(), expected);
        return myself;
    }

    /**
     * Verifies that the actual validation is valid and its value satisfies the given requirements.
     *
     * @param requirement the requirements the value must satisfy; MUST NOT be {@code null}
     * @return this assertion object
     */
    public ValidationAssert<I, V> hasValueSatisfying(final Consumer<? super V> requirement) {
        Objects.requireNonNull(requirement, "requirement");
        isValid();
        requirement.accept(validValue());
        return myself;
    }

    /**
     * Verifies that the actual validation is invalid and carries exactly the expected invalid
     * value, as determined by value equality.
     *
     * @param expected the expected invalid value
     * @return this assertion object
     */
    public ValidationAssert<I, V> hasInvalid(final I expected) {
        isInvalid();
        objects.assertEqual(info, invalidValue(), expected);
        return myself;
    }

    /**
     * Verifies that the actual validation is valid and returns an object assertion over its value,
     * as an escape hatch to the full AssertJ vocabulary.
     *
     * @return an object assertion over the valid value
     */
    public ObjectAssert<V> extractingValue() {
        isValid();
        return assertThat(validValue());
    }

    /**
     * Verifies that the actual validation is invalid and returns an object assertion over its
     * invalid value, as an escape hatch to the full AssertJ vocabulary.
     *
     * @return an object assertion over the invalid value
     */
    public ObjectAssert<I> extractingInvalid() {
        isInvalid();
        return assertThat(invalidValue());
    }

    private V validValue() {
        return actual.orNull();
    }

    private I invalidValue() {
        return actual.fold(invalid -> invalid, valid -> null);
    }
}
