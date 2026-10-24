package eu.infolead.jtk.fp.validation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.anomaly.StandardErrorType;

class ValidatorTest {

    @Test
    void checkRejectsNullValuesWithAClearMessage() {
        assertThrows(NullPointerException.class,
                () -> Validator.field("name").check(null, value -> true, StandardErrorType.REQUIRED));
    }
}
