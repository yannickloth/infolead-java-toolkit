package eu.infolead.jtk.rfc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import eu.infolead.jtk.fp.either.Result;

/**
 * A reference to an RFC document.
 *
 * @param number the RFC number; must be positive
 */
public record RfcReference(int number) {
    /**
     * Base URL of the RFC editor.
     */
    public static final String BASE_URL = "https://www.rfc-editor.org/rfc/rfc";

    private static final Pattern SECTION_PATTERN = Pattern.compile("[1-9]\\d*(\\.\\d+)*");

    /**
     * Canonical constructor with validation.
     */
    public RfcReference {
        if (number <= 0) {
            throw new IllegalArgumentException("RFC number must be positive, was " + number);
        }
    }

    /**
     * Returns the canonical RFC URL.
     *
     * @return the RFC URL
     * @throws IllegalArgumentException if the URL cannot be built
     * @deprecated use {@link #url()} for a consistent value-or-failure contract
     */
    @Deprecated(forRemoval = true)
    public URL toUrl() {
        return createUrl(BASE_URL + number());
    }

    /**
     * Returns the JSON representation URL of this RFC.
     *
     * @return the JSON URL, or a defensive failure when the URL cannot be built
     */
    public Result<String, URL> jsonUrl() {
        return createResult(BASE_URL + number() + ".json");
    }

    /**
     * Returns the plain-text representation URL of this RFC.
     *
     * @return the text URL, or a defensive failure when the URL cannot be built
     */
    public Result<String, URL> textUrl() {
        return createResult(BASE_URL + number() + ".txt");
    }

    /**
     * Returns the canonical RFC URL wrapped in a result.
     *
     * @return the RFC URL, or a defensive failure when the URL cannot be built
     */
    public Result<String, URL> url() {
        return createResult(BASE_URL + number());
    }

    /**
     * Returns the URL of a specific section of this RFC.
     *
     * @param section the section number, such as {@code 6.5.1}
     * @return the section URL, or a failure when the section is null or
     *         malformed
     */
    public Result<String, URL> url(final String section) {
        if (section == null) {
            return Result.failure("RFC section cannot be null");
        }
        final String trimmed = section.strip();
        if (!SECTION_PATTERN.matcher(trimmed).matches()) {
            return Result.failure("Invalid RFC section: '" + section + "'");
        }
        return createResult(BASE_URL + number() + "#section-" + trimmed);
    }

    private static URL createUrl(final String spec) {
        try {
            return new URL(spec);
        } catch (final MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid RFC URL: " + spec, exception);
        }
    }

    private static Result<String, URL> createResult(final String spec) {
        try {
            return Result.success(createUrl(spec));
        } catch (final IllegalArgumentException exception) {
            return Result.failure("Invalid RFC URL: " + spec + " (" + exception.getMessage() + ")");
        }
    }
}
