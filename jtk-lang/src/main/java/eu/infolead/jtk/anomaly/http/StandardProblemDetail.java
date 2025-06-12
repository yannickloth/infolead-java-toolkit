package eu.infolead.jtk.anomaly.http;

import java.net.URI;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.infolead.jtk.fp.either.Maybe;

/**
 * A standard implementation of {@link ProblemDetail} as defined by RFC 7807.
 * This record provides a simple, immutable implementation of the Problem Details format.
 * 
 * @param problemType the problem type URI (required)
 * @param problemTypeTitle the problem type title (optional)
 * @param httpStatus the HTTP status code (optional)
 * @param fullDetail the detailed explanation (optional)
 * @param problemInstanceId the instance identifier (optional)
 */
public record StandardProblemDetail(
    @JsonProperty("type") URI problemType,
    @JsonProperty("title") @JsonInclude(Include.NON_EMPTY) Maybe<String> problemTypeTitle,
    @JsonProperty("status") @JsonInclude(Include.NON_EMPTY) Maybe<Integer> httpStatus,
    @JsonProperty("detail") @JsonInclude(Include.NON_EMPTY) Maybe<String> fullDetail,
    @JsonProperty("instance") @JsonInclude(Include.NON_EMPTY) Maybe<URI> problemInstanceId
) implements ProblemDetail {

    /**
     * Canonical constructor with validation.
     */
    public StandardProblemDetail {
        Objects.requireNonNull(problemType, "problemType cannot be null");
        Objects.requireNonNull(problemTypeTitle, "problemTypeTitle cannot be null");
        Objects.requireNonNull(httpStatus, "httpStatus cannot be null");
        Objects.requireNonNull(fullDetail, "fullDetail cannot be null");
        Objects.requireNonNull(problemInstanceId, "problemInstanceId cannot be null");
    }

    /**
     * Convenience constructor for minimal problem details.
     * 
     * @param problemType the problem type URI
     */
    public StandardProblemDetail(URI problemType) {
        this(problemType, Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with type and title.
     * 
     * @param problemType the problem type URI
     * @param title the problem type title
     */
    public StandardProblemDetail(URI problemType, String title) {
        this(problemType, Maybe.of(title), Maybe.none(), Maybe.none(), Maybe.none());
    }

    /**
     * Convenience constructor with type, title, and status.
     * 
     * @param problemType the problem type URI
     * @param title the problem type title
     * @param status the HTTP status code
     */
    public StandardProblemDetail(URI problemType, String title, Integer status) {
        this(problemType, Maybe.of(title), Maybe.of(status), Maybe.none(), Maybe.none());
    }

    // Interface implementation methods
    @Override
    public URI getProblemType() {
        return problemType;
    }

    @Override
    public Maybe<String> getProblemTypeTitle() {
        return problemTypeTitle;
    }

    @Override
    public Maybe<Integer> getHttpStatus() {
        return httpStatus;
    }

    @Override
    public Maybe<String> getFullDetail() {
        return fullDetail;
    }

    @Override
    public Maybe<URI> getProblemInstanceId() {
        return problemInstanceId;
    }

    /**
     * Creates a new Builder for constructing StandardProblemDetail instances.
     * 
     * @param problemType the required problem type URI
     * @return a new Builder instance
     */
    public static Builder builder(URI problemType) {
        return new Builder(problemType);
    }

    /**
     * Factory method for the "about:blank" problem type.
     * 
     * @return a StandardProblemDetail with "about:blank" type
     */
    public static StandardProblemDetail aboutBlank() {
        return new StandardProblemDetail(URI.create("about:blank"));
    }

    /**
     * Factory method for the "about:blank" problem type with a title.
     * 
     * @param title the problem title
     * @return a StandardProblemDetail with "about:blank" type and title
     */
    public static StandardProblemDetail aboutBlank(String title) {
        return new StandardProblemDetail(URI.create("about:blank"), title);
    }

    /**
     * Factory method for the "about:blank" problem type with title and status.
     * 
     * @param title the problem title
     * @param status the HTTP status code
     * @return a StandardProblemDetail with "about:blank" type, title, and status
     */
    public static StandardProblemDetail aboutBlank(String title, Integer status) {
        return new StandardProblemDetail(URI.create("about:blank"), title, status);
    }

    /**
     * Builder pattern for constructing StandardProblemDetail instances.
     */
    public static final class Builder {
        private final URI problemType;
        private Maybe<String> problemTypeTitle = Maybe.none();
        private Maybe<Integer> httpStatus = Maybe.none();
        private Maybe<String> fullDetail = Maybe.none();
        private Maybe<URI> problemInstanceId = Maybe.none();

        private Builder(URI problemType) {
            this.problemType = Objects.requireNonNull(problemType, "problemType cannot be null");
        }

        /**
         * Sets the problem type title.
         * 
         * @param title the problem type title
         * @return this builder
         */
        public Builder title(String title) {
            this.problemTypeTitle = Maybe.ofNullable(title);
            return this;
        }

        /**
         * Sets the HTTP status code.
         * 
         * @param status the HTTP status code
         * @return this builder
         */
        public Builder status(Integer status) {
            this.httpStatus = Maybe.ofNullable(status);
            return this;
        }

        /**
         * Sets the detailed explanation.
         * 
         * @param detail the detailed explanation
         * @return this builder
         */
        public Builder detail(String detail) {
            this.fullDetail = Maybe.ofNullable(detail);
            return this;
        }

        /**
         * Sets the problem instance identifier.
         * 
         * @param instance the instance identifier URI
         * @return this builder
         */
        public Builder instance(URI instance) {
            this.problemInstanceId = Maybe.ofNullable(instance);
            return this;
        }

        /**
         * Builds the StandardProblemDetail instance.
         * 
         * @return a new StandardProblemDetail instance
         */
        public StandardProblemDetail build() {
            return new StandardProblemDetail(problemType, problemTypeTitle, httpStatus, fullDetail, problemInstanceId);
        }
    }
}