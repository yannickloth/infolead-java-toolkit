package eu.infolead.jtk.anomaly.http;

import java.net.URI;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.infolead.jtk.http.HttpStatus;
import eu.infolead.jtk.http.HttpStatusCode;

/**
 * One {@code response} element of an RFC 4918 {@code multistatus} body.
 * <p>
 * RFC 4918 represents {@code status} as a status-line string such as
 * {@code HTTP/1.1 423 Locked}. For convenience, the constructor also accepts a
 * bare three-digit code and normalizes it to a status line. The numeric form is
 * retained by {@link #statusCode()} for callers that need the code.
 * <p>
 * The alternative RFC 4918 {@code propstat} member is not modelled; it is
 * ignored during deserialization. Callers that need it should extend this model
 * or parse the raw WebDAV response.
 *
 * @param href   the hypertext reference of the affected resource; required
 * @param status the HTTP status line for that resource, or {@code null} when
 *               omitted
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record MultiStatusResponseElement(
        @JsonProperty("href") URI href,
        @JsonProperty("status") String status) {

    private static final Pattern STATUS_LINE = Pattern.compile("HTTP/\\d\\.\\d (\\d{3})(?: .*)?");
    private static final Pattern BARE_CODE = Pattern.compile("\\d{3}");

    /**
     * Canonical constructor with validation and status normalization.
     *
     * @param href   the hypertext reference of the affected resource
     * @param status the status line or numeric status code
     */
    public MultiStatusResponseElement {
        Objects.requireNonNull(href, "href");
        status = normalizeStatus(status);
    }

    /**
     * Creates a response element from a numeric status code.
     *
     * @param href   the hypertext reference of the affected resource
     * @param status the HTTP status code
     */
    public MultiStatusResponseElement(final URI href, final int status) {
        this(href, Integer.toString(status));
    }

    /**
     * Returns the numeric status code, if one is present.
     *
     * @return the status code, or empty when the status is omitted
     */
    public OptionalInt statusCode() {
        if (status == null) {
            return OptionalInt.empty();
        }
        final Matcher matcher = STATUS_LINE.matcher(status);
        return matcher.matches()
                ? OptionalInt.of(Integer.parseInt(matcher.group(1)))
                : OptionalInt.empty();
    }

    private static String normalizeStatus(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (BARE_CODE.matcher(trimmed).matches()) {
            final int code = Integer.parseInt(trimmed);
            validateCode(code);
            return statusLine(code);
        }
        final Matcher matcher = STATUS_LINE.matcher(trimmed);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid HTTP status line: '" + value + "'");
        }
        validateCode(Integer.parseInt(matcher.group(1)));
        return trimmed;
    }

    private static void validateCode(final int code) {
        if (!HttpStatusCode.isValid(code).toBoolean()) {
            throw new IllegalArgumentException("Invalid HTTP status code: " + code);
        }
    }

    private static String statusLine(final int code) {
        for (final HttpStatus known : HttpStatus.values()) {
            if (known.code() == code) {
                return "HTTP/1.1 " + code + " " + known.description();
            }
        }
        return "HTTP/1.1 " + code + " ";
    }
}
