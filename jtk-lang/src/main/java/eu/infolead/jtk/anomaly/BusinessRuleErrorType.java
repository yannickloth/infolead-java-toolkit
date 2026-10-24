package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;
import eu.infolead.jtk.http.HttpStatusCode;

import java.net.URI;

/**
 * Error types for business rule violations.
 *
 * <pre>{@code
 * BusinessRuleError error = new BusinessRuleError(
 *         "age restriction", BusinessRuleErrorType.AGE_RESTRICTION,
 *         new Object[] { 18, 16 });
 * ProblemDetail problem = error.toLocalizedProblemDetail(Locale.GERMAN);
 * }</pre>
 *
 * @see BusinessRuleError
 * @see ErrorType
 */
public enum BusinessRuleErrorType implements ErrorType {

    /** The account does not have enough funds. */
    INSUFFICIENT_FUNDS(
        4001, "INSUFFICIENT_FUNDS", "Account %s has insufficient funds: requested %s, available %s",
            HttpStatus.UNPROCESSABLE_CONTENT.code(), "Unprocessable Content - Insufficient Funds",
        ErrorCategory.BUSINESS_LOGIC
    ),

    /** The subject does not meet the minimum age. */
    AGE_RESTRICTION(
        4002, "AGE_RESTRICTION", "Minimum age %d required, actual age %d",
            HttpStatus.UNPROCESSABLE_CONTENT.code(), "Unprocessable Content - Age Restriction",
        ErrorCategory.BUSINESS_LOGIC
    ),

    /** The operands use different currencies. */
    CURRENCY_MISMATCH(
        4003, "CURRENCY_MISMATCH", "Operation requires currency %s but received currency %s",
            HttpStatus.UNPROCESSABLE_CONTENT.code(), "Unprocessable Content - Currency Mismatch",
        ErrorCategory.BUSINESS_LOGIC
    );

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    BusinessRuleErrorType(int typeNumber, String code, String messageTemplate, int httpStatus,
                         String problemTitle, ErrorCategory category) {
        this.typeNumber = typeNumber;
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
        this.problemType = ErrorType.problemType(category, "business-rule", code);
        this.problemTitle = problemTitle;
        this.category = category;
    }

    @Override public int getTypeNumber() { return typeNumber; }
    @Override public String getCode() { return code; }
    @Override public String getMessageTemplate() { return messageTemplate; }
    @Override public int getHttpStatus() { return httpStatus; }
    @Override public URI getProblemType() { return problemType; }
    @Override public String getProblemTitle() { return problemTitle; }
    @Override public ErrorCategory getCategory() { return category; }
}