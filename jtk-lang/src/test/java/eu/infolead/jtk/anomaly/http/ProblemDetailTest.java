package eu.infolead.jtk.anomaly.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.either.Maybe;

class ProblemDetailTest {

    @Test
    void missingProblemTypeDefaultsToAboutBlank() {
        assertEquals(URI.create("about:blank"),
                StandardProblemDetail.fromJson(null, null, null, null, null).getProblemType());
    }

    @Test
    void multiStatusResponseRequiresAnHref() {
        assertThrows(NullPointerException.class, () -> new MultiStatusResponseElement(null, 423));
    }

    @Test
    void multiStatusResponseRejectsAnInvalidStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> new MultiStatusResponseElement(URI.create("/resource"), 602));
    }

    @Test
    void multiStatusProblemDetailRequiresHttp207() {
        assertThrows(IllegalArgumentException.class,
                () -> new StandardMultiStatusProblemDetail(
                        URI.create("about:blank"),
                        Maybe.none(),
                        Maybe.some(200),
                        Maybe.none(),
                        Maybe.none(),
                        java.util.List.of()));
    }

    @Test
    void multiStatusWrapperToleratesAMissingResponseList() {
        assertEquals(0, new StandardMultiStatusProblemDetail.MultiStatus(null).response().size());
    }
}
