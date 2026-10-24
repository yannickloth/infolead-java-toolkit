package eu.infolead.jtk.jackson3;

import tools.jackson.databind.json.JsonMapper;

/**
 * Factory for mappers that know the Infolead wire contract.
 *
 * <p>Use this instead of constructing a mapper by hand when the application reads {@link
 * eu.infolead.jtk.anomaly.http.ProblemDetail} from an untrusted source; every other type in the
 * toolkit already round-trips with a plain mapper.
 */
public final class JtkJackson {

    private JtkJackson() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns a builder with the Infolead module registered.
     *
     * @return a preconfigured mapper builder
     */
    public static JsonMapper.Builder builder() {
        return JsonMapper.builder().addModule(new JtkJacksonModule());
    }

    /**
     * Returns a mapper with the Infolead module registered.
     *
     * @return a preconfigured mapper
     */
    public static JsonMapper mapper() {
        return builder().build();
    }
}
