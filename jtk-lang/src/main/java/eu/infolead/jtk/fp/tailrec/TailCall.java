package eu.infolead.jtk.fp.tailrec;

import eu.infolead.jtk.fp.Fn;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.logic.Bool;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utility interface that allows the implementation of tail recursive calls in Java.
 *
 * <p>This is necessary because Java does not directly support tail call optimisation (TCO) at
 * compiler level.
 *
 * @see Factorial
 * @param <T>
 */
@FunctionalInterface
public interface TailCall<T> {
  /**
   * This actually chains the next TailCall to this TailCall, without executing it.
   *
   * @param nextCall
   * @return
   * @param <T>
   */
  static <T> TailCall<T> call(final TailCall<T> nextCall) {
    return nextCall;
  }

  static <T> TailCall<T> done(final T value) {
    return new TailCall<>() {

      @Override
      public TailCall<T> apply() {
        throw new UnsupportedOperationException("not implemented");
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
   * The generic algorithms that implements tail recursion.
   *
   * @param accumulator The seed/accumulator. With each recursion, this is updated.
   * @param currentValue The current value for which the computation is done. With each recursion,
   *     this should be changed to get closer to the base case.
   * @param baseCasePredicate Checks if the currentValue is equal to the base case for the
   *     algorithm.
   * @param nextAccumulator A BiFunction that takes the accumulator and the currentValue to compute
   *     the next value of the accumulator.
   * @param nextValue A function that takes the current value and computes the one for the next
   *     recursion.
   * @return Returns a "call" TailCall or a "done" TailCall instance.
   * @param <T>
   */
  static <T> TailCall<T> tailCallOptimisationRecursion(
      final T accumulator,
      final T currentValue,
      final Predicate<T> baseCasePredicate,
      final Fn.FN2<T, T, T> nextAccumulator,
      final Fn.FN2<T, T, T> nextValue) {
    // System.out.println(accumulator+" "+currentValue);
    return Bool.of(baseCasePredicate.test(
            currentValue)).fold(() -> {
              // This is the lazy recursive call (it's not executed eagerly, as it's wrapped into a lambda
              // expression for lazy/later execution).
              // The trick to make the algorithm tail recursive (and benefit of tail call optimisation) is
              // that the result of the recursive call is
              // not used in this function: the accumulator is immediately computed and provided as a
              // parameter to the recursive call. This allows the compiler to remove the current function
              // from the stack (as there is no more computation to do) before entering the following one.
              return TailCall.call(
                      () ->
                              tailCallOptimisationRecursion(
                                      // compute the next accumulator value
                                      nextAccumulator.apply(accumulator, currentValue),
                                      // compute the next value
                                      nextValue.apply(accumulator,currentValue),
                                      // lambda to check if the currentValue is equal to the base case
                                      baseCasePredicate,
                                      // lambda used to compute the next accumulator value
                                      nextAccumulator,
                                      // lambda used to compute the next value
                                      nextValue));
            },
            // The recursion has reached its base case and can finally return the
            // computed factorial.
            () -> TailCall.done(accumulator));
  }

  /**
   * This is the abstract method of the functional interface. It is used as a generator of TailCall
   * instances by {@link TailCall#invoke()}.
   *
   * @return
   */
  TailCall<T> apply();

  default boolean isComplete() {
    return false;
  }

  default T result() {
    throw new UnsupportedOperationException("not implemented");
  }

  default T invoke() {
    return Maybe.of(
            Stream.iterate(
                    this,
                    TailCall::apply) // this is the moment when the successive evaluations of the
                // factorial's accumulator and next number is actually done and
                // produces and stream of TailCall instances
                .filter(
                    TailCall
                        ::isComplete) // we filter out all TailCall instances which are not the last
                // computed one
                .findFirst()) // we extract the only remaining value of the stream, which results in
        // an Optional<TailCall<T>> instance which is replaced by a Maybe
        .fold(() -> null, TailCall::result) // we unwrap the TailCall instance from the optional
    ; // and we return the value encapsulated inside the last TailCall
  }
}
