package eu.infolead.jtk.fp;

import eu.infolead.jtk.lang.AliasFor;

/**
 * A function which takes one parameter and returns a result.
 * This exists just to allow more expressivity with a richer vocabulary that
 * supports more abstractions:
 * sometimes the code's intent is more clear when <em>transforming</em> a value
 * into another instead of <em>applying</em> a function on a parameter, even if
 * it concretely is the same exact thing.
 */
@FunctionalInterface
@AliasFor(type = Fn.FN1.class)
public interface Transformer<U, T> extends Fn.FN1<U, T> {

    /**
     * The same as {@link Transformer#apply(T)}, which is inherited from
     * {@link Fn.FN1#apply(T)}.
     * 
     * @param object the object to transform. MUST NOT be {@code null}.
     * @return the result of the transformation code
     */
    @AliasFor(type = Fn.FN1.class, method = "apply")
    default U transform(T object) {
        return apply(object);
    }
}
