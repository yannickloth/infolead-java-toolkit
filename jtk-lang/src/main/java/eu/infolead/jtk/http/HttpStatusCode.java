package eu.infolead.jtk.http;

import java.util.Objects;

import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.logic.Bool;
import eu.infolead.jtk.rfc.RfcReference;
import jakarta.annotation.Nonnull;

public interface HttpStatusCode {
    int code();

    /**
     * According to RFC 9110 section 15, all valid status codes are within the range
     * of 100 to 599.
     *
     * @param code the code to validate
     * @return {@link Bool#TRUE} when the code is in the valid range
     */
    static Bool isValid(final int code) {
        return Bool.of(code >= 100 && code <= 599);
    }

    default Bool isValid() {
        return isValid(code());
    }

    RfcReference rfcReference();

    Maybe<String> rfcSection();

    String description();

    Maybe<String> additionalInformation();

    default int rfcNumber() {
        return rfcReference().number();
    }

    /**
     * Indicates whether the specified status code is currently unassigned by
     * IANA.
     * <p>
     * Codes outside the valid HTTP range are not considered unassigned: they are
     * simply invalid.
     *
     * @param value the status code to inspect
     * @return {@link Bool#TRUE} when the value is a valid but unassigned code
     */
    static Bool isUnassignedCode(final int value) {
        if (!isValid(value).toBoolean()) {
            return Bool.FALSE;
        }
        return Bool.of((value >= 104 && value <= 199) || (value >= 209 && value <= 225)
                || (value >= 227 && value <= 299) || (value >= 309 && value <= 399) || value == 419
                || value == 420 || value == 427 || value == 430 || (value >= 432 && value <= 450)
                || (value >= 452 && value <= 499) || value == 509 || (value >= 512 && value <= 599));
    }

    /**
     * Indicates whether this status code is currently unassigned by IANA.
     *
     * @return {@link Bool#TRUE} when this code is valid but unassigned
     */
    default Bool isUnassigned() {
        return isUnassignedCode(code());
    }

    /**
     * Indicates whether the specified status code is currently unassigned by
     * IANA.
     * <p>
     * Codes outside the valid HTTP range are not considered unassigned: they are
     * simply invalid.
     *
     * @param value the status code to inspect
     * @return {@link Bool#TRUE} when the value is a valid but unassigned code
     * @deprecated use {@link #isUnassignedCode(int)} instead; the receiver is
     *             ignored by this overload
     */
    @Deprecated(forRemoval = true)
    default Bool isUnassigned(final int value) {
        return isUnassignedCode(value);
    }

    record DefaultHttpStatus(@Nonnull int code, @Nonnull RfcReference rfcReference,
            @Nonnull Maybe<String> rfcSection, @Nonnull String description,
            @Nonnull Maybe<String> additionalInformation)
            implements HttpStatusCode {

        public DefaultHttpStatus {
            if (!HttpStatusCode.isValid(code).toBoolean()) {
                throw new IllegalArgumentException("Invalid HTTP status code: " + code);
            }
            Objects.requireNonNull(rfcReference, "rfcReference");
            Objects.requireNonNull(rfcSection, "rfcSection");
            Objects.requireNonNull(description, "description");
            Objects.requireNonNull(additionalInformation, "additionalInformation");
        }

        public DefaultHttpStatus(@Nonnull final int code, @Nonnull final RfcReference rfcReference,
                @Nonnull final Maybe<String> rfcSection, @Nonnull final String description,
                @Nonnull final String additionalInformation) {
            this(code, rfcReference, rfcSection, description, Maybe.ofNullable(additionalInformation));
        }

        public DefaultHttpStatus(@Nonnull final int code, @Nonnull final RfcReference rfcReference,
                @Nonnull final Maybe<String> rfcSection, @Nonnull final String description) {
            this(code, rfcReference, rfcSection, description, Maybe.none());
        }
    }
}
