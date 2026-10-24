package eu.infolead.jtk.fp.either;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class MaybeTest {

    @Test
    void someIsPresentAndNoneIsEmpty() {
        assertTrue(Maybe.some("value").isPresent().toBoolean());
        assertFalse(Maybe.none().isPresent().toBoolean());
        assertTrue(Maybe.none().isEmpty().toBoolean());
    }

    @Test
    void ofRejectsNull() {
        assertThrows(NullPointerException.class, () -> Maybe.<String>of((String) null));
    }

    @Test
    void ofBlankableHandlesNullAndBlank() {
        assertTrue(Maybe.ofBlankable(null).isEmpty().toBoolean());
        assertTrue(Maybe.ofBlankable("   ").isEmpty().toBoolean());
        assertEquals(Maybe.of("value"), Maybe.ofBlankable("value"));
    }

    @Test
    void mappingToNullBecomesNone() {
        assertTrue(Maybe.some("value").map(value -> (String) null).isEmpty().toBoolean());
    }

    @Test
    void whenDoesNotInvokeTheProviderForAFalseCondition() {
        final AtomicInteger calls = new AtomicInteger();
        final Maybe<String> result = Maybe.when(false, () -> {
            calls.incrementAndGet();
            return "value";
        });
        assertTrue(result.isEmpty().toBoolean());
        assertEquals(0, calls.get());
    }
}
