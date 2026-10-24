package eu.infolead.jtk.lang;

/**
 * Names of {@code javac} warning categories, for use with
 * {@link SuppressWarnings @SuppressWarnings}.
 * <p>
 * Using named constants avoids typos in warning names and makes suppressions
 * searchable. Earlier releases exposed this type as an enum; the values are now
 * plain {@code String} constants, so {@code values()}/{@code valueOf()} and
 * enum collections are no longer available.
 */
public final class CompilerWarning {
    /** Suppresses warnings about raw generic types. */
    public static final String RAW_TYPES = "rawtypes";

    /** Suppresses warnings about unchecked generic operations. */
    public static final String UNCHECKED = "unchecked";

    private CompilerWarning() {
        throw new UnsupportedOperationException("Utility class");
    }
}
