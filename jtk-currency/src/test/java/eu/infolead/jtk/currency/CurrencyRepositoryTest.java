package eu.infolead.jtk.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class CurrencyRepositoryTest {

    private static final CurrencyRepository REPOSITORY = CurrencyRepository.load();

    @Test
    void loadsTheBundledSnapshot() {
        assertEquals(LocalDate.of(2026, 9, 17), REPOSITORY.publishedOn());
        assertTrue(REPOSITORY.size() > 150, "the snapshot should contain the ISO currencies");
        assertTrue(REPOSITORY.entryCount() > REPOSITORY.size(),
                "countries using the same currency produce separate associations");
    }

    @Test
    void preservesZeroPaddedNumericCodes() {
        final Currency lek = REPOSITORY.findByCode("ALL").orElseThrow();
        assertEquals("008", lek.numericCode());
        assertEquals(2, lek.minorUnits().orElseThrow());
    }

    @Test
    void handlesNotApplicableMinorUnits() {
        final Currency sdr = REPOSITORY.findByCode("XDR").orElseThrow();
        assertTrue(sdr.minorUnits().isEmpty());
        assertFalse(sdr.isSpendable());
    }

    @Test
    void skipsEntriesWithoutACurrencyCode() {
        assertTrue(REPOSITORY.currencies().stream()
                .noneMatch(currency -> "No universal currency".equals(currency.name())),
                "currency-less entries such as ANTARCTICA must be skipped");
    }

    @Test
    void mergesDuplicateCurrencyRows() {
        final long dollarDefinitions = REPOSITORY.currencies().stream()
                .filter(currency -> "USD".equals(currency.code()))
                .count();
        assertEquals(1, dollarDefinitions);
        assertTrue(REPOSITORY.findByCountry("UNITED STATES OF AMERICA (THE)").stream()
                .anyMatch(currency -> "USD".equals(currency.code())));
    }

    @Test
    void keepsMultipleCurrenciesPerCountry() {
        final List<Currency> bolivia = REPOSITORY.findByCountry("bolivia (plurinational state of)");
        final Set<String> codes = bolivia.stream()
                .map(Currency::code)
                .collect(Collectors.toSet());
        assertEquals(Set.of("BOB", "BOV"), codes);
    }

    @Test
    void marksFundsAsNonSpendable() {
        final Currency mvdol = REPOSITORY.findByCode("BOV").orElseThrow();
        assertTrue(mvdol.fund());
        assertFalse(mvdol.isSpendable());
    }

    @Test
    void trimsNamesAndCountryNames() {
        assertEquals("Comorian Franc", REPOSITORY.findByCode("KMF").orElseThrow().name());
        assertTrue(REPOSITORY.countries().contains("INTERNATIONAL MONETARY FUND (IMF)"));
    }

    @Test
    void excludesTestCodesMetalsAndUnitsOfAccount() {
        assertFalse(REPOSITORY.findByCode("XTS").orElseThrow().isSpendable());
        assertFalse(REPOSITORY.findByCode("XXX").orElseThrow().isSpendable());
        assertFalse(REPOSITORY.findByCode("XAU").orElseThrow().isSpendable());
        assertFalse(REPOSITORY.findByCode("XDR").orElseThrow().isSpendable());
        assertTrue(REPOSITORY.findByCode("EUR").orElseThrow().isSpendable());
    }

    @Test
    void countryLookupHandlesTurkishDottedCapitalI() {
        assertTrue(REPOSITORY.findByCountry("Türkiye").stream()
                .anyMatch(currency -> "TRY".equals(currency.code())));
    }

    @Test
    void lookupIsCaseInsensitiveAndTrimmed() {
        assertEquals("USD", REPOSITORY.findByCode(" usd ").orElseThrow().code());
    }
}
