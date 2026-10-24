package eu.infolead.jtk.fp;

/**
 * A runnable whose computation may throw a checked exception.
 * <p>
 * {@link Runnable} cannot propagate checked exceptions, so callers of
 * exception-boundary helpers such as {@code Result.fromTry} would otherwise
 * have to wrap every lambda body in a try/catch. This interface declares the
 * exception in its {@code throws} clause instead, so throwing lambdas and
 * method references can be passed to those helpers directly. A lambda that
 * throws nothing is equally valid here.
 * <p>
 * Exceptions thrown by the computation are captured by the boundary helper
 * and mapped to an error value; {@link Error} instances are deliberately not
 * captured. See {@code Either#fromTry} for the full policy.
 *
 * <p>Example &mdash; run a throwing side effect on the railway without any
 * try/catch:</p>
 *
 * <pre>{@code
 * Result<String, Void> sent = Result.fromTry(
 *     ex -> "Welcome email failed: " + ex.getMessage(),
 *     () -> mailer.sendWelcome(user));     // may declare a checked exception
 * }</pre>
 */
@FunctionalInterface
public interface CheckedRunnable {
    /**
     * Runs the computation, possibly by throwing a checked exception.
     *
     * @throws Exception if the computation fails
     */
    void run() throws Exception;

    /**
     * Adapts this runnable to {@link Runnable}: checked exceptions are rethrown
     * wrapped in an {@link UncheckedException}, while runtime exceptions and
     * errors are rethrown unchanged. An {@link InterruptedException} keeps its
     * interrupt status: the flag is restored before the exception propagates,
     * so handing the adapter to executors does not defeat cooperative
     * cancellation.
     *
     * @return an unchecked runnable running the same computation
     */
    default Runnable unchecked() {
        return () -> {
            try {
                run();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw UncheckedException.<RuntimeException>throwUnchecked(e);
            } catch (final Exception e) {
                throw UncheckedException.<RuntimeException>throwUnchecked(e);
            }
        };
    }
}
