package eu.infolead.jtk.fp.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.anomaly.StandardErrorType;
import eu.infolead.jtk.fp.either.Maybe;

class ValidationErrorTest {

    @Test
    void toStringDoesNotLeakTheRejectedValue() {
        final ValidationError error = new ValidationError(
                "password",
                StandardErrorType.REQUIRED,
                new Object[] { "context-secret" },
                Maybe.of("rejected-secret"),
                Maybe.none());

        assertFalse(error.toString().contains("rejected-secret"));
        assertFalse(error.toString().contains("context-secret"));
        assertTrue(error.formatWithRejectedValue().contains("rejected-secret"));
    }

    @Test
    void contextArgsAccessorReturnsADefensiveCopy() {
        final ValidationError error = new ValidationError(
                "field",
                StandardErrorType.REQUIRED,
                new Object[] { "original" },
                Maybe.none(),
                Maybe.none());
        final Object[] first = error.contextArgs();
        final Object[] second = error.contextArgs();

        assertNotSame(first, second);
        first[0] = "mutated";
        assertTrue("original".equals(error.contextArgs()[0]));
    }
}
