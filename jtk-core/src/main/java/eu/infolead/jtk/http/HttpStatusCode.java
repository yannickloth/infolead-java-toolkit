package eu.infolead.jtk.http;

import java.util.Objects;

import eu.infolead.jtk.fp.Bool;
import eu.infolead.jtk.fp.Maybe;
import eu.infolead.jtk.rfc.RfcReference;
import jakarta.annotation.Nonnull;

public interface HttpStatusCode {
    int code();

    /**
     * According to rfc 9110 section 15, all valid status codes are within the range
     * of 100 to 599.
     * 
     * @return
     */
    static Bool isValid(int code) {
        return Bool.of(code >= 100 && code <= 599);
    }

    default Bool isValid() {
        return isValid(code());
    }

    RfcReference rfcReference();

    Maybe<String> rfcSection();

    String description();

    default int rfcNumber() {
        return rfcReference().number();
    }

    default Bool isUnassigned(int value) {
        return Bool.of(value >= 104 && value <= 199 || value >= 209 && value <= 225
                || value >= 227 && value <= 299 || value >= 309 && value <= 399 || value == 419 || value == 420
                || value == 427 || value == 430 || value >= 432 && value <= 450 || value >= 452 && value <= 499
                || value == 509 || value >= 512 && value <= 599);
    }

    public record DefaultHttpStatus(@Nonnull int code, @Nonnull RfcReference rfcReference,
            @Nonnull Maybe<String> rfcSection, @Nonnull String description)
            implements HttpStatusCode {

        public DefaultHttpStatus(@Nonnull int code, @Nonnull RfcReference rfcReference,
                @Nonnull Maybe<String> rfcSection, @Nonnull String description) {
            this.code = Objects.requireNonNull(code);
            this.rfcReference = Objects.requireNonNull(rfcReference);
            this.rfcSection = Objects.requireNonNull(rfcSection);
            this.description = Objects.requireNonNull(description);
        }
    }
}
