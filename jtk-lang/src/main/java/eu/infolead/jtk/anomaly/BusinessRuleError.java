package eu.infolead.jtk.anomaly;

import java.util.Arrays;
import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents business rule violations - domain-specific constraint failures.
 * These typically map to HTTP 422 Unprocessable Content status codes.
 *
 * @param rule        the business rule that was violated
 * @param errorType   the specific business rule error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual value that violated the rule (optional)
 * @param errorId     unique identifier for this error instance (optional)
 */
public record BusinessRuleError(
        String rule,
        BusinessRuleErrorType errorType,
        Object[] contextArgs,
        Maybe<Object> actualValue,
        Maybe<String> errorId) implements SystemError {

    public BusinessRuleError {
        Objects.requireNonNull(rule, "rule cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    public BusinessRuleError(final String rule, final BusinessRuleErrorType errorType) {
        this(rule, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return String.format("Business rule '%s': %s", rule, errorType.formatMessage(contextArgs));
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
        return new BusinessRuleError(rule, errorType, contextArgs, actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new BusinessRuleError(rule, errorType, contextArgs, actualValue,
                Maybe.some(Objects.requireNonNull(customErrorId, "customErrorId")));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BusinessRuleError that)) {
            return false;
        }
        return rule.equals(that.rule)
                && errorType == that.errorType
                && actualValue.equals(that.actualValue)
                && errorId.equals(that.errorId)
                && Arrays.equals(contextArgs, that.contextArgs);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(rule, errorType, actualValue, errorId)
                + Arrays.hashCode(contextArgs);
    }

    @Override
    public String toString() {
        return "BusinessRuleError[rule=" + rule
                + ", errorType=" + errorType
                + ", contextArgs=" + Arrays.toString(contextArgs)
                + ", actualValue=" + actualValue
                + ", errorId=" + errorId + "]";
    }

    public static BusinessRuleError insufficientFunds(final String accountId, final Object requestedAmount,
            final Object availableAmount) {
        return new BusinessRuleError("Insufficient funds", BusinessRuleErrorType.INSUFFICIENT_FUNDS,
                new Object[] { accountId, requestedAmount, availableAmount }, Maybe.none(), Maybe.none());
    }

    public static BusinessRuleError ageRestriction(final int minimumAge, final int actualAge) {
        return new BusinessRuleError("Age restriction", BusinessRuleErrorType.AGE_RESTRICTION,
                new Object[] { minimumAge, actualAge }, Maybe.none(), Maybe.none());
    }
}
