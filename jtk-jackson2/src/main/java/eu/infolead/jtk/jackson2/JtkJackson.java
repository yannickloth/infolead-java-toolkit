package eu.infolead.jtk.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory for Jackson 2 mappers that know the Infolead wire contract.
 *
 * @deprecated use {@code eu.infolead.jtk.jackson3.JtkJackson}.
 */
@Deprecated(since = "0.1.0")
public final class JtkJackson {

    private JtkJackson() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns a mapper with the Infolead bridge module registered.
     *
     * @return a preconfigured mapper
     */
    public static ObjectMapper mapper() {
        return new ObjectMapper().registerModule(new JtkJacksonModule());
    }
}
