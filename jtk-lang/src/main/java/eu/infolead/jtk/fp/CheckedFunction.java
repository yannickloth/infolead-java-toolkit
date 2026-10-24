package eu.infolead.jtk.fp;

import java.util.function.Function;

/**
 * A function whose computation may throw a checked exception.
 * <p>
 * {@link Function} cannot propagate checked exceptions, so callers of
 * exception-boundary helpers such as {@code Either.liftTry} and
 * {@code Result.liftTry} would otherwise have to wrap every lambda body in a
 * try/catch. This interface declares the exception in its {@code throws}
 * clause instead, so throwing lambdas and method references can be passed to
 * those helpers directly. A function that throws nothing is equally valid
 * here.
 * <p>
 * Exceptions thrown by the computation are captured by the boundary helper
 * and mapped to an error value; {@link Error} instances are deliberately not
 * captured. See {@code Either#fromTry} for the full policy.
 *
 * <p>Example &mdash; lift a throwing function into a railway switch:</p>
 *
 * <pre>{@code
 * Fn.FN1<Result<String, String>, Path> read = Result.liftTry(
 *     ex -> "Cannot read: " + ex.getMessage(),
 *     path -> Files.readString(path));            // throws IOException
 *
 * Result<String, String> content = read.apply(Path.of("config.properties"));
 * }</pre>
 *
 * @param <T> the type of the input argument
 * @param <R> the type of the produced value
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
    /**
     * Produces a value from the specified argument, possibly by throwing a
     * checked exception.
     *
     * @param argument the input argument
     * @return the produced value
     * @throws Exception if the computation fails
     */
    R apply(T argument) throws Exception;

    /**
     * Adapts this function to {@link Function}: checked exceptions are rethrown
     * wrapped in an {@link UncheckedException}, while runtime exceptions and
     * errors are rethrown unchanged. An {@link InterruptedException} keeps its
     * interrupt status: the flag is restored before the exception propagates,
     * so handing the adapter to executors does not defeat cooperative
     * cancellation.
     *
     * @return an unchecked function running the same computation
     */
    default Function<T, R> unchecked() {
        return argument -> {
            try {
                return apply(argument);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw UncheckedException.<RuntimeException>throwUnchecked(e);
            } catch (final Exception e) {
                throw UncheckedException.<RuntimeException>throwUnchecked(e);
            }
        };
    }
}
