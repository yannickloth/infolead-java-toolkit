package eu.infolead.jtk.anomaly;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Marks a value as sensitive so that it is never rendered by error messages,
 * logs or {@code ProblemDetail} serialization.
 *
 * <p>Error context arguments and actual values are frequently echoed into
 * logs and API responses. Wrapping passwords, tokens, API keys or personal
 * identifiers in a {@code Sensitive} makes the safe representation the
 * default:</p>
 *
 * <pre>{@code
 * ParameterError error = ParameterError.invalidFormat(
 *         "password", Sensitive.of(rawPassword));
 *
 * error.getMessage();  // contains [REDACTED], never rawPassword
 * error.format();      // contains [REDACTED], never rawPassword
 * }</pre>
 *
 * <p>The wrapped value can only be obtained deliberately through
 * {@link #reveal()}. Both {@link #toString()} and
 * {@link SystemError#renderValue(Object)} return {@value #REDACTED}.</p>
 *
 * <p>Jackson serializes the wrapper as {@code "[REDACTED]"}, so generic error
 * serialization cannot leak the wrapped value either:</p>
 *
 * <pre>{@code
 * new ObjectMapper().writeValueAsString(Sensitive.of(rawPassword));
 * // "[REDACTED]"
 * }</pre>
 */
public final class Sensitive implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Replacement text used wherever a sensitive value would be rendered. */
    public static final String REDACTED = "[REDACTED]";

    /** The wrapped value; reachable only through {@link #reveal()}. */
    private final Object value;

    /**
     * Creates a sensitive wrapper. The creator is delegating so that the JSON
     * representation produced by {@link #redactedJson()} can be read back
     * without exposing the wrapped value.
     *
     * @param value the value to protect
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    private Sensitive(final Object value) {
        this.value = Objects.requireNonNull(value, "value cannot be null");
    }

    /**
     * Wraps a value as sensitive.
     *
     * @param value the value to protect
     * @return a sensitive wrapper
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static Sensitive of(final Object value) {
        return new Sensitive(value);
    }

    /**
     * Returns the wrapped value. Call this only at the point where the real
     * value is required, never when building a log line or an HTTP response.
     *
     * @return the wrapped value
     */
    public Object reveal() {
        return value;
    }

    /**
     * Jackson serializes a sensitive value as the redaction marker only, so
     * even generic record serialization (for example a whole
     * {@link ParameterError}) cannot write the wrapped value to JSON.
     *
     * @return {@value #REDACTED}
     */
    @JsonValue
    public String redactedJson() {
        return REDACTED;
    }

    /**
     * Returns the redaction marker, never the wrapped value.
     *
     * @return {@value #REDACTED}
     */
    @Override
    public String toString() {
        return REDACTED;
    }

    @Override
    public boolean equals(final Object other) {
        return this == other
                || other instanceof Sensitive sensitive && value.equals(sensitive.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
