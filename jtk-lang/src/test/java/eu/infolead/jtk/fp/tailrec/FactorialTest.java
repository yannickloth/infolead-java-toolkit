package eu.infolead.jtk.fp.tailrec;

import static org.junit.jupiter.api.Assertions.*;

import eu.infolead.jtk.fp.either.Maybe;
import java.math.BigInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Factorial.class)
public class FactorialTest {
  @Test
  public void testFactorialMinus1() {
    var result = Factorial.factorial(BigInteger.ONE.negate());
    assertEquals(Maybe.none(),result, "the factorial of -1 must be an empty Maybe");
  }
  @Test
  public void testFactorial0() {
    var result = Factorial.factorial(BigInteger.ZERO);
    assertEquals(Maybe.none(),result, "the factorial of zero must be an empty Maybe");
  }
  @Test
  public void testFactorial1() {
    var result = Factorial.factorial(BigInteger.ONE);
    result.apply(() -> Assertions.fail("the factorial of 1 must not be null"), r->
      assertEquals(BigInteger.ONE, r, "factorial(1) must be 1")
    );

  }
  @Test
  public void testFactorialRec5() {
    var result = Factorial.factorialRec(BigInteger.valueOf(5));
    Assertions.assertEquals(BigInteger.valueOf(120), result, "factorial(5) is not 120");
  }

  @Test
  public void testFactorialRec20k() {
    final var exception =
        assertThrows(
            StackOverflowError.class,
            () -> Factorial.factorialRec(BigInteger.valueOf(20000)));
    assertNotNull(exception);
  }

  @Test
  public void testFactorial5() {
    var result = Factorial.factorial(BigInteger.valueOf(5));
    result.apply(() -> Assertions.fail("the factorial of 5 must not be null"), r-> {
      assertTrue(r.compareTo(BigInteger.ONE) > 0);
      assertEquals(BigInteger.valueOf(120), r);
    });

  }

  @Test
  public void testFactorial16() {
    var number = 16;
    var result = Factorial.factorial(BigInteger.valueOf(number));
    System.out.println("factorial(" + number + ") = " + result);
    result.apply(() -> Assertions.fail("the factorial of 16 must not be null"), r->
      assertTrue(r.compareTo(BigInteger.ONE) > 0)
    );
  }
  @Test
  public void testFactorial17() {
    var number = 17;
    var result = Factorial.factorial(BigInteger.valueOf(number));
    System.out.println("factorial(" + number + ") = " + result);
    result.apply(() -> Assertions.fail("the factorial of 17 must not be null"), r->
      assertTrue(r.compareTo(BigInteger.ONE) > 0)
    );
  }
  @Test
  public void testFactorial20k() {
    var number = 20000;
    var result = Factorial.factorial(BigInteger.valueOf(number));
    System.out.println("factorial(" + number + ") = " + result);
    result.apply(() -> Assertions.fail("the factorial of 20.000 must not be null"), r->
      assertTrue(r.compareTo(BigInteger.ONE) > 0)
    );
  }
}
