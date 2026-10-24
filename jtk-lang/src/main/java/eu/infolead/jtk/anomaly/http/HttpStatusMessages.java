package eu.infolead.jtk.anomaly.http;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.http.HttpStatus;

/**
 * Resolves the localized reason phrase of an HTTP status code.
 *
 * <p>RFC 7807 requires an {@code about:blank} problem title to be the
 * recommended status phrase, localised according to the client's
 * {@code Accept-Language} preference. This class supplies that phrase from a
 * small resource bundle and falls back to the status enum's English
 * description when no translation exists.</p>
 *
 * <p><strong>Coverage:</strong> the French and German bundles translate the
 * statuses used by the toolkit's error types (2xx multi-status and the common
 * 4xx/5xx codes). Statuses outside that set fall back to the English
 * description declared by {@link HttpStatus}; use {@link #titleIfKnown(int,
 * Locale)} when the caller must be able to distinguish a translated phrase
 * from an English fallback.</p>
 *
 * <pre>{@code
 * HttpStatusMessages.title(404, Locale.FRENCH);        // "Introuvable"
 * HttpStatusMessages.title(404, Locale.GERMAN);        // "Nicht gefunden"
 * HttpStatusMessages.title(404, null);                 // "Not Found"
 * HttpStatusMessages.titleIfKnown(599, Locale.GERMAN); // Maybe.none()
 * }</pre>
 */
public final class HttpStatusMessages {

    private static final String BUNDLE = "eu.infolead.jtk.http.http-status";

    private static final List<Locale> SUPPORTED =
            List.of(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN);

    private HttpStatusMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns the localized reason phrase for a status code, or
     * {@link Maybe#none()} when the status is neither translated nor a known
     * {@link HttpStatus} constant.
     *
     * <p>Callers that already hold a meaningful title (for example a custom
     * {@code about:blank} problem) can keep it instead of replacing it with a
     * synthetic phrase.</p>
     *
     * <pre>{@code
     * HttpStatusMessages.titleIfKnown(404, Locale.FRENCH); // Maybe.of("Introuvable")
     * HttpStatusMessages.titleIfKnown(599, Locale.FRENCH); // Maybe.none()
     * }</pre>
     *
     * @param status the HTTP status code
     * @param locale the requested locale; {@code null} means English
     * @return the localized or declared phrase, or {@link Maybe#none()}
     */
    public static Maybe<String> titleIfKnown(final int status, final Locale locale) {
        final ResourceBundle bundle = bundle(normalize(locale));
        final String key = "http.status." + status;
        if (bundle != null && bundle.containsKey(key)) {
            return Maybe.of(bundle.getString(key));
        }
        return Arrays.stream(HttpStatus.values())
                .filter(candidate -> candidate.code() == status)
                .map(HttpStatus::description)
                .findFirst()
                .map(Maybe::of)
                .orElseGet(Maybe::none);
    }

    /**
     * Returns the localized reason phrase for a status code, falling back to
     * the English enum description and finally to {@code "HTTP <status>"} for
     * codes unknown to the toolkit.
     *
     * @param status the HTTP status code
     * @param locale the requested locale; {@code null} means English
     * @return the localized phrase, never {@code null}
     */
    public static String title(final int status, final Locale locale) {
        return titleIfKnown(status, locale).orElse("HTTP " + status).orNull();
    }

    private static Locale normalize(final Locale locale) {
        if (locale == null) {
            return Locale.ENGLISH;
        }
        return SUPPORTED.stream()
                .filter(supported -> supported.getLanguage().equals(locale.getLanguage()))
                .findFirst()
                .orElse(Locale.ENGLISH);
    }

    private static ResourceBundle bundle(final Locale locale) {
        try {
            return ResourceBundle.getBundle(BUNDLE, locale);
        } catch (final MissingResourceException exception) {
            return null;
        }
    }
}
