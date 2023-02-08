package eu.infolead.jtk.fp;

public final class UncheckedException extends RuntimeException {
    private static final long serialVersionUID = -1L;

    private UncheckedException(final Throwable cause) {
        super(cause);
    }

    public static <E extends Exception> UncheckedException uncheck(final E cause) {
        return new UncheckedException(cause);
    }

    public static <E extends Exception> void throwUnchecked(final E cause) {
        if (RuntimeException.class.isAssignableFrom(cause.getClass())) {
            throw (RuntimeException) cause;
        } else {
            throw new UncheckedException(cause);
        }
    }

    public static <R, E extends Exception> R throwUnchecked(final E cause, final Class<R> resultClass) {
        if (RuntimeException.class.isAssignableFrom(cause.getClass())) {
            throw (RuntimeException) cause;
        } else {
            throw new UncheckedException(cause);
        }
    }
}
