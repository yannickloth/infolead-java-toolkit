package eu.infolead.jtk.fp.either;

import java.util.function.Consumer;

import eu.infolead.jtk.lang.AliasFor;

public interface BiValuedConsumable<L, R> {
    /**
     * Method which, if this is a right instance, consumes the right value to
     * produce
     * some side effect(s), and which else consumes the left value to produce some
     * side effect(s).
     * <p>
     * This is similar to the {@link Either#fold} method, but has no return value
     * and is thus used to produce side effects.
     * <p>
     * In other words, this allows to observe this instance and do something with
     * it.
     * <p>
     * Same as {@link Either#peek(Consumer, Consumer)}.
     * 
     * @param leftConsumer  the consumer applied if this is a left instance.
     * 
     * @param rightConsumer the consumer applied if this is a right instance.
     * @return this.
     */
    <E extends Either<L, R>> E apply(Consumer<? super L> leftConsumer,
            Consumer<? super R> rightConsumer);

    /**
     * Same as {@link Either#apply(Consumer, Consumer)}. Cf.
     * {@link Either#apply(Consumer, Consumer)} for additional information.
     * 
     * @param leftConsumer  the consumer applied if this is a left instance.
     * 
     * @param rightConsumer the consumer applied if this is a right instance.
     * @return this.
     * 
     */
    @AliasFor(type = BiValuedConsumable.class, method = "apply")
    default <E extends Either<L, R>> E peek(final Consumer<? super L> leftConsumer,
            final Consumer<? super R> rightConsumer) {
        return apply(leftConsumer, rightConsumer);
    }
}
