package eu.infolead.jtk.jackson2;

import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.infolead.jtk.anomaly.http.ProblemDetail;

/**
 * Jackson 2 bridge module for the Infolead types.
 *
 * <p>Carries only the mechanics that Jackson annotations cannot express; the wire contract itself
 * is declared on the types in {@code jtk-lang}.
 *
 * @deprecated use {@code eu.infolead.jtk.jackson3.JtkJacksonModule}.
 */
@Deprecated(since = "0.1.0")
public final class JtkJacksonModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    /** Stable module name, used by Jackson's duplicate-registration guard. */
    public static final String MODULE_NAME = "jtk-jackson2";

    /** Creates the module and registers the Infolead deserializers. */
    public JtkJacksonModule() {
        super(MODULE_NAME);
        addDeserializer(ProblemDetail.class, new ProblemDetailDeserializer());
    }
}
