package eu.infolead.jtk.anomaly;

import eu.infolead.jtk.http.HttpStatus;

import java.net.URI;

/**
 * Error types for authentication and authorization failures.
 *
 * <pre>{@code
 * AuthorizationError error = new AuthorizationError(
 *         "admin role", AuthorizationErrorType.INSUFFICIENT_PERMISSIONS);
 * ProblemDetail problem = error.toProblemDetail();
 * }</pre>
 *
 * @see AuthorizationError
 * @see ErrorType
 */
public enum AuthorizationErrorType implements ErrorType {

    /** No authenticated principal was supplied. */
    NOT_AUTHENTICATED(
        5001, "NOT_AUTHENTICATED", "Authentication required",
            HttpStatus.UNAUTHORIZED.code(), "Unauthorized",
        ErrorCategory.SECURITY
    ),

    /** The authenticated principal lacks a required permission. */
    INSUFFICIENT_PERMISSIONS(
        5002, "INSUFFICIENT_PERMISSIONS", "Insufficient permissions: %s required", 
        HttpStatus.FORBIDDEN.code(), "Forbidden",
        ErrorCategory.SECURITY
    );

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    AuthorizationErrorType(int typeNumber, String code, String messageTemplate, int httpStatus,
                          String problemTitle, ErrorCategory category) {
        this.typeNumber = typeNumber;
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
        this.problemType = ErrorType.problemType(category, "authorization", code);
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