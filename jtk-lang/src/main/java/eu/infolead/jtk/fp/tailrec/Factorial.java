package eu.infolead.jtk.fp.tailrec;

import java.math.BigInteger;
import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * This class provides methods to compute the factorial of a specified number.
 *
 * <p>
 * The factorial function is defined for non-negative integers; by convention
 * {@code 0! == 1}. Negative inputs are rejected:
 * {@link #factorialRec(BigInteger)} throws {@link IllegalArgumentException},
 * while {@link #factorial(BigInteger)} returns an empty {@link Maybe}.
 */
public final class Factorial {

    private Factorial() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Implementation without tail call optimisation.
     *
     * @param number the non-negative number whose factorial is computed
     * @return the factorial of {@code number}
     * @throws NullPointerException     if {@code number} is {@code null}
     * @throws IllegalArgumentException if {@code number} is negative
     */
    public static BigInteger factorialRec(final BigInteger number) {
        Objects.requireNonNull(number, "number");
        if (number.signum() < 0) {
            throw new IllegalArgumentException("The factorial is not defined for negative numbers: " + number);
        }
        return number.compareTo(BigInteger.ONE) <= 0
                ? BigInteger.ONE
                : number.multiply(factorialRec(number.subtract(BigInteger.ONE)));
    }

    /**
     * Recursive factorial with tail call optimisation.
     *
     * @param number the number whose factorial is computed
     * @return the factorial of {@code number}, or an empty {@link Maybe} when
     *         {@code number} is negative
     * @throws NullPointerException if {@code number} is {@code null}
     */
    public static Maybe<BigInteger> factorial(final BigInteger number) {
        Objects.requireNonNull(number, "number");
        if (number.signum() < 0) {
            return Maybe.none();
        }
        if (number.compareTo(BigInteger.ONE) <= 0) {
            return Maybe.of(BigInteger.ONE);
        }
        final TailCall<BigInteger> tailCall = TailCall.tailCallOptimisationRecursion(
                BigInteger.ONE,
                number,
                num -> num.compareTo(BigInteger.ONE) <= 0,
                BigInteger::multiply,
                num -> num.subtract(BigInteger.ONE));
        return Maybe.of(tailCall.invoke());
    }
}
