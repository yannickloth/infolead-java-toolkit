package eu.infolead.jtk.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

class BoolTest {

    @Test
    void eagerAndRejectsNullOnBothReceivers() {
        assertThrows(NullPointerException.class, () -> Bool.TRUE.and((Bool) null));
        assertThrows(NullPointerException.class, () -> Bool.FALSE.and((Bool) null));
    }

    @Test
    void eagerOrRejectsNullOnBothReceivers() {
        assertThrows(NullPointerException.class, () -> Bool.TRUE.or((Bool) null));
        assertThrows(NullPointerException.class, () -> Bool.FALSE.or((Bool) null));
    }

    @Test
    void lazyAndRejectsANullSupplierOnBothReceivers() {
        assertThrows(NullPointerException.class, () -> Bool.TRUE.and((Supplier<Bool>) null));
        assertThrows(NullPointerException.class, () -> Bool.FALSE.and((Supplier<Bool>) null));
    }

    @Test
    void lazyAndRejectsANullSupplierResult() {
        assertThrows(NullPointerException.class, () -> Bool.TRUE.and(() -> null));
        assertEquals(Bool.FALSE, Bool.FALSE.and(() -> null));
    }

    @Test
    void truthTablesRemainCorrect() {
        assertEquals(Bool.TRUE, Bool.TRUE.and(() -> Bool.TRUE));
        assertEquals(Bool.FALSE, Bool.FALSE.and(() -> Bool.TRUE));
        assertEquals(Bool.TRUE, Bool.FALSE.or(Bool.TRUE));
        assertEquals(Bool.FALSE, Bool.TRUE.xor(Bool.TRUE));
    }
}
