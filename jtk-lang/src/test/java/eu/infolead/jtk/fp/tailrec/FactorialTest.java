package eu.infolead.jtk.fp.tailrec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = Factorial.class)
public class FactorialTest {
  @Test
  public void testFactorialRec5() {
    var result=Factorial.factorialRec(5);
    Assertions.assertEquals(120,result, "factorial(5) is not 120");
  }
  @Test
  public void testFactorialRec20k() {
    final var exception = assertThrows(StackOverflowError.class, () -> {
      var result=Factorial.factorialRec(20000);
    });
    assertNotNull(exception);
  }
}
