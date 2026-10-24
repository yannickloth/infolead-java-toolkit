package eu.infolead.jtk.test.serial;

import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.lang.CompilerWarning;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Round-trip helpers for the toolkit's two independent serialization contracts:
 *
 * <ul>
 *   <li><strong>Session replication</strong> through {@code java.io.Serializable} &mdash; {@link
 *       #java(Object)} and the split {@link #serializeToJava(Object)}/{@link
 *       #deserializeFromJava(byte[])} pair, which also lets tests feed corrupted streams to the
 *       deserialization paths.
 *   <li><strong>The JSON wire envelope</strong> through Jackson&nbsp;3 &mdash; the {@link
 *       #jackson(JsonMapper, Object, Class)} variants, plus {@link #envelopeType(JsonMapper, Class,
 *       Class, Class)} to build the parameterized target type for the {@code left}/{@code right}
 *       envelope of an {@link Either} subtype ({@code Either}, {@code Maybe}, {@code Result},
 *       {@code Validation}).
 * </ul>
 *
 * <p>This is a test-scope helper: it exists so that the round-trip contracts can be asserted in one
 * line, not as a general serialization facade.
 */
public final class RoundTrip {

    private RoundTrip() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Serializes the value through Java serialization.
     *
     * @param value the value to serialize
     * @return the serialized bytes
     * @throws IllegalStateException if serialization fails
     */
    public static byte[] serializeToJava(final Object value) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(value);
        } catch (final IOException e) {
            throw new IllegalStateException("Java serialization failed for " + value, e);
        }
        return bytes.toByteArray();
    }

    /**
     * Deserializes a value previously written by {@link #serializeToJava(Object)}.
     *
     * @param <T> the expected value type
     * @param bytes the serialized bytes
     * @return the deserialized value
     * @throws IllegalStateException if deserialization fails; this is the expected outcome for
     *     corrupted streams
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeFromJava(final byte[] bytes) {
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) input.readObject();
        } catch (final IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Java deserialization failed", e);
        }
    }

    /**
     * Serializes and deserializes the value through Java serialization and returns the result.
     * Session-replication round trips MUST preserve {@code equals}/{@code hashCode}, and the
     * toolkit's singletons MUST come back as the same instance.
     *
     * @param <T> the value type
     * @param value the value to round-trip
     * @return the round-tripped value
     */
    public static <T> T java(final T value) {
        return deserializeFromJava(serializeToJava(value));
    }

    /**
     * Serializes the value to JSON with the given mapper and reads it back typed by its runtime
     * class. Suitable for concrete types such as {@code StandardProblemDetail}.
     *
     * @param <T> the value type
     * @param mapper the mapper to use
     * @param value the value to round-trip
     * @return the round-tripped value
     */
    @SuppressWarnings("unchecked")
    public static <T> T jackson(final JsonMapper mapper, final T value) {
        Objects.requireNonNull(value, "value");
        return jackson(mapper, value, (Class<T>) value.getClass());
    }

    /**
     * Serializes the value to JSON with the given mapper and reads it back as the given type.
     *
     * @param <T> the value type
     * @param mapper the mapper to use
     * @param value the value to round-trip
     * @param type the type to read back as
     * @return the round-tripped value
     */
    public static <T> T jackson(final JsonMapper mapper, final T value, final Class<T> type) {
        return mapper.readValue(mapper.writeValueAsString(value), type);
    }

    /**
     * Serializes the value to JSON with the given mapper and reads it back as the given
     * parameterized type.
     *
     * @param <T> the value type
     * @param mapper the mapper to use
     * @param value the value to round-trip
     * @param type the parameterized type to read back as
     * @return the round-tripped value
     */
    public static <T> T jackson(final JsonMapper mapper, final T value, final JavaType type) {
        return mapper.readValue(mapper.writeValueAsString(value), type);
    }

    /**
     * Builds the parameterized type for the {@code left}/{@code right} JSON envelope of an {@link
     * Either} subtype with a single type parameter ({@code Maybe}).
     *
     * @param mapper the mapper whose type factory builds the type
     * @param envelope the envelope interface, e.g. {@code Maybe.class}
     * @param singleType the payload type
     * @return the parameterized envelope type
     */
    @SuppressWarnings(CompilerWarning.RAW_TYPES)
    public static JavaType envelopeType(
            final JsonMapper mapper,
            final Class<? extends Either> envelope,
            final Class<?> singleType) {
        return mapper.getTypeFactory().constructParametricType(envelope, singleType);
    }

    /**
     * Builds the parameterized type for the {@code left}/{@code right} JSON envelope of an {@link
     * Either} subtype with two type parameters ({@code Either}, {@code Result}, {@code
     * Validation}).
     *
     * @param mapper the mapper whose type factory builds the type
     * @param envelope the envelope interface, e.g. {@code Result.class}
     * @param leftType the left payload type
     * @param rightType the right payload type
     * @return the parameterized envelope type
     */
    @SuppressWarnings(CompilerWarning.RAW_TYPES)
    public static JavaType envelopeType(
            final JsonMapper mapper,
            final Class<? extends Either> envelope,
            final Class<?> leftType,
            final Class<?> rightType) {
        return mapper.getTypeFactory().constructParametricType(envelope, leftType, rightType);
    }
}
