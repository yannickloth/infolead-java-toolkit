package eu.infolead.jtk.anomaly;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import eu.infolead.jtk.anomaly.http.HttpStatusMessages;
import eu.infolead.jtk.fp.either.Maybe;

/**
 * Resolves localized error titles and message templates from an
 * {@code Accept-Language} preference.
 *
 * <p>Every {@link ErrorType} exposes a stable {@link ErrorType#getMessageKey()
 * message key}. The resolver looks up {@code <key>.title} and
 * {@code <key>.message} in the localization bundle, falling back to the
 * error type's own English title and template. This means adding a
 * translation never requires touching an error type enum.</p>
 *
 * <p>Use {@link #acceptLanguage(String)} at an HTTP boundary:</p>
 *
 * <pre>{@code
 * ErrorLocalizer localizer = ErrorLocalizer.acceptLanguage(request.getHeader("Accept-Language"));
 * ProblemDetail problem = error.toProblemDetail(instance, localizer);
 * }</pre>
 */
public final class ErrorLocalizer {

    private static final String BUNDLE = "eu.infolead.jtk.anomaly.messages";
    private static final List<Locale> SUPPORTED =
            List.of(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN);
    private static final ErrorLocalizer ENGLISH = new ErrorLocalizer(Locale.ENGLISH);

    private final Locale locale;

    private ErrorLocalizer(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Returns the English resolver.
     *
     * @return the default resolver
     */
    public static ErrorLocalizer english() {
        return ENGLISH;
    }

    /**
     * Returns a resolver for a locale. Unsupported languages fall back to
     * English.
     *
     * @param locale the requested locale; {@code null} means English
     * @return a resolver
     */
    public static ErrorLocalizer of(final Locale locale) {
        return locale == null || Locale.ENGLISH.equals(normalize(locale))
                ? ENGLISH
                : new ErrorLocalizer(normalize(locale));
    }

    /**
     * Returns the best resolver for an {@code Accept-Language} header.
     *
     * <p>Quality values, wildcards and unsupported languages are handled
     * according to RFC 4647 basic filtering. A missing or malformed header
     * resolves to English.</p>
     *
     * @param acceptLanguage the raw header value; may be {@code null}
     * @return a resolver
     */
    public static ErrorLocalizer acceptLanguage(final String acceptLanguage) {
        return of(preferredLocale(acceptLanguage));
    }

    /**
     * Returns the preferred supported locale from an {@code Accept-Language}
     * header.
     *
     * @param acceptLanguage the raw header value; may be {@code null}
     * @return the preferred locale, or English when nothing matches
     */
    public static Locale preferredLocale(final String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return Locale.ENGLISH;
        }
        try {
            final List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(acceptLanguage)
                    .stream()
                    .filter(range -> range.getWeight() > 0.0)
                    .toList();
            final Locale match = Locale.lookup(ranges, SUPPORTED);
            return match == null ? Locale.ENGLISH : match;
        } catch (final RuntimeException exception) {
            return Locale.ENGLISH;
        }
    }

    /**
     * Returns the locale used by this resolver.
     *
     * @return the resolved locale
     */
    public Locale locale() {
        return locale;
    }

    /**
     * Returns the localized problem title for an error type.
     *
     * <p>English resolves to the title declared on the error type, so the
     * non-localized and the English-localized APIs always agree. Other
     * supported languages read {@code <message-key>.title} from
     * {@code messages.properties} and fall back to the declared English
     * title when no translation exists.</p>
     *
     * <pre>{@code
     * ErrorLocalizer.of(Locale.FRENCH).title(StandardErrorType.REQUIRED);
     * // "Champ obligatoire manquant"
     * }</pre>
     *
     * @param errorType the error type
     * @return the localized title, or the type's built-in title
     */
    public String title(final ErrorType errorType) {
        Objects.requireNonNull(errorType, "errorType cannot be null");
        return resource(errorType.getMessageKey() + ".title")
                .orElse(errorType.getProblemTitle())
                .orNull();
    }

    /**
     * Returns the localized message template for an error type.
     *
     * @param errorType the error type
     * @return the localized template, or the type's built-in template
     */
    public String messageTemplate(final ErrorType errorType) {
        Objects.requireNonNull(errorType, "errorType cannot be null");
        return resource(errorType.getMessageKey() + ".message")
                .orElse(errorType.getMessageTemplate())
                .orNull();
    }

    /**
     * Formats the localized message template.
     *
     * <pre>{@code
     * ErrorLocalizer.acceptLanguage("de")
     *         .formatMessage(StandardErrorType.TOO_LONG, 32);
     * // "Das Feld überschreitet die maximale Länge von 32 Zeichen"
     * }</pre>
     *
     * @param errorType the error type
     * @param args      the format arguments
     * @return the localized message, or the template when formatting fails
     */
    public String formatMessage(final ErrorType errorType, final Object... args) {
        final String template = messageTemplate(errorType);
        if (args == null || args.length == 0) {
            return template;
        }
        final Object[] redacted = RedactionPolicy.standard().redactAll(args);
        boolean redactionChangedAnArgument = false;
        for (int index = 0; index < args.length; index++) {
            if (args[index] != redacted[index]) {
                redactionChangedAnArgument = true;
                break;
            }
        }
        try {
            return String.format(locale, template, redacted);
        } catch (final RuntimeException exception) {
            return redactionChangedAnArgument ? messageWithoutArgs(errorType) : template;
        }
    }

    /**
     * Returns the localized detail for an error that carries an error ID.
     *
     * <p>The {@code error.detail.with-id} template is translated per locale and
     * falls back to the English {@code "%1$s (error ID: %2$s)"} when no
     * translation exists.</p>
     *
     * <pre>{@code
     * ErrorLocalizer.of(Locale.FRENCH).detailWithId("Paramètre nul", "1001-7");
     * // "Paramètre nul (ID d'erreur : 1001-7)"
     * }</pre>
     *
     * @param message the already localized error message
     * @param errorId the error identifier
     * @return the message with a localized error-ID suffix
     */
    public String detailWithId(final String message, final String errorId) {
        Objects.requireNonNull(message, "message cannot be null");
        Objects.requireNonNull(errorId, "errorId cannot be null");
        return label("error.detail.with-id", "%1$s (error ID: %2$s)", message, errorId);
    }

    /**
     * Resolves a localized label such as {@code "Business rule '%s'"}.
     *
     * <pre>{@code
     * ErrorLocalizer.of(Locale.FRENCH)
     *         .label("error.label.business-rule", "Business rule '%s'", "Currency mismatch");
     * // "Règle métier 'Currency mismatch'"
     * }</pre>
     *
     * @param key      the resource key
     * @param fallback the English fallback template
     * @param args     optional format arguments
     * @return the localized and formatted label
     */
    public String label(final String key, final String fallback, final Object... args) {
        final String template = resource(key).orElse(fallback).orNull();
        if (args == null || args.length == 0) {
            return template;
        }
        try {
            return String.format(locale, template, RedactionPolicy.standard().redactAll(args));
        } catch (final RuntimeException exception) {
            return template;
        }
    }

    /**
     * Returns a localized message without unresolved format specifiers.
     *
     * @param errorType the error type
     * @return the localized template, or the localized title when the template
     *         still contains placeholders
     */
    public String messageWithoutArgs(final ErrorType errorType) {
        final String template = messageTemplate(errorType);
        if (template == null) {
            return title(errorType);
        }
        return template.matches("(?s).*%[a-zA-Z%].*") ? title(errorType) : template;
    }

    /**
     * Returns the localized reason phrase for an HTTP status code. This is
     * used for {@code about:blank} problem titles.
     *
     * @param status the HTTP status code
     * @return the localized phrase
     */
    public String httpStatusTitle(final int status) {
        return HttpStatusMessages.title(status, locale);
    }

    private Maybe<String> resource(final String key) {
        if (Locale.ENGLISH.equals(locale)) {
            // English declarations on the error types are the source of truth:
            // messages.properties is the reference template for translators.
            return Maybe.none();
        }
        try {
            final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale);
            return bundle.containsKey(key) ? Maybe.of(bundle.getString(key)) : Maybe.none();
        } catch (final MissingResourceException exception) {
            return Maybe.none();
        }
    }

    private static Locale normalize(final Locale locale) {
        return SUPPORTED.stream()
                .filter(supported -> supported.getLanguage().equals(locale.getLanguage()))
                .findFirst()
                .orElse(Locale.ENGLISH);
    }
}
