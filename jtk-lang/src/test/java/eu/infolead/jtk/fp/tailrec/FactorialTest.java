package eu.infolead.jtk.fp.tailrec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.either.Maybe;

class FactorialTest {
    @Test
    void testFactorialMinus1() {
        final Maybe<BigInteger> result = Factorial.factorial(BigInteger.ONE.negate());
        assertEquals(Maybe.none(), result, "the factorial of -1 must be an empty Maybe");
    }

    @Test
    void testFactorial0() {
        final Maybe<BigInteger> result = Factorial.factorial(BigInteger.ZERO);
        assertEquals(Maybe.of(BigInteger.ONE), result, "the factorial of zero must be one");
    }

    @Test
    void testFactorial1() {
        final Maybe<BigInteger> result = Factorial.factorial(BigInteger.ONE);
        result.apply(
                () -> {
                    throw new AssertionError("the factorial of 1 must not be empty");
                },
                r -> assertEquals(BigInteger.ONE, r, "factorial(1) must be 1"));
    }

    @Test
    void testFactorialRec5() {
        final BigInteger result = Factorial.factorialRec(BigInteger.valueOf(5));
        assertEquals(BigInteger.valueOf(120), result, "factorial(5) is not 120");
    }

    @Test
    void testFactorialRecRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> Factorial.factorialRec(BigInteger.ONE.negate()));
    }

    @Test
    void testFactorialRec20k() {
        assertThrows(StackOverflowError.class,
                () -> Factorial.factorialRec(BigInteger.valueOf(20000)));
    }

    @Test
    void testFactorial5() {
        final Maybe<BigInteger> result = Factorial.factorial(BigInteger.valueOf(5));
        result.apply(
                () -> {
                    throw new AssertionError("the factorial of 5 must not be empty");
                },
                r -> assertEquals(BigInteger.valueOf(120), r));
    }

    @Test
    void testFactorial16() {
        final Maybe<BigInteger> result = Factorial.factorial(BigInteger.valueOf(16));
        result.apply(
                () -> {
                    throw new AssertionError("the factorial of 16 must not be empty");
                },
                r -> assertTrue(r.compareTo(BigInteger.ONE) > 0));
    }

    @Test
    void testFactorial17() {
        final Maybe<BigInteger> result = Factorial.factorial(BigInteger.valueOf(17));
        result.apply(
                () -> {
                    throw new AssertionError("the factorial of 17 must not be empty");
                },
                r -> assertTrue(r.compareTo(BigInteger.ONE) > 0));
    }

    @Test
    void testFactorial20k() {
        final Maybe<BigInteger> result = Factorial.factorial(BigInteger.valueOf(20000));
        result.apply(
                () -> {
                    throw new AssertionError("the factorial of 20.000 must not be empty");
                },
                r -> assertTrue(r.compareTo(BigInteger.ONE) > 0));
    }
}
