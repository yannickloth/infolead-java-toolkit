package eu.infolead.jtk.rfc;

import java.util.Objects;

/**
 * Metadata about an RFC document.
 *
 * @param number the RFC reference; must not be {@code null}
 * @param title  the RFC title; must not be blank
 */
public record Rfc(RfcReference number, String title) {

    /**
     * Canonical constructor with validation.
     */
    public Rfc {
        Objects.requireNonNull(number, "number cannot be null");
        Objects.requireNonNull(title, "title cannot be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title cannot be blank");
        }
    }
}
