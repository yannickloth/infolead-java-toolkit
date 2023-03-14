package eu.infolead.jtk.fp.either;

import java.util.function.Supplier;

import eu.infolead.jtk.fp.Fn;
import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.fp.Provider;
import eu.infolead.jtk.lang.AliasFor;
import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;

/**
 * Interface for an <em>Either</em> type.
 * <p>
 * An either type is a type that may contain any of two values (of two different
 * types).
 * <p>
 * By convention, any successful value is wrapped by a right instance while any
 * error value is wrapped by the left instance.
 * <p>
 * This interface has more specialised sub-interfaces: {@link Maybe},
 * {@link Validation} and {@link Result}.
 */
public sealed interface Either<L, R> extends Foldable<L, R>, BiValuedConsumable<L, R>
        permits AbstractEither, Maybe, Result, Validation {
    /**
     * Returns a new right instance with the specified value.
     * 
     * @param <G>   the type of the left value
     * @param <D>   the type of the right value
     * @param value the value contained in the right instance
     * @return a new right instance with the specified value.
     */
    static <G, D> Either<G, D> right(final D value) {
        return new Right<>(value);
    }

    /**
     * Returns a new left instance with the specified value.
     * 
     * @param <G>   the type of the left value
     * @param <D>   the type of the right value
     * @param value the value contained in the left instance
     * @return a new left instance with the specified value.
     */
    static <G, D> Either<G, D> left(final G value) {
        return new Left<>(value);
    }

    /**
     * Returns a new empty right instance.
     * 
     * @param <G> the type of the left value
     * @param <D> the type of the right value
     * @return a new empty right instance.
     */
    static <G, D> Either<G, D> right() {
        return new Right<>(null);
    }

    /**
     * Returns a new empty left instance.
     * 
     * @param <G> the type of the left value
     * @param <D> the type of the right value
     * @return a new empty left instance.
     */
    static <G, D> Either<G, D> left() {
        return new Left<>(null);
    }

    /**
     * Checks if this is a right instance.
     * 
     * @return {@link Bool#TRUE} if this is a right instance, {@link Bool#FALSE}
     *         else.
     */
    Bool isRight();

    /**
     * Checks if this is a left instance.
     * 
     * @return {@link Bool#TRUE} if this is a left instance, {@link Bool#FALSE}
     *         else.
     */
    Bool isLeft();

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <R1> Either<L, R1> map(final Mapper<R1, ? super R> mapper) {
        return fold(l -> (Either<L, R1>) Either.left(l),
                r -> (Either<L, R1>) Either.right(mapper.map(r)));
    }

    default <R1> Either<L, R1> map(final Provider<R1> valueProvider) {
        return fold(Either::left, r -> Either.right(valueProvider.get()));
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <L1> Either<L1, R> mapLeft(final Mapper<L1, ? super L> mapper) {
        return fold(l -> (Either<L1, R>) Either.left(mapper.map(l)),
                r -> (Either<L1, R>) Either.right(r));
    }

    default <L1> Either<L1, R> mapLeft(final Provider<L1> provider) {
        return fold(l -> Either.left(provider.get()), Either::right);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <G extends L, D, E extends Either<G, D>> E flatMap(final Mapper<E, ? super R> mapper) {
        return fold(l -> (E) this, mapper);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <G extends L, D, E extends Either<G, D>> E flatMap(final Supplier<E> supplier) {
        return fold(l -> (E) this, r -> supplier.get());
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <G, D, A extends Either<G, D>> A flatMapLeft(final Mapper<A, ? super L> mapper) {
        return fold(mapper, r -> (A) this);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <G, D, A extends Either<G, D>> A flatMapLeft(final Supplier<A> supplier) {
        return fold(l -> supplier.get(), r -> (A) this);
    }

    /**
     * Another name for {@link Either#flatMap(Mapper))}.
     * 
     * @param <G>
     * @param <D>
     * @param <A>
     * @param mapper
     * @return
     */
    @AliasFor(type = Either.class, method = "flatMap")
    default <G extends L, D> Either<G, D> bind(final Mapper<Either<G, D>, ? super R> mapper) {
        return flatMap(mapper);
    }

    @AliasFor(type = Either.class, method = "flatMap")
    default <G extends L, D, E extends Either<G, D>> E bind(final Supplier<E> supplier) {
        return flatMap(supplier);
    }

    @AliasFor(type = Either.class, method = "flatMap")
    default <G extends L, D> Either<G, D> andThen(final Mapper<Either<G, D>, ? super R> mapper) {
        return flatMap(mapper);
    }

    @AliasFor(type = Either.class, method = "flatMap")
    default <G extends L, D, E extends Either<G, D>> E andThen(final Supplier<E> supplier) {
        return flatMap(supplier);
    }

    @AliasFor(type = Either.class, method = "flatMap")
    default <G extends L, D, E extends Either<G, D>> E ifRightDo(final Mapper<E, ? super R> mapper) {
        return flatMap(mapper);
    }

    @AliasFor(type = Either.class, method = "flatMap")
    default <G extends L, D, E extends Either<G, D>> E ifRightDo(final Supplier<E> supplier) {
        return flatMap(supplier);
    }

    default <E extends Either<L, R>> E bindLeft(final Mapper<E, ? super L> mapper) {
        return flatMapLeft(mapper);
    }

    default <E extends Either<L, R>> E bindLeft(final Supplier<E> supplier) {
        return flatMapLeft(supplier);
    }

    default <E extends Either<L, R>> E leftAndThen(final Mapper<E, ? super L> mapper) {
        return flatMapLeft(mapper);
    }

    default <E extends Either<L, R>> E leftAndThen(final Supplier<E> supplier) {
        return flatMapLeft(supplier);
    }

    default <E extends Either<L, R>> E ifLeftDo(final Mapper<E, ? super L> mapper) {
        return flatMapLeft(mapper);
    }

    default <E extends Either<L, R>> E ifLeftDo(final Supplier<E> supplier) {
        return flatMapLeft(supplier);
    }

    default R or(final R replacement) {
        return fold(anomaly -> replacement, Fn::identity);
    }

    default R or(final Supplier<R> replacementSupplier) {
        return fold(anomaly -> replacementSupplier.get(), Fn::identity);
    }

    default R orNull() {
        return fold(Fn::toNull, Fn::identity);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <E extends Either<L, R>> E orElse(final E replacement) {
        return fold(anomaly -> replacement, successValue -> (E) this);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <E extends Either<L, R>> E orElse(final Supplier<E> supplier) {
        return fold(anomaly -> supplier.get(), successValue -> (E) this);
    }

    /**
     * Swaps the left and right values of this instance: a left becomes a right with
     * the same value, and a right becomes a left with the same value.
     * 
     * @param <E> The type of the returned {@link Either}.
     * @return a new swapped {@link Either} with the same value. Never returns
     *         {@code null}.
     */
    <E extends Either<R, L>> E swap();

    /**
     * <h3>Note</h3>
     * It is not currently clear whether this method makes sense: what would be its
     * use-case?
     * 
     * @return
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <E extends Either<L, R>> E orElseNull() {
        return fold(Fn::toNull, successValue -> (E) this);
    }
}
