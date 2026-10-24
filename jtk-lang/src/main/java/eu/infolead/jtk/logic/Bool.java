package eu.infolead.jtk.logic;

import static eu.infolead.jtk.lang.SonarLintWarning.JAVA_S1172;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import eu.infolead.jtk.fp.Filterable;

/**
 * Represents the classical two-valued boolean, which may have the value
 * {@link Bool#TRUE} or {@link Bool#FALSE}.
 *
 * <h3>Notes</h3>
 * <ol>
 * <li>The API of this class SHOULD always consume and return unboxed
 * {@code boolean} instances instead of boxed {@link Boolean} instances: only the
 * unboxed ones have only two possible values like instances of this class. The
 * boxed ones have the third possible value {@code null}, and the classical
 * two-valued logic does not define how to handle {@code null}.</li>
 * <li>As a consequence of the preceding discussion/point, the API of this class
 * MUST never allow {@code null} as an argument or a return value.</li>
 * </ol>
 */
public abstract sealed class Bool implements Serializable, Filterable<Bool> {
    private static final long serialVersionUID = 1L;

    private Bool() {
        // sealed hierarchy: instances are the TRUE and FALSE constants
    }

    public static final Bool TRUE = new True();
    public static final Bool FALSE = new False();

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
        return Objects.requireNonNull(value, "value must not be null; use NullableBool for three-valued logic");
    }

    public final Bool negate() {
        return Bool.of(!toBoolean());
    }

    public static Bool negate(final Bool b) {
        return of(b).negate();
    }

    public abstract Bool and(Supplier<Bool> bool);

    public Bool and(final Bool bool) {
        Objects.requireNonNull(bool, "bool");
        return and(() -> bool);
    }

    public final Bool and(final boolean bool) {
        return and(Bool.of(bool));
    }

    public abstract Bool or(Bool bool);

    public final Bool or(final boolean bool) {
        return or(Bool.of(bool));
    }

    public abstract Bool xor(Bool bool);

    public final Bool xor(final boolean bool) {
        return xor(Bool.of(bool));
    }

    @SuppressWarnings(JAVA_S1172)
    public static <T> Bool toTrue(final T value) {
        return Bool.TRUE;
    }

    public static Bool toTrue() {
        return Bool.TRUE;
    }

    @SuppressWarnings(JAVA_S1172)
    public static <T> Bool toFalse(final T value) {
        return Bool.FALSE;
    }

    public static Bool toFalse() {
        return Bool.FALSE;
    }

    public abstract <R> R fold(Supplier<R> falseSupplier, Supplier<R> trueSupplier);

    public abstract Bool apply(Runnable falseAction, Runnable trueAction);

    public abstract Bool apply(Consumer<Bool> falseConsumer, Consumer<Bool> trueConsumer);

    public abstract Bool ifTrue(Consumer<Bool> trueConsumer);

    public abstract Bool ifTrue(Runnable trueAction);

    public abstract Bool ifFalse(Consumer<Bool> falseConsumer);

    public abstract Bool ifFalse(Runnable falseAction);

    public abstract boolean toBoolean();

    static final class True extends Bool {
        private static final long serialVersionUID = 1L;

        private True() {
        }

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

    static final class False extends Bool {
        private static final long serialVersionUID = 1L;

        private False() {
        }

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
