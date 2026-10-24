package eu.infolead.jtk.async;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The delay between retry attempts.
 *
 * <p>Two strategies are provided. {@link Fixed} waits the same amount every time; {@link
 * Exponential} grows the delay and then applies <em>full jitter</em>, drawing the actual delay
 * uniformly from zero to the computed ceiling. Jitter prevents a fleet of clients that failed
 * together from retrying together.
 *
 * <p>Backoff only computes delays. It never sleeps: the retry engine schedules the next attempt on
 * a timer, so waiting consumes no thread.
 *
 * <pre>{@code
 * Backoff backoff = Backoff.exponential(
 *         Duration.ofMillis(100), 2.0d, Duration.ofSeconds(5));
 *
 * backoff.delayFor(1); // somewhere in [0, 100ms]
 * backoff.delayFor(3); // somewhere in [0, 400ms]
 * backoff.delayFor(9); // somewhere in [0, 5s] (the ceiling)
 * }</pre>
 */
public sealed interface Backoff permits Backoff.Fixed, Backoff.Exponential {

    /**
     * Returns the delay before the given attempt.
     *
     * @param attempt the 1-based number of the attempt that just failed
     * @return the delay to wait before the next attempt
     */
    Duration delayFor(int attempt);

    /**
     * Creates a constant backoff.
     *
     * @param delay the delay between attempts
     * @return the fixed backoff
     */
    static Fixed fixed(final Duration delay) {
        return new Fixed(delay);
    }

    /**
     * Creates a jittered exponential backoff using {@link ThreadLocalRandom}.
     *
     * @param base the delay before the second attempt
     * @param multiplier the growth factor, at least 1
     * @param max the ceiling of the computed delay
     * @return the exponential backoff
     */
    static Exponential exponential(
            final Duration base, final double multiplier, final Duration max) {
        return new Exponential(base, multiplier, max, null);
    }

    /**
     * Creates a jittered exponential backoff with an explicit random source, so tests can make the
     * delay deterministic.
     *
     * @param base the delay before the second attempt
     * @param multiplier the growth factor, at least 1
     * @param max the ceiling of the computed delay
     * @param random the random source, or {@code null} for {@link ThreadLocalRandom}
     * @return the exponential backoff
     */
    static Exponential exponential(
            final Duration base, final double multiplier, final Duration max, final Random random) {
        return new Exponential(base, multiplier, max, random);
    }

    /**
     * A constant delay between attempts.
     *
     * @param delay the delay
     */
    record Fixed(Duration delay) implements Backoff {

        /**
         * Canonical constructor with validation.
         *
         * @param delay the delay
         */
        public Fixed {
            Objects.requireNonNull(delay, "delay");
            if (delay.isNegative()) {
                throw new IllegalArgumentException("delay must not be negative");
            }
        }

        @Override
        public Duration delayFor(final int attempt) {
            return delay;
        }
    }

    /**
     * An exponentially growing delay with full jitter.
     *
     * <p>The computed delay for attempt {@code n} is {@code min(max, base * multiplier^(n-1))}; the
     * returned delay is drawn uniformly from {@code [0, computed]}.
     *
     * @param base the delay before the second attempt
     * @param multiplier the growth factor
     * @param max the ceiling of the computed delay
     * @param random the random source
     */
    record Exponential(Duration base, double multiplier, Duration max, Random random)
            implements Backoff {

        /**
         * Canonical constructor with validation.
         *
         * @param base the base delay
         * @param multiplier the growth factor
         * @param max the ceiling
         * @param random the random source, or {@code null}
         */
        public Exponential {
            Objects.requireNonNull(base, "base");
            Objects.requireNonNull(max, "max");
            if (base.isNegative() || max.isNegative()) {
                throw new IllegalArgumentException("base and max must not be negative");
            }
            if (!(multiplier >= 1.0d) || Double.isInfinite(multiplier)) {
                throw new IllegalArgumentException("multiplier must be finite and at least 1");
            }
            random = random == null ? ThreadLocalRandom.current() : random;
        }

        @Override
        public Duration delayFor(final int attempt) {
            final int safeAttempt = Math.max(1, attempt);
            final double factor = Math.pow(multiplier, safeAttempt - 1);
            final double ceiling = (double) max.toNanos();
            final double computed = Math.min(ceiling, (double) base.toNanos() * factor);
            if (computed <= 0.0d) {
                return Duration.ZERO;
            }
            final long bound = (long) computed;
            return Duration.ofNanos((long) (random.nextDouble() * bound));
        }
    }
}
