package eu.infolead.jtk.async;

import java.io.Serializable;

/**
 * How many operations a parallel traversal may run at the same time.
 *
 * <p>The bound is enforced by permits, not by chunking the input: a new operation starts as soon as
 * one completes, so a slow item never blocks a whole chunk of idle capacity.
 *
 * <pre>{@code
 * Parallelism eightAtATime = Parallelism.of(8);
 * }</pre>
 *
 * @param maxConcurrency the maximum number of in-flight operations; at least 1
 */
public record Parallelism(int maxConcurrency) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Canonical constructor with validation.
     *
     * @param maxConcurrency the maximum number of in-flight operations
     * @throws IllegalArgumentException if the value is below 1
     */
    public Parallelism {
        if (maxConcurrency < 1) {
            throw new IllegalArgumentException(
                    "maxConcurrency must be at least 1, was " + maxConcurrency);
        }
    }

    /**
     * Creates a parallelism bound.
     *
     * @param maxConcurrency the maximum number of in-flight operations
     * @return the bound
     */
    public static Parallelism of(final int maxConcurrency) {
        return new Parallelism(maxConcurrency);
    }

    /**
     * Returns whether two tasks may run at the same time.
     *
     * @return true when the bound is greater than one
     */
    public boolean isConcurrent() {
        return maxConcurrency > 1;
    }
}
