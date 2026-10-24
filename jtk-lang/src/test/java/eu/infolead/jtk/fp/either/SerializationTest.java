package eu.infolead.jtk.fp.either;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.logic.Bool;
import eu.infolead.jtk.logic.NullableBool;

class SerializationTest {

    @Test
    void maybeAndEitherVariantsRoundTrip() throws Exception {
        final Maybe<String> some = Maybe.of("value");
        assertEquals(some, roundTrip(some));
        assertTrue(roundTrip(some).isPresent().toBoolean());

        final Maybe<String> none = Maybe.none();
        assertSame(none, roundTrip(none));

        final Either<String, Integer> left = Either.left("bad");
        final Either<String, Integer> right = Either.right(42);
        assertEquals(left, roundTrip(left));
        assertEquals(right, roundTrip(right));
    }

    @Test
    void resultVariantsRoundTrip() throws Exception {
        final Result<String, Integer> success = Result.success(42);
        final Result<String, Integer> failure = Result.failure("bad");
        assertEquals(success, roundTrip(success));
        assertEquals(failure, roundTrip(failure));

        final Result<String, Integer> empty = Result.success();
        assertSame(empty, roundTrip(empty));
    }

    @Test
    void validationVariantsRoundTrip() throws Exception {
        final Validation<String, Integer> valid = Validation.valid(42);
        final Validation<String, Integer> invalid = Validation.invalid("bad");

        assertEquals(valid, roundTrip(valid));
        assertEquals(invalid, roundTrip(invalid));
    }

    @Test
    void valueBasedEqualityAndHashCodeAreSharedAcrossTheHierarchy() {
        assertEquals(Maybe.of("x"), Maybe.of("x"));
        assertEquals(Maybe.of("x").hashCode(), Maybe.of("x").hashCode());
        assertEquals(Result.success(1), Result.success(1));
        assertEquals(Result.failure("x"), Result.failure("x"));
        assertEquals(Validation.valid(1), Validation.valid(1));
        assertEquals(Either.right(1), Either.right(1));
        assertEquals(Either.left("x"), Either.left("x"));
    }

    @Test
    void singletonConstantsSurviveSerialization() throws Exception {
        assertSame(Bool.TRUE, roundTrip(Bool.TRUE));
        assertSame(Bool.FALSE, roundTrip(Bool.FALSE));
        assertSame(NullableBool.TRUE, roundTrip(NullableBool.TRUE));
        assertSame(NullableBool.FALSE, roundTrip(NullableBool.FALSE));
        assertSame(NullableBool.NULL, roundTrip(NullableBool.NULL));
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(final T value) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(value);
        }
        try (ObjectInputStream input = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) input.readObject();
        }
    }
}
