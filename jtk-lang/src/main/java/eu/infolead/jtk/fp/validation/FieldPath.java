package eu.infolead.jtk.fp.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * A parsed path to a field in a validated object tree.
 *
 * <p>A path is a sequence of named segments and index segments. Named segments
 * are separated by dots ({@code .}); index segments are written in brackets
 * ({@code [0]}, {@code [key]}). The canonical rendering round-trips with
 * {@link #parse(String)}:</p>
 *
 * <pre>{@code
 * FieldPath path = FieldPath.parse("items[0].price");
 * path.segments();            // ["items", "[0]", "price"]
 * path.size();                // 3
 * path.last();                // Maybe.of("price")
 * path.parent();              // "items[0]"
 * path.toString();            // "items[0].price"
 * }</pre>
 *
 * <p>Parsing is strict: an empty name ({@code user..name}), a dangling dot, an
 * unterminated bracket or a missing separator after an index
 * ({@code items[0]name}) is rejected with {@link IllegalArgumentException}.
 * A blank path is the root path.</p>
 *
 * <p>Paths are immutable and value-comparable.</p>
 */
public final class FieldPath {

    private static final FieldPath ROOT = new FieldPath(List.of());

    private final List<String> segments;
    private final String text;

    private FieldPath(final List<String> segments) {
        this.segments = List.copyOf(segments);
        this.text = render(this.segments);
    }

    /**
     * Returns the root path.
     *
     * @return a path with no segments
     */
    public static FieldPath root() {
        return ROOT;
    }

    /**
     * Parses a field path.
     *
     * <pre>{@code
     * FieldPath.parse("user.address.city"); // three named segments
     * FieldPath.parse("items[0].name");     // name, index, name
     * FieldPath.parse("");                  // root
     * }</pre>
     *
     * @param path the path text; {@code null} is rejected
     * @return the parsed path
     * @throws NullPointerException     if {@code path} is {@code null}
     * @throws IllegalArgumentException if the path is malformed
     */
    public static FieldPath parse(final String path) {
        Objects.requireNonNull(path, "path");
        return path.isBlank() ? ROOT : new FieldPath(parseSegments(path));
    }

    /**
     * Creates a path from already-parsed segments.
     *
     * @param segments the segments, in order
     * @return the path
     * @throws NullPointerException     if {@code segments} or an element is
     *                                  {@code null}
     * @throws IllegalArgumentException if a segment is blank
     */
    public static FieldPath of(final String... segments) {
        Objects.requireNonNull(segments, "segments");
        return of(Arrays.asList(segments));
    }

    /**
     * Creates a path from already-parsed segments.
     *
     * @param segments the segments, in order
     * @return the path
     * @throws NullPointerException     if {@code segments} or an element is
     *                                  {@code null}
     * @throws IllegalArgumentException if a segment is blank
     */
    public static FieldPath of(final List<String> segments) {
        Objects.requireNonNull(segments, "segments");
        for (final String segment : segments) {
            Objects.requireNonNull(segment, "segments must not contain null");
            if (segment.isBlank()) {
                throw new IllegalArgumentException("A field-path segment must not be blank");
            }
        }
        return segments.isEmpty() ? ROOT : new FieldPath(segments);
    }

    /**
     * Returns the segments of this path.
     *
     * @return an immutable list of segments
     */
    public List<String> segments() {
        return segments;
    }

    /**
     * Returns the number of segments.
     *
     * @return the segment count
     */
    public int size() {
        return segments.size();
    }

    /**
     * Indicates whether this is the root path.
     *
     * @return {@code true} when the path has no segments
     */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /**
     * Returns the segment at the given position.
     *
     * @param index the zero-based position
     * @return the segment
     * @throws IndexOutOfBoundsException if the position is out of range
     */
    public String get(final int index) {
        return segments.get(index);
    }

    /**
     * Returns the last segment.
     *
     * @return the leaf segment, or {@link Maybe#none()} for the root path
     */
    public Maybe<String> last() {
        return segments.isEmpty() ? Maybe.none() : Maybe.of(segments.get(segments.size() - 1));
    }

    /**
     * Returns the parent path.
     *
     * <pre>{@code
     * FieldPath.parse("user.address.city").parent(); // "user.address"
     * FieldPath.root().parent();                     // root
     * }</pre>
     *
     * @return the path without its last segment, or the root path
     */
    public FieldPath parent() {
        if (segments.isEmpty()) {
            return ROOT;
        }
        return new FieldPath(segments.subList(0, segments.size() - 1));
    }

    /**
     * Returns a child path with a named segment appended.
     *
     * @param name the segment name
     * @return the extended path
     */
    public FieldPath child(final String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        final List<String> extended = new ArrayList<>(segments);
        extended.add(name);
        return new FieldPath(extended);
    }

    /**
     * Returns a child path with an index segment appended.
     *
     * @param index the array or collection index
     * @return the extended path
     * @throws IllegalArgumentException if the index is negative
     */
    public FieldPath index(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative, was " + index);
        }
        final List<String> extended = new ArrayList<>(segments);
        extended.add("[" + index + "]");
        return new FieldPath(extended);
    }

    /**
     * Returns this path nested under a prefix.
     *
     * <p>The merge is idempotent: a path that already starts with the prefix is
     * returned unchanged, so re-nesting an error cannot produce
     * {@code user.user.name}.</p>
     *
     * <pre>{@code
     * FieldPath.parse("address.city").prefixed(FieldPath.parse("user"));
     * // "user.address.city"
     * FieldPath.parse("user.name").prefixed(FieldPath.parse("user"));
     * // "user.name"
     * }</pre>
     *
     * @param prefix the prefix path
     * @return the merged path
     */
    public FieldPath prefixed(final FieldPath prefix) {
        Objects.requireNonNull(prefix, "prefix");
        if (prefix.isEmpty()) {
            return this;
        }
        if (isEmpty()) {
            return prefix;
        }
        if (startsWith(prefix)) {
            return this;
        }
        final List<String> merged = new ArrayList<>(prefix.segments.size() + segments.size());
        merged.addAll(prefix.segments);
        merged.addAll(segments);
        return new FieldPath(merged);
    }

    /**
     * Returns this path nested under a prefix.
     *
     * @param prefix the prefix path text
     * @return the merged path
     * @see #prefixed(FieldPath)
     */
    public FieldPath prefixed(final String prefix) {
        return prefixed(parse(prefix));
    }

    /**
     * Indicates whether this path begins with the given prefix.
     *
     * @param prefix the prefix path
     * @return {@code true} when every prefix segment matches this path in order
     */
    public boolean startsWith(final FieldPath prefix) {
        Objects.requireNonNull(prefix, "prefix");
        if (prefix.size() > segments.size()) {
            return false;
        }
        for (int index = 0; index < prefix.size(); index++) {
            final String expected = prefix.segments.get(index);
            final String actual = segments.get(index);
            if (!expected.equals(actual)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || (other instanceof FieldPath that && segments.equals(that.segments));
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }

    /**
     * Returns the canonical path text.
     *
     * @return the dot/bracket rendered path, or an empty string for the root
     */
    @Override
    public String toString() {
        return text;
    }

    private static String render(final List<String> segments) {
        final StringBuilder rendered = new StringBuilder();
        for (final String segment : segments) {
            if (segment.startsWith("[")) {
                rendered.append(segment);
            } else {
                if (rendered.length() > 0) {
                    rendered.append('.');
                }
                rendered.append(segment);
            }
        }
        return rendered.toString();
    }

    private static List<String> parseSegments(final String path) {
        final List<String> segments = new ArrayList<>();
        int position = 0;
        boolean expectName = true;
        while (position < path.length()) {
            final char current = path.charAt(position);
            if (current == '.') {
                if (expectName) {
                    throw new IllegalArgumentException(
                            "Field path '" + path + "' has an empty segment at index " + position);
                }
                expectName = true;
                position++;
                continue;
            }
            if (current == '[') {
                final int end = path.indexOf(']', position);
                if (end < 0) {
                    throw new IllegalArgumentException(
                            "Field path '" + path + "' has an unterminated index at index " + position);
                }
                final String index = path.substring(position + 1, end);
                if (index.isBlank()) {
                    throw new IllegalArgumentException(
                            "Field path '" + path + "' has an empty index at index " + position);
                }
                segments.add("[" + index + "]");
                position = end + 1;
                expectName = false;
                continue;
            }
            if (current == ']') {
                throw new IllegalArgumentException(
                        "Field path '" + path + "' has an unmatched ']' at index " + position);
            }
            if (!expectName) {
                throw new IllegalArgumentException("Field path '" + path
                        + "' is missing a separator before index " + position);
            }
            final int start = position;
            while (position < path.length()
                    && path.charAt(position) != '.'
                    && path.charAt(position) != '[') {
                position++;
            }
            final String name = path.substring(start, position).trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException(
                        "Field path '" + path + "' has an empty segment at index " + start);
            }
            segments.add(name);
            expectName = false;
        }
        if (expectName) {
            throw new IllegalArgumentException("Field path '" + path + "' ends with a separator");
        }
        return segments;
    }
}
