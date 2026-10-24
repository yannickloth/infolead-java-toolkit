package eu.infolead.jtk.fp;

import java.util.function.Supplier;

/**
 * A supplier whose computation may throw a checked exception.
 * <p>
 * {@link Supplier} cannot propagate checked exceptions, so callers of
 * exception-boundary helpers such as {@code Either.fromTry} and
 * {@code Result.fromTry} would otherwise have to wrap every lambda body in a
 * try/catch. This interface declares the exception in its {@code throws}
 * clause instead, so throwing lambdas and method references can be passed to
 * those helpers directly. A lambda that throws nothing is equally valid here.
 * <p>
 * Exceptions thrown by the computation are captured by the boundary helper
 * and mapped to an error value; {@link Error} instances are deliberately not
 * captured. See {@code Either#fromTry} for the full policy.
 *
 * <p>Example &mdash; a lambda that throws a checked exception, passed straight
 * to an exception boundary without any try/catch:</p>
 *
 * <pre>{@code
 * Result<String, String> config = Result.fromTry(
 *     ex -> "Cannot read config: " + ex.getMessage(),
 *     () -> Files.readString(Path.of("config.properties")));
 * }</pre>
 *
 * @param <T> the type of the supplied value
 */
@FunctionalInterface
public interface CheckedSupplier<T> {
    /**
     * Produces a value, possibly by throwing a checked exception.
     *
     * @return the produced value
     * @throws Exception if the computation fails
     */
    T get() throws Exception;

    /**
     * Adapts this supplier to {@link Supplier}: checked exceptions are rethrown
     * wrapped in an {@link UncheckedException}, while runtime exceptions and
     * errors are rethrown unchanged. An {@link InterruptedException} keeps its
     * interrupt status: the flag is restored before the exception propagates,
     * so handing the adapter to executors does not defeat cooperative
     * cancellation.
     *
     * @return an unchecked supplier running the same computation
     */
    default Supplier<T> unchecked() {
        return () -> {
            try {
                return get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw UncheckedException.<RuntimeException>throwUnchecked(e);
            } catch (final Exception e) {
                throw UncheckedException.<RuntimeException>throwUnchecked(e);
            }
        };
    }
}
