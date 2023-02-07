package eu.infolead.jtk.fp;

import java.io.Serializable;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.annotation.Nonnull;

/**
 * This class captures explicitly that {@link Boolean} has three possible
 * states: {@code true}, {@code false} and {@code null}.
 */
public abstract sealed class Bool implements Serializable {
    public static final Bool TRUE = new True();
    public static final Bool FALSE = new False();

    /**
     * This method creates a {@link NullableBool}, and captures that the specified
     * value may be {@code null} and that this value is valid and expected.
     * 
     * @param value
     * @return
     */
    public static NullableBool ofNullable(final Boolean value) {
        if (value == null) {
            return NullableBool.NULL;
        } else if (value) {
            return NullableBool.TRUE;
        } else {
            return NullableBool.FALSE;
        }
    }

    /**
     * 
     * @param valueSupplier the supplier of Boolean. Note: do not use
     *                      {@link BooleanSupplier} here, as it won't return
     *                      {@code null} (it only returns the boolean primitive) and
     *                      we need {@code null} as a possible value.
     * @return
     */
    @SuppressWarnings("java:S4276")
    public static NullableBool ofNullable(final Supplier<Boolean> valueSupplier) {
        return Bool.ofNullable(valueSupplier.get());
    }

    public static Bool of(@Nonnull final Boolean value) {
        if (value == null) {
            throw new IllegalArgumentException("The specified Boolean must not be null.");
        } else if (value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public static Bool of(final boolean value) {
        return value ? TRUE : FALSE;
    }

    public static Bool of(final Bool value) {
        return value;
    }

    public final Bool and(final Bool bool) {
        return Bool.of(toBoolean() && bool.toBoolean());
    }

    public final Bool or(final Bool bool) {
        return Bool.of(toBoolean() || bool.toBoolean());
    }

    public final Bool xor(final Bool bool) {
        return Bool.of(toBoolean() ^ bool.toBoolean());
    }

    public final Bool negate() {
        return Bool.of(!toBoolean());
    }

    public static final Bool negate(final Bool b) {
        return b.negate();
    }

    public final Bool and(final boolean bool) {
        return Bool.of(toBoolean() && bool);
    }

    public final Bool or(final boolean bool) {
        return Bool.of(toBoolean() || bool);
    }

    public final Bool xor(final boolean bool) {
        return Bool.of(toBoolean() ^ bool);
    }

    public final Bool and(final Boolean bool) {
        return Bool.of(toBoolean() && bool);
    }

    public final Bool or(final Boolean bool) {
        return Bool.of(toBoolean() || bool);
    }

    public final Bool xor(final Boolean bool) {
        return Bool.of(toBoolean() ^ bool);
    }

    public final Bool and(final NullableBool bool) {
        return Bool.of(toBoolean() && bool.toBoolean());
    }

    public final Bool or(final NullableBool bool) {
        return Bool.of(toBoolean() || bool.toBoolean());
    }

    public final Bool xor(final NullableBool bool) {
        return Bool.of(toBoolean() ^ bool.toBoolean());
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
