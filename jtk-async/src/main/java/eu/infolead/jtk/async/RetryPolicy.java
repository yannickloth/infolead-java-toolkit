package eu.infolead.jtk.async;

import eu.infolead.jtk.fp.either.Maybe;
import java.time.Duration;
import java.util.Objects;

/**
 * How many times an operation may be retried and how long the attempts may take.
 *
 * <p>{@code maxAttempts} counts the first attempt, so a value of 1 disables retries. The retry
 * engine always consults the {@link FailureStrategy} as well: a policy that allows retries cannot
 * repeat a failure the strategy rejects, and no policy can repeat a cancellation.
 *
 * <pre>{@code
 * RetryPolicy policy = RetryPolicy.of(4, Backoff.exponential(
 *                 Duration.ofMillis(100), 2.0d, Duration.ofSeconds(5)))
 *         .withAttemptTimeout(Duration.ofSeconds(2))
 *         .withOverallDeadline(Duration.ofSeconds(20));
 * }</pre>
 *
 * @param maxAttempts the total number of attempts, including the first
 * @param backoff the delay between attempts
 * @param overallDeadline an optional budget for all attempts together
 * @param attemptTimeout an optional budget for each attempt separately
 */
public record RetryPolicy(
        int maxAttempts,
        Backoff backoff,
        Maybe<Duration> overallDeadline,
        Maybe<Duration> attemptTimeout) {

    /**
     * Canonical constructor with validation.
     *
     * @param maxAttempts the total number of attempts
     * @param backoff the delay between attempts
     * @param overallDeadline the overall budget
     * @param attemptTimeout the per-attempt budget
     */
    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "maxAttempts must be at least 1, was " + maxAttempts);
        }
        Objects.requireNonNull(backoff, "backoff");
        Objects.requireNonNull(overallDeadline, "overallDeadline");
        Objects.requireNonNull(attemptTimeout, "attemptTimeout");
        requireNonNegative(overallDeadline, "overallDeadline");
        requireNonNegative(attemptTimeout, "attemptTimeout");
    }

    /**
     * Creates a policy without deadlines.
     *
     * @param maxAttempts the total number of attempts
     * @param backoff the delay between attempts
     * @return the policy
     */
    public static RetryPolicy of(final int maxAttempts, final Backoff backoff) {
        return new RetryPolicy(maxAttempts, backoff, Maybe.none(), Maybe.none());
    }

    /**
     * Returns a copy with an overall deadline.
     *
     * @param deadline the budget for all attempts
     * @return the copy
     */
    public RetryPolicy withOverallDeadline(final Duration deadline) {
        return new RetryPolicy(maxAttempts, backoff, Maybe.of(deadline), attemptTimeout);
    }

    /**
     * Returns a copy with a per-attempt deadline.
     *
     * @param timeout the budget for each attempt
     * @return the copy
     */
    public RetryPolicy withAttemptTimeout(final Duration timeout) {
        return new RetryPolicy(maxAttempts, backoff, overallDeadline, Maybe.of(timeout));
    }

    /**
     * Returns whether another attempt is allowed after the given one.
     *
     * @param attempt the 1-based attempt that just failed
     * @return true when attempts remain
     */
    public boolean hasAttemptsLeft(final int attempt) {
        return attempt < maxAttempts;
    }

    private static void requireNonNegative(final Maybe<Duration> duration, final String name) {
        if (duration.isPresent().toBoolean() && duration.or(Duration.ZERO).isNegative()) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
