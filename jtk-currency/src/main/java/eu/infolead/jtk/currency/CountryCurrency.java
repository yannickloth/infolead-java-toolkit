package eu.infolead.jtk.currency;

import java.util.Objects;

/**
 * Associates a country or territory with a currency.
 * <p>
 * A country may appear in several entries when it uses more than one currency
 * (for example, Bhutan uses both the Indian rupee and the ngultrum).
 *
 * @param country  the country or territory name
 * @param currency the currency used there
 */
public record CountryCurrency(String country, Currency currency) {

    /**
     * Canonical constructor with validation.
     */
    public CountryCurrency {
        country = Objects.requireNonNull(country, "country").replace('\u00A0', ' ').strip();
        if (country.isEmpty()) {
            throw new IllegalArgumentException("Country name cannot be blank");
        }
        Objects.requireNonNull(currency, "currency");
    }
}
