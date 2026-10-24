package eu.infolead.jtk.fp.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
    void parsesAndNestsFieldPaths() {
        final ValidationError nested = ValidationError.required("address.city").under("user");
        assertEquals("user.address.city", nested.fieldPath());
        assertEquals(List.of("user", "address", "city"), nested.path().segments());
        assertTrue(nested.isNested());
        assertEquals(nested, nested.under("user"), "nesting must be idempotent");

        assertEquals("user", ValidationError.required("").under("user").fieldPath());
        assertEquals("items[0].price", ValidationError.required("price").under("items[0]").fieldPath());
        assertEquals("name", ValidationError.required("name").withFieldPath("name").fieldPath());
        assertFalse(ValidationError.required("name").isNested());

        assertThrows(IllegalArgumentException.class,
                () -> new ValidationError("a..b", StandardErrorType.REQUIRED));
    }

    @Test
    void crossFieldErrorsUseTheDedicatedTypeAndRootFormatting() {
        final ValidationError root = ValidationError.crossField("", "start must be before end");
        assertEquals(StandardErrorType.CROSS_FIELD_VIOLATION, root.errorType());
        assertEquals(400, root.getHttpStatus());
        assertTrue(root.getMessage().contains("start must be before end"));
        assertTrue(root.format().startsWith("[CROSS_FIELD_VIOLATION] "), root.format());
        assertFalse(root.format().contains("] :"), root.format());
        assertFalse(root.toProblemDetail().getDetail().startsWith(":"), root.toProblemDetail().getDetail());

        final ValidationError located = ValidationError.crossField("period", "start must be before end");
        assertTrue(located.format().contains("period: "), located.format());
        assertTrue(located.isNested() == false);
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
