package eu.infolead.jtk.fp.tailrec;

import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.logic.Bool;
import java.math.BigInteger;

/**
 * This class provides methods to compute the factorial of a specified number.
 *
 * <p>The highest integer whose factorial computation does not create an arithmetic overflow is 16.
 */
public class Factorial {
  /**
   * Implementation without tail call optimisation.
   *
   * @param number
   * @return
   */
  public static BigInteger factorialRec(final BigInteger number) {
    return Bool.of(number.equals(BigInteger.ONE))
        .fold(
            () -> number.multiply(factorialRec(number.subtract(BigInteger.ONE))),
            () -> BigInteger.ONE);
  }

  /**
   * Recursive factorial with tail call optimisation.
   *
   * <p>this actually executes the recursion and the computation of the values specified as
   * parameters of {@link Factorial#factorial(BigInteger, BigInteger)}.
   *
   * @param number
   * @return
   */
  public static Maybe<BigInteger> factorial(final BigInteger number) {
    return Bool.of(number.compareTo(BigInteger.ONE) < 0)
        .fold(
            () -> {
              // var tailCall = factorial(BigInteger.ONE, number);
              var tailCall =
                  TailCall.tailCallOptimisationRecursion(
                      BigInteger.ONE,
                      number,
                      (num) -> num.equals(BigInteger.ONE),
                      BigInteger::multiply,
                      (acc,n) -> n.subtract(BigInteger.ONE));
              var result = tailCall.invoke();
              return Maybe.of(result);
            },
            () -> Maybe.none());
  }

  /**
   * This method returns either a "done" TailCall, or a new TailCall
   *
   * @param accumulator
   * @param number
   * @return
   */
  private static TailCall<BigInteger> factorial(
      final BigInteger accumulator, final BigInteger number) {
    return TailCall.tailCallOptimisationRecursion(
        accumulator,
        number,
        (num) -> num.equals(BigInteger.ONE),
        BigInteger::multiply,
        (acc,n) -> n.subtract(BigInteger.ONE));
  }

  /**
   * Does not use the generic tail call optimised recursion algorithm.
   *
   * @param accumulator
   * @param number
   * @return
   */
  private static TailCall<BigInteger> factorialSpecific(
      final BigInteger accumulator, final BigInteger number) {
    return Bool.of(number.equals(BigInteger.ONE))
        .fold(
            () -> {
              // This is the lazy recursive call (it's not executed eagerly, as it's wrapped into a
              // lambda
              // expression for lazy/later execution).
              // The trick to make the algorithm tail recursive (and benefit of tail call
              // optimisation) is
              // that the result of the recursive call is
              // not used in this function: the accumulator is immediately computed and provided as
              // a
              // parameter to the recursive call. This allows the compiler to remove the current
              // function
              // from the stack (as there is no more computation to do) before entering the
              // following one.
              return TailCall.call(
                  () -> factorial(accumulator.multiply(number), number.subtract(BigInteger.ONE)));
            },
            // The recursion has reached its base case and can finally return the computed
            // factorial.
            () -> TailCall.done(accumulator));
  }
}
