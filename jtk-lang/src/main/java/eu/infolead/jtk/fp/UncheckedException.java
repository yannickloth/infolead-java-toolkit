package eu.infolead.jtk.fp;

import java.util.Objects;

/**
 * Helpers for unchecked exception handling.
 * <p>
 * Prefer {@link #throwUnchecked(Throwable)} over wrapping checked exceptions in
 * new runtime exceptions when the checked exception has already been handled
 * elsewhere.
 */
public final class UncheckedException extends RuntimeException {
    private static final long serialVersionUID = -1L;

    private UncheckedException(final Throwable cause) {
        super(cause);
    }

    /**
     * Wraps the specified checked exception in an unchecked exception. A runtime
     * exception is rethrown unchanged so both helpers behave consistently.
     *
     * @param cause the cause to wrap or rethrow
     * @return an unchecked exception wrapping {@code cause}
     * @throws NullPointerException if {@code cause} is {@code null}
     * @throws RuntimeException if {@code cause} is already unchecked
     */
    public static UncheckedException uncheck(final Exception cause) {
        Objects.requireNonNull(cause, "cause");
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        return new UncheckedException(cause);
    }

    /**
     * Rethrows the specified exception as an unchecked exception: runtime
     * exceptions and errors are rethrown unchanged, checked exceptions are wrapped
     * in an {@link UncheckedException}.
     *
     * @param <R>   the expected result type, never returned because this method
     *              always throws
     * @param cause the exception to rethrow
     * @return never returns normally
     * @throws NullPointerException if {@code cause} is {@code null}
     */
    public static <R> R throwUnchecked(final Throwable cause) {
        Objects.requireNonNull(cause, "cause");
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        throw new UncheckedException(cause);
    }
}
