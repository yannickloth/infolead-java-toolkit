package eu.infolead.jtk.anomaly.http;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.MultiStatusResponseElement;

public non-sealed interface MultiStatusProblemDetail extends ProblemDetail {
    @JsonProperty("multistatus")
    List<MultiStatusResponseElement> getResponses();
}
