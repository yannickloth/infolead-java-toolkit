package eu.infolead.jtk.anomaly;

import java.util.Arrays;
import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents object or system invariant violations.
 *
 * @param invariant   the invariant that was violated
 * @param errorType   the specific invariant error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual value that violated the invariant (optional)
 * @param errorId     unique identifier for this error instance (optional)
 */
public record InvariantError(
        String invariant,
        InvariantErrorType errorType,
        Object[] contextArgs,
        Maybe<Object> actualValue,
        Maybe<String> errorId) implements SystemError {

    public InvariantError {
        Objects.requireNonNull(invariant, "invariant cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    public InvariantError(final String invariant, final InvariantErrorType errorType) {
        this(invariant, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    public InvariantError(final String invariant, final InvariantErrorType errorType, final String contextArg) {
        this(invariant, errorType, new Object[] { contextArg }, Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return String.format("Invariant '%s': %s", invariant, errorType.formatMessage(contextArgs));
    }

    @Override
    public Maybe<String> getErrorId() {
        return errorId;
    }

    @Override
    public Object[] contextArgs() {
        return contextArgs.clone();
    }

    @Override
    public Object[] getContextArgs() {
        return contextArgs();
    }

    @Override
    public Maybe<Object> getActualValue() {
        return actualValue;
    }

    @Override
    public SystemError withGeneratedId() {
        if (errorId.isPresent().toBoolean()) {
            return this;
        }
        return new InvariantError(invariant, errorType, contextArgs, actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new InvariantError(invariant, errorType, contextArgs, actualValue,
                Maybe.some(Objects.requireNonNull(customErrorId, "customErrorId")));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof InvariantError that)) {
            return false;
        }
        return invariant.equals(that.invariant)
                && errorType == that.errorType
                && actualValue.equals(that.actualValue)
                && errorId.equals(that.errorId)
                && Arrays.equals(contextArgs, that.contextArgs);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(invariant, errorType, actualValue, errorId)
                + Arrays.hashCode(contextArgs);
    }

    @Override
    public String toString() {
        return "InvariantError[invariant=" + invariant
                + ", errorType=" + errorType
                + ", contextArgs=" + Arrays.toString(contextArgs)
                + ", actualValue=" + actualValue
                + ", errorId=" + errorId + "]";
    }

    public static InvariantError objectConsistency(final String description) {
        return new InvariantError("Object consistency", InvariantErrorType.OBJECT_CONSISTENCY,
                new Object[] { description }, Maybe.none(), Maybe.none());
    }

    public static InvariantError dataCorruption(final String description) {
        return new InvariantError("Data integrity", InvariantErrorType.DATA_CORRUPTION,
                new Object[] { description }, Maybe.none(), Maybe.none());
    }
}
