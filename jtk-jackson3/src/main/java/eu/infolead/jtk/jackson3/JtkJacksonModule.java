package eu.infolead.jtk.jackson3;

import eu.infolead.jtk.anomaly.http.ProblemDetail;
import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson 3 module for the Infolead types.
 *
 * <p>The module carries only what Jackson annotations cannot express. The wire contract of the
 * functional types and of the error hierarchy is defined on the types themselves, so a plain mapper
 * already reads and writes it; registering this module adds the two-shape {@link ProblemDetail}
 * reader on top.
 *
 * <p>This artifact is deliberately framework-free. Spring Boot and Quarkus integration live in
 * their own artifacts so that this module is not released when a framework moves.
 */
public final class JtkJacksonModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    /** Stable module name, used by Jackson's duplicate-registration guard. */
    public static final String MODULE_NAME = "jtk-jackson3";

    /** Creates the module and registers the Infolead deserializers. */
    public JtkJacksonModule() {
        super(MODULE_NAME, Version.unknownVersion());
        addDeserializer(ProblemDetail.class, new ProblemDetailDeserializer());
    }
}
