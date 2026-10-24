package eu.infolead.jtk.rfc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RfcReferenceTest {

    @Test
    void buildsSectionUrls() {
        final RfcReference reference = new RfcReference(7231);
        assertEquals("https://www.rfc-editor.org/rfc/rfc7231#section-6.5.1",
                reference.url("6.5.1").fold(message -> {
                    throw new AssertionError(message);
                }, url -> url.toString()));
    }

    @Test
    void rejectsLeadingZeroAndNullSections() {
        final RfcReference reference = new RfcReference(7231);
        assertTrue(reference.url("06.5").isFailure().toBoolean());
        assertTrue(reference.url((String) null).isFailure().toBoolean());
    }
}
