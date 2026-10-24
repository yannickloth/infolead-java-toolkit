package eu.infolead.jtk.fp.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.anomaly.StandardErrorType;

class ValidatorTest {

    private record Booking(String start, String end) {
    }

    @Test
    void checkRejectsNullValuesWithAClearMessage() {
        assertThrows(NullPointerException.class,
                () -> Validator.field("name").check(null, value -> true, StandardErrorType.REQUIRED));
    }

    @Test
    void crossFieldInvariantsPassAndFailAtTheGivenPath() {
        final ValidationResult<Booking> success = Validator.object(new Booking("a", "b"))
                .invariant("period", booking -> booking.start().compareTo(booking.end()) < 0,
                        "start must be before end")
                .result();
        assertTrue(success.isSuccess().toBoolean());

        final ValidationResult<Booking> failure = Validator.object(new Booking("z", "a"))
                .invariant("period", booking -> booking.start().compareTo(booking.end()) < 0,
                        "start must be before end")
                .result();
        assertTrue(failure.isFailure().toBoolean());
        assertEquals(1, failure.getErrors().size());
        assertEquals("period", failure.getErrors().get(0).fieldPath());
        assertEquals(StandardErrorType.CROSS_FIELD_VIOLATION, failure.getErrors().get(0).errorType());
    }

    @Test
    void crossFieldInvariantsAccumulateWithFieldErrors() {
        final ValidationResult<Booking> result = Validator.object(new Booking(null, "a"))
                .validateAndAccumulate(Booking::start,
                        value -> Validator.<String>field("start").required(value))
                .invariant("period",
                        booking -> booking.start() != null && booking.start().compareTo(booking.end()) < 0,
                        "start must be before end")
                .result();

        assertEquals(2, result.getErrors().size());
        assertEquals("start", result.getErrors().get(0).fieldPath());
        assertEquals("period", result.getErrors().get(1).fieldPath());
    }

    @Test
    void wholeObjectInvariantsUseTheRootPath() {
        final ValidationResult<Booking> result = Validator.object(new Booking("z", "a"))
                .invariant(booking -> false, "booking is invalid")
                .result();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).fieldPath().isEmpty());
        assertTrue(result.getErrors().get(0).format().startsWith("[CROSS_FIELD_VIOLATION] "),
                result.getErrors().get(0).format());
    }
}
