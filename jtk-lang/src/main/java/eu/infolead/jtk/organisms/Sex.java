package eu.infolead.jtk.organisms;

import java.util.Locale;

import eu.infolead.jtk.fp.either.Result;
import jakarta.annotation.Nullable;

/**
 * Represents the essentially binary biological form of living organisms on the
 * basis of their reproductive organs and structures.
 * <p>
 * Parsing is case-insensitive and ignores surrounding whitespace. A missing or
 * blank value maps to {@link #UNSPECIFIED}; an unrecognized value produces a
 * failure explaining the problem.
 */
public enum Sex {
    /** Female sex. */
    FEMALE,

    /** Male sex. */
    MALE,

    /** Sex not specified or unknown. */
    UNSPECIFIED;

    /**
     * Parses a textual sex value.
     *
     * @param gender the textual representation, possibly {@code null}
     * @return the parsed sex, or a failure message when the value is not
     *         recognized
     */
    public static Result<String, Sex> of(@Nullable final String gender) {
        if (gender == null || gender.isBlank()) {
            return Result.success(UNSPECIFIED);
        }
        final String normalized = gender.strip();
        try {
            return Result.success(valueOf(normalized.toUpperCase(Locale.ROOT)));
        } catch (final IllegalArgumentException exception) {
            return Result.failure("Unsupported sex value: '" + gender + "'");
        }
    }
}
