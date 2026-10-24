package eu.infolead.jtk.anomaly.http;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * Standard RFC 4918 multi-status problem detail.
 * <p>
 * Serializes as a regular problem detail plus a {@code multistatus} object
 * containing a {@code response} array.
 *
 * @param problemType       the problem type URI (required)
 * @param problemTypeTitle  the problem type title (optional)
 * @param httpStatus        the HTTP status code (optional, usually 207)
 * @param fullDetail        the detailed explanation (optional)
 * @param problemInstanceId the instance identifier (optional)
 * @param responses         the per-resource responses
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StandardMultiStatusProblemDetail(
        @JsonProperty("type") URI problemType,
        @JsonIgnore Maybe<String> problemTypeTitle,
        @JsonIgnore Maybe<Integer> httpStatus,
        @JsonIgnore Maybe<String> fullDetail,
        @JsonIgnore Maybe<URI> problemInstanceId,
        @JsonIgnore List<MultiStatusResponseElement> responses) implements MultiStatusProblemDetail {

    public StandardMultiStatusProblemDetail {
        Objects.requireNonNull(problemType, "problemType cannot be null");
        Objects.requireNonNull(problemTypeTitle, "problemTypeTitle cannot be null");
        Objects.requireNonNull(httpStatus, "httpStatus cannot be null");
        Objects.requireNonNull(fullDetail, "fullDetail cannot be null");
        Objects.requireNonNull(problemInstanceId, "problemInstanceId cannot be null");
        responses = List.copyOf(Objects.requireNonNull(responses, "responses"));
        if (httpStatus.isPresent().toBoolean() && httpStatus.orNull() != 207) {
            throw new IllegalArgumentException(
                    "A multi-status problem detail must use HTTP 207, was " + httpStatus.orNull());
        }
    }

    /**
     * Creates an instance from the nullable JSON representation.
     *
     * @param problemType the problem type URI
     * @param title       the optional title
     * @param status      the optional HTTP status
     * @param detail      the optional detail
     * @param instance    the optional instance URI
     * @param multiStatus the optional {@code multistatus} wrapper
     * @return a new multi-status problem detail
     */
    @JsonCreator
    public static StandardMultiStatusProblemDetail fromJson(
            @JsonProperty("type") final URI problemType,
            @JsonProperty("title") final String title,
            @JsonProperty("status") final Integer status,
            @JsonProperty("detail") final String detail,
            @JsonProperty("instance") final URI instance,
            @JsonProperty("multistatus") final MultiStatusProblemDetail.MultiStatus multiStatus) {
        return new StandardMultiStatusProblemDetail(
                problemType == null ? URI.create("about:blank") : problemType,
                Maybe.ofNullable(title),
                Maybe.ofNullable(status),
                Maybe.ofNullable(detail),
                Maybe.ofNullable(instance),
                multiStatus == null ? List.of() : multiStatus.response());
    }

    @Override
    public URI getProblemType() {
        return problemType;
    }

    @Override
    @JsonIgnore
    public Maybe<String> getProblemTypeTitle() {
        return problemTypeTitle;
    }

    @Override
    @JsonIgnore
    public Maybe<Integer> getHttpStatus() {
        return httpStatus;
    }

    @Override
    @JsonIgnore
    public Maybe<String> getFullDetail() {
        return fullDetail;
    }

    @Override
    @JsonIgnore
    public Maybe<URI> getProblemInstanceId() {
        return problemInstanceId;
    }

    @Override
    @JsonIgnore
    public List<MultiStatusResponseElement> getResponses() {
        return responses;
    }

}
