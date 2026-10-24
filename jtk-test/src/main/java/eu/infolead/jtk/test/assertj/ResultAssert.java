package eu.infolead.jtk.test.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import eu.infolead.jtk.fp.either.Result;
import java.util.Objects;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.ObjectAssert;

/**
 * Assertions for {@link Result}.
 *
 * <p>{@link #isSuccess()} and {@link #isFailure()} deliberately shadow the domain methods of the
 * same names (which return {@link Bool}); as AssertJ methods they assert the state instead of
 * returning it.
 *
 * @param <F> the failure type
 * @param <S> the success type
 */
public final class ResultAssert<F, S>
        extends AbstractObjectAssert<ResultAssert<F, S>, Result<F, S>> {

    public ResultAssert(final Result<F, S> actual) {
        super(actual, ResultAssert.class);
    }

    /**
     * Verifies that the actual result is a success, including the {@link Result#success() empty
     * success}. Use {@link #isEmptySuccess()} when the empty variant must be distinguished.
     *
     * @return this assertion object
     */
    public ResultAssert<F, S> isSuccess() {
        isNotNull();
        if (actual.isFailure().toBoolean()) {
            failWithMessage(
                    "Expected result to be a success but was a failure with anomaly <%s>",
                    failureValue());
        }
        return myself;
    }

    /**
     * Verifies that the actual result is a failure.
     *
     * @return this assertion object
     */
    public ResultAssert<F, S> isFailure() {
        isNotNull();
        if (actual.isSuccess().toBoolean()) {
            failWithMessage(
                    actual == Result.success()
                            ? "Expected result to be a failure but was an empty success"
                            : "Expected result to be a failure but was a success with value <%s>",
                    successValue());
        }
        return myself;
    }

    /**
     * Verifies that the actual result is the shared {@link Result#success() empty success}
     * singleton. Note that {@link #isSuccess()} also passes for the empty success; the two
     * assertions differ in strictness.
     *
     * @return this assertion object
     */
    public ResultAssert<F, S> isEmptySuccess() {
        isNotNull();
        if (actual != Result.success()) {
            failWithMessage("Expected result to be the empty success but was <%s>", actual);
        }
        return myself;
    }

    /**
     * Verifies that the actual result is a success carrying exactly the expected value, as
     * determined by value equality. Fails on an empty success, because it carries no value.
     *
     * @param expected the expected success value
     * @return this assertion object
     */
    public ResultAssert<F, S> hasValue(final S expected) {
        assertIsSuccess();
        final S actualValue = successValue();
        if (actualValue == null) {
            failWithMessage(
                    "Expected the result to carry the value <%s> but it was an empty success without a value",
                    expected);
        }
        objects.assertEqual(info, actualValue, expected);
        return myself;
    }

    /**
     * Verifies that the actual result is a success whose value satisfies the given requirements.
     * Fails on an empty success, because it carries no value.
     *
     * @param requirement the requirements the value must satisfy; MUST NOT be {@code null}
     * @return this assertion object
     */
    public ResultAssert<F, S> hasValueSatisfying(final Consumer<? super S> requirement) {
        Objects.requireNonNull(requirement, "requirement");
        assertIsSuccess();
        final S actualValue = successValue();
        if (actualValue == null) {
            failWithMessage("Expected the result to carry a value but it was an empty success");
        }
        requirement.accept(actualValue);
        return myself;
    }

    /**
     * Verifies that the actual result is a success and returns an object assertion over its value,
     * as an escape hatch to the full AssertJ vocabulary.
     *
     * @return an object assertion over the success value
     */
    public ObjectAssert<S> extractingValue() {
        assertIsSuccess();
        return assertThat(successValue());
    }

    /**
     * Verifies that the actual result is a failure carrying exactly the expected anomaly, as
     * determined by value equality.
     *
     * @param expected the expected failure anomaly
     * @return this assertion object
     */
    public ResultAssert<F, S> hasFailure(final F expected) {
        assertIsFailure();
        objects.assertEqual(info, failureValue(), expected);
        return myself;
    }

    /**
     * Verifies that the actual result is a failure whose anomaly satisfies the given requirements.
     *
     * @param requirement the requirements the anomaly must satisfy; MUST NOT be {@code null}
     * @return this assertion object
     */
    public ResultAssert<F, S> hasFailureSatisfying(final Consumer<? super F> requirement) {
        Objects.requireNonNull(requirement, "requirement");
        assertIsFailure();
        requirement.accept(failureValue());
        return myself;
    }

    /**
     * Verifies that the actual result is a failure and returns an object assertion over its
     * anomaly, as an escape hatch to the full AssertJ vocabulary.
     *
     * @return an object assertion over the failure anomaly
     */
    public ObjectAssert<F> extractingFailure() {
        assertIsFailure();
        return assertThat(failureValue());
    }

    private void assertIsSuccess() {
        isNotNull();
        if (actual.isFailure().toBoolean()) {
            failWithMessage(
                    "Expected result to be a success but was a failure with anomaly <%s>",
                    failureValue());
        }
    }

    private void assertIsFailure() {
        isNotNull();
        if (actual.isSuccess().toBoolean()) {
            failWithMessage(
                    actual == Result.success()
                            ? "Expected result to be a failure but was an empty success"
                            : "Expected result to be a failure but was a success with value <%s>",
                    successValue());
        }
    }

    private S successValue() {
        return actual.orNull();
    }

    private F failureValue() {
        return actual.fold(anomaly -> anomaly, value -> null);
    }
}
