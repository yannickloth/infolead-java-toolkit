package eu.infolead.jtk.organisms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.either.Result;

class SexTest {

    @Test
    void nullAndBlankValuesAreUnspecified() {
        assertEquals(Sex.UNSPECIFIED, sexOf(null));
        assertEquals(Sex.UNSPECIFIED, sexOf("   "));
    }

    @Test
    void parsingIsCaseInsensitiveAndIgnoresSurroundingWhitespace() {
        assertEquals(Sex.MALE, sexOf("male"));
        assertEquals(Sex.FEMALE, sexOf(" FEMALE "));
        assertEquals(Sex.MALE, sexOf("\u2003MALE"));
    }

    @Test
    void unknownValuesReturnAFailureWithTheOriginalInput() {
        final Result<String, Sex> result = Sex.of("male x");
        assertTrue(result.isFailure().toBoolean());
        result.fold(message -> {
            assertTrue(message.contains("male x"));
            return null;
        }, sex -> {
            throw new AssertionError("expected a failure");
        });
    }

    private static Sex sexOf(final String value) {
        return Sex.of(value).fold(message -> {
            throw new AssertionError(message);
        }, sex -> sex);
    }
}
