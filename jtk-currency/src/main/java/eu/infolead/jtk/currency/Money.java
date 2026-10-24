package eu.infolead.jtk.currency;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import eu.infolead.jtk.anomaly.BusinessRuleError;
import eu.infolead.jtk.anomaly.ParameterError;
import eu.infolead.jtk.fp.either.Either;

/**
 * A monetary amount in a specific {@link Currency}, held as a
 * {@link BigDecimal} normalized to the currency's minor units.
 *
 * <p>Money is an immutable value: every operation returns a new instance and
 * the amount is always expressed with the scale of the currency's
 * {@link Currency#minorUnits() minor units} (for example scale 2 for EUR).
 * Amounts supplied with a different scale are rounded once, on creation, using
 * banker's rounding ({@link RoundingMode#HALF_EVEN}), the rounding mode
 * conventionally required in financial computations because it does not
 * accumulate a systematic upward bias. For codes whose ISO 4217 minor-unit
 * entry is {@code N.A.}, such as the precious metals, no fixed scale exists:
 * the amount keeps its own precision, and trailing zeros are stripped so that
 * equal values always compare equal.</p>
 *
 * <p>Operations that combine two amounts, {@link #plus(Money)} and
 * {@link #minus(Money)}, refuse mixed currencies. Rather than throwing, they
 * return an {@link Either} whose left channel carries a
 * {@link BusinessRuleError} of type
 * {@code CURRENCY_MISMATCH}, so callers remain on the
 * railway-oriented error path this toolkit is built around. Currency
 * compatibility is decided by the ISO 4217 alphabetic code alone: amounts
 * whose currency definitions differ only in display metadata, such as a
 * renamed currency, still combine, while {@link #equals(Object)} remains full
 * value equality of amount and currency definition.</p>
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * Currency eur = CurrencyRepository.load().findByCode("EUR").orElseThrow();
 * Currency usd = CurrencyRepository.load().findByCode("USD").orElseThrow();
 *
 * Money unitPrice = new Money(new BigDecimal("19.99"), eur);   // 19.99 EUR
 * Money total = unitPrice.times(3);                            // 59.97 EUR
 * List<Money> shares = unitPrice.allocate(3);                  // 6.67, 6.66, 6.66 EUR
 *
 * Either<BusinessRuleError, Money> change =
 *         Money.of(50, eur).minus(unitPrice);                  // right: 30.01 EUR
 * Either<BusinessRuleError, Money> rejected =
 *         unitPrice.plus(Money.of(5, usd));                    // left:  CURRENCY_MISMATCH
 *
 * String text = unitPrice.format();                            // "19.99 EUR"
 * Money parsed = Money.parse("19.99 EUR", eur)
 *         .fold(error -> null, money -> money);                // 19.99 EUR
 * }</pre>
 *
 * <p>Instances are serializable for web-session storage; deserialization
 * restores values through the canonical constructor, so a corrupted amount
 * cannot bypass normalization.</p>
 */
public record Money(BigDecimal amount, Currency currency)
        implements Comparable<Money>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The rounding mode applied whenever an operation must reduce the scale of
     * an amount: banker's rounding, {@link RoundingMode#HALF_EVEN}.
     */
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;

    private static final BigInteger ONE = BigInteger.ONE;

    /**
     * The canonical amount grammar accepted by
     * {@link #parse(String, Currency)}: an optionally signed plain decimal
     * without exponent. Keeping exponents out both matches {@link #format()}
     * and prevents inputs such as {@code 1e1000000000} from making the
     * constructor materialize an enormous unscaled value.
     */
    private static final Pattern CANONICAL_AMOUNT = Pattern.compile("[+-]?\\d+(\\.\\d+)?");

    /**
     * Canonical constructor with validation and minor-unit normalization.
     *
     * @param amount   the amount; rounded to the currency's minor units with
     *                 {@link #DEFAULT_ROUNDING} when the currency defines them
     * @param currency the currency of the amount
     * @throws NullPointerException if amount or currency is {@code null}
     */
    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        amount = normalize(amount, currency);
    }

    /**
     * Creates money from a whole number of major units.
     *
     * @param amount   the whole major-unit amount, such as {@code 25}
     * @param currency the currency of the amount
     * @return money worth {@code amount} major units
     */
    public static Money of(final long amount, final Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Creates zero money in the given currency.
     *
     * @param currency the currency of the amount
     * @return zero in the given currency
     */
    public static Money zero(final Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Adds another amount of the same currency.
     *
     * <pre>{@code
     * Money eur10 = Money.of(10, eur);
     * eur10.plus(new Money(new BigDecimal("1.50"), eur));
     *         // Either.right: 11.50 EUR
     * eur10.plus(Money.of(5, usd));
     *         // Either.left: CURRENCY_MISMATCH business rule error
     * }</pre>
     *
     * @param augend the amount to add
     * @return either the sum, or a {@code CURRENCY_MISMATCH}
     *         {@link BusinessRuleError} when the currency codes differ
     */
    public Either<BusinessRuleError, Money> plus(final Money augend) {
        Objects.requireNonNull(augend, "augend");
        if (isMismatch(augend)) {
            return Either.left(mismatch(augend));
        }
        return Either.right(new Money(amount.add(augend.amount), currency));
    }

    /**
     * Subtracts another amount of the same currency.
     *
     * @param subtrahend the amount to subtract
     * @return either the difference, or a {@code CURRENCY_MISMATCH}
     *         {@link BusinessRuleError} when the currency codes differ
     */
    public Either<BusinessRuleError, Money> minus(final Money subtrahend) {
        Objects.requireNonNull(subtrahend, "subtrahend");
        if (isMismatch(subtrahend)) {
            return Either.left(mismatch(subtrahend));
        }
        return Either.right(new Money(amount.subtract(subtrahend.amount), currency));
    }

    /**
     * Multiplies by an integer factor. The product is exact: integer
     * multiplication never requires rounding beyond the normalization the
     * constructor applies.
     *
     * <pre>{@code
     * new Money(new BigDecimal("10.01"), eur).times(3)   // 30.03 EUR
     * }</pre>
     *
     * @param multiplicand the integer factor
     * @return the product
     */
    public Money times(final long multiplicand) {
        return new Money(amount.multiply(BigDecimal.valueOf(multiplicand)), currency);
    }

    /**
     * Multiplies by a decimal factor, rounding to the currency's minor units
     * with {@link #DEFAULT_ROUNDING}.
     *
     * <pre>{@code
     * new Money(new BigDecimal("100.05"), eur).times(new BigDecimal("0.5"))
     *         // 50.025 EUR rounded HALF_EVEN to 50.02 EUR
     * }</pre>
     *
     * @param multiplicand the decimal factor
     * @return the rounded product
     */
    public Money times(final BigDecimal multiplicand) {
        Objects.requireNonNull(multiplicand, "multiplicand");
        return new Money(amount.multiply(multiplicand), currency);
    }

    /**
     * Multiplies by a decimal factor, rounding to the currency's minor units
     * with an explicit rounding mode.
     *
     * @param multiplicand the decimal factor
     * @param roundingMode the rounding mode to apply; {@code UNNECESSARY}
     *                     throws when the product is not exactly
     *                     representable
     * @return the rounded product
     */
    public Money times(final BigDecimal multiplicand, final RoundingMode roundingMode) {
        Objects.requireNonNull(multiplicand, "multiplicand");
        Objects.requireNonNull(roundingMode, "roundingMode");
        final BigDecimal product = amount.multiply(multiplicand);
        return new Money(
                currency.minorUnits().isPresent()
                        ? product.setScale(currency.minorUnits().getAsInt(), roundingMode)
                        : product,
                currency);
    }

    /**
     * Returns the negated amount: {@code (-this)}.
     *
     * @return the negated amount
     */
    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    /**
     * Returns the absolute amount.
     *
     * @return the absolute amount
     */
    public Money abs() {
        return new Money(amount.abs(), currency);
    }

    /**
     * Indicates whether the amount is zero.
     *
     * @return true when the amount is zero
     */
    public boolean isZero() {
        return amount.signum() == 0;
    }

    /**
     * Indicates whether the amount is strictly positive.
     *
     * @return true when the amount is greater than zero
     */
    public boolean isPositive() {
        return amount.signum() > 0;
    }

    /**
     * Indicates whether the amount is strictly negative.
     *
     * @return true when the amount is less than zero
     */
    public boolean isNegative() {
        return amount.signum() < 0;
    }

    /**
     * Indicates whether both amounts share the same ISO 4217 currency code.
     * Arithmetic compatibility is decided by the alphabetic code alone, so
     * amounts whose currency definitions differ only in display metadata,
     * such as a renamed currency, still combine. Use {@link #equals(Object)}
     * for full value equality of amount and currency definition.
     *
     * @param other the amount to compare against
     * @return true when both amounts use the same currency code
     */
    public boolean isSameCurrencyAs(final Money other) {
        Objects.requireNonNull(other, "other");
        return currency.code().equals(other.currency.code());
    }

    /**
     * Compares two amounts by value. Comparing amounts of different currencies
     * is a programming error because no exchange rate is implied; use
     * {@link #isSameCurrencyAs(Money)} to test compatibility first.
     *
     * @param other the amount to compare against
     * @return a negative, zero, or positive number as this amount is less
     *         than, equal to, or greater than the other amount
     * @throws IllegalArgumentException if the currencies differ
     */
    @Override
    public int compareTo(final Money other) {
        Objects.requireNonNull(other, "other");
        if (isMismatch(other)) {
            throw new IllegalArgumentException(
                    "Cannot compare " + format() + " with " + other.format()
                            + ": currencies differ");
        }
        return amount.compareTo(other.amount);
    }

    /**
     * Indicates whether this amount is strictly greater than the other.
     *
     * @param other the amount to compare against
     * @return true when this amount is greater
     * @throws IllegalArgumentException if the currencies differ
     */
    public boolean isGreaterThan(final Money other) {
        return compareTo(other) > 0;
    }

    /**
     * Indicates whether this amount is greater than or equal to the other.
     *
     * @param other the amount to compare against
     * @return true when this amount is greater than or equal
     * @throws IllegalArgumentException if the currencies differ
     */
    public boolean isGreaterThanOrEqualTo(final Money other) {
        return compareTo(other) >= 0;
    }

    /**
     * Indicates whether this amount is strictly less than the other.
     *
     * @param other the amount to compare against
     * @return true when this amount is less
     * @throws IllegalArgumentException if the currencies differ
     */
    public boolean isLessThan(final Money other) {
        return compareTo(other) < 0;
    }

    /**
     * Indicates whether this amount is less than or equal to the other.
     *
     * @param other the amount to compare against
     * @return true when this amount is less than or equal
     * @throws IllegalArgumentException if the currencies differ
     */
    public boolean isLessThanOrEqualTo(final Money other) {
        return compareTo(other) <= 0;
    }

    /**
     * Returns the smaller of two amounts.
     *
     * @param first  the first amount
     * @param second the second amount
     * @return the smaller amount
     * @throws IllegalArgumentException if the currencies differ
     */
    public static Money min(final Money first, final Money second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    /**
     * Returns the larger of two amounts.
     *
     * @param first  the first amount
     * @param second the second amount
     * @return the larger amount
     * @throws IllegalArgumentException if the currencies differ
     */
    public static Money max(final Money first, final Money second) {
        return first.compareTo(second) >= 0 ? first : second;
    }

    /**
     * Splits this amount into equal parts, distributing any remainder of minor
     * units among the earlier parts so that the parts always sum to this
     * amount exactly. The granularity is the currency's minor units; for
     * codes without minor units it is the amount's own precision, never
     * coarser than whole units.
     *
     * <pre>{@code
     * Money eur100 = new Money(new BigDecimal("100.00"), eur);
     * eur100.allocate(3)   // [33.34 EUR, 33.33 EUR, 33.33 EUR]
     * eur100.allocate(1)   // [100.00 EUR]
     * }</pre>
     *
     * @param parts the number of parts; must be positive
     * @return an unmodifiable list of exactly {@code parts} amounts
     * @throws IllegalArgumentException if {@code parts} is not positive
     */
    public List<Money> allocate(final int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("parts must be positive: " + parts);
        }
        final BigInteger[] quotientRemainder =
                minorUnitAmount().divideAndRemainder(BigInteger.valueOf(parts));
        BigInteger base = quotientRemainder[0];
        BigInteger remainder = quotientRemainder[1];
        if (remainder.signum() < 0) {
            // BigInteger truncates towards zero; normalize so that the
            // remainder is non-negative and the base is floored.
            base = base.subtract(ONE);
            remainder = remainder.add(BigInteger.valueOf(parts));
        }
        final List<Money> allocation = new ArrayList<>(parts);
        for (int index = 0; index < parts; index++) {
            allocation.add(withMinorUnitAmount(
                    index < remainder.intValueExact() ? base.add(ONE) : base));
        }
        return List.copyOf(allocation);
    }

    /**
     * Splits this amount proportionally to the given weights, distributing any
     * remainder of minor units by the largest-remainder rule (Hamilton's
     * method): every part first receives its floored proportional share, and
     * the leftover minor units go to the parts with the largest fractional
     * remainders, earlier parts winning ties. The parts always sum to this
     * amount exactly.
     *
     * <pre>{@code
     * Money eur10 = new Money(new BigDecimal("10.00"), eur);
     * eur10.allocate(1, 1, 1)   // [3.34 EUR, 3.33 EUR, 3.33 EUR]
     * eur10.allocate(2, 1, 1)   // [5.00 EUR, 2.50 EUR, 2.50 EUR]
     * eur10.allocate(0, 1)      // [0.00 EUR, 10.00 EUR]
     * }</pre>
     *
     * @param weights the relative weights; must be non-empty, non-negative,
     *                and contain at least one positive value
     * @return an unmodifiable list with one amount per weight, in weight order
     * @throws IllegalArgumentException if the weights are empty, negative, or
     *                                  all zero
     */
    public List<Money> allocate(final long... weights) {
        Objects.requireNonNull(weights, "weights");
        if (weights.length == 0) {
            throw new IllegalArgumentException("weights must not be empty");
        }
        BigInteger totalWeight = BigInteger.ZERO;
        for (final long weight : weights) {
            if (weight < 0) {
                throw new IllegalArgumentException("weights must be non-negative: " + weight);
            }
            totalWeight = totalWeight.add(BigInteger.valueOf(weight));
        }
        if (totalWeight.signum() == 0) {
            throw new IllegalArgumentException("at least one weight must be positive");
        }

        final BigInteger total = minorUnitAmount();
        final List<BigInteger> shares = new ArrayList<>(weights.length);
        final List<BigInteger> remainders = new ArrayList<>(weights.length);
        BigInteger allocated = BigInteger.ZERO;
        for (final long weight : weights) {
            final BigInteger[] quotientRemainder =
                    total.multiply(BigInteger.valueOf(weight)).divideAndRemainder(totalWeight);
            BigInteger share = quotientRemainder[0];
            BigInteger remainder = quotientRemainder[1];
            if (remainder.signum() < 0) {
                // Negative totals truncate towards zero; normalize the
                // remainder to the non-negative range as above.
                share = share.subtract(ONE);
                remainder = remainder.add(totalWeight);
            }
            shares.add(share);
            remainders.add(remainder);
            allocated = allocated.add(share);
        }

        final BigInteger leftover = total.subtract(allocated);
        final List<Integer> byLargestRemainder = new ArrayList<>(weights.length);
        for (int index = 0; index < weights.length; index++) {
            byLargestRemainder.add(index);
        }
        byLargestRemainder.sort((left, right) -> {
            final int comparison = remainders.get(right).compareTo(remainders.get(left));
            return comparison != 0 ? comparison : Integer.compare(left, right);
        });
        final int leftToDistribute = leftover.intValueExact();
        for (int position = 0; position < leftToDistribute; position++) {
            final int index = byLargestRemainder.get(position);
            shares.set(index, shares.get(index).add(ONE));
        }

        final List<Money> allocation = new ArrayList<>(weights.length);
        for (final BigInteger share : shares) {
            allocation.add(withMinorUnitAmount(share));
        }
        return List.copyOf(allocation);
    }

    /**
     * Renders this amount in the canonical, locale-independent form
     * {@code <amount> <code>}, such as {@code 12.34 EUR}, with the amount
     * written without an exponent.
     *
     * <pre>{@code
     * new Money(new BigDecimal("12.34"), eur).format()   // "12.34 EUR"
     * }</pre>
     *
     * @return the canonical textual form of this amount
     */
    public String format() {
        return amount.toPlainString() + ' ' + currency.code();
    }

    /**
     * Parses text in the canonical form produced by {@link #format()}, that is
     * a plain decimal amount without exponent, optionally followed by the
     * currency code, such as {@code 12.34} or {@code 12.34 EUR} (the code is
     * case-insensitive). The text MUST NOT use exponent notation; a present
     * code must match the expected currency. The parsed amount is rounded to
     * the currency's minor units exactly as in the canonical constructor.
     *
     * <pre>{@code
     * Money.parse("12.34", eur)         // Either.right: 12.34 EUR
     * Money.parse(" 12.34 eur ", eur)   // Either.right: 12.34 EUR
     * Money.parse("12.34 USD", eur)     // Either.left:  INVALID_FORMAT
     * Money.parse("1.5e2", eur)         // Either.left:  INVALID_FORMAT
     * }</pre>
     *
     * @param text     the text to parse
     * @param currency the expected currency
     * @return either the parsed money, or a {@link ParameterError} describing
     *         why the text is not a valid amount of that currency
     */
    public static Either<ParameterError, Money> parse(final String text, final Currency currency) {
        Objects.requireNonNull(currency, "currency");
        if (text == null) {
            return Either.left(ParameterError.nullParameter("text"));
        }
        final String trimmed = text.strip();
        if (trimmed.isEmpty()) {
            return Either.left(ParameterError.empty("text"));
        }

        String value = trimmed;
        final int separator = trimmed.indexOf(' ');
        if (separator >= 0) {
            value = trimmed.substring(0, separator).strip();
            final String code = trimmed.substring(separator + 1).strip()
                    .toUpperCase(Locale.ROOT);
            if (!currency.code().equals(code)) {
                return Either.left(ParameterError.invalidFormat("text", text));
            }
        }
        if (!CANONICAL_AMOUNT.matcher(value).matches()) {
            return Either.left(ParameterError.invalidFormat("text", text));
        }
        try {
            return Either.right(new Money(new BigDecimal(value), currency));
        } catch (final NumberFormatException exception) {
            return Either.left(ParameterError.invalidFormat("text", text));
        }
    }

    /**
     * Returns {@link #format()}; money prints as {@code 12.34 EUR}.
     *
     * @return the canonical textual form of this amount
     */
    @Override
    public String toString() {
        return format();
    }

    private boolean isMismatch(final Money other) {
        return !currency.code().equals(other.currency.code());
    }

    private BusinessRuleError mismatch(final Money other) {
        return BusinessRuleError.currencyMismatch(currency.code(), other.currency.code());
    }

    private static BigDecimal normalize(final BigDecimal amount, final Currency currency) {
        return currency.minorUnits().isPresent()
                ? amount.setScale(currency.minorUnits().getAsInt(), DEFAULT_ROUNDING)
                : (amount.signum() == 0 ? BigDecimal.ZERO : amount.stripTrailingZeros());
    }

    /**
     * The allocation granularity: the currency's minor units, or for codes
     * without minor units the amount's own scale, never coarser than whole
     * units. The floor at zero avoids granularity drift, where
     * {@code 100} would otherwise allocate at granularity 100 (its
     * trailing-zero-stripped scale is -2) while {@code 100.5} allocated at
     * granularity 0.5.
     */
    private int scale() {
        return currency.minorUnits().isPresent()
                ? currency.minorUnits().getAsInt()
                : Math.max(amount.scale(), 0);
    }

    /**
     * The amount counted at the {@link #scale() allocation granularity};
     * exact because that scale never exceeds the amount's own scale.
     */
    private BigInteger minorUnitAmount() {
        return amount.movePointRight(scale()).toBigIntegerExact();
    }

    private Money withMinorUnitAmount(final BigInteger minorUnitAmount) {
        return new Money(new BigDecimal(minorUnitAmount, scale()), currency);
    }
}
