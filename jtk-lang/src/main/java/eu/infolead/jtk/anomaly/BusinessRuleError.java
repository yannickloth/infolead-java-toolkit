package eu.infolead.jtk.anomaly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.either.Maybe;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents business rule violations - domain-specific constraint failures. These typically map to
 * HTTP 422 Unprocessable Content status codes.
 *
 * @param rule the business rule that was violated
 * @param errorType the specific business rule error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual value that violated the rule (optional)
 * @param errorId unique identifier for this error instance (optional)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BusinessRuleError(
        String rule,
        BusinessRuleErrorType errorType,
        Object[] contextArgs,
        @JsonIgnore Maybe<Object> actualValue,
        @JsonIgnore Maybe<String> errorId)
        implements SystemError {
    private static final long serialVersionUID = 1L;

    /**
     * Canonical constructor with validation.
     *
     * @param rule the business rule that was violated
     * @param errorType the specific business rule error type
     * @param contextArgs additional arguments for message formatting
     * @param actualValue the actual value, if available
     * @param errorId the unique error instance identifier, if assigned
     */
    public BusinessRuleError {
        Objects.requireNonNull(rule, "rule cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     *
     * @param rule the business rule that was violated
     * @param errorType the specific business rule error type
     */
    public BusinessRuleError(final String rule, final BusinessRuleErrorType errorType) {
        this(rule, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return String.format("Business rule '%s': %s", rule, getTypeMessage());
    }

    /**
     * Returns the message with the error-type text localized for the requested language; the rule
     * name is preserved verbatim inside a localized label.
     *
     * <pre>{@code
     * BusinessRuleError.ageRestriction(18, 16)
     *         .getLocalizedMessage(Locale.GERMAN);
     * // "Geschäftsregel 'Age restriction': Mindestalter 18 ..."
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return the localized error message
     */
    @Override
    public String getLocalizedMessage(final Locale locale) {
        final String label =
                ErrorLocalizer.of(locale)
                        .label("error.label.business-rule", "Business rule '%s'", rule);
        return String.format("%s: %s", label, getLocalizedTypeMessage(locale));
    }

    @Override
    public Maybe<String> getErrorId() {
        return errorId;
    }

    /**
     * Returns a defensive copy of the message-format arguments.
     *
     * @return a copy of the context arguments
     */
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
        return new BusinessRuleError(
                rule, errorType, contextArgs, actualValue, Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new BusinessRuleError(
                rule,
                errorType,
                contextArgs,
                actualValue,
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
        return "BusinessRuleError[rule="
                + rule
                + ", errorType="
                + errorType
                + ", contextArgs="
                + SystemError.renderValue(contextArgs)
                + ", actualValue="
                + actualValue.fold(() -> "none", SystemError::renderValue)
                + ", errorId="
                + errorId
                + "]";
    }

    /**
     * Creates an error for insufficient funds.
     *
     * @param accountId the account identifier
     * @param requestedAmount the requested amount
     * @param availableAmount the available amount
     * @return the business rule error
     */
    public static BusinessRuleError insufficientFunds(
            final String accountId, final Object requestedAmount, final Object availableAmount) {
        return new BusinessRuleError(
                "Insufficient funds",
                BusinessRuleErrorType.INSUFFICIENT_FUNDS,
                new Object[] {accountId, requestedAmount, availableAmount},
                Maybe.none(),
                Maybe.none());
    }

    /**
     * Creates an error for an age restriction violation.
     *
     * @param minimumAge the minimum required age
     * @param actualAge the actual age
     * @return the business rule error
     */
    public static BusinessRuleError ageRestriction(final int minimumAge, final int actualAge) {
        return new BusinessRuleError(
                "Age restriction",
                BusinessRuleErrorType.AGE_RESTRICTION,
                new Object[] {minimumAge, actualAge},
                Maybe.none(),
                Maybe.none());
    }

    /**
     * Creates an error for a monetary operation that received amounts in two different currencies,
     * such as adding a EUR amount to a USD amount:
     *
     * <pre>{@code
     * BusinessRuleError error = BusinessRuleError.currencyMismatch("EUR", "USD");
     * // CURRENCY_MISMATCH (4003), HTTP 422
     * }</pre>
     *
     * @param expectedCurrency the currency the operation requires, such as {@code EUR}
     * @param actualCurrency the currency of the rejected amount, such as {@code USD}
     * @return the currency mismatch error
     */
    public static BusinessRuleError currencyMismatch(
            final Object expectedCurrency, final Object actualCurrency) {
        return new BusinessRuleError(
                "Currency mismatch",
                BusinessRuleErrorType.CURRENCY_MISMATCH,
                new Object[] {expectedCurrency, actualCurrency},
                Maybe.none(),
                Maybe.none());
    }

    /**
     * Creates an error for a generic business rule violation.
     *
     * @param ruleName the name of the violated rule
     * @return the business rule error
     */
    public static BusinessRuleError ruleViolated(final String ruleName) {
        return new BusinessRuleError(
                "Business rule violated",
                BusinessRuleErrorType.RULE_VIOLATED,
                new Object[] {ruleName},
                Maybe.none(),
                Maybe.none());
    }

    /**
     * Restores the record from its JSON representation.
     *
     * <p>The operational members ({@code actualValue}, {@code errorId}) are not part of the wire
     * contract and are restored as {@link Maybe#none()}. They are reattached by the application
     * when an error crosses a process boundary.
     *
     * @param rule the business rule
     * @param errorType the error type
     * @param contextArgs the message-format arguments
     * @return the restored error
     */
    @JsonCreator
    public static BusinessRuleError fromJson(
            @JsonProperty("rule") final String rule,
            @JsonProperty("errorType") final BusinessRuleErrorType errorType,
            @JsonProperty("contextArgs") final Object[] contextArgs) {
        return new BusinessRuleError(
                rule,
                errorType,
                contextArgs == null ? new Object[0] : contextArgs,
                Maybe.none(),
                Maybe.none());
    }
}
