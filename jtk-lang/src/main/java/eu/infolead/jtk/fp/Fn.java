package eu.infolead.jtk.fp;

import static eu.infolead.jtk.lang.SonarLintWarning.JAVA_S107;
import static eu.infolead.jtk.lang.SonarLintWarning.JAVA_S1172;
import static eu.infolead.jtk.lang.SonarLintWarning.JAVA_S119;
import static eu.infolead.jtk.lang.SonarLintWarning.JAVA_S2326;

import java.util.function.Function;
import java.util.function.Supplier;

import eu.infolead.jtk.logic.Bool;
import jakarta.annotation.Nullable;

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

    public static <T> T identity(@Nullable final T value) {
        return value;
    }

    /**
     * Static method that just returns another value if the specified {@code value}
     * argument is {@code null}.
     * 
     * @param value
     * @param other
     * @return
     */
    public static <U> U or(final U value, final U other) {
        return Bool.of(value != null).fold(() -> other, () -> value);
    }

    @SuppressWarnings(JAVA_S1172)
    public static void doNothing() {
        // empty method body because this method does nothing
    }

    @SuppressWarnings(JAVA_S1172)
    public static <T> void doNothing(@Nullable final T value) {
        // empty method body because this method does nothing
    }

    @SuppressWarnings(JAVA_S1172)
    public static <U, T> U toNull(@Nullable final T value) {
        return null;
    }

    public static <U> U toNull() {
        return null;
    }

    /**
     * Universal consumers of values which do nothing with input values. Useful for
     * cases when API requires function, but there is no need to do anything with
     * the received values.
     * 
     * @param <T0>
     */
    @SuppressWarnings(JAVA_S2326)
    public static <T0> void unit() {
        // empty method body
    }

    @SuppressWarnings(JAVA_S1172)
    public static <T1> void unit(@Nullable final T1 value) {
        doNothing(value);
    }

    @SuppressWarnings({ JAVA_S1172 })
    public static <T1, T2> void unit(@Nullable final T1 val1, @Nullable final T2 val2) {
        // empty method body
    }

    @SuppressWarnings({ JAVA_S1172 })
    public static <T1, T2, T3> void unit(@Nullable final T1 val1, @Nullable final T2 val2, @Nullable final T3 val3) {
        // empty method body
    }

    @SuppressWarnings({ JAVA_S1172 })
    public static <T1, T2, T3, T4> void unit(@Nullable final T1 val1, @Nullable final T2 val2, @Nullable final T3 val3,
            @Nullable final T4 val4) {
        // empty method body
    }

    @SuppressWarnings({ JAVA_S1172 })
    public static <T1, T2, T3, T4, T5> void unit(@Nullable final T1 val1, @Nullable final T2 val2,
            @Nullable final T3 val3, @Nullable final T4 val4,
            @Nullable final T5 val5) {
        // empty method body
    }

    @SuppressWarnings({ JAVA_S1172 })
    public static <T1, T2, T3, T4, T5, T6> void unit(@Nullable final T1 val1, @Nullable final T2 val2,
            @Nullable final T3 val3, @Nullable final T4 val4,
            @Nullable final T5 val5, @Nullable final T6 val6) {
        // empty method body
    }

    @SuppressWarnings({ JAVA_S1172 })
    public static <T1, T2, T3, T4, T5, T6, T7> void unit(@Nullable final T1 val1, @Nullable final T2 val2,
            @Nullable final T3 val3, @Nullable final T4 val4,
            @Nullable final T5 val5, @Nullable final T6 val6, @Nullable final T7 val7) {
        // empty method body
    }

    @SuppressWarnings({ JAVA_S1172, JAVA_S107 })
    public static <T1, T2, T3, T4, T5, T6, T7, T8> void unit(@Nullable final T1 val1, @Nullable final T2 val2,
            @Nullable final T3 val3, @Nullable final T4 val4,
            @Nullable final T5 val5, @Nullable final T6 val6, @Nullable final T7 val7, @Nullable final T8 val8) {
        // empty method body
    }

    @SuppressWarnings({ JAVA_S1172, JAVA_S107 })
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> void unit(@Nullable final T1 val1, @Nullable final T2 val2,
            @Nullable final T3 val3,
            @Nullable final T4 val4, @Nullable final T5 val5, @Nullable final T6 val6, @Nullable final T7 val7,
            @Nullable final T8 val8, @Nullable final T9 val9) {
        // empty method body
    }

    @SuppressWarnings({ JAVA_S119, JAVA_S107 })
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, TA> void unit(@Nullable final T1 val1, @Nullable final T2 val2,
            @Nullable final T3 val3,
            final T4 val4, @Nullable final T5 val5, @Nullable final T6 val6, @Nullable final T7 val7,
            @Nullable final T8 val8, @Nullable final T9 val9, @Nullable final TA valA) {
        // empty method body
    }

    @SuppressWarnings({ JAVA_S119, JAVA_S107 })
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB> void unit(@Nullable final T1 val1,
            @Nullable final T2 val2, @Nullable final T3 val3,
            final T4 val4, @Nullable final T5 val5, @Nullable final T6 val6, @Nullable final T7 val7,
            @Nullable final T8 val8, @Nullable final T9 val9, @Nullable final TA val10,
            final TB val11) {
        // empty method body
    }

    /**
     * Function with no parameters (supplier). Provided for consistency.
     */
    @FunctionalInterface
    public interface FN0<U> extends Supplier<U> {
        default U apply() {
            return get();
        }
    }

    /**
     * Function with one parameter and one return value.
     * This class exists to be able to define additional operations.
     * <p>
     * <strong>Note : </strong> The generic types are reversed: first the type of
     * the result, then the type of the parameter, as is usually done in functional
     * programming.
     */
    @FunctionalInterface
    public interface FN1<U, T1> extends Function<T1, U> {
    }

    /**
     * Function with two parameters and one return value.
     */
    @FunctionalInterface
    public interface FN2<U, T1, T2> {
        U apply(T1 param1, T2 param2);

        default FN1<U, T2> bind(final T1 param) {
            return v2 -> apply(param, v2);
        }

        default <N> FN2<N, T1, T2> andThen(final FN1<N, U> function) {
            return (v1, v2) -> function.apply(apply(v1, v2));
        }
    }

    /**
     * Function with three parameters and one return value.
     */
    @FunctionalInterface
    public interface FN3<U, T1, T2, T3> {
        U apply(T1 param1, T2 param2, T3 param3);

        default <N> FN3<N, T1, T2, T3> andThen(FN1<N, U> function) {
            return (v1, v2, v3) -> function.apply(apply(v1, v2, v3));
        }
    }

    /**
     * Function with four parameters and one return value.
     */
    @FunctionalInterface
    public interface FN4<U, T1, T2, T3, T4> {
        U apply(T1 param1, T2 param2, T3 param3, T4 param4);

        default <N> FN4<N, T1, T2, T3, T4> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4) -> function.apply(apply(v1, v2, v3, v4));
        }
    }

    /**
     * Function with 5 parameters and one return value.
     */
    @FunctionalInterface
    public interface FN5<U, T1, T2, T3, T4, T5> {
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5);

        default <N> FN5<N, T1, T2, T3, T4, T5> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5) -> function.apply(apply(v1, v2, v3, v4, v5));
        }
    }

    /**
     * Function with 6 parameters and one return value.
     */
    @FunctionalInterface
    public interface FN6<U, T1, T2, T3, T4, T5, T6> {
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6);

        default <N> FN6<N, T1, T2, T3, T4, T5, T6> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6) -> function.apply(apply(v1, v2, v3, v4, v5, v6));
        }
    }

    /**
     * Function with 7 parameters and one return value.
     */
    @FunctionalInterface
    public interface FN7<U, T1, T2, T3, T4, T5, T6, T7> {
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7);

        default <N> FN7<N, T1, T2, T3, T4, T5, T6, T7> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7) -> function.apply(apply(v1, v2, v3, v4, v5, v6, v7));
        }
    }

    /**
     * Function with 8 parameters and one return value.
     */
    @FunctionalInterface
    public interface FN8<U, T1, T2, T3, T4, T5, T6, T7, T8> {
        @SuppressWarnings({ JAVA_S107 })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8);

        default <N> FN8<N, T1, T2, T3, T4, T5, T6, T7, T8> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7, v8) -> function.apply(apply(v1, v2, v3, v4, v5, v6, v7, v8));
        }
    }

    /**
     * Function with 9 parameters and one return value.
     */
    @FunctionalInterface
    public interface FN9<U, T1, T2, T3, T4, T5, T6, T7, T8, T9> {
        @SuppressWarnings({ JAVA_S107 })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9);

        default <N> FN9<N, T1, T2, T3, T4, T5, T6, T7, T8, T9> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7, v8, v9) -> function.apply(apply(v1, v2, v3, v4, v5, v6, v7, v8, v9));
        }
    }

    /**
     * Function with 10 parameters and one return value.
     */
    @SuppressWarnings(JAVA_S119)
    @FunctionalInterface
    public interface FN10<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA> {
        @SuppressWarnings({ JAVA_S107 })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10);

        default <N> FN10<N, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7, v8, v9, va) -> function
                    .apply(apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, va));
        }
    }

    /**
     * Function with 11 parameters and one return value.
     */
    @SuppressWarnings(JAVA_S119)
    @FunctionalInterface
    public interface FN11<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB> {
        @SuppressWarnings({ JAVA_S107 })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10, TB param11);

        default <N> FN11<N, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7, v8, v9, va, vb) -> function
                    .apply(apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, va, vb));
        }
    }

    /**
     * Function with 12 parameters and one return value.
     */
    @SuppressWarnings(JAVA_S119)
    @FunctionalInterface
    public interface FN12<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB, TC> {
        @SuppressWarnings({ JAVA_S107 })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10, TB param11, TC param12);

        default <N> FN12<N, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB, TC> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7, v8, v9, va, vb, vc) -> function
                    .apply(apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, va, vb, vc));
        }
    }

    /**
     * Function with 13 parameters and one return value.
     */
    @SuppressWarnings(JAVA_S119)
    @FunctionalInterface
    public interface FN13<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB, TC, TD> {
        @SuppressWarnings({ JAVA_S107 })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10, TB param11, TC param12, TD param13);

        default <N> FN13<N, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB, TC, TD> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7, v8, v9, va, vb, vc, vd) -> function
                    .apply(apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, va, vb, vc, vd));
        }
    }

    /**
     * Function with 14 parameters and one return value.
     */
    @SuppressWarnings(JAVA_S119)
    @FunctionalInterface
    public interface FN14<U, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB, TC, TD, TE> {
        @SuppressWarnings({ JAVA_S107 })
        U apply(T1 param1, T2 param2, T3 param3, T4 param4, T5 param5, T6 param6, T7 param7, T8 param8, T9 param9,
                TA param10, TB param11, TC param12, TD param13, TE param14);

        default <N> FN14<N, T1, T2, T3, T4, T5, T6, T7, T8, T9, TA, TB, TC, TD, TE> andThen(FN1<N, U> function) {
            return (v1, v2, v3, v4, v5, v6, v7, v8, v9, va, vb, vc, vd, ve) -> function
                    .apply(apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, va, vb, vc, vd, ve));
        }
    }
}
