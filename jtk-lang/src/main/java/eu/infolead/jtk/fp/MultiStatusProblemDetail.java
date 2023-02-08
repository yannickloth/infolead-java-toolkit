package eu.infolead.jtk.fp;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface MultiStatusProblemDetail extends ProblemDetail {
    @JsonProperty("multistatus")
    List<MultiStatusResponseElement> getResponses();
}
