package eu.infolead.jtk.anomaly;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Represents business rule violations - domain-specific constraint failures.
 * These typically map to HTTP 422 Unprocessable Entity status codes.
 */
public record BusinessRuleError(
    String rule,
    BusinessRuleErrorType errorType,
    Object[] contextArgs,
    Maybe<Object> actualValue,
    Maybe<String> errorId
) implements SystemError {

    public BusinessRuleError {
        Objects.requireNonNull(rule, "rule cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
    }

    public BusinessRuleError(String rule, BusinessRuleErrorType errorType) {
        this(rule, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() { return errorType; }

    @Override
    public String getMessage() {
        return String.format("Business rule '%s': %s", rule, errorType.formatMessage(contextArgs));
    }

    @Override
    public Maybe<String> getErrorId() { return errorId; }

    @Override
    public Object[] getContextArgs() { return contextArgs.clone(); }

    @Override
    public Maybe<Object> getActualValue() { return actualValue; }

    @Override
    public SystemError withGeneratedId() {
        return new BusinessRuleError(rule, errorType, contextArgs, actualValue, 
                                   Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(String customErrorId) {
        return new BusinessRuleError(rule, errorType, contextArgs, actualValue, 
                                   Maybe.ofNullable(customErrorId));
    }

    public static BusinessRuleError insufficientFunds(String accountId, Object requestedAmount, Object availableAmount) {
        return new BusinessRuleError("Insufficient funds", BusinessRuleErrorType.INSUFFICIENT_FUNDS, 
                                   new Object[]{accountId, requestedAmount, availableAmount}, Maybe.none(), Maybe.none());
    }

    public static BusinessRuleError ageRestriction(int minimumAge, int actualAge) {
        return new BusinessRuleError("Age restriction", BusinessRuleErrorType.AGE_RESTRICTION, 
                                   new Object[]{minimumAge, actualAge}, Maybe.none(), Maybe.none());
    }
}