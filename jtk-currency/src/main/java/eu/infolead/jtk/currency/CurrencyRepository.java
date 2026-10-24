package eu.infolead.jtk.currency;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Read-only repository of ISO 4217 currency data loaded from the SIX
 * {@code list-one.xml} snapshot.
 * <p>
 * The parser is deliberately tolerant of the quirks of the official list:
 * entries without a currency code are skipped, {@code CcyMnrUnts="N.A."} maps
 * to an empty {@link OptionalInt}, zero-padded numeric codes are preserved as
 * strings, and repeated country/currency rows are merged without losing
 * currencies.
 * <p>
 * The bundled snapshot is pinned by {@link #publishedOn()} so that consumers
 * can detect stale data instead of silently transacting with retired codes.
 */
public final class CurrencyRepository {

    /** Classpath location of the bundled ISO 4217 snapshot. */
    public static final String RESOURCE = "/list-one.xml";

    private static final String ROOT_ELEMENT = "ISO_4217";
    private static final String ENTRY_ELEMENT = "CcyNtry";
    private static final String NOT_APPLICABLE = "N.A.";

    private final LocalDate publishedOn;
    private final Map<String, Currency> currenciesByCode;
    private final Map<String, List<Currency>> currenciesByCountry;
    private final Map<String, String> countryNames;
    private final List<CountryCurrency> entries;

    private CurrencyRepository(
            final LocalDate publishedOn,
            final Map<String, Currency> currenciesByCode,
            final Map<String, List<Currency>> currenciesByCountry,
            final Map<String, String> countryNames,
            final List<CountryCurrency> entries) {
        this.publishedOn = publishedOn;
        this.currenciesByCode = Collections.unmodifiableMap(new LinkedHashMap<>(currenciesByCode));
        final Map<String, List<Currency>> immutableByCountry = new LinkedHashMap<>();
        currenciesByCountry.forEach((country, currencies) ->
                immutableByCountry.put(country, List.copyOf(currencies)));
        this.currenciesByCountry = Collections.unmodifiableMap(immutableByCountry);
        this.countryNames = Collections.unmodifiableMap(new LinkedHashMap<>(countryNames));
        this.entries = List.copyOf(entries);
    }

    /**
     * Loads the bundled ISO 4217 snapshot from the classpath.
     *
     * @return the loaded repository
     * @throws IllegalStateException if the resource is missing or malformed
     */
    public static CurrencyRepository load() {
        return Holder.INSTANCE;
    }

    private static CurrencyRepository read() {
        try (InputStream input = CurrencyRepository.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing classpath resource " + RESOURCE);
            }
            try {
                return parse(input);
            } catch (final IllegalArgumentException exception) {
                throw new IllegalStateException("Malformed " + RESOURCE, exception);
            }
        } catch (final IOException exception) {
            throw new IllegalStateException("Unable to read " + RESOURCE, exception);
        }
    }

    /** Lazily initialized singleton for the immutable classpath snapshot. */
    private static final class Holder {
        private static final CurrencyRepository INSTANCE = read();

        private Holder() {
        }
    }

    /**
     * Parses an ISO 4217 {@code list-one.xml} document.
     *
     * @param input the XML input; the caller keeps ownership of the stream
     * @return the parsed repository
     * @throws IllegalArgumentException if the document is malformed
     */
    public static CurrencyRepository parse(final InputStream input) {
        Objects.requireNonNull(input, "input");
        final Document document;
        try {
            document = newDocumentBuilder().parse(input);
        } catch (final SAXException | IOException exception) {
            throw new IllegalArgumentException("Invalid ISO 4217 XML document", exception);
        }

        final Element root = document.getDocumentElement();
        if (!ROOT_ELEMENT.equals(root.getTagName())) {
            throw new IllegalArgumentException(
                    "Expected root element " + ROOT_ELEMENT + ", found " + root.getTagName());
        }

        final LocalDate publishedOn = parsePublishedDate(root.getAttribute("Pblshd"));
        final List<RawEntry> rawEntries = readEntries(root);
        final Map<String, Currency> currencies = mergeCurrencies(rawEntries);

        final Map<String, List<Currency>> byCountry = new LinkedHashMap<>();
        final Map<String, String> countryNames = new LinkedHashMap<>();
        final List<CountryCurrency> entries = new ArrayList<>();
        final Set<String> seenPairs = new LinkedHashSet<>();

        for (final RawEntry raw : rawEntries) {
            if (raw.country() == null || raw.country().isEmpty() || isPseudoCountry(raw.country())) {
                continue;
            }
            final Currency currency = currencies.get(raw.code().strip().toUpperCase(Locale.ROOT));
            if (currency == null) {
                throw new IllegalArgumentException("Unknown currency code: " + raw.code());
            }
            final String countryKey = normalizeCountry(raw.country());
            countryNames.putIfAbsent(countryKey, raw.country());
            final List<Currency> countryCurrencies = byCountry.computeIfAbsent(
                    countryKey, key -> new ArrayList<>());
            if (!countryCurrencies.contains(currency)) {
                countryCurrencies.add(currency);
            }
            if (seenPairs.add(countryKey + '\u0000' + currency.code())) {
                entries.add(new CountryCurrency(raw.country(), currency));
            }
        }

        return new CurrencyRepository(
                publishedOn,
                currencies,
                byCountry,
                countryNames,
                entries);
    }

    /**
     * Returns the publication date of the bundled snapshot.
     *
     * @return the ISO 4217 publication date
     */
    public LocalDate publishedOn() {
        return publishedOn;
    }

    /**
     * Returns every distinct currency in the snapshot, in document order.
     *
     * @return an unmodifiable list of currencies
     */
    public List<Currency> currencies() {
        return List.copyOf(currenciesByCode.values());
    }

    /**
     * Returns the currencies that can be offered to end users for payment.
     *
     * @return an unmodifiable list of spendable currencies
     */
    public List<Currency> spendableCurrencies() {
        return currenciesByCode.values().stream()
                .filter(Currency::isSpendable)
                .toList();
    }

    /**
     * Looks up a currency by its alphabetic code.
     *
     * @param code the three-letter code, case-insensitive
     * @return the currency, or empty when the code is unknown or has been
     *         withdrawn from the current snapshot; use {@link #publishedOn()} to
     *         detect an outdated snapshot
     */
    public Optional<Currency> findByCode(final String code) {
        Objects.requireNonNull(code, "code");
        return Optional.ofNullable(currenciesByCode.get(code.strip().toUpperCase(Locale.ROOT)));
    }

    /**
     * Returns all currencies used by a country or territory.
     *
     * @param country the country name, case-insensitive
     * @return an unmodifiable list, empty when the country is unknown
     */
    public List<Currency> findByCountry(final String country) {
        Objects.requireNonNull(country, "country");
        return List.copyOf(currenciesByCountry.getOrDefault(
                normalizeCountry(country), List.of()));
    }

    /**
     * Returns all known country and territory names.
     *
     * @return an unmodifiable list of country names
     */
    public List<String> countries() {
        return List.copyOf(countryNames.values());
    }

    /**
     * Returns every country/currency association in document order.
     *
     * @return an unmodifiable list of associations
     */
    public List<CountryCurrency> entries() {
        return entries;
    }

    /**
     * Returns the number of distinct currencies.
     *
     * @return the number of currencies
     */
    public int size() {
        return currenciesByCode.size();
    }

    /**
     * Returns the number of country/currency associations.
     *
     * @return the number of associations
     */
    public int entryCount() {
        return entries.size();
    }

    private static List<RawEntry> readEntries(final Element root) {
        final NodeList nodes = root.getElementsByTagName(ENTRY_ELEMENT);
        final List<RawEntry> rawEntries = new ArrayList<>(nodes.getLength());
        for (int index = 0; index < nodes.getLength(); index++) {
            final Element entry = (Element) nodes.item(index);
            final String code = text(entry, "Ccy");
            if (code == null || code.isEmpty()) {
                // Entries such as ANTARCTICA only describe why no currency exists.
                continue;
            }
            final String numericCode = text(entry, "CcyNbr");
            if (numericCode == null || numericCode.isEmpty()) {
                throw new IllegalArgumentException("Missing CcyNbr for currency " + code);
            }
            final String name = text(entry, "CcyNm");
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Missing CcyNm for currency " + code);
            }
            rawEntries.add(new RawEntry(
                    text(entry, "CtryNm"),
                    code,
                    name,
                    numericCode,
                    parseMinorUnits(text(entry, "CcyMnrUnts")),
                    isFund(entry)));
        }
        return rawEntries;
    }

    private static Map<String, Currency> mergeCurrencies(final List<RawEntry> rawEntries) {
        final Map<String, Currency> currencies = new LinkedHashMap<>();
        for (final RawEntry raw : rawEntries) {
            final Currency candidate = new Currency(
                    raw.code(), raw.numericCode(), raw.name(), raw.minorUnits(), raw.fund());
            final Currency existing = currencies.get(candidate.code());
            currencies.put(candidate.code(),
                    existing == null ? candidate : merge(existing, candidate));
        }
        return currencies;
    }

    private static Currency merge(final Currency existing, final Currency candidate) {
        if (!existing.numericCode().equals(candidate.numericCode())
                || !existing.name().equals(candidate.name())
                || !existing.minorUnits().equals(candidate.minorUnits())) {
            throw new IllegalArgumentException("Conflicting definitions for currency "
                    + existing.code() + ": existing=" + existing + ", candidate=" + candidate);
        }
        return existing.fund() || candidate.fund()
                ? new Currency(existing.code(), existing.numericCode(), existing.name(),
                        existing.minorUnits(), true)
                : existing;
    }

    private static OptionalInt parseMinorUnits(final String value) {
        if (value == null || value.isEmpty() || NOT_APPLICABLE.equalsIgnoreCase(value)) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(value));
        } catch (final NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid CcyMnrUnts value: '" + value + "'", exception);
        }
    }

    private static boolean isFund(final Element entry) {
        final NodeList names = entry.getElementsByTagName("CcyNm");
        if (names.getLength() == 0) {
            return false;
        }
        final Element name = (Element) names.item(0);
        return "true".equalsIgnoreCase(name.getAttribute("IsFund").strip());
    }

    private static String text(final Element parent, final String tagName) {
        final NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return normalize(nodes.item(0).getTextContent());
    }

    private static String normalize(final String value) {
        return value == null ? null : value.replace('\u00A0', ' ').strip();
    }

    private static String normalizeCountry(final String country) {
        return normalize(country).toUpperCase(Locale.ROOT).replace('\u0130', 'I');
    }

    private static boolean isPseudoCountry(final String country) {
        return country.regionMatches(true, 0, "ZZ", 0, 2);
    }

    private static LocalDate parsePublishedDate(final String value) {
        final String normalized = normalize(value);
        if (normalized == null || normalized.isEmpty()) {
            throw new IllegalArgumentException("Missing ISO_4217 Pblshd attribute");
        }
        try {
            return LocalDate.parse(normalized);
        } catch (final DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid ISO_4217 Pblshd value: '" + value + "'", exception);
        }
    }

    private static DocumentBuilder newDocumentBuilder() {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            setFeatureIfSupported(factory, "http://xml.org/sax/features/external-general-entities", false);
            setFeatureIfSupported(factory, "http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            try {
                factory.setXIncludeAware(false);
            } catch (final UnsupportedOperationException ignored) {
                // Optional hardening; the mandatory DOCTYPE restriction remains in force.
            }
            try {
                factory.setExpandEntityReferences(false);
            } catch (final UnsupportedOperationException ignored) {
                // Optional hardening; the mandatory DOCTYPE restriction remains in force.
            }
            return factory.newDocumentBuilder();
        } catch (final ParserConfigurationException | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to configure a secure XML parser", exception);
        }
    }

    private static void setFeatureIfSupported(
            final DocumentBuilderFactory factory, final String feature, final boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (final ParserConfigurationException | RuntimeException ignored) {
            // Provider-specific feature; the standard secure-processing settings remain in force.
        }
    }

    private record RawEntry(
            String country,
            String code,
            String name,
            String numericCode,
            OptionalInt minorUnits,
            boolean fund) {
    }
}
