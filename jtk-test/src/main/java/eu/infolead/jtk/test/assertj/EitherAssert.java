package eu.infolead.jtk.test.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import eu.infolead.jtk.fp.either.Either;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.ObjectAssert;

/**
 * Assertions for {@link Either} using the left/right vocabulary. Use {@link ResultAssert} or {@link
 * MaybeAssert} when the concrete container type is known statically.
 *
 * @param <L> the left type
 * @param <R> the right type
 */
public final class EitherAssert<L, R>
        extends AbstractObjectAssert<EitherAssert<L, R>, Either<L, R>> {

    public EitherAssert(final Either<L, R> actual) {
        super(actual, EitherAssert.class);
    }

    /**
     * Verifies that the actual either is a right value.
     *
     * @return this assertion object
     */
    public EitherAssert<L, R> isRight() {
        isNotNull();
        if (actual.isLeft().toBoolean()) {
            failWithMessage(
                    "Expected either to be a right value but was a left with <%s>", leftValue());
        }
        return myself;
    }

    /**
     * Verifies that the actual either is a left value.
     *
     * @return this assertion object
     */
    public EitherAssert<L, R> isLeft() {
        isNotNull();
        if (actual.isRight().toBoolean()) {
            failWithMessage(
                    "Expected either to be a left value but was a right with <%s>", rightValue());
        }
        return myself;
    }

    /**
     * Verifies that the actual either is a right carrying exactly the expected value, as determined
     * by value equality.
     *
     * @param expected the expected right value
     * @return this assertion object
     */
    public EitherAssert<L, R> hasRight(final R expected) {
        isRight();
        objects.assertEqual(info, rightValue(), expected);
        return myself;
    }

    /**
     * Verifies that the actual either is a left carrying exactly the expected value, as determined
     * by value equality.
     *
     * @param expected the expected left value
     * @return this assertion object
     */
    public EitherAssert<L, R> hasLeft(final L expected) {
        isLeft();
        objects.assertEqual(info, leftValue(), expected);
        return myself;
    }

    /**
     * Verifies that the actual either is a right and returns an object assertion over its value, as
     * an escape hatch to the full AssertJ vocabulary.
     *
     * @return an object assertion over the right value
     */
    public ObjectAssert<R> extractingRight() {
        isRight();
        return assertThat(rightValue());
    }

    /**
     * Verifies that the actual either is a left and returns an object assertion over its value, as
     * an escape hatch to the full AssertJ vocabulary.
     *
     * @return an object assertion over the left value
     */
    public ObjectAssert<L> extractingLeft() {
        isLeft();
        return assertThat(leftValue());
    }

    private L leftValue() {
        return actual.fold(left -> left, right -> null);
    }

    private R rightValue() {
        return actual.orNull();
    }
}
