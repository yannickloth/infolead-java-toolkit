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
 * Represents object or system invariant violations.
 *
 * @param invariant the invariant that was violated
 * @param errorType the specific invariant error type
 * @param contextArgs additional arguments for error message formatting
 * @param actualValue the actual value that violated the invariant (optional)
 * @param errorId unique identifier for this error instance (optional)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InvariantError(
        String invariant,
        InvariantErrorType errorType,
        Object[] contextArgs,
        @JsonIgnore Maybe<Object> actualValue,
        @JsonIgnore Maybe<String> errorId)
        implements SystemError {
    private static final long serialVersionUID = 1L;

    /**
     * Canonical constructor with validation.
     *
     * @param invariant the invariant that was violated
     * @param errorType the specific invariant error type
     * @param contextArgs additional arguments for message formatting
     * @param actualValue the actual value, if available
     * @param errorId the unique error instance identifier, if assigned
     */
    public InvariantError {
        Objects.requireNonNull(invariant, "invariant cannot be null");
        Objects.requireNonNull(errorType, "errorType cannot be null");
        Objects.requireNonNull(contextArgs, "contextArgs cannot be null");
        Objects.requireNonNull(actualValue, "actualValue cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        contextArgs = contextArgs.clone();
    }

    /**
     * Convenience constructor without context args, actual value, or error ID.
     *
     * @param invariant the invariant that was violated
     * @param errorType the specific invariant error type
     */
    public InvariantError(final String invariant, final InvariantErrorType errorType) {
        this(invariant, errorType, new Object[0], Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with one message-format argument.
     *
     * @param invariant the invariant that was violated
     * @param errorType the specific invariant error type
     * @param contextArg the argument substituted into the message template
     */
    public InvariantError(
            final String invariant, final InvariantErrorType errorType, final String contextArg) {
        this(invariant, errorType, new Object[] {contextArg}, Maybe.none(), Maybe.none());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return String.format("Invariant '%s': %s", invariant, getTypeMessage());
    }

    /**
     * Returns the message with the error-type text localized for the requested language; the
     * invariant name is kept untranslated inside a localized label.
     *
     * <pre>{@code
     * InvariantError.objectConsistency("balance")
     *         .getLocalizedMessage(Locale.FRENCH);
     * // "Invariant 'Object consistency': Cohérence d'objet violée : balance"
     * }</pre>
     *
     * @param locale the requested locale; {@code null} means English
     * @return the localized error message
     */
    @Override
    public String getLocalizedMessage(final Locale locale) {
        final String label =
                ErrorLocalizer.of(locale)
                        .label("error.label.invariant", "Invariant '%s'", invariant);
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
        return new InvariantError(
                invariant,
                errorType,
                contextArgs,
                actualValue,
                Maybe.some(errorType.generateErrorId()));
    }

    @Override
    public SystemError withErrorId(final String customErrorId) {
        return new InvariantError(
                invariant,
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
        if (!(other instanceof InvariantError that)) {
            return false;
        }
        return invariant.equals(that.invariant)
                && errorType == that.errorType
                && actualValue.equals(that.actualValue)
                && errorId.equals(that.errorId)
                && Arrays.equals(contextArgs, that.contextArgs);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(invariant, errorType, actualValue, errorId)
                + Arrays.hashCode(contextArgs);
    }

    @Override
    public String toString() {
        return "InvariantError[invariant="
                + invariant
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
     * Creates an error for an object consistency violation.
     *
     * @param description the violated consistency rule
     * @return the invariant error
     */
    public static InvariantError objectConsistency(final String description) {
        return new InvariantError(
                "Object consistency",
                InvariantErrorType.OBJECT_CONSISTENCY,
                new Object[] {description},
                Maybe.none(),
                Maybe.none());
    }

    /**
     * Creates an error for detected data corruption.
     *
     * @param description the corruption description
     * @return the invariant error
     */
    public static InvariantError dataCorruption(final String description) {
        return new InvariantError(
                "Data integrity",
                InvariantErrorType.DATA_CORRUPTION,
                new Object[] {description},
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
     * @param invariant the invariant
     * @param errorType the error type
     * @param contextArgs the message-format arguments
     * @return the restored error
     */
    @JsonCreator
    public static InvariantError fromJson(
            @JsonProperty("invariant") final String invariant,
            @JsonProperty("errorType") final InvariantErrorType errorType,
            @JsonProperty("contextArgs") final Object[] contextArgs) {
        return new InvariantError(
                invariant,
                errorType,
                contextArgs == null ? new Object[0] : contextArgs,
                Maybe.none(),
                Maybe.none());
    }
}
