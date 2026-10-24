package eu.infolead.jtk.fp.tailrec;

import java.util.Objects;
import java.util.function.Predicate;

import eu.infolead.jtk.fp.Fn;

/**
 * Utility interface that allows the implementation of tail recursive calls in
 * Java.
 *
 * <p>
 * This is necessary because Java does not directly support tail call
 * optimisation (TCO) at compiler level.
 *
 * @param <T> the result type
 * @see Factorial
 */
@FunctionalInterface
public interface TailCall<T> {
    /**
     * This actually chains the next TailCall to this TailCall, without executing
     * it.
     *
     * @param <T>      the result type
     * @param nextCall the next call
     * @return the next call
     */
    static <T> TailCall<T> call(final TailCall<T> nextCall) {
        return Objects.requireNonNull(nextCall, "nextCall");
    }

    /**
     * Creates a completed tail call holding the specified result.
     *
     * @param <T>   the result type
     * @param value the computed result
     * @return a completed tail call
     */
    static <T> TailCall<T> done(final T value) {
        return new TailCall<>() {

            @Override
            public TailCall<T> apply() {
                throw new UnsupportedOperationException("A completed TailCall cannot be advanced");
            }

            @Override
            public boolean isComplete() {
                return true;
            }

            @Override
            public T result() {
                return value;
            }
        };
    }

    /**
     * The generic algorithm that implements tail recursion.
     *
     * @param <T>               the value type
     * @param accumulator       the seed/accumulator; updated with each recursion
     * @param currentValue      the current value for which the computation is done;
     *                          each recursion must move it closer to the base case
     * @param baseCasePredicate checks whether {@code currentValue} reached the
     *                          base case
     * @param nextAccumulator   computes the next accumulator from the accumulator
     *                          and the current value
     * @param nextValue         computes the next value from the current value
     * @return a "call" TailCall or a "done" TailCall instance
     * @throws NullPointerException if the accumulator, current value, or any
     *                              functional argument is {@code null}
     */
    static <T> TailCall<T> tailCallOptimisationRecursion(
            final T accumulator,
            final T currentValue,
            final Predicate<T> baseCasePredicate,
            final Fn.FN2<T, T, T> nextAccumulator,
            final Fn.FN1<T, T> nextValue) {
        Objects.requireNonNull(accumulator, "accumulator");
        Objects.requireNonNull(currentValue, "currentValue");
        Objects.requireNonNull(baseCasePredicate, "baseCasePredicate");
        Objects.requireNonNull(nextAccumulator, "nextAccumulator");
        Objects.requireNonNull(nextValue, "nextValue");
        if (baseCasePredicate.test(currentValue)) {
            return TailCall.done(accumulator);
        }
        return TailCall.call(() -> tailCallOptimisationRecursion(
                nextAccumulator.apply(accumulator, currentValue),
                nextValue.apply(currentValue),
                baseCasePredicate,
                nextAccumulator,
                nextValue));
    }

    /**
     * This is the abstract method of the functional interface. It is used as a
     * generator of TailCall instances by {@link TailCall#invoke()}.
     *
     * @return the next tail call
     */
    TailCall<T> apply();

    /**
     * Indicates whether this call is the final one.
     *
     * @return {@code true} when the computation is complete
     */
    default boolean isComplete() {
        return false;
    }

    /**
     * Returns the computed result of a completed call.
     *
     * @return the computed result
     * @throws UnsupportedOperationException if this call is not complete
     */
    default T result() {
        throw new UnsupportedOperationException("The computation is not complete yet");
    }

    /**
     * Executes the trampoline until a completed call is reached.
     *
     * @return the computed result
     * @throws NullPointerException if a step returns {@code null}
     */
    default T invoke() {
        TailCall<T> call = this;
        while (!call.isComplete()) {
            call = Objects.requireNonNull(call.apply(), "TailCall.apply() returned null");
        }
        return call.result();
    }
}
