package eu.infolead.jtk.fp.either;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Result.class)
public class RailwayTest {
  @Test
  public void testSuccessTrack() {
    var helloResult = Result.success("hello");
    var firstBound = helloResult.bind(() -> Result.success(" there"));
    var lastSuccess=" and there ";
    var secondBound = firstBound.bind(() -> Result.success(lastSuccess));
    Assertions.assertTrue(secondBound.isSuccess().toBoolean(), "not a Success");
    Assertions.assertEquals(secondBound.fold(o -> null,s -> s),lastSuccess, "not a Success");
  }
  @Test
  public void testLongerSuccessTrack() {
    var helloResult = Result.success("hello");
    var firstBound = helloResult.bind(() -> Result.success(" there"));
    firstBound=firstBound.bind(() -> Result.success(", there"));
    firstBound=firstBound.bind(() -> Result.success(", there"));
    firstBound=firstBound.bind(() -> Result.success(", there"));
    firstBound=firstBound.bind(() -> Result.success(", there"));
    firstBound=firstBound.bind(() -> Result.success(", there"));
    var lastSuccessString=" and there ";
    var secondBound = firstBound.bind(() -> Result.success(lastSuccessString));

    Assertions.assertTrue(secondBound.isSuccess().toBoolean(), "not a Success");
    Assertions.assertEquals(secondBound.fold(o -> null,s -> s),lastSuccessString, "not a Success");
  }

  @Test
  public void testFailureTrack() {
    var failureText = "failed to say hello";
    var notHelloResult = Result.failure(failureText);
    var firstBound = notHelloResult.bind(() -> Result.success(" there"));
    var secondBound = firstBound.bind(() -> Result.success(" and there "));
    Assertions.assertTrue(secondBound.isFailure().toBoolean(), "not a failure");
    Assertions.assertEquals(failureText,secondBound.fold(s -> s,s->null));
  }
}
