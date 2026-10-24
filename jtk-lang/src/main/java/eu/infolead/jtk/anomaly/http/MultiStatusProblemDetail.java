package eu.infolead.jtk.anomaly.http;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A {@link ProblemDetail} carrying the responses of an RFC 4918
 * {@code 207 Multi-Status} response.
 */
public non-sealed interface MultiStatusProblemDetail extends ProblemDetail {
    /**
     * Returns the individual resource responses.
     *
     * @return the responses, never {@code null}
     */
    @JsonIgnore
    List<MultiStatusResponseElement> getResponses();

    /**
     * Returns the RFC 4918 {@code multistatus} wrapper for serialization.
     *
     * @return the wrapper holding the responses
     */
    @JsonProperty("multistatus")
    default MultiStatus getMultiStatus() {
        return new MultiStatus(Objects.requireNonNull(getResponses(), "getResponses returned null"));
    }

    /**
     * The RFC 4918 {@code multistatus} object.
     *
     * @param response the per-resource responses
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record MultiStatus(@JsonProperty("response") List<MultiStatusResponseElement> response) {
        /**
         * Canonical constructor.
         *
         * @param response the per-resource responses; {@code null} becomes an
         *                 empty list
         */
        public MultiStatus {
            response = response == null ? List.of() : List.copyOf(response);
        }
    }
}
