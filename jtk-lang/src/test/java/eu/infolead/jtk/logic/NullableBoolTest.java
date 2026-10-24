package eu.infolead.jtk.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NullableBoolTest {

    @Test
    void ofBoolMapsNullToNullState() {
        assertEquals(NullableBool.NULL, NullableBool.of((Bool) null));
        assertEquals(NullableBool.TRUE, NullableBool.of(Bool.TRUE));
        assertEquals(NullableBool.FALSE, NullableBool.of(Bool.FALSE));
    }

    @Test
    void ofNullableMapsNullToNullState() {
        assertEquals(NullableBool.NULL, NullableBool.ofNullable(null));
        assertEquals(NullableBool.TRUE, NullableBool.ofNullable(Boolean.TRUE));
        assertEquals(NullableBool.FALSE, NullableBool.ofNullable(Boolean.FALSE));
    }
}
