package eu.infolead.jtk.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.anomaly.BusinessRuleError;
import eu.infolead.jtk.anomaly.BusinessRuleErrorType;
import eu.infolead.jtk.anomaly.ParameterError;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Either;

class MoneyTest {

    private static final Currency EUR =
            new Currency("EUR", "978", "Euro", OptionalInt.of(2), false);
    private static final Currency USD =
            new Currency("USD", "840", "US Dollar", OptionalInt.of(2), false);
    private static final Currency JPY =
            new Currency("JPY", "392", "Yen", OptionalInt.of(0), false);
    private static final Currency XAU =
            new Currency("XAU", "959", "Gold", OptionalInt.empty(), false);

    private static Money eur(final String amount) {
        return new Money(new BigDecimal(amount), EUR);
    }

    private static Money valueOf(final Either<? extends SystemError, Money> result) {
        assertTrue(result.isRight().toBoolean(), "expected a value");
        return result.fold(
                error -> {
                    throw new AssertionError("expected a value, got " + error.getMessage());
                },
                money -> money);
    }

    private static <L extends SystemError> L errorOf(final Either<L, Money> result) {
        assertTrue(result.isLeft().toBoolean(), "expected an error");
        return result.fold(
                error -> error,
                money -> {
                    throw new AssertionError("expected an error, got " + money);
                });
    }

    private static <T> T roundTrip(final T value) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            @SuppressWarnings("unchecked")
            final T restored = (T) in.readObject();
            return restored;
        }
    }

    // Creation and normalization

    @Test
    void normalizesAmountsToMinorUnitsWithBankersRounding() {
        assertEquals("1.00", eur("1.005").amount().toPlainString());
        assertEquals("1.02", eur("1.015").amount().toPlainString());
        assertEquals("2", new Money(new BigDecimal("1.5"), JPY).amount().toPlainString());
        assertEquals("0", new Money(new BigDecimal("0.5"), JPY).amount().toPlainString());
    }

    @Test
    void keepsPrecisionWhenMinorUnitsAreNotApplicable() {
        final Money gold = new Money(new BigDecimal("1.50"), XAU);
        assertEquals("1.5", gold.amount().toPlainString());
        assertEquals(gold, new Money(new BigDecimal("1.5"), XAU));
    }

    @Test
    void createsZeroAndWholeMajorUnits() {
        assertEquals("0.00 EUR", Money.zero(EUR).format());
        assertTrue(Money.zero(EUR).isZero());
        assertEquals("25.00 EUR", Money.of(25, EUR).format());
    }

    // Arithmetic

    @Test
    void addsAndSubtractsSameCurrency() {
        assertEquals(eur("30.30"), valueOf(eur("10.10").plus(eur("20.20"))));
        assertEquals(eur("14.75"), valueOf(eur("20.00").minus(eur("5.25"))));
        assertEquals(Money.zero(EUR), valueOf(eur("5.25").minus(eur("5.25"))));
    }

    @Test
    void plusReportsCurrencyMismatchAsBusinessRuleError() {
        final var error = errorOf(eur("1.00").plus(new Money(new BigDecimal("1.00"), USD)));
        assertEquals(BusinessRuleErrorType.CURRENCY_MISMATCH, error.getErrorType());
        assertEquals(422, error.getHttpStatus());
        assertTrue(error.getMessage().contains("EUR"));
        assertTrue(error.getMessage().contains("USD"));
    }

    @Test
    void minusReportsCurrencyMismatchAsBusinessRuleError() {
        final var error = errorOf(eur("1.00").minus(new Money(new BigDecimal("1.00"), USD)));
        assertEquals(BusinessRuleErrorType.CURRENCY_MISMATCH, error.getErrorType());
    }

    @Test
    void multipliesByIntegerFactorsExactly() {
        assertEquals(eur("30.03"), eur("10.01").times(3));
        assertEquals(Money.zero(EUR), eur("19.99").times(0));
        assertEquals(eur("-25.00"), eur("25.00").times(-1));
    }

    @Test
    void multipliesByDecimalFactorsWithDefaultRounding() {
        assertEquals(eur("50.02"), eur("100.05").times(new BigDecimal("0.5")));
        assertEquals(eur("10.06"), eur("10.01").times(new BigDecimal("1.005")));
    }

    @Test
    void multipliesByDecimalFactorsWithExplicitRounding() {
        final BigDecimal half = new BigDecimal("0.5");
        assertEquals(eur("50.03"), eur("100.05").times(half, RoundingMode.HALF_UP));
        assertEquals(eur("50.02"), eur("100.05").times(half, RoundingMode.HALF_EVEN));
        assertEquals(eur("50.02"), eur("100.05").times(half, RoundingMode.DOWN));
    }

    @Test
    void negatesAndTakesAbsoluteValue() {
        assertEquals(eur("-12.34"), eur("12.34").negate());
        assertEquals(eur("12.34"), eur("-12.34").abs());
        assertEquals(eur("12.34"), eur("12.34").abs());
    }

    @Test
    void classifiesSigns() {
        assertTrue(eur("0.01").isPositive());
        assertTrue(eur("-0.01").isNegative());
        assertTrue(eur("0.00").isZero());
        assertFalse(eur("0.01").isNegative());
        assertFalse(eur("-0.01").isPositive());
        assertFalse(eur("0.00").isPositive());
    }

    // Comparison

    @Test
    void comparesAmountsOfTheSameCurrency() {
        assertTrue(eur("5.00").isLessThan(eur("10.00")));
        assertTrue(eur("10.00").isGreaterThan(eur("5.00")));
        assertTrue(eur("10.00").isGreaterThanOrEqualTo(eur("10.00")));
        assertTrue(eur("10.00").isLessThanOrEqualTo(eur("10.00")));
        assertEquals(0, eur("10.00").compareTo(eur("10.00")));
        assertEquals(eur("5.00"), Money.min(eur("5.00"), eur("10.00")));
        assertEquals(eur("10.00"), Money.max(eur("5.00"), eur("10.00")));
    }

    @Test
    void comparisonRejectsMixedCurrencies() {
        assertThrows(IllegalArgumentException.class,
                () -> eur("1.00").compareTo(new Money(new BigDecimal("1.00"), USD)));
        assertThrows(IllegalArgumentException.class, () -> Money.min(eur("1.00"), Money.zero(USD)));
    }

    @Test
    void equalityIgnoresInputScaleButIncludesCurrency() {
        assertEquals(eur("10.00"), Money.of(10, EUR));
        assertEquals(eur("10.00").hashCode(), Money.of(10, EUR).hashCode());
        assertEquals(Money.zero(EUR), eur("0.00"));
        assertFalse(eur("10.00").equals(new Money(new BigDecimal("10.00"), USD)));
    }

    @Test
    void currencyIdentityIsTheAlphabeticCode() {
        final Currency renamedEuro =
                new Currency("EUR", "978", "Euro (renamed)", OptionalInt.of(2), false);
        final Money one = eur("1.00");
        final Money two = new Money(new BigDecimal("2.00"), renamedEuro);
        assertTrue(one.isSameCurrencyAs(two));
        assertTrue(one.isLessThan(two));
        assertEquals(eur("3.00"), valueOf(one.plus(two)));
        assertFalse(one.equals(two));
    }

    // Allocation

    @Test
    void allocatesEqualPartsAndDistributesTheRemainder() {
        final List<Money> parts = eur("100.00").allocate(3);
        assertEquals(List.of(eur("33.34"), eur("33.33"), eur("33.33")), parts);
        assertEquals(eur("100.00"), valueOf(sum(parts)));
    }

    @Test
    void allocatesASinglePartAsItself() {
        assertEquals(List.of(eur("12.34")), eur("12.34").allocate(1));
    }

    @Test
    void allocatesNegativeAmountsFairly() {
        final List<Money> parts = eur("-100.00").allocate(3);
        assertEquals(List.of(eur("-33.33"), eur("-33.33"), eur("-33.34")), parts);
        assertEquals(eur("-100.00"), valueOf(sum(parts)));
        assertEquals(eur("-0.01"), valueOf(sum(eur("-0.01").allocate(3))));
    }

    @Test
    void allocatesWithoutMinorUnitsAndWithoutFixedScale() {
        final Money gold = new Money(new BigDecimal("10"), JPY);
        assertEquals(List.of(
                new Money(new BigDecimal("4"), JPY),
                new Money(new BigDecimal("3"), JPY),
                new Money(new BigDecimal("3"), JPY)), gold.allocate(3));
        final Money goldHalf = new Money(new BigDecimal("10.02"), XAU);
        final List<Money> halves = goldHalf.allocate(2);
        assertEquals(goldHalf, valueOf(sum(halves)));
    }

    @Test
    void allocatesRoundAmountsWithoutMinorUnitsAtUnitGranularity() {
        // stripTrailingZeros turns 100 into 1E+2; the granularity must not
        // drift to 100 for such amounts (review finding M1).
        final Money gold = new Money(new BigDecimal("100"), XAU);
        assertEquals(List.of(
                new Money(new BigDecimal("34"), XAU),
                new Money(new BigDecimal("33"), XAU),
                new Money(new BigDecimal("33"), XAU)), gold.allocate(3));
        assertEquals(gold, valueOf(sum(gold.allocate(3))));
        assertEquals(List.of(
                new Money(new BigDecimal("33.5"), XAU),
                new Money(new BigDecimal("33.5"), XAU),
                new Money(new BigDecimal("33.5"), XAU)),
                new Money(new BigDecimal("100.5"), XAU).allocate(3));
    }

    @Test
    void allocatesProportionallyToWeights() {
        assertEquals(List.of(eur("3.34"), eur("3.33"), eur("3.33")),
                eur("10.00").allocate(1, 1, 1));
        assertEquals(List.of(eur("45.00"), eur("22.50"), eur("22.50")),
                eur("90.00").allocate(2, 1, 1));
        assertEquals(List.of(eur("0.00"), eur("100.00")),
                eur("100.00").allocate(0, 1));
    }

    @Test
    void weightedAllocationPreservesTheTotal() {
        final String[] totals = { "0.01", "100.00", "-0.01", "999.99", "12345.67" };
        final long[][] weightSets = { { 1, 1, 1 }, { 2, 1, 1 }, { 1, 0, 1 }, { 7, 3 }, { 1 } };
        for (final String total : totals) {
            for (final long[] weights : weightSets) {
                final Money original = eur(total);
                final List<Money> parts = original.allocate(weights);
                assertEquals(weights.length, parts.size());
                assertEquals(original, valueOf(sum(parts)), () -> "total changed for " + total);
            }
        }
    }

    @Test
    void rejectsInvalidAllocations() {
        assertThrows(IllegalArgumentException.class, () -> eur("1.00").allocate(0));
        assertThrows(IllegalArgumentException.class, () -> eur("1.00").allocate(-1));
        assertThrows(IllegalArgumentException.class, () -> eur("1.00").allocate());
        assertThrows(IllegalArgumentException.class, () -> eur("1.00").allocate(1, -1));
        assertThrows(IllegalArgumentException.class, () -> eur("1.00").allocate(0, 0));
    }

    // Parse and format

    @Test
    void parsesAmountsWithAndWithoutCurrencyCodes() {
        assertEquals(eur("12.34"), valueOf(Money.parse("12.34", EUR)));
        assertEquals(eur("12.34"), valueOf(Money.parse("12.34 EUR", EUR)));
        assertEquals(eur("12.34"), valueOf(Money.parse("  12.34  eur ", EUR)));
        assertEquals(eur("-12.34"), valueOf(Money.parse("-12.34", EUR)));
        assertEquals(Money.zero(EUR), valueOf(Money.parse("0.00 EUR", EUR)));
    }

    @Test
    void parsesIntoOtherMinorUnitSchemes() {
        assertEquals(new Money(new BigDecimal("5"), JPY), valueOf(Money.parse("5", JPY)));
        assertEquals(new Money(new BigDecimal("1.25"), XAU), valueOf(Money.parse("1.25 XAU", XAU)));
    }

    @Test
    void roundsParsedAmountsToMinorUnits() {
        assertEquals(eur("1.00"), valueOf(Money.parse("1.005", EUR)));
        assertEquals(eur("1.02"), valueOf(Money.parse("1.015", EUR)));
    }

    @Test
    void rejectsMalformedAndMismatchedParseInput() {
        assertTrue(Money.parse(null, EUR).isLeft().toBoolean());
        assertTrue(Money.parse("", EUR).isLeft().toBoolean());
        assertTrue(Money.parse("   ", EUR).isLeft().toBoolean());
        assertTrue(Money.parse("abc", EUR).isLeft().toBoolean());
        assertTrue(Money.parse("12,34", EUR).isLeft().toBoolean());
        final Either<ParameterError, Money> mismatch = Money.parse("12.34 USD", EUR);
        assertTrue(mismatch.isLeft().toBoolean());
        assertEquals("INVALID_FORMAT", errorOf(mismatch).getErrorType().getCode());
    }

    @Test
    void parseRejectsExponentNotation() {
        // Exponents must be rejected before BigDecimal construction; a huge
        // exponent would otherwise exhaust memory in the constructor (M2).
        assertTrue(Money.parse("1e2", EUR).isLeft().toBoolean());
        assertTrue(Money.parse("1E+2", EUR).isLeft().toBoolean());
        assertTrue(Money.parse("1.5e-3 EUR", EUR).isLeft().toBoolean());
        assertTrue(Money.parse("1e1000000000", EUR).isLeft().toBoolean());
        assertEquals(eur("12.34"), valueOf(Money.parse("12.34", EUR)));
    }

    @Test
    void formatsAndPrintsCanonically() {
        assertEquals("12.34 EUR", eur("12.34").format());
        assertEquals("12.34 EUR", eur("12.34").toString());
        assertEquals("-0.01 EUR", eur("-0.01").format());
        assertEquals("0 JPY", new Money(new BigDecimal("0"), JPY).format());
        assertEquals("100 XAU", new Money(new BigDecimal("100"), XAU).format());
    }

    @Test
    void formatOutputRoundTripsThroughParse() {
        for (final Money money : List.of(eur("12.34"), eur("-0.01"),
                new Money(new BigDecimal("100"), XAU), new Money(new BigDecimal("7"), JPY))) {
            assertEquals(money, valueOf(Money.parse(money.format(), money.currency())));
        }
    }

    // Serialization

    @Test
    void roundTripsThroughJavaSerialization() throws IOException, ClassNotFoundException {
        final Money money = eur("12.34");
        assertEquals(money, roundTrip(money));
        assertEquals(new Money(new BigDecimal("1.5"), XAU), roundTrip(new Money(new BigDecimal("1.50"), XAU)));
        assertEquals(EUR, roundTrip(EUR));
    }

    private static Either<BusinessRuleError, Money> sum(final List<Money> parts) {
        Either<BusinessRuleError, Money> total =
                Either.right(Money.zero(parts.get(0).currency()));
        for (final Money part : parts) {
            total = total.flatMap(sum -> part.plus(sum));
        }
        return total;
    }
}
