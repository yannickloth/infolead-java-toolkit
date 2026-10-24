package eu.infolead.jtk.fp.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.either.Maybe;

class FieldPathTest {

    @Test
    void parsesNamedAndIndexedSegments() {
        final FieldPath path = FieldPath.parse("items[0].price");
        assertEquals(List.of("items", "[0]", "price"), path.segments());
        assertEquals(3, path.size());
        assertFalse(path.isEmpty());
        assertEquals("items", path.get(0));
        assertEquals(Maybe.of("price"), path.last());
        assertEquals("items[0].price", path.toString());
    }

    @Test
    void parsesNestedNamesAndBracketKeys() {
        assertEquals(List.of("user", "address", "city"),
                FieldPath.parse("user.address.city").segments());
        assertEquals(List.of("map", "[primary]"),
                FieldPath.parse("map[primary]").segments());
        assertEquals(List.of("[0]", "name"),
                FieldPath.parse("[0].name").segments());
    }

    @Test
    void blankInputIsTheRootPath() {
        assertTrue(FieldPath.parse("").isEmpty());
        assertTrue(FieldPath.parse("   ").isEmpty());
        assertTrue(FieldPath.root().isEmpty());
        assertEquals(Maybe.none(), FieldPath.root().last());
        assertEquals(FieldPath.root(), FieldPath.root().parent());
    }

    @Test
    void rejectsMalformedPaths() {
        assertThrows(IllegalArgumentException.class, () -> FieldPath.parse("user..name"));
        assertThrows(IllegalArgumentException.class, () -> FieldPath.parse("user."));
        assertThrows(IllegalArgumentException.class, () -> FieldPath.parse(".user"));
        assertThrows(IllegalArgumentException.class, () -> FieldPath.parse("items[0"));
        assertThrows(IllegalArgumentException.class, () -> FieldPath.parse("items[]"));
        assertThrows(IllegalArgumentException.class, () -> FieldPath.parse("items]0["));
        assertThrows(IllegalArgumentException.class, () -> FieldPath.parse("items[0]name"));
        assertThrows(NullPointerException.class, () -> FieldPath.parse(null));
    }

    @Test
    void parentAndNavigationFollowTheSegments() {
        final FieldPath path = FieldPath.parse("user.address.city");
        assertEquals("user.address", path.parent().toString());
        assertEquals("user", path.parent().parent().toString());
        assertEquals("user.address.city.zip", path.child("zip").toString());
        assertEquals("user.address.city[2]", path.index(2).toString());
        assertEquals("user.address.city.zip[2]", path.child("zip").index(2).toString());
        assertThrows(IllegalArgumentException.class, () -> path.index(-1));
        assertThrows(IllegalArgumentException.class, () -> path.child(" "));
    }

    @Test
    void prefixedMergesWithoutDuplicatingThePrefix() {
        assertEquals("user.address.city",
                FieldPath.parse("address.city").prefixed("user").toString());
        assertEquals("user.address.city",
                FieldPath.parse("user.address.city").prefixed("user.address").toString());
        assertEquals("user",
                FieldPath.root().prefixed("user").toString());
        assertEquals("name",
                FieldPath.parse("name").prefixed(FieldPath.root()).toString());
        assertTrue(FieldPath.parse("user.name").startsWith(FieldPath.parse("user")));
        assertFalse(FieldPath.parse("user.name").startsWith(FieldPath.parse("account")));
    }

    @Test
    void equalityIsBasedOnSegments() {
        assertEquals(FieldPath.parse("a.b[1]"), FieldPath.of("a", "b", "[1]"));
        assertEquals(FieldPath.parse("a.b[1]").hashCode(), FieldPath.of("a", "b", "[1]").hashCode());
        assertTrue(FieldPath.parse("a.b").equals(FieldPath.parse("a.b")));
    }
}
