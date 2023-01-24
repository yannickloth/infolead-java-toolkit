package eu.infolead.jtk.organisms;

import eu.infolead.jtk.fp.NN;
import eu.infolead.jtk.fp.R;
import eu.infolead.jtk.fp.Result;

/**
 * Represents the gender of a living organism.
 */
public enum Gender {
    /**
     *
     */
    FEMALE,
    /**
     *
     */
    MALE,
    /**
     *
     */
    UNSPECIFIED;

    public static Result<Gender> of(final NN<String> gender) {
        try {
            return R.success(gender == null ? UNSPECIFIED : valueOf(gender.get()));
        } catch (final IllegalArgumentException e) {
            return R.failure();
        }
    }
}
