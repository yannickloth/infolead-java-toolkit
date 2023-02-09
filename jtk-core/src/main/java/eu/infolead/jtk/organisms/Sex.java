package eu.infolead.jtk.organisms;

import eu.infolead.jtk.fp.NN;
import eu.infolead.jtk.fp.Result;

/**
 * Represents the essentially binary biological form of living organisms on the
 * basis of their
 * reproductive organs and structures.
 */
public enum Sex {
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

    public static Result<Void, Sex> of(final NN<String> gender) {
        try {
            return Result.success(gender == null ? UNSPECIFIED : valueOf(gender.get()));
        } catch (final IllegalArgumentException e) {
            return Result.failure(null/* TODO */);
        }
    }
}
