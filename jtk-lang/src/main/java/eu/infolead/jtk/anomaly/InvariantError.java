package eu.infolead.jtk.anomaly;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

public record InvariantError(
    String invariant,
    InvariantErrorType errorType,
    Object[] contextArgs,
    Maybe<Object> actualValue,
    Maybe<String> errorId
) implements SystemError {

    public InvariantError {
        Objects.requireNonNull(invariant, "invariant cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
    }

    public InvariantError(String invariant, InvariantErrorType errorType) {
        this(invariant, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    public InvariantError(String invariant, InvariantErrorType errorType, String contextArg) {
        this(invariant, errorType, new Object[]{contextArg}, Maybe.none(), Maybe.none());
    }

    @Override public ErrorType getErrorType() { return errorType; }
    @Override public String getMessage() { return String.format("Invariant '%s': %s", invariant, errorType.formatMessage(contextArgs)); }
    @Override public Maybe<String> getErrorId() { return errorId; }
    @Override public Object[] getContextArgs() { return contextArgs.clone(); }
    @Override public Maybe<Object> getActualValue() { return actualValue; }

    @Override
    public SystemError withGeneratedId() {
        return new InvariantError(invariant, errorType, contextArgs, actualValue, 
                                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(String customErrorId) {
        return new InvariantError(invariant, errorType, contextArgs, actualValue, 
                                Maybe.ofNullable(customErrorId));
    }

    public static InvariantError objectConsistency(String description) {
        return new InvariantError("Object consistency", InvariantErrorType.OBJECT_CONSISTENCY, description);
    }

    public static InvariantError dataCorruption(String description) {
        return new InvariantError("Data integrity", InvariantErrorType.DATA_CORRUPTION, description);
    }
}