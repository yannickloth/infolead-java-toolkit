package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;
import eu.infolead.jtk.http.HttpStatusCode;

import java.net.URI;

public enum BusinessRuleErrorType implements ErrorType {

    INSUFFICIENT_FUNDS(
        4001, "INSUFFICIENT_FUNDS", "Account %s has insufficient funds: requested %s, available %s",
            HttpStatus.UNPROCESSABLE_CONTENT.code(), "https://tools.ietf.org/html/rfc4918#section-11.2", "Unprocessable Entity - Insufficient Funds",
        ErrorCategory.BUSINESS_LOGIC
    ),

    AGE_RESTRICTION(
        4002, "AGE_RESTRICTION", "Minimum age %d required, actual age %d",
            HttpStatus.UNPROCESSABLE_CONTENT.code(), "https://tools.ietf.org/html/rfc4918#section-11.2", "Unprocessable Entity - Age Restriction",
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
                         String problemType, String problemTitle, ErrorCategory category) {
        this.typeNumber = typeNumber;
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
        this.problemType = URI.create(problemType);
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