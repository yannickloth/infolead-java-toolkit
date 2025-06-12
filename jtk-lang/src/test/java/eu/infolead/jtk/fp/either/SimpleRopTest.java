package eu.infolead.jtk.fp.either;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Simple tests for Railway Oriented Programming operations.
 */
class SimpleRopTest {

    @Test
    void testBimap() {
        Either<String, Integer> right = Either.right(5);
        Either<String, String> result = right.bimap(
            error -> "Error: " + error,
            value -> "Value: " + value
        );
        
        assertTrue(result.isRight().toBoolean());
        assertEquals("Value: 5", result.orNull());
    }

    @Test
    void testMap2() {
        Either<String, Integer> either1 = Either.right(3);
        Either<String, Integer> either2 = Either.right(4);
        
        Either<String, Integer> result = either1.map2(either2, Integer::sum);
        
        assertTrue(result.isRight().toBoolean());
        assertEquals(Integer.valueOf(7), result.orNull());
    }

    @Test
    void testFilter() {
        Either<String, Integer> either = Either.right(10);
        
        Either<String, Integer> result = either.filter(
            x -> x > 5,
            () -> "Value too small"
        );
        
        assertTrue(result.isRight().toBoolean());
        assertEquals(Integer.valueOf(10), result.orNull());
    }

    @Test
    void testFromOptional() {
        java.util.Optional<String> optional = java.util.Optional.of("value");
        
        Either<String, String> result = Either.fromOptional(
            () -> "No value",
            optional);
        
        assertTrue(result.isRight().toBoolean());
        assertEquals("value", result.orNull());
    }
}