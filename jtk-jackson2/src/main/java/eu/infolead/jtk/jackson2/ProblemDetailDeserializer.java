package eu.infolead.jtk.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardMultiStatusProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import java.io.IOException;

/**
 * Jackson 2 bridge for the two permitted {@link ProblemDetail} shapes.
 *
 * @deprecated use {@code eu.infolead.jtk.jackson3.ProblemDetailDeserializer} once the application
 *     runs Jackson 3.
 */
@Deprecated(since = "0.1.0")
public final class ProblemDetailDeserializer extends JsonDeserializer<ProblemDetail> {

    /** The RFC 9457 extension member that distinguishes the multi-status shape. */
    static final String MULTI_STATUS_MEMBER = "multistatus";

    @Override
    public ProblemDetail deserialize(final JsonParser parser, final DeserializationContext context)
            throws IOException {
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
