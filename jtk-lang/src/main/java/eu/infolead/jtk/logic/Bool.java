package eu.infolead.jtk.logic;

import static eu.infolead.jtk.lang.SonarLintWarning.JAVA_S1172;

import eu.infolead.jtk.fp.Filterable;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents the classical two-valued boolean, which may have the value {@link Bool#TRUE} or {@link
 * Bool#FALSE}.
 *
 * <h2>Notes</h2>
 *
 * <ol>
 *   <li>The API of this class SHOULD always consume and return unboxed {@code boolean} instances
 *       instead of boxed {@link Boolean} instances: only the unboxed ones have only two possible
 *       values like instances of this class. The boxed ones have the third possible value {@code
 *       null}, and the classical two-valued logic does not define how to handle {@code null}.
 *   <li>As a consequence of the preceding discussion/point, the API of this class MUST never allow
 *       {@code null} as an argument or a return value.
 * </ol>
 *
 * <h2>Usage: {@code fold()} versus {@code if/else}</h2>
 *
 * <p>{@link #fold(Supplier, Supplier)} is the <em>expression</em> form of branching. It is not a
 * replacement for {@code if/else}, and the two MUST NOT be used interchangeably. The rules are:
 *
 * <ol>
 *   <li>Code whose branch is a statement - a guard clause, an early {@code return}, a {@code
 *       throw}, a log call, an assignment, or any other side effect - MUST use {@code if
 *       (b.toBoolean())} and MUST NOT use {@code fold()}. A {@code fold()} branch written as {@code
 *       () -> { sideEffect(); return null; }} is always a sign that {@code if/else} was wanted.
 *   <li>Code whose branch computes a value inside an expression SHOULD use {@code fold()}, provided
 *       both branches are pure, cheap and symmetric, and read best as named method references such
 *       as {@code fold(this::renderNone, this::renderValue)}.
 *   <li>A plain {@code boolean} condition whose two operands are already cheap values SHOULD use
 *       the ternary {@code cond ? a : b}: Java evaluates only the taken branch of a ternary, so
 *       {@code fold()} adds two {@link Supplier}s and an indirection without adding any laziness.
 *   <li>Wrapping a primitive {@code boolean} in {@link #of(boolean)} solely in order to branch on
 *       it MUST be avoided: use {@code if}.
 *   <li>{@code fold()} carries no exhaustiveness advantage over {@code if/else} for this class: a
 *       classical two-valued conditional is already total. Its value is purely that of a named,
 *       typed expression inside a fluent pipeline.
 * </ol>
 *
 * <p>The branching members are split by purpose:
 *
 * <ul>
 *   <li>{@link #fold(Supplier, Supplier)} - the only branching member that produces a value: it
 *       chooses one {@link Supplier} and returns the value it yields.
 *   <li>{@link #apply(Runnable, Runnable)}, {@link #apply(Consumer, Consumer)}, {@link
 *       #ifTrue(Runnable)}, {@link #ifTrue(Consumer)}, {@link #ifFalse(Runnable)} and {@link
 *       #ifFalse(Consumer)} - run side effects and return a {@code Bool} so a chain can continue.
 *       Prefer {@code if/else} over all of them unless a fluent {@code Bool} chain is already the
 *       established style of the surrounding code.
 * </ul>
 *
 * <p>The argument order of {@link #fold(Supplier, Supplier)} places the {@code falseSupplier}
 * first. Swapping the branches compiles, and when both branches return the same type it silently
 * inverts the behaviour: this is the main footgun of the method, and a further reason to prefer
 * {@code if/else} whenever the branches are not trivially readable. Only the branch that is taken
 * is validated, so both branches MUST be non-{@code null}: a {@code null} branch for the untaken
 * side is not detected.
 *
 * <p>Three-valued logic is a separate concern. {@link NullableBool} has no total {@code if/else}
 * equivalent, so its {@code fold(falseSupplier, nullSupplier, trueSupplier)} is genuinely safer
 * than {@code if/else}, because it makes the {@code NULL} branch impossible to forget. That
 * reasoning does not apply to this class.
 *
 * <h2>Examples</h2>
 *
 * <pre>{@code
 * // Value produced inside an expression, symmetric named branches: fold().
 * String text = actualValue.fold(() -> "none", SystemError::renderValue);
 *
 * // Guard clause with a side effect: if/else, never fold().
 * if (!HttpStatusCode.isValid(code).toBoolean()) {
 *     throw new ParameterError(...);
 * }
 *
 * // Cheap value choice on a primitive condition: ternary, not fold().
 * return rendered ? value : "none";
 *
 * // Antipattern: fold() used to run statements. This wanted if/else.
 * Bool.of(valid).fold(() -> { log.warn("invalid"); return null; },
 *                      () -> { metrics.increment(); return null; });
 *
 * // Antipattern: wrapping a primitive only to branch on it.
 * if (Bool.of(count > 0).toBoolean()) { ... }
 * }</pre>
 *
 * @see NullableBool
 * @see eu.infolead.jtk.fp.either
 */
public abstract sealed class Bool implements Serializable, Filterable<Bool> {
    private static final long serialVersionUID = 1L;

    private Bool() {
        // sealed hierarchy: instances are the TRUE and FALSE constants
    }

    /** The true value of classical two-valued logic. */
    public static final Bool TRUE = new True();

    /** The false value of classical two-valued logic. */
    public static final Bool FALSE = new False();

    /**
     * Boxes a primitive {@code boolean} into its two-valued equivalent.
     *
     * @param value the value to box
     * @return {@link #TRUE} for {@code true}, {@link #FALSE} for {@code false}
     */
    public static Bool of(final boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Returns the specified value after checking that it is not {@code null}.
     *
     * @param value the value to return
     * @return {@code value}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static Bool of(final Bool value) {
        return Objects.requireNonNull(
                value, "value must not be null; use NullableBool for three-valued logic");
    }

    /**
     * Returns the classical negation (NOT) of this value.
     *
     * @return {@link #TRUE} if this value is {@link #FALSE}, {@link #FALSE} otherwise
     */
    public final Bool negate() {
        return Bool.of(!toBoolean());
    }

    /**
     * Null-checking form of {@link #negate()}.
     *
     * @param b the value to negate; must not be {@code null}
     * @return the negation of {@code b}
     * @throws NullPointerException if {@code b} is {@code null}
     */
    public static Bool negate(final Bool b) {
        return of(b).negate();
    }

    /**
     * Returns the conjunction (AND) of this value and a lazily evaluated operand.
     *
     * <p>The operand is evaluated only when this value is {@link #TRUE}: when this value is {@link
     * #FALSE} the result is {@link #FALSE} and the supplier is never invoked, which is the reason
     * this overload takes a {@link Supplier}.
     *
     * @param bool the right-hand operand, evaluated lazily; must not be {@code null} when it is
     *     evaluated, and must not produce {@code null}
     * @return {@link #TRUE} only when both operands are {@link #TRUE}
     * @throws NullPointerException if {@code bool} is {@code null} when it is evaluated, or
     *     produces {@code null}
     */
    public abstract Bool and(Supplier<Bool> bool);

    /**
     * Returns the conjunction (AND) of two {@code Bool} values.
     *
     * <p>The argument is evaluated eagerly by the caller. Use {@link #and(Supplier)} when the
     * right-hand operand is expensive or has side effects that must be skipped when this value is
     * {@link #FALSE}.
     *
     * @param bool the right-hand operand; must not be {@code null}
     * @return {@link #TRUE} only when both operands are {@link #TRUE}
     * @throws NullPointerException if {@code bool} is {@code null}
     */
    public Bool and(final Bool bool) {
        Objects.requireNonNull(bool, "bool");
        return and(() -> bool);
    }

    /**
     * Returns the conjunction (AND) of this value and a primitive {@code boolean}.
     *
     * @param bool the right-hand operand
     * @return {@link #TRUE} only when this value is {@link #TRUE} and {@code bool} is {@code true}
     */
    public final Bool and(final boolean bool) {
        return and(Bool.of(bool));
    }

    /**
     * Returns the disjunction (OR) of this value and another value.
     *
     * <p>When this value is {@link #TRUE} the result is {@link #TRUE} and the argument is ignored,
     * although the caller has already evaluated it. This method has no lazy overload: when the
     * operand is expensive, guard the call with {@code if (b.toBoolean())} or use {@link
     * #or(boolean)}.
     *
     * @param bool the right-hand operand; must not be {@code null} when this value is {@link
     *     #FALSE}
     * @return {@link #TRUE} when at least one operand is {@link #TRUE}
     * @throws NullPointerException if this value is {@link #FALSE} and {@code bool} is {@code null}
     */
    public abstract Bool or(Bool bool);

    /**
     * Returns the disjunction (OR) of this value and a primitive {@code boolean}.
     *
     * @param bool the right-hand operand
     * @return {@link #TRUE} when at least one operand is {@code true}
     */
    public final Bool or(final boolean bool) {
        return or(Bool.of(bool));
    }

    /**
     * Returns the exclusive disjunction (XOR) of this value and another value.
     *
     * @param bool the right-hand operand; must not be {@code null}
     * @return {@link #TRUE} when exactly one operand is {@link #TRUE}
     * @throws NullPointerException if {@code bool} is {@code null}
     */
    public abstract Bool xor(Bool bool);

    /**
     * Returns the exclusive disjunction (XOR) of this value and a primitive {@code boolean}.
     *
     * @param bool the right-hand operand
     * @return {@link #TRUE} when exactly one operand is {@code true}
     */
    public final Bool xor(final boolean bool) {
        return xor(Bool.of(bool));
    }

    /**
     * Returns {@link #TRUE}, ignoring its argument: a constant function.
     *
     * <p>Intended as a method reference in {@code map} and {@code fold} chains, where a lambda such
     * as {@code x -> Bool.TRUE} would carry an unused parameter.
     *
     * @param <T> the ignored input type
     * @param value the value to ignore
     * @return {@link #TRUE}
     */
    @SuppressWarnings(JAVA_S1172)
    public static <T> Bool toTrue(final T value) {
        return Bool.TRUE;
    }

    /**
     * Returns {@link #TRUE}.
     *
     * @return {@link #TRUE}
     */
    public static Bool toTrue() {
        return Bool.TRUE;
    }

    /**
     * Returns {@link #FALSE}, ignoring its argument: a constant function.
     *
     * <p>Intended as a method reference in {@code map} and {@code fold} chains, where a lambda such
     * as {@code x -> Bool.FALSE} would carry an unused parameter.
     *
     * @param <T> the ignored input type
     * @param value the value to ignore
     * @return {@link #FALSE}
     */
    @SuppressWarnings(JAVA_S1172)
    public static <T> Bool toFalse(final T value) {
        return Bool.FALSE;
    }

    /**
     * Returns {@link #FALSE}.
     *
     * @return {@link #FALSE}
     */
    public static Bool toFalse() {
        return Bool.FALSE;
    }

    /**
     * Returns the value produced by the branch that matches this value.
     *
     * <p>This is the only branching member of this class that produces a value: it is the
     * <em>expression</em> form of {@code if/else}, not a replacement for it.
     *
     * <h4>When to use it</h4>
     *
     * <ul>
     *   <li>The result is a value of type {@code R} appearing inside an expression: a {@code
     *       return}, an argument, a chain link.
     *   <li>Both branches are pure, cheap and symmetric, ideally named method references that
     *       document themselves at the call site.
     *   <li>Breaking out into a statement would interrupt a fluent pipeline.
     * </ul>
     *
     * <h4>When not to use it</h4>
     *
     * <ul>
     *   <li>The branch is a statement - a guard clause, an early {@code return}, a {@code throw},
     *       logging, an assignment, any side effect. Use {@code if (b.toBoolean()) { ... }}.
     *   <li>One branch does the work and the other does nothing. Use {@code if (b.toBoolean()) {
     *       ... }}.
     *   <li>Both operands are already cheap values. Use {@code cond ? a : b}: only the taken branch
     *       of a ternary is evaluated, so {@code fold()} adds no laziness here.
     *   <li>The code is on a hot path: each call site passes two {@link Supplier}s and an
     *       indirection, and a capturing lambda allocates.
     *   <li>Readable stack traces and step-through debugging matter more than keeping the branch an
     *       expression.
     * </ul>
     *
     * <p>Note the argument order: {@code falseSupplier} comes first. Swapping the branches compiles
     * and, when both return the same type, silently inverts the behaviour. Only the taken branch is
     * validated, so both {@link Supplier}s MUST be non-{@code null}.
     *
     * @param <R> the type of the result
     * @param falseSupplier the supplier to use when this value is {@link #FALSE}; must not be
     *     {@code null}
     * @param trueSupplier the supplier to use when this value is {@link #TRUE}; must not be {@code
     *     null}
     * @return the value of the matching supplier
     * @throws NullPointerException if the matching supplier is {@code null}
     */
    public abstract <R> R fold(Supplier<R> falseSupplier, Supplier<R> trueSupplier);

    /**
     * Runs the action matching this value and returns a {@code Bool}.
     *
     * <p>A side-effect form of {@code if/else}. Prefer {@code if/else} over this method unless a
     * fluent {@code Bool} chain is already the established style of the surrounding code.
     *
     * @param falseAction the action to run when this value is {@link #FALSE}; must not be {@code
     *     null}
     * @param trueAction the action to run when this value is {@link #TRUE}; must not be {@code
     *     null}
     * @return {@link #FALSE} when the false action ran, {@link #TRUE} when the true action ran, so
     *     the chain can continue
     * @throws NullPointerException if an action is {@code null}
     */
    public abstract Bool apply(Runnable falseAction, Runnable trueAction);

    /**
     * Accepts this value on the consumer matching it and returns a {@code Bool}.
     *
     * <p>A side-effect form of {@code if/else}. Prefer {@code if/else} over this method unless a
     * fluent {@code Bool} chain is already the established style of the surrounding code. When a
     * branch does not need the value, prefer {@link #apply(Runnable, Runnable)}.
     *
     * @param falseConsumer the consumer to accept {@link #FALSE}; must not be {@code null}
     * @param trueConsumer the consumer to accept {@link #TRUE}; must not be {@code null}
     * @return {@link #FALSE} when the false consumer ran, {@link #TRUE} when the true consumer ran,
     *     so the chain can continue
     * @throws NullPointerException if a consumer is {@code null}
     */
    public abstract Bool apply(Consumer<Bool> falseConsumer, Consumer<Bool> trueConsumer);

    /**
     * Accepts {@link #TRUE} on the consumer when this value is {@link #TRUE}, and returns a {@code
     * Bool}.
     *
     * <p>A side-effect form of {@code if (b.toBoolean()) { ... }}. Prefer {@code if/else} over this
     * method unless a fluent {@code Bool} chain is already the established style of the surrounding
     * code.
     *
     * @param trueConsumer the consumer to accept {@link #TRUE}; must not be {@code null}
     * @return this value, so the chain can continue
     * @throws NullPointerException if {@code trueConsumer} is {@code null} and this value is {@link
     *     #TRUE}
     */
    public abstract Bool ifTrue(Consumer<Bool> trueConsumer);

    /**
     * Runs the action when this value is {@link #TRUE}, and returns a {@code Bool}.
     *
     * <p>A side-effect form of {@code if (b.toBoolean()) { ... }}. Prefer {@code if/else} over this
     * method unless a fluent {@code Bool} chain is already the established style of the surrounding
     * code.
     *
     * @param trueAction the action to run; must not be {@code null}
     * @return this value, so the chain can continue
     * @throws NullPointerException if {@code trueAction} is {@code null} and this value is {@link
     *     #TRUE}
     */
    public abstract Bool ifTrue(Runnable trueAction);

    /**
     * Accepts {@link #FALSE} on the consumer when this value is {@link #FALSE}, and returns a
     * {@code Bool}.
     *
     * <p>A side-effect form of {@code if (!b.toBoolean()) { ... }}. Prefer {@code if/else} over
     * this method unless a fluent {@code Bool} chain is already the established style of the
     * surrounding code.
     *
     * @param falseConsumer the consumer to accept {@link #FALSE}; must not be {@code null}
     * @return this value, so the chain can continue
     * @throws NullPointerException if {@code falseConsumer} is {@code null} and this value is
     *     {@link #FALSE}
     */
    public abstract Bool ifFalse(Consumer<Bool> falseConsumer);

    /**
     * Runs the action when this value is {@link #FALSE}, and returns a {@code Bool}.
     *
     * <p>A side-effect form of {@code if (!b.toBoolean()) { ... }}. Prefer {@code if/else} over
     * this method unless a fluent {@code Bool} chain is already the established style of the
     * surrounding code.
     *
     * @param falseAction the action to run; must not be {@code null}
     * @return this value, so the chain can continue
     * @throws NullPointerException if {@code falseAction} is {@code null} and this value is {@link
     *     #FALSE}
     */
    public abstract Bool ifFalse(Runnable falseAction);

    /**
     * Unboxes this value to a primitive {@code boolean}.
     *
     * <p>This is the intended bridge to {@code if/else} and to the ternary: use it for conditions
     * and other statement-shaped uses, where {@link #fold(Supplier, Supplier)} would only add
     * indirection.
     *
     * @return {@code true} for {@link #TRUE}, {@code false} for {@link #FALSE}
     */
    public abstract boolean toBoolean();

    /** The {@link #TRUE} singleton of the sealed hierarchy. */
    static final class True extends Bool {
        private static final long serialVersionUID = 1L;

        private True() {}

        @Override
        public Bool and(final Supplier<Bool> boolSupplier) {
            return of(Objects.requireNonNull(boolSupplier, "boolSupplier").get());
        }

        @Override
        public Bool or(final Bool bool) {
            Objects.requireNonNull(bool, "bool");
            return this;
        }

        @Override
        public Bool xor(final Bool bool) {
            return of(bool).fold(() -> Bool.TRUE, () -> Bool.FALSE);
        }

        @Override
        public boolean toBoolean() {
            return true;
        }

        @Override
        public Bool ifFalse(final Runnable falseAction) {
            return this;
        }

        @Override
        public Bool ifTrue(final Runnable trueAction) {
            Objects.requireNonNull(trueAction, "trueAction").run();
            return this;
        }

        @Override
        public Bool ifFalse(final Consumer<Bool> falseConsumer) {
            return this;
        }

        @Override
        public Bool ifTrue(final Consumer<Bool> trueConsumer) {
            Objects.requireNonNull(trueConsumer, "trueConsumer").accept(TRUE);
            return this;
        }

        @Override
        public <R> R fold(final Supplier<R> falseSupplier, final Supplier<R> trueSupplier) {
            return Objects.requireNonNull(trueSupplier, "trueSupplier").get();
        }

        @Override
        public Bool apply(final Runnable falseAction, final Runnable trueAction) {
            Objects.requireNonNull(trueAction, "trueAction").run();
            return this;
        }

        @Override
        public Bool apply(final Consumer<Bool> falseConsumer, final Consumer<Bool> trueConsumer) {
            Objects.requireNonNull(trueConsumer, "trueConsumer").accept(TRUE);
            return this;
        }

        private Object readResolve() {
            return TRUE;
        }
    }

    /** The {@link #FALSE} singleton of the sealed hierarchy. */
    static final class False extends Bool {
        private static final long serialVersionUID = 1L;

        private False() {}

        @Override
        public Bool and(final Supplier<Bool> bool) {
            Objects.requireNonNull(bool, "bool");
            return this;
        }

        @Override
        public Bool or(final Bool bool) {
            return of(bool);
        }

        @Override
        public Bool xor(final Bool bool) {
            return of(bool).fold(() -> Bool.FALSE, () -> Bool.TRUE);
        }

        @Override
        public boolean toBoolean() {
            return false;
        }

        @Override
        public Bool ifFalse(final Runnable falseAction) {
            Objects.requireNonNull(falseAction, "falseAction").run();
            return this;
        }

        @Override
        public Bool ifTrue(final Runnable trueAction) {
            return this;
        }

        @Override
        public Bool ifFalse(final Consumer<Bool> falseConsumer) {
            Objects.requireNonNull(falseConsumer, "falseConsumer").accept(FALSE);
            return this;
        }

        @Override
        public Bool ifTrue(final Consumer<Bool> trueConsumer) {
            return this;
        }

        @Override
        public <R> R fold(final Supplier<R> falseSupplier, final Supplier<R> trueSupplier) {
            return Objects.requireNonNull(falseSupplier, "falseSupplier").get();
        }

        @Override
        public Bool apply(final Runnable falseAction, final Runnable trueAction) {
            Objects.requireNonNull(falseAction, "falseAction").run();
            return FALSE;
        }

        @Override
        public Bool apply(final Consumer<Bool> falseConsumer, final Consumer<Bool> trueConsumer) {
            Objects.requireNonNull(falseConsumer, "falseConsumer").accept(FALSE);
            return FALSE;
        }

        private Object readResolve() {
            return FALSE;
        }
    }
}
