package eu.infolead.jtk.currency;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * An ISO 4217 currency definition.
 *
 * @param code        the three-letter alphabetic code, such as {@code EUR}
 * @param numericCode the zero-padded three-digit numeric code, such as
 *                    {@code 978}
 * @param name        the currency name
 * @param minorUnits  the number of minor units, or empty when the ISO list
 *                    reports {@code N.A.}
 * @param fund        whether the ISO list marks this code as a fund
 */
public record Currency(
        String code,
        String numericCode,
        String name,
        OptionalInt minorUnits,
        boolean fund) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern ALPHABETIC_CODE = Pattern.compile("[A-Z]{3}");
    private static final Pattern NUMERIC_CODE = Pattern.compile("[0-9]{3}");

    private static final Set<String> TEST_CODES = Set.of("XTS", "XXX");
    private static final Set<String> METALS = Set.of("XAU", "XPD", "XPT", "XAG");
    private static final Set<String> UNITS_OF_ACCOUNT = Set.of(
            "XDR", "XUA", "XSU", "XBA", "XBB", "XBC", "XBD");

    /**
     * Canonical constructor with validation and normalization.
     */
    public Currency {
        code = Objects.requireNonNull(code, "code").strip().toUpperCase(Locale.ROOT);
        if (!ALPHABETIC_CODE.matcher(code).matches()) {
            throw new IllegalArgumentException("Invalid ISO 4217 alphabetic code: '" + code + "'");
        }
        numericCode = Objects.requireNonNull(numericCode, "numericCode").strip();
        if (!NUMERIC_CODE.matcher(numericCode).matches()) {
            throw new IllegalArgumentException(
                    "Invalid ISO 4217 numeric code for " + code + ": '" + numericCode + "'");
        }
        name = Objects.requireNonNull(name, "name").replace('\u00A0', ' ').strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Currency name cannot be blank for " + code);
        }
        minorUnits = Objects.requireNonNull(minorUnits, "minorUnits");
        if (minorUnits.isPresent() && (minorUnits.getAsInt() < 0 || minorUnits.getAsInt() > 4)) {
            throw new IllegalArgumentException(
                    "Invalid number of minor units for " + code + ": " + minorUnits.getAsInt());
        }
    }

    /**
     * Indicates whether this code is reserved for testing or for transactions
     * without a currency.
     *
     * @return true for {@code XTS} and {@code XXX}
     */
    public boolean isTestCode() {
        return TEST_CODES.contains(code);
    }

    /**
     * Indicates whether this code denotes a precious metal.
     *
     * @return true for gold, palladium, platinum, and silver
     */
    public boolean isMetal() {
        return METALS.contains(code);
    }

    /**
     * Indicates whether this code is a unit of account rather than a
     * transactable currency.
     *
     * @return true for SDR, the European composite units, and the bond-market
     *         units
     */
    public boolean isUnitOfAccount() {
        return UNITS_OF_ACCOUNT.contains(code);
    }

    /**
     * Indicates whether this currency can be offered to end users as a payment
     * currency. Test codes, funds, metals, and units of account are excluded.
     *
     * @return true when this is a spendable currency
     */
    public boolean isSpendable() {
        return !fund && minorUnits.isPresent() && !isTestCode() && !isMetal() && !isUnitOfAccount();
    }

    /**
     * Returns a serializable proxy in place of this record. Needed because
     * {@link OptionalInt} is not serializable; record serialization honors
     * {@code writeReplace} even though it ignores {@code readObject} and
     * {@code writeObject}.
     *
     * <p>Maintenance note: {@link SerializationProxy} must carry every record
     * component. A component added to this record but omitted from the proxy
     * compiles and then silently deserializes as {@code null}. Deserialization
     * cannot bypass validation even so: the proxy rebuilds through the
     * canonical constructor, so no separate {@code readObject} guard is
     * required.</p>
     *
     * @return the serialization proxy
     */
    @Serial
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    /** Serializable stand-in carrying the currency definition; keep its
     * fields in sync with the {@code Currency} record components. */
    private static final class SerializationProxy implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private final String code;
        private final String numericCode;
        private final String name;
        private final Integer minorUnits;
        private final boolean fund;

        SerializationProxy(final Currency target) {
            this.code = target.code;
            this.numericCode = target.numericCode;
            this.name = target.name;
            this.minorUnits = target.minorUnits.isPresent()
                    ? target.minorUnits.getAsInt()
                    : null;
            this.fund = target.fund;
        }

        @Serial
        private Object readResolve() {
            return new Currency(code, numericCode, name,
                    minorUnits == null ? OptionalInt.empty() : OptionalInt.of(minorUnits),
                    fund);
        }
    }
}
