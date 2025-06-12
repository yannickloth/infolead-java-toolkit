package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;

import java.net.URI;

public enum InvariantErrorType implements ErrorType {

    OBJECT_CONSISTENCY(
        7001, "OBJECT_CONSISTENCY", "Object consistency violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(), "https://tools.ietf.org/html/rfc7231#section-6.6.1", "Internal Server Error - Object Consistency Violated",
        ErrorCategory.CONTRACT
    ),

    DATA_CORRUPTION(
        7002, "DATA_CORRUPTION", "Data corruption detected: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(), "https://tools.ietf.org/html/rfc7231#section-6.6.1", "Internal Server Error - Data Corruption",
        ErrorCategory.SYSTEM
    );

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    InvariantErrorType(int typeNumber, String code, String messageTemplate, int httpStatus,
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

    @Override
    public ErrorSeverity getSeverity() {
        return ErrorSeverity.CRITICAL; // Invariant violations are always critical
    }
}