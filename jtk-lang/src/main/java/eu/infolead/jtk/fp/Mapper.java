package eu.infolead.jtk.fp;

import eu.infolead.jtk.lang.AliasFor;

/**
 * A function which takes one parameter and returns a result.
 * This exists just to allow more expressivity with a richer vocabulary that
 * supports more abstractions:
 * sometimes the code's intent is more clear when <em>mapping</em> a value onto
 * another instead of <em>applying</em> a function on a parameter, even if it
 * concretely is the same exact thing.
 *
 * <p>
 * The generic parameters follow the convention used by {@link Fn.FN1}: the
 * result type comes first, the parameter type second.
 *
 * @param <U> the result type
 * @param <T> the parameter type
 */
@FunctionalInterface
@AliasFor(type = Fn.FN1.class)
public interface Mapper<U, T> extends Fn.FN1<U, T> {
    /**
     * The same as {@link Fn.FN1#apply(Object)}.
     *
     * @param object the value to map
     * @return the mapped value
     */
    @AliasFor(type = Fn.FN1.class, method = "apply")
    default U map(final T object) {
        return apply(object);
    }
}
