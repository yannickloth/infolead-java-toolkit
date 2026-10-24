package eu.infolead.jtk.anomaly;

import com.fasterxml.jackson.annotation.JsonCreator;
import eu.infolead.jtk.http.HttpStatus;
import java.net.URI;

/**
 * Error types for object and system invariant violations.
 *
 * <pre>{@code
 * InvariantError error = new InvariantError(
 *         "balance", InvariantErrorType.DATA_CORRUPTION,
 *         new Object[] { "ledger mismatch" });
 * boolean retry = error.isRetryable(); // false: critical integrity failure
 * }</pre>
 *
 * @see InvariantError
 * @see ErrorType
 */
public enum InvariantErrorType implements ErrorType {

    /** An object violated one of its consistency rules. */
    OBJECT_CONSISTENCY(
            7001,
            "OBJECT_CONSISTENCY",
            "Object consistency violated: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
            "Internal Server Error - Object Consistency Violated",
            ErrorCategory.CONTRACT),

    /** Stored data failed an integrity check. */
    DATA_CORRUPTION(
            7002,
            "DATA_CORRUPTION",
            "Data corruption detected: %s",
            HttpStatus.INTERNAL_SERVER_ERROR.code(),
            "Internal Server Error - Data Corruption",
            ErrorCategory.SYSTEM);

    private final int typeNumber;
    private final String code;
    private final String messageTemplate;
    private final int httpStatus;
    private final URI problemType;
    private final String problemTitle;
    private final ErrorCategory category;

    InvariantErrorType(
            int typeNumber,
            String code,
            String messageTemplate,
            int httpStatus,
            String problemTitle,
            ErrorCategory category) {
        this.typeNumber = typeNumber;
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
        this.problemType = ErrorType.problemType(category, "invariant", code);
        this.problemTitle = problemTitle;
        this.category = category;
    }

    @Override
    public int getTypeNumber() {
        return typeNumber;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessageTemplate() {
        return messageTemplate;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public URI getProblemType() {
        return problemType;
    }

    @Override
    public String getProblemTitle() {
        return problemTitle;
    }

    @Override
    public ErrorCategory getCategory() {
        return category;
    }

    @Override
    public ErrorSeverity getSeverity() {
        return switch (this) {
            case DATA_CORRUPTION, OBJECT_CONSISTENCY -> ErrorSeverity.CRITICAL;
        };
    }

    /**
     * Reads the enum constant from its stable message key, which is also the JSON representation
     * written by {@code ErrorType#getMessageKey()}.
     *
     * @param code the stable message key
     * @return the matching constant
     * @throws IllegalArgumentException if the code is unknown
     */
    @JsonCreator
    static InvariantErrorType fromJson(final String code) {
        for (final InvariantErrorType value : values()) {
            if (value.getMessageKey().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown InvariantErrorType code: " + code);
    }
}
