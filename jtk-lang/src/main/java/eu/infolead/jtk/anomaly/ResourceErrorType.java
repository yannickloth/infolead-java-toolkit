package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;

import java.net.URI;

public enum ResourceErrorType implements ErrorType {

    NOT_FOUND(
        6001, "NOT_FOUND", "Resource with ID %s not found",
            HttpStatus.NOT_FOUND.code(), "https://tools.ietf.org/html/rfc7231#section-6.5.4", "Not Found",
        ErrorCategory.RESOURCE
    ),

    ALREADY_EXISTS(
        6002, "ALREADY_EXISTS", "Resource with ID %s already exists",
            HttpStatus.CONFLICT.code(), "https://tools.ietf.org/html/rfc7231#section-6.5.8", "Conflict - Resource Already Exists",
        ErrorCategory.RESOURCE
    );

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    ResourceErrorType(int typeNumber, String code, String messageTemplate, int httpStatus,
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