package eu.infolead.jtk.async;

import static eu.infolead.jtk.async.AsyncTestSupport.successValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import org.junit.jupiter.api.Test;

class PartitionSerializationTest {

    @Test
    void aPartitionSurvivesJavaSerialization() throws Exception {
        final Partition<String, Integer> original =
                new Partition<>(List.of("first", "second"), List.of(1, 2, 3));

        final Partition<String, Integer> copy = roundTrip(original);

        assertEquals(original, copy);
        assertEquals(List.of(1, 2, 3), copy.successes());
        assertEquals(List.of("first", "second"), copy.failures());
    }

    @Test
    void aParallelismBoundSurvivesJavaSerialization() throws Exception {
        final Parallelism original = Parallelism.of(8);

        assertEquals(original, roundTrip(original));
        assertEquals(8, roundTrip(original).maxConcurrency());
    }

    @Test
    void anEmptyPartitionCollapsesToASuccess() {
        final Partition<String, Integer> partition = new Partition<>(List.of(), List.of(1, 2));

        assertTrue(partition.isCompleteSuccess().toBoolean());
        assertTrue(partition.firstFailure().isEmpty().toBoolean());
        assertEquals(List.of(1, 2), successValue(partition.toResult()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Serializable> T roundTrip(final T value) throws Exception {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in =
                new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }
}
