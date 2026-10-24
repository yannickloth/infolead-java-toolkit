package eu.infolead.jtk.jackson3;

import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardMultiStatusProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Reads either permitted {@link ProblemDetail} shape from the same entry point.
 *
 * <p>{@link ProblemDetail} is a sealed interface whose two permitted types differ by their payload:
 * a {@code multistatus} member selects {@link StandardMultiStatusProblemDetail}, anything else
 * selects {@link StandardProblemDetail}. Jackson cannot express that deduction with annotations on
 * a sealed interface, which is exactly the kind of mechanic this module exists for. The shape
 * itself is still defined by the annotations on the two record types.
 */
public final class ProblemDetailDeserializer extends ValueDeserializer<ProblemDetail> {

    /** The RFC 9457 extension member that distinguishes the multi-status shape. */
    static final String MULTI_STATUS_MEMBER = "multistatus";

    @Override
    public ProblemDetail deserialize(final JsonParser parser, final DeserializationContext context)
            throws JacksonException {
        final JsonNode node = context.readTree(parser);
        if (!node.isObject()) {
            return context.reportInputMismatch(
                    ProblemDetail.class,
                    "A problem detail must be a JSON object, was %s",
                    node.getNodeType());
        }
        final Class<? extends ProblemDetail> shape =
                node.has(MULTI_STATUS_MEMBER)
                        ? StandardMultiStatusProblemDetail.class
                        : StandardProblemDetail.class;
        return context.readTreeAsValue(node, shape);
    }
}
