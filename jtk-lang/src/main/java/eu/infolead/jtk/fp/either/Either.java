package eu.infolead.jtk.fp.either;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
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

    /**
     * <p>
     *     In Railway-Oriented Programming, this method transforms a "track switch" function (a switch is something that
     *     has a one-track input and returns a two-track output) into a two-track input function that returns a
     *     two-track output.
     * </p>
     * <p>
     *     This is of course very different from a combination of validation functions, in which usually one wants to
     *     get the full list of anomalies: this only returns the first anomaly and short-circuits all following steps.
     * </p>
     * @param mapper
     * @return
     * @param <G>
     * @param <D>
     * @param <E>
     */
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

    // ====================== NEW RAILWAY ORIENTED PROGRAMMING OPERATIONS ======================

    /**
     * <h3>Railway Metaphor: Bypass Function for Both Tracks</h3>
     * Transform both success track (right) and error track (left) values simultaneously.
     * This is like installing signal processors that work on both the main railway line
     * and the emergency siding - the train stays on whichever track it was already on,
     * but the cargo gets transformed as it passes through.
     * 
     * <p>In Railway-Oriented Programming, this is a "bypass function" that operates
     * on both tracks without changing which track the train is on.</p>
     * 
     * @param <L1> the new error track cargo type
     * @param <R1> the new success track cargo type
     * @param leftMapper transformer for error track cargo
     * @param rightMapper transformer for success track cargo
     * @return Either with transformed cargo, staying on the same track
     * 
     * @see <a href="https://fsharpforfunandprofit.com/rop/">Railway Oriented Programming</a>
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <L1, R1> Either<L1, R1> bimap(final Fn.FN1<L1, ? super L> leftMapper, final Fn.FN1<R1, ? super R> rightMapper) {
        return fold(
            l -> (Either<L1, R1>) Either.left(leftMapper.apply(l)),
            r -> (Either<L1, R1>) Either.right(rightMapper.apply(r))
        );
    }

    /**
     * <h3>Railway Metaphor: Track Junction with Function Train</h3>
     * Apply a function carried by one train to data carried by another train.
     * This is like having two trains meet at a railway junction - one carrying
     * processing equipment (function) and another carrying raw materials (data).
     * 
     * <p>Both trains must be on the success track for the processing to happen.
     * If either train is derailed (on error track), the junction fails and
     * the error gets passed along.</p>
     * 
     * @param <R1> the type of processed cargo after applying the function
     * @param functionEither a train carrying processing function
     * @return Either containing processed cargo, or the first error encountered
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <R1> Either<L, R1> apply(final Either<L, Mapper<R1, ? super R>> functionEither) {
        return functionEither.fold(
            l -> (Either<L, R1>) Either.left(l),
            f -> this.map(f)
        );
    }

    /**
     * <h3>Railway Metaphor: Two-Train Cargo Merger</h3>
     * Combine cargo from two trains at a railway junction using a merger function.
     * Both trains must arrive successfully on the success track for the cargo 
     * combination to happen. If either train is derailed, the merger operation
     * fails and the error is propagated.
     * 
     * <p>This is like having two freight trains meet at a junction where their
     * cargo gets combined into a new shipment that continues on the success track.</p>
     * 
     * @param <R1> the type of cargo from the second train
     * @param <R2> the type of combined cargo
     * @param other the second train carrying cargo to combine
     * @param combiner function to merge cargo from both trains
     * @return Either containing the merged cargo, or the first error encountered
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <R1, R2> Either<L, R2> map2(final Either<L, R1> other, final Fn.FN2<R2, R, R1> combiner) {
        return this.fold(
            l -> (Either<L, R2>) Either.left(l),
            r -> other.fold(
                l -> (Either<L, R2>) Either.left(l),
                r1 -> (Either<L, R2>) Either.right(combiner.apply(r, r1))
            )
        );
    }

    /**
     * Combine three Either values using a ternary function.
     * All three Either instances must be Right for the function to be applied.
     * 
     * @param <R1> the type of the second Either's right value
     * @param <R2> the type of the third Either's right value
     * @param <R3> the result type
     * @param second the second Either to combine with
     * @param third the third Either to combine with
     * @param combiner function to combine the three right values
     * @return Either containing the combined result, or the first error encountered
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <R1, R2, R3> Either<L, R3> map3(final Either<L, R1> second, final Either<L, R2> third, 
                                            final Fn.FN3<R3, R, R1, R2> combiner) {
        return this.fold(
            l -> (Either<L, R3>) Either.left(l),
            r -> second.fold(
                l -> (Either<L, R3>) Either.left(l),
                r1 -> third.fold(
                    l -> (Either<L, R3>) Either.left(l),
                    r2 -> (Either<L, R3>) Either.right(combiner.apply(r, r1, r2))
                )
            )
        );
    }

    /**
     * <h3>Railway Metaphor: Railway Switch with Inspection</h3>
     * Inspect cargo at a railway switch point. If the cargo passes inspection,
     * the train continues on the success track. If inspection fails, the train
     * gets switched to the error track.
     * 
     * <p>This is like having an automated inspection system at a railway junction
     * that can divert trains to a maintenance siding if their cargo doesn't meet
     * quality standards. Trains already on the error track bypass the inspection.</p>
     * 
     * @param predicate the inspection criteria that cargo must satisfy
     * @param errorSupplier provides the error message if inspection fails
     * @return Either staying on current track, or switched to error track if inspection fails
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default Either<L, R> filter(final Predicate<R> predicate, final Supplier<L> errorSupplier) {
        return fold(
            l -> (Either<L, R>) this,
            r -> predicate.test(r) ? (Either<L, R>) this : Either.left(errorSupplier.get())
        );
    }

    /**
     * <h3>Railway Metaphor: Safety Check Alias</h3>
     * Alias for filter. Ensures that cargo meets safety standards at a checkpoint.
     * Same as filter() but with clearer intent for safety/validation scenarios.
     * 
     * @param predicate the safety standards that cargo must satisfy
     * @param errorSupplier provides the error message if safety check fails
     * @return Either staying on track, or switched to error track if safety violated
     */
    default Either<L, R> ensure(final Predicate<R> predicate, final Supplier<L> errorSupplier) {
        return filter(predicate, errorSupplier);
    }

    /**
     * Combine this Either with another into a tuple.
     * Both must be Right for the combination to succeed.
     * 
     * @param <R1> the type of the other Either's right value
     * @param other the Either to zip with
     * @return Either containing a Tuple2 of both right values, or the first error
     */
    default <R1> Either<L, Tuple2<R, R1>> zip(final Either<L, R1> other) {
        return map2(other, Tuple2::new);
    }

    /**
     * Combine this Either with another using a function.
     * 
     * @param <R1> the type of the other Either's right value
     * @param <R2> the result type
     * @param other the Either to zip with
     * @param combiner function to combine both right values
     * @return Either containing the combined result, or the first error
     */
    default <R1, R2> Either<L, R2> zipWith(final Either<L, R1> other, final Fn.FN2<R2, R, R1> combiner) {
        return map2(other, combiner);
    }

    /**
     * <h3>Railway Metaphor: Emergency Recovery System</h3>
     * Attempt to recover from a derailment using an emergency recovery procedure.
     * If the train is on the error track (derailed), try to get it back on track
     * using the recovery function. If the train is already on the success track,
     * no recovery is needed.
     * 
     * <p>This is like having an emergency response team that can potentially
     * repair a derailed train and get it back on the main line, or decide that
     * the damage is too severe and issue a different error report.</p>
     * 
     * @param recovery emergency procedure to attempt getting back on track
     * @return Either with recovered train or this Either if already on success track
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default Either<L, R> recover(final Mapper<Either<L, R>, ? super L> recovery) {
        return fold(recovery, r -> (Either<L, R>) this);
    }

    /**
     * Merge an Either where both sides have the same type.
     * 
     * @param <T> the common type of both sides
     * @return the value from either side
     * @throws UnsupportedOperationException if L and R are not the same type
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <T> T merge() {
        // This is a compile-time constraint - L and R must be the same type
        return fold(l -> (T) l, r -> (T) r);
    }

    // ====================== STATIC UTILITY CONSTRUCTORS ======================

    /**
     * Convert an Optional to Either.
     * 
     * @param <L> the error type
     * @param <R> the success type
     * @param errorSupplier provides the error value if Optional is empty
     * @param optional the Optional to convert
     * @return Either.right if Optional has value, Either.left otherwise
     */
    static <L, R> Either<L, R> fromOptional(final Supplier<L> errorSupplier, final Optional<R> optional) {
        return optional.map(Either::<L, R>right).orElseGet(() -> Either.left(errorSupplier.get()));
    }

    /**
     * Convert a boolean condition to Either.
     * 
     * @param <L> the error type
     * @param <R> the success type
     * @param condition the boolean condition
     * @param errorSupplier provides the error value if condition is false
     * @param successSupplier provides the success value if condition is true
     * @return Either.right if condition is true, Either.left otherwise
     */
    static <L, R> Either<L, R> fromBoolean(final boolean condition, 
                                           final Supplier<L> errorSupplier,
                                           final Supplier<R> successSupplier) {
        return condition ? Either.right(successSupplier.get()) : Either.left(errorSupplier.get());
    }

    /**
     * Execute a computation that might throw an exception, converting to Either.
     * 
     * @param <L> the error type
     * @param <R> the success type
     * @param errorMapper function to convert exceptions to error values
     * @param computation the computation that might throw
     * @return Either.right with result, or Either.left with mapped exception
     */
    static <L, R> Either<L, R> fromTry(final Fn.FN1<L, Exception> errorMapper, final Supplier<R> computation) {
        try {
            return Either.right(computation.get());
        } catch (Exception e) {
            return Either.left(errorMapper.apply(e));
        }
    }

    /**
     * Lift a regular function to work with Either values.
     * 
     * @param <L> the error type
     * @param <R> the input type
     * @param <R1> the output type
     * @param mapper the mapper to lift
     * @return a function that works on Either values
     */
    static <L, R, R1> Fn.FN1<Either<L, R1>, Either<L, R>> lift(final Mapper<R1, ? super R> mapper) {
        return either -> either.map(mapper);
    }

    /**
     * Lift a binary function to work with Either values.
     * 
     * @param <L> the error type
     * @param <R1> the first input type
     * @param <R2> the second input type
     * @param <R3> the output type
     * @param function the binary function to lift
     * @return a function that works on two Either values
     */
    static <L, R1, R2, R3> Fn.FN2<Either<L, R3>, Either<L, R1>, Either<L, R2>> lift2(
            final Fn.FN2<R3, R1, R2> function) {
        return (either1, either2) -> either1.map2(either2, function);
    }

    // ====================== COLLECTION OPERATIONS ======================

    /**
     * Transform a collection of values using a function that returns Either,
     * collecting all results. Stops at first error.
     * 
     * @param <L> the error type
     * @param <T> the input element type
     * @param <R> the output element type
     * @param collection the collection to transform
     * @param mapper function that transforms elements and might fail
     * @return Either containing list of all results, or first error
     */
    static <L, T, R> Either<L, List<R>> traverse(final Collection<T> collection, 
                                                 final Mapper<Either<L, R>, ? super T> mapper) {
        List<R> results = new ArrayList<>();
        for (T element : collection) {
            Either<L, R> result = mapper.map(element);
            if (result.isLeft().toBoolean()) {
                return Either.left(result.fold(Fn::identity, r -> null));
            }
            results.add(result.fold(l -> null, Fn::identity));
        }
        return Either.right(results);
    }

    /**
     * Convert a collection of Either values to an Either of collection.
     * Stops at first error.
     * 
     * @param <L> the error type
     * @param <R> the success type
     * @param eithers collection of Either values
     * @return Either containing list of all right values, or first left value
     */
    static <L, R> Either<L, List<R>> sequence(final Collection<Either<L, R>> eithers) {
        return traverse(eithers, Fn::identity);
    }

    /**
     * Partition a collection of Either values into successes and failures.
     * 
     * @param <L> the error type
     * @param <R> the success type
     * @param eithers collection of Either values
     * @return Tuple2 containing (list of errors, list of successes)
     */
    static <L, R> Tuple2<List<L>, List<R>> partition(final Collection<Either<L, R>> eithers) {
        List<L> lefts = new ArrayList<>();
        List<R> rights = new ArrayList<>();
        
        for (Either<L, R> either : eithers) {
            either.fold(
                l -> { lefts.add(l); return null; },
                r -> { rights.add(r); return null; }
            );
        }
        
        return new Tuple2<>(lefts, rights);
    }

    // ====================== HELPER INTERFACES ======================

    /**
     * Simple tuple class for holding two values.
     */
    record Tuple2<T, U>(T first, U second) {}
}
