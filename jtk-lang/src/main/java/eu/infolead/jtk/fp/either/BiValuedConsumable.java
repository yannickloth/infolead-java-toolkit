package eu.infolead.jtk.fp.either;

import java.util.function.Consumer;

import eu.infolead.jtk.lang.AliasFor;

/**
 * A value with two possible cases whose contents can be consumed for their side
 * effects.
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 */
public interface BiValuedConsumable<L, R> {
    /**
     * Consumes the value carried by this instance: the left consumer is applied
     * to a left instance, and the right consumer to a right instance. A right
     * instance that carries no value (for example {@code Result.EmptySuccess})
     * may skip the right consumer instead of invoking it with {@code null}.
     * <p>
     * This is similar to {@link Foldable#fold} but has no return
     * value and is therefore intended for side effects.
     *
     * @param leftConsumer  the consumer applied if this is a left instance
     * @param rightConsumer the consumer applied if this is a right instance
     * @param <E>           the concrete either type returned for chaining
     * @return this
     */
    <E extends Either<L, R>> E apply(Consumer<? super L> leftConsumer,
            Consumer<? super R> rightConsumer);

    /**
     * Alias for {@link #apply(Consumer, Consumer)}.
     *
     * @param leftConsumer  the consumer applied if this is a left instance
     * @param rightConsumer the consumer applied if this is a right instance
     * @param <E>           the concrete either type returned for chaining
     * @return this
     */
    @AliasFor(type = BiValuedConsumable.class, method = "apply")
    default <E extends Either<L, R>> E peek(final Consumer<? super L> leftConsumer,
            final Consumer<? super R> rightConsumer) {
        return apply(leftConsumer, rightConsumer);
    }
}
