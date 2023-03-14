package eu.infolead.jtk.logic;

import static eu.infolead.jtk.lang.SonarLintWarning.JAVA_S1172;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import eu.infolead.jtk.fp.Filterable;

/**
 * Represents the classical two-valued boolean, which may have the value
 * {@link Bool#TRUE} or {@link Bool#False}.
 * 
 * <h3>Notes</h3>
 * <ol>
 * <li>The API of this class SHOULD always consume and return unboxed
 * {@code boolean} instances instead of boxed
 * {@link Boolean} instances: only
 * the unboxed ones have only two possible values like instances of this class.
 * The boxed ones have the
 * third possible value {@code null}, and the classical two-valued logic does
 * not define how to handle {@code null}.</li>
 * <li>As a consequence of the preceding discussion/point, the API of this class
 * MUST never allow {@code null} as an argument or a return value.</li>
 * </ol>
 */
public abstract sealed class Bool implements Serializable, Filterable<Bool> {
    public static final Bool TRUE = new True();
    public static final Bool FALSE = new False();

    public static Bool of(final boolean value) {
        return value ? TRUE : FALSE;
    }

    public static Bool of(final Bool value) {
        return value;
    }

    public final Bool negate() {
        return Bool.of(!toBoolean());
    }

    public static final Bool negate(final Bool b) {
        return b.negate();
    }

    public abstract Bool and(Supplier<Bool> bool);

    public Bool and(final Bool bool) {
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

    public Stream<Bool> filter(final Predicate<Bool> p) {
        return Stream.of(this).filter(p);
    }

    static final class True extends Bool {

        @Override
        public Bool and(final Supplier<Bool> boolSupplier) {
            return boolSupplier.get();
        }

        @Override
        public Bool or(final Bool bool) {
            return this;
        }

        @Override
        public Bool xor(final Bool bool) {
            return bool.fold(() -> Bool.TRUE, () -> Bool.FALSE);
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
            trueAction.run();
            return this;
        }

        @Override
        public Bool ifFalse(final Consumer<Bool> falseConsumer) {
            // do nothing
            return this;
        }

        @Override
        public Bool ifTrue(final Consumer<Bool> trueConsumer) {
            trueConsumer.accept(TRUE);
            return this;
        }

        @Override
        public <R> R fold(final Supplier<R> falseSupplier, final Supplier<R> trueSupplier) {
            return trueSupplier.get();
        }

        @Override
        public Bool apply(final Runnable falseAction, final Runnable trueAction) {
            trueAction.run();
            return this;
        }

        @Override
        public Bool apply(final Consumer<Bool> falseConsumer, final Consumer<Bool> trueConsumer) {
            trueConsumer.accept(TRUE);
            return this;
        }
    }

    static final class False extends Bool {
        @Override
        public Bool and(final Supplier<Bool> bool) {
            return this;
        }

        @Override
        public Bool or(final Bool bool) {
            return bool;
        }

        @Override
        public Bool xor(final Bool bool) {
            return bool.fold(() -> Bool.FALSE, () -> Bool.TRUE);
        }

        @Override
        public boolean toBoolean() {
            return false;
        }

        @Override
        public Bool ifFalse(final Runnable falseAction) {
            falseAction.run();
            return this;
        }

        @Override
        public Bool ifTrue(final Runnable trueAction) {
            // do nothing
            return this;
        }

        @Override
        public Bool ifFalse(final Consumer<Bool> falseConsumer) {
            falseConsumer.accept(FALSE);
            return this;
        }

        @Override
        public Bool ifTrue(final Consumer<Bool> trueConsumer) {
            return this;
        }

        @Override
        public <R> R fold(final Supplier<R> falseSupplier, final Supplier<R> trueSupplier) {
            return falseSupplier.get();
        }

        @Override
        public Bool apply(final Runnable falseAction, final Runnable trueAction) {
            falseAction.run();
            return FALSE;
        }

        @Override
        public Bool apply(final Consumer<Bool> falseConsumer, final Consumer<Bool> trueConsumer) {
            falseConsumer.accept(FALSE);
            return FALSE;
        }
    }
}
