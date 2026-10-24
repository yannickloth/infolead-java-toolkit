package eu.infolead.jtk.time;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class TZTest {

    @Test
    void everyConstantIsAValidIanaZoneId() {
        for (final TZ timeZone : TZ.values()) {
            assertDoesNotThrow(() -> ZoneId.of(timeZone.getId()),
                    () -> "Invalid IANA zone id for " + timeZone.name() + ": " + timeZone.getId());
        }
    }

    @Test
    void etcGmtSignsFollowTheIanaConvention() {
        assertEquals("Etc/GMT+1", TZ.ETC_GMT_PLUS1.getId());
        assertEquals("Etc/GMT-1", TZ.ETC_GMT_MINUS1.getId());
    }

    @Test
    void canonicalIdsRemovedFromTheOldSetArePresent() {
        assertTrue(TZ.values().length > 600);
        assertTrue(java.util.Arrays.stream(TZ.values())
                .anyMatch(zone -> "America/Argentina/Catamarca".equals(zone.getId())));
        assertTrue(java.util.Arrays.stream(TZ.values())
                .anyMatch(zone -> "America/Kentucky/Louisville".equals(zone.getId())));
    }

    @Test
    void javaUtilTimeZoneKeepsTheZoneId() {
        assertEquals("Asia/Tokyo", TZ.ASIA_TOKYO.toJavaUtilTimeZone().getID());
    }
}
