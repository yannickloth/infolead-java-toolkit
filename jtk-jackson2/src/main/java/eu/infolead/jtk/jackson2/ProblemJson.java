package eu.infolead.jtk.jackson2;

import eu.infolead.jtk.anomaly.ErrorLocalizer;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;
import java.util.Locale;
import java.util.Objects;

/**
 * The {@code application/problem+json} binding, Jackson 2 bridge.
 *
 * @deprecated use {@code eu.infolead.jtk.jackson3.ProblemJson}.
 */
@Deprecated(since = "0.1.0")
public final class ProblemJson {

    /** The RFC 9457 media type. */
    public static final String MEDIA_TYPE = "application/problem+json";

    /** Mappers are thread-safe after configuration, so one shared instance is safe. */
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = JtkJackson.mapper();

    private ProblemJson() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Checks whether a {@code Content-Type} header value denotes a problem document, ignoring
     * parameters such as {@code charset}.
     *
     * @param contentType the header value, possibly {@code null}
     * @return {@code true} when the media type is {@code application/problem+json}
     */
    public static boolean isProblemJson(final String contentType) {
        if (contentType == null) {
            return false;
        }
        final int separator = contentType.indexOf(';');
        final String mediaType = separator < 0 ? contentType : contentType.substring(0, separator);
        return MEDIA_TYPE.equalsIgnoreCase(mediaType.trim());
    }

    /**
     * Writes a problem document.
     *
     * @param detail the document
     * @return the JSON representation
     */
    public static String write(final ProblemDetail detail) {
        Objects.requireNonNull(detail, "detail");
        try {
            return MAPPER.writeValueAsString(detail);
        } catch (final com.fasterxml.jackson.core.JacksonException failure) {
            throw new IllegalStateException("Cannot write the problem document", failure);
        }
    }

    /**
     * Writes an error as a problem document in the requested language.
     *
     * @param error the error
     * @param locale the language for the title and detail; {@code null} means English
     * @return the JSON problem document
     */
    public static String write(final SystemError error, final Locale locale) {
        Objects.requireNonNull(error, "error");
        return write(error.toProblemDetail(Maybe.none(), ErrorLocalizer.of(locale)));
    }

    /**
     * Reads a problem document of either permitted shape.
     *
     * @param json the JSON document
     * @return the problem detail
     */
    public static ProblemDetail read(final String json) {
        Objects.requireNonNull(json, "json");
        try {
            return MAPPER.readValue(json, ProblemDetail.class);
        } catch (final com.fasterxml.jackson.core.JacksonException failure) {
            throw new IllegalArgumentException("Not a problem document: " + json, failure);
        }
    }
}
