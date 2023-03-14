package eu.infolead.jtk.fp;

import eu.infolead.jtk.lang.AliasFor;

/**
 * A function which takes one parameter and returns a result.
 * This exists just to allow more expressivity with a richer vocabulary that
 * supports more abstractions:
 * sometimes the code's intent is more clear when <em>mapping</em> a value onto
 * another
 * instead of <em>applying</em> a function on a parameter, even if it concretely
 * is the
 * same exact thing.
 */
@FunctionalInterface
@AliasFor(type = Fn.FN1.class)
public interface Mapper<U, T> extends Fn.FN1<U, T> {
    /**
     * The same as {@link Mapper#apply(T)}, which is inherited from
     * {@link Fn.FN1#apply(T)}.
     * 
     * @param object
     * @return
     */
    @AliasFor(type = Fn.FN1.class, method = "apply")
    default U map(T object) {
        return apply(object);
    }
}
