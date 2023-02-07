package eu.infolead.jtk.fp;

import java.util.function.Supplier;

/**
 * This class provides a library of useful static methods and classes for
 * functional programming.
 * 
 * <p>
 * This class is not meant to be instantiated.
 */
public final class Fn {
    private Fn() {
        throw new UnsupportedOperationException("The class Fn is not meant to be instantiated.");
    }

    public static <T> T identity(final T value) {
        return value;
    }

    @SuppressWarnings("squid:S1172")
    public static <T> void doNothing(final T value) {
        // empty method body because this method does nothing
    }

    @SuppressWarnings("squid:S1172")
    public static <U, T> U toNull(final T value) {
        return null;
    }

    public static <U> U toNull() {
        return null;
    }

    @SuppressWarnings("squid:S1172")
    public static <T> Bool toTrue(final T value) {
        return Bool.TRUE;
    }

    public static Bool toTrue() {
        return Bool.TRUE;
    }

    @SuppressWarnings("squid:S1172")
    public static <T> Bool toFalse(final T value) {
        return Bool.FALSE;
    }

    public static Bool toFalse() {
        return Bool.FALSE;
    }

    /**
     * Universal consumers of values which do nothing with input values. Useful for
     * cases when API requires function, but there is no need to do anything with
     * the received values.
     * 
     * @param <T0>
     */
    @SuppressWarnings("java:S2326")
    public static <T0> void unit() {
        // empty method body
    }

    @SuppressWarnings("java:S1172")
    public static <T1> void unit(final T1 value) {
        // empty method body
    }

    @SuppressWarnings({ "java:1172" })
    public static <T1, T2> void unit(final T1 val1, final T2 val2) {
        // empty method body
    }

    @SuppressWarnings({ "java:1172" })
    public static <T1, T2, T3> void unit(final T1 val1, final T2 val2, final T3 val3) {
        // empty method body
    }

    @SuppressWarnings({ "java:1172" })
    public static <T1, T2, T3, T4> void unit(final T1 val1, final T2 val2, final T3 val3, final T4 val4) {
        // empty method body
    }

    @SuppressWarnings({ "java:1172" })
    public static <T1, T2, T3, T4, T5> void unit(final T1 val1, final T2 val2, final T3 val3, final T4 val4,
            final T5 val5) {
        // empty method body
    }

    @SuppressWarnings({ "java:1172" })
    public static <T1, T2, T3, T4, T5, T6> void unit(final T1 val1, final T2 val2, final T3 val3, final T4 val4,
            final T5 val5, final T6 val6) {
        // empty method body
    }

    @SuppressWarnings({ "java:1172" })
    public static <T1, T2, T3, T4, T5, T6, T7> void unit(final T1 val1, final T2 val2, final T3 val3, final T4 val4,
            final T5 val5, final T6 val6, final T7 val7) {
        // empty method body
    }

    @SuppressWarnings({ "java:1172", "java:S107" })
    public static <T1, T2, T3, T4, T5, T6, T7, T8> void unit(final T1 val1, final T2 val2, final T3 val3, final T4 val4,
            final T5 val5, final T6 val6, final T7 val7, final T8 val8) {
        // empty method body
    }

    @SuppressWarnings({ "java:1172", "java:S107" })
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> void unit(final T1 val1, final T2 val2, final T3 val3,
            final T4 val4, final T5 val5, final T6 val6, final T7 val7, final T8 val8, final T9 val9) {
        // empty method body
    }

    @SuppressWarnings({ "java:S119", "java:S107" })
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, TA> void unit(final T1 val1, final T2 val2, final T3 val3,
            final T4 val4, final T5 val5, final T6 val6, final T7 val7, final T8 val8, final T9 val9, final TA valA) {
        // empty method body
    }

    @SuppressWarnings({ "java:S119", "java:S107" })
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB> void unit(final T1 val1, final T2 val2, final T3 val3,
            final T4 val4, final T5 val5, final T6 val6, final T7 val7, final T8 val8, final T9 val9, final TA val10,
            final TB val11) {
        // empty method body
    }

    /**
     * Function with no parameters (supplier). Provided for consistency.
     */
    @FunctionalInterface
    interface FN0<U> extends Supplier<U> {
        default U apply() {
            return get();
        }
    }

    /**
     * Function with one parameter and one return value.
     */
    @FunctionalInterface
    interface FN1<U, T1> {
        U apply(T1 param1);

        default <N> FN1<N, T1> then(FN1<N, U> function) {
            return v1 -> function.apply(apply(v1));
        }

        default <N> FN1<U, N> before(FN1<T1, N> function) {
            return v1 -> apply(function.apply(v1));
        }

        static <T> FN1<T, T> identity() {
            return Fn::identity;
        }
    }

    /**
     * Function with two parameters and one return value.
     */
    @FunctionalInterface
    interface FN2<U, T1, T2> {
        U apply(T1 param1, T2 param2);

        default FN1<U, T2> bind(T1 param) {
            return v2 -> apply(param, v2);
        }

        default <N> FN2<N, T1, T2> then(FN1<N, U> function) {
            return (v1, v2) -> function.apply(apply(v1, v2));
        }

    }

    /**
     * Function with three parameters and one return value.
     */
    @FunctionalInterface
    interface FN3<U, T1, T2, T3> {
        U apply(T1 param1, T2 param2, T3 param3);

        default <N> FN3<N, T1, T2, T3> then(FN1<N, U> function) {
            return (v1, v2, v3) -> function.apply(apply(v1, v2, v3));
        }
    }

    /**
     * Function with four parameters and one return value.
     */
    @FunctionalInterface
    interface FN4<U, T1, T2, T3, T4> {
        U apply(T1 param1, T2 param2, T3 param3, T4 param4);

        default <N> FN4<N, T1, T2, T3, T4> then(FN1<N, U> function) {
            return (v1, v2, v3, v4) -> function.apply(apply(v1, v2, v3, v4));
        }
    }

    /**
     * Function with 5 parameters and one return value.
     */
    @FunctionalInterface
    interface FN5<U, T1, T2, T3, T4, T5> {
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5);

        default <N> FN5<N, T1, T2, T3, T4, T5> then(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5) -> function.apply(apply(v1, v2, v3, v4, v5));
        }
    }

    /**
     * Function with 6 parameters and one return value.
     */
    @FunctionalInterface
    interface FN6<U, T1, T2, T3, T4, T5, T6> {
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6);

        default <N> FN6<N, T1, T2, T3, T4, T5, T6> then(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6) -> function.apply(apply(v1, v2, v3, v4, v5, v6));
        }
    }

    /**
     * Function with 7 parameters and one return value.
     */
    @FunctionalInterface
    interface FN7<U, T1, T2, T3, T4, T5, T6, T7> {
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7);

        default <N> FN7<N, T1, T2, T3, T4, T5, T6, T7> then(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7) -> function.apply(apply(v1, v2, v3, v4, v5, v6, v7));
        }
    }

    /**
     * Function with 8 parameters and one return value.
     */
    @FunctionalInterface
    interface FN8<U, T1, T2, T3, T4, T5, T6, T7, T8> {
        @SuppressWarnings({ "java:S107" })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8);

        default <N> FN8<N, T1, T2, T3, T4, T5, T6, T7, T8> then(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7, v8) -> function.apply(apply(v1, v2, v3, v4, v5, v6, v7, v8));
        }
    }

    /**
     * Function with 9 parameters and one return value.
     */
    @FunctionalInterface
    interface FN9<U, T1, T2, T3, T4, T5, T6, T7, T8, T9> {
        @SuppressWarnings({ "java:S107" })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9);

        default <N> FN9<N, T1, T2, T3, T4, T5, T6, T7, T8, T9> then(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7, v8, v9) -> function.apply(apply(v1, v2, v3, v4, v5, v6, v7, v8, v9));
        }
    }

    /**
     * Function with 10 parameters and one return value.
     */
    @SuppressWarnings("java:S119")
    @FunctionalInterface
    interface FN10<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA> {
        @SuppressWarnings({ "java:S107" })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10);
    }

    /**
     * Function with 11 parameters and one return value.
     */
    @SuppressWarnings("java:S119")
    @FunctionalInterface
    interface FN11<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB> {
        @SuppressWarnings({ "java:S107" })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10, TB param11);
    }

    /**
     * Function with 12 parameters and one return value.
     */
    @SuppressWarnings("java:S119")
    @FunctionalInterface
    interface FN12<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB, TC> {
        @SuppressWarnings({ "java:S107" })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10, TB param11, TC param12);
    }

    /**
     * Function with 13 parameters and one return value.
     */
    @SuppressWarnings("java:S119")
    @FunctionalInterface
    interface FN13<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB, TC, TD> {
        @SuppressWarnings({ "java:S107" })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10, TB param11, TC param12, TD param13);
    }

    /**
     * Function with 14 parameters and one return value.
     */
    @SuppressWarnings("java:S119")
    @FunctionalInterface
    interface FN14<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB, TC, TD, TE> {
        @SuppressWarnings({ "java:S107" })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10, TB param11, TC param12, TD param13, TE param14);
    }
}
