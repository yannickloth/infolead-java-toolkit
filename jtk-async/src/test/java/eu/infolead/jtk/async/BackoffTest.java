package eu.infolead.jtk.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BackoffTest {

    @Test
    void fixedRepeatsTheSameDelay() {
        final Backoff backoff = Backoff.fixed(Duration.ofMillis(250));

        assertEquals(Duration.ofMillis(250), backoff.delayFor(1));
        assertEquals(Duration.ofMillis(250), backoff.delayFor(9));
    }

    @Test
    void exponentialGrowsAndStaysWithinItsCeiling() {
        // A source that always returns the top of the jitter window.
        final Random nearlyOne =
                new Random(1L) {
                    @Override
                    public double nextDouble() {
                        return 0.999_999d;
                    }
                };
        final Backoff backoff =
                Backoff.exponential(Duration.ofMillis(100), 2.0d, Duration.ofSeconds(1), nearlyOne);

        final long first = backoff.delayFor(1).toMillis();
        final long second = backoff.delayFor(2).toMillis();
        final long fifth = backoff.delayFor(5).toMillis();

        assertTrue(first >= 95 && first <= 100, "first delay was " + first);
        assertTrue(second >= 190 && second <= 200, "second delay was " + second);
        assertTrue(fifth <= 1000, "delay must never exceed the ceiling");
        assertEquals(999, fifth);
    }

    @Test
    void jitterStaysWithinTheWindow() {
        final Backoff backoff =
                Backoff.exponential(
                        Duration.ofMillis(100), 2.0d, Duration.ofMillis(200), new Random(42L));

        for (int attempt = 1; attempt <= 20; attempt++) {
            final long delay = backoff.delayFor(attempt).toMillis();
            assertTrue(delay >= 0 && delay <= 200, "delay out of range: " + delay);
        }
    }

    @Test
    void theConvenienceFactoryProducesAJitteredDelay() {
        final Backoff backoff =
                Backoff.exponential(Duration.ofMillis(10), 2.0d, Duration.ofSeconds(1));

        assertTrue(backoff.delayFor(2).toMillis() <= 20);
    }

    @Test
    void aZeroBaseReturnsZero() {
        assertEquals(
                Duration.ZERO,
                Backoff.exponential(Duration.ZERO, 2.0d, Duration.ofSeconds(1)).delayFor(5));
    }

    @Test
    void nullAndInfiniteConfigurationIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> Backoff.exponential(null, 2.0d, Duration.ofSeconds(1)));
        assertThrows(
                NullPointerException.class,
                () -> Backoff.exponential(Duration.ofMillis(1), 2.0d, null));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Backoff.exponential(
                                Duration.ofMillis(1),
                                Double.POSITIVE_INFINITY,
                                Duration.ofSeconds(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> Backoff.exponential(Duration.ofMillis(1), 2.0d, Duration.ofMillis(-1)));
    }

    @Test
    void invalidConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Backoff.fixed(Duration.ofMillis(-1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> Backoff.exponential(Duration.ofMillis(1), 0.5d, Duration.ofSeconds(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> RetryPolicy.of(0, Backoff.fixed(Duration.ofMillis(1))));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RetryPolicy.of(1, Backoff.fixed(Duration.ofMillis(1)))
                                .withOverallDeadline(Duration.ofMillis(-1)));
    }
}
