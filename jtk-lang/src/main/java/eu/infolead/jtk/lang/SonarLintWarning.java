package eu.infolead.jtk.lang;

/**
 * Names of SonarLint rule identifiers, for use with
 * {@link SuppressWarnings @SuppressWarnings}.
 * <p>
 * Using named constants avoids typos and documents why a rule is suppressed.
 * Only rules that are actually suppressed in this code base are listed. Earlier
 * releases exposed this type as an enum; the values are now plain {@code String}
 * constants.
 */
public final class SonarLintWarning {
    /** Too many parameters. */
    public static final String JAVA_S107 = "java:S107";

    /** Type parameter names should comply with a naming convention. */
    public static final String JAVA_S119 = "java:S119";

    /** Unused method parameters should be removed. */
    public static final String JAVA_S1172 = "java:S1172";

    /** Type parameters should not be shadowed. */
    public static final String JAVA_S2326 = "java:S2326";

    /** Return values should not always be {@code null}. */
    public static final String JAVA_S2447 = "java:S2447";

    private SonarLintWarning() {
        throw new UnsupportedOperationException("Utility class");
    }
}
