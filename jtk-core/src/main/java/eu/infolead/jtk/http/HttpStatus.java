package eu.infolead.jtk.http;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import eu.infolead.jtk.fp.Maybe;
import eu.infolead.jtk.rfc.RfcReference;
import jakarta.annotation.Nonnull;

public enum HttpStatus implements HttpStatusCode {
    CONTINUE(100, "Continue", 9110, Maybe.of("15.2.1")),
    /**
     * 
     */
    SWITCHING_PROTOCOLS(101, "Switching Protocols", 9110, Maybe.of("15.2.2")),
    /**
     * 
     */
    PROCESSING(102, "Processing", 2518, Maybe.none()),
    /**
     * 
     */
    EARLY_HINTS(103, "Early Hints", 8297, Maybe.none()),
    /**
     * 
     */
    OK(200, "OK", 9110, Maybe.of("15.3.1")),
    /**
     * 
     */
    CREATED(201, "Created", 9110, Maybe.of("15.3.2")),
    /**
     * 
     */
    ACCEPTED(202, "Accepted", 9110, Maybe.of("15.3.3")),
    /**
     * 
     */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information", 9110, Maybe.of("15.3.4")),
    /**
     * 
     */
    NO_CONTENT(204, "No Content", 9110, Maybe.of("15.3.5")),
    /**
     * 
     */
    RESET_CONTENT(205, "Reset Content", 9110, Maybe.of("15.3.6")),
    /**
     * 
     */
    PARTIAL_CONTENT(206, "Partial Content", 9110, Maybe.of("15.3.7")),
    /**
     * 
     */
    MULTI_STATUS(207, "Multi-Status", 4918, Maybe.none()),
    /**
     * 
     */
    ALREADY_REPORTED(208, "Already Reported", 5842, Maybe.none()),
    /**
     * 
     */
    IM_USED(226, "IM Used", 3229, Maybe.none()),
    /**
     * 
     */
    MULTIPLE_CHOICES(300, "Multiple Choices", 9110, Maybe.of("15.4.1")),
    /**
     * 
     */
    MOVED_PERMANENTLY(301, "Moved Permanently", 9110, Maybe.of("15.4.2")),
    /**
     * 
     */
    FOUND(302, "Found", 9110, Maybe.of("15.4.3")),
    /**
     * 
     */
    SEE_OTHER(303, "See Other", 9110, Maybe.of("15.4.4")),
    /**
     * 
     */
    NOT_MODIFIED(304, "Not Modified", 9110, Maybe.of("15.4.5")),
    /**
     * 
     */
    USE_PROXY(305, "Use Proxy", 9110, Maybe.of("15.4.6")),
    /**
     * 
     */
    UNUSED(306, "(Unused)", 9110, Maybe.of("15.4.7")),
    /**
     * 
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect", 9110, Maybe.of("15.4.8")),
    /**
     * 
     */
    PERMANENT_REDIRECT(308, "Permanent Redirect", 9110, Maybe.of("15.4.9")),
    /**
     * 
     */
    BAD_REQUEST(400, "Bad Request", 9110, Maybe.of("15.5.1")),
    /**
     * 
     */
    UNAUTHORIZED(401, "Unauthorized", 9110, Maybe.of("15.5.2")),
    /**
     * 
     */
    PAYMENT_REQUIRED(402, "Payment Required", 9110, Maybe.of("15.5.3")),
    /**
     * 
     */
    FORBIDDEN(403, "Forbidden", 9110, Maybe.of("15.5.4")),
    /**
     * 
     */
    NOT_FOUND(404, "Not Found", 9110, Maybe.of("15.5.5")),
    /**
     * 
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed", 9110, Maybe.of("15.5.6")),
    /**
     * 
     */
    NOT_ACCEPTABLE(406, "Not Acceptable", 9110, Maybe.of("15.5.7")),
    /**
     * 
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required", 9110, Maybe.of("15.5.8")),
    /**
     * 
     */
    REQUEST_TIMEOUT(408, "Request Timeout", 9110, Maybe.of("15.5.9")),
    /**
     * 
     */
    CONFLICT(409, "Conflict", 9110, Maybe.of("15.5.10")),
    /**
     * 
     */
    GONE(410, "Gone", 9110, Maybe.of("15.5.11")),
    /**
     * 
     */
    LENGTH_REQUIRED(411, "Length Required", 9110, Maybe.of("15.5.12")),
    /**
     * 
     */
    PRECONDITION_FAILED(412, "Precondition Failed", 9110, Maybe.of("15.5.13")),
    /**
     * 
     */
    CONTENT_TOO_LARGE(413, "Content Too Large", 9110, Maybe.of("15.5.14")),
    /**
     * 
     */
    URI_TOO_LONG(414, "URI Too Long", 9110, Maybe.of("15.5.15")),
    /**
     * 
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type", 9110, Maybe.of("15.5.16")),
    /**
     * 
     */
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable", 9110, Maybe.of("15.5.17")),
    /**
     * 
     */
    EXPECTATION_FAILED(417, "Expectation Failed", 9110, Maybe.of("15.5.18")),
    /**
     * 
     */
    I_AM_A_TEAPOT(418, "(Unused)", 9110, Maybe.of("15.5.19")),
    /**
     * 
     */
    MISDIRECTED_REQUEST(421, "Misdirected Request", 9110, Maybe.of("15.5.20")),
    /**
     * 
     */
    UNPROCESSABLE_CONTENT(422, "Unprocessable Content", 9110, Maybe.of("15.5.21")),
    /**
     * 
     */
    LOCKED(423, "Locked", 4918, Maybe.none()),
    /**
     * 
     */
    FAILED_DEPENDENCY(424, "Failed Dependency", 4918, Maybe.none()),
    /**
     * 
     */
    TOO_EARLY(425, "Too Early", 8470, Maybe.none()),
    /**
     * 
     */
    UPGRADE_REQUIRED(426, "Upgrade Required", 9110, Maybe.of("15.5.22")),
    /**
     * 
     */
    PRECONDITION_REQUIRED(428, "Precondition Required", 6585, Maybe.none()),
    /**
     * 
     */
    TOO_MANY_REQUESTS(429, "Too Many Requests", 6585, Maybe.none()),
    /**
     * 
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large", 6585, Maybe.none()),
    /**
     * 
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons", 7725, Maybe.none()),
    /**
     * 
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error", 9110, Maybe.of("15.6.1")),
    /**
     * 
     */
    NOT_IMPLEMENTED(501, "Not Implemented", 9110, Maybe.of("15.6.2")),
    /**
     * 
     */
    BAD_GATEWAY(502, "Bad Gateway", 9110, Maybe.of("15.6.3")),
    /**
     * 
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable", 9110, Maybe.of("15.6.4")),
    /**
     * 
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout", 9110, Maybe.of("15.6.5")),
    /**
     * 
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported", 9110, Maybe.of("15.6.6")),
    /**
     * 
     */
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates", 2295, Maybe.none()),
    /**
     * 
     */
    INSUFFICIENT_STORAGE(507, "Insufficient Storage", 4918, Maybe.none()),
    /**
     * 
     */
    LOOP_DETECTED(508, "Loop Detected", 5842, Maybe.none()),
    /**
     * 
     */
    NOT_EXTENDED(510, "Not Extended (OBSOLETED)", 2774, Maybe.none()),
    /**
     * 
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required", 6585, Maybe.none());

    private final int code;
    private final RfcReference reference;
    private final String description;
    private final Maybe<String> rfcSection;
    private static final Map<Integer, HttpStatus> VALUES = initValues(); // init this once and for all to avoid
                                                                         // reallocating an array each time

    private static Map<Integer, HttpStatus> initValues() {
        final Map<Integer, HttpStatus> values = new HashMap<>();
        Arrays.stream(HttpStatus.values()).forEach(v -> values.put(v.code(), v));
        return Collections.unmodifiableMap(values);
    }

    HttpStatus(@Nonnull final int code, @Nonnull final String description, @Nonnull final int rfcNumber,
            @Nonnull final Maybe<String> rfcSection) {
        if (HttpStatusCode.isValid(code).toBoolean()) { // here we can't use fold because the instance is not
                                                        // initialized yet
            this.code = code;
            this.reference = new RfcReference(rfcNumber);
            this.description = description;
            this.rfcSection = Objects.requireNonNull(rfcSection);
        } else {
            throw new IllegalArgumentException("the specified HTTP status code is not valid.");
        }
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public RfcReference rfcReference() {
        return reference;
    }

    @Override
    public Maybe<String> rfcSection() {
        return rfcSection;
    }

    @Override
    public String description() {
        return description;
    }

    public static HttpStatusCode of(@Nonnull final int code, @Nonnull final RfcReference rfcReference,
            @Nonnull final Maybe<String> rfcSection,
            @Nonnull final String description) {
        final Maybe<HttpStatus> status = Maybe.ofNullable(VALUES.get(code));
        return status.fold(() -> new HttpStatusCode.DefaultHttpStatus(code, rfcReference, rfcSection, description),
                s -> s);
    }
}
