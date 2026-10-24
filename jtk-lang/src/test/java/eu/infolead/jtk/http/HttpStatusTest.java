package eu.infolead.jtk.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.rfc.RfcReference;

class HttpStatusTest {

    private static final RfcReference RFC_9110 = new RfcReference(9110);

    @Test
    void allEnumConstantsAreValidAndUnique() {
        final Set<Integer> codes = new HashSet<>();
        for (final HttpStatus status : HttpStatus.values()) {
            assertTrue(HttpStatusCode.isValid(status.code()).toBoolean(),
                    () -> "Invalid status code: " + status);
            assertTrue(codes.add(status.code()), () -> "Duplicate status code: " + status);
            assertNotNull(status.rfcReference());
            assertTrue(status.rfcReference().number() > 0);
            assertNotNull(status.rfcSection());
            assertTrue(status.description() != null && !status.description().isBlank());
            assertNotNull(status.additionalInformation());
        }
    }

    @Test
    void knownCodeReturnsTheEnumConstant() {
        assertSame(HttpStatus.OK, HttpStatus.of(200, RFC_9110, Maybe.none(), "ignored"));
        assertSame(HttpStatus.NOT_FOUND, HttpStatus.of(404, RFC_9110, Maybe.some("15.5"), "ignored"));
    }

    @Test
    void unknownValidCodeReturnsACustomStatus() {
        final HttpStatusCode custom = HttpStatus.of(299, RFC_9110, Maybe.some("15.3"), "Custom Status");

        assertEquals(299, custom.code());
        assertEquals("Custom Status", custom.description());
        assertEquals(9110, custom.rfcNumber());
        assertEquals(Maybe.some("15.3"), custom.rfcSection());
    }

    @Test
    void invalidCodeAndNullMetadataAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> HttpStatus.of(99, RFC_9110, Maybe.none(), "Invalid"));
        assertThrows(NullPointerException.class,
                () -> HttpStatus.of(200, null, Maybe.none(), "OK"));
        assertThrows(NullPointerException.class,
                () -> HttpStatus.of(200, RFC_9110, null, "OK"));
        assertThrows(NullPointerException.class,
                () -> HttpStatus.of(200, RFC_9110, Maybe.none(), null));
    }

    @Test
    void validityAndUnassignedBoundariesFollowIana() {
        assertTrue(!HttpStatusCode.isValid(99).toBoolean());
        assertTrue(HttpStatusCode.isValid(100).toBoolean());
        assertTrue(HttpStatusCode.isValid(599).toBoolean());
        assertTrue(!HttpStatusCode.isValid(600).toBoolean());

        assertTrue(!HttpStatusCode.isUnassignedCode(103).toBoolean());
        assertTrue(HttpStatusCode.isUnassignedCode(104).toBoolean());
        assertTrue(HttpStatusCode.isUnassignedCode(199).toBoolean());
        assertTrue(!HttpStatusCode.isUnassignedCode(200).toBoolean());
        assertTrue(HttpStatusCode.isUnassignedCode(209).toBoolean());
        assertTrue(HttpStatusCode.isUnassignedCode(225).toBoolean());
        assertTrue(!HttpStatusCode.isUnassignedCode(226).toBoolean());
        assertTrue(HttpStatusCode.isUnassignedCode(309).toBoolean());
        assertTrue(HttpStatusCode.isUnassignedCode(419).toBoolean());
        assertTrue(!HttpStatusCode.isUnassignedCode(451).toBoolean());
        assertTrue(HttpStatusCode.isUnassignedCode(512).toBoolean());
        assertTrue(HttpStatusCode.isUnassignedCode(599).toBoolean());
    }

    @Test
    void defaultStatusValidatesItsComponents() {
        final HttpStatusCode status = new HttpStatusCode.DefaultHttpStatus(
                200, RFC_9110, Maybe.none(), "Custom");

        assertEquals(200, status.code());
        assertEquals(9110, status.rfcNumber());
        assertTrue(status.isValid().toBoolean());
        assertTrue(!status.isUnassigned().toBoolean());

        assertThrows(IllegalArgumentException.class,
                () -> new HttpStatusCode.DefaultHttpStatus(99, RFC_9110, Maybe.none(), "Invalid"));
        assertThrows(NullPointerException.class,
                () -> new HttpStatusCode.DefaultHttpStatus(200, null, Maybe.none(), "OK"));
        assertThrows(NullPointerException.class,
                () -> new HttpStatusCode.DefaultHttpStatus(200, RFC_9110, null, "OK"));
        assertThrows(NullPointerException.class,
                () -> new HttpStatusCode.DefaultHttpStatus(200, RFC_9110, Maybe.none(), null));
    }
}
