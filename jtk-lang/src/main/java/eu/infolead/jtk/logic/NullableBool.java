package eu.infolead.jtk.logic;

import eu.infolead.jtk.lang.SonarLintWarning;
import eu.infolead.jtk.logic.NullableBool.False;
import eu.infolead.jtk.logic.NullableBool.Null;
import eu.infolead.jtk.logic.NullableBool.True;
import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This represents a three-valued boolean, which accepts the values {@link NullableBool#TRUE},
 * {@link NullableBool#FALSE} and {@link NullableBool#NULL}.
 *
 * <h2>Notes</h2>
 *
 * <ol>
 *   <li>The API of this class SHOULD always consume and return boxed {@link Boolean} instances
 *       instead of unboxed {@code boolean} instances: only the boxed ones are able to represent the
 *       three states {@link Boolean#TRUE}, {@link Boolean#FALSE} and {@code null}.
 * </ol>
 */
public abstract sealed class NullableBool implements Serializable permits True, False, Null {
    private static final long serialVersionUID = 1L;

    public static final NullableBool TRUE = new True();
    public static final NullableBool FALSE = new False();
    public static final NullableBool NULL = new Null();

    /**
     * Converts a two-valued boolean to a three-valued boolean.
     *
     * @param bool the boolean to convert; {@code null} maps to {@link #NULL}
     * @return {@link #TRUE}, {@link #FALSE}, or {@link #NULL}
     */
    public static NullableBool of(@Nullable final Bool bool) {
        if (bool == null) {
            return NULL;
        }
        return bool.fold(() -> FALSE, () -> TRUE);
    }

    /**
     * Converts a boxed boolean to a three-valued boolean.
     *
     * @param value the value to convert; {@code null} maps to {@link #NULL}
     * @return the corresponding three-valued boolean
     */
    public static NullableBool ofNullable(@Nullable final Boolean value) {
        if (value == null) {
            return NULL;
        }
        return value ? TRUE : FALSE;
    }

    public abstract NullableBool apply(
            Runnable falseAction, Runnable nullAction, Runnable trueAction);

    /**
     * Runs the consumer when this value is {@link #TRUE}.
     *
     * @param trueConsumer the consumer to run; ignored for other values
     * @return this
     */
    public abstract NullableBool ifTrue(Consumer<Bool> trueConsumer);

    /**
     * Runs the consumer when this value is {@link #FALSE}.
     *
     * @param falseConsumer the consumer to run; ignored for other values
     * @return this
     */
    public abstract NullableBool ifFalse(Consumer<Bool> falseConsumer);

    /**
     * Runs the consumer when this value is {@link #NULL}. The consumer receives the {@link #NULL}
     * singleton (never a Java {@code null}).
     *
     * @param nullConsumer the consumer to run; ignored for other values
     * @return this
     */
    public abstract NullableBool ifNull(Consumer<NullableBool> nullConsumer);

    public abstract <R> R fold(
            Supplier<R> falseSupplier, Supplier<R> nullSupplier, Supplier<R> trueSupplier);

    @Nullable
    public abstract Boolean toBoolean();

    static final class True extends NullableBool {
        private static final long serialVersionUID = 1L;

        private True() {}

        @Override
        public NullableBool apply(
                final Runnable falseAction, final Runnable nullAction, final Runnable trueAction) {
            Objects.requireNonNull(trueAction, "trueAction").run();
            return this;
        }

        @Override
        public <R> R fold(
                final Supplier<R> falseSupplier,
                final Supplier<R> nullSupplier,
                final Supplier<R> trueSupplier) {
            return Objects.requireNonNull(trueSupplier, "trueSupplier").get();
        }

        @Override
        public Boolean toBoolean() {
            return Boolean.TRUE;
        }

        @Override
        public NullableBool ifTrue(final Consumer<Bool> trueConsumer) {
            Objects.requireNonNull(trueConsumer, "trueConsumer").accept(Bool.TRUE);
            return this;
        }

        @Override
        public NullableBool ifFalse(final Consumer<Bool> falseConsumer) {
            return this;
        }

        @Override
        public NullableBool ifNull(final Consumer<NullableBool> nullConsumer) {
            return this;
        }

        private Object readResolve() {
            return TRUE;
        }
    }

    static final class False extends NullableBool {
        private static final long serialVersionUID = 1L;

        private False() {}

        @Override
        public NullableBool apply(
                final Runnable falseAction, final Runnable nullAction, final Runnable trueAction) {
            Objects.requireNonNull(falseAction, "falseAction").run();
            return this;
        }

        @Override
        public <R> R fold(
                final Supplier<R> falseSupplier,
                final Supplier<R> nullSupplier,
                final Supplier<R> trueSupplier) {
            return Objects.requireNonNull(falseSupplier, "falseSupplier").get();
        }

        @Override
        public Boolean toBoolean() {
            return Boolean.FALSE;
        }

        @Override
        public NullableBool ifTrue(final Consumer<Bool> trueConsumer) {
            return this;
        }

        @Override
        public NullableBool ifFalse(final Consumer<Bool> falseConsumer) {
            Objects.requireNonNull(falseConsumer, "falseConsumer").accept(Bool.FALSE);
            return this;
        }

        @Override
        public NullableBool ifNull(final Consumer<NullableBool> nullConsumer) {
            return this;
        }

        private Object readResolve() {
            return FALSE;
        }
    }

    static final class Null extends NullableBool {
        private static final long serialVersionUID = 1L;

        private Null() {}

        @Override
        public NullableBool apply(
                final Runnable falseAction, final Runnable nullAction, final Runnable trueAction) {
            Objects.requireNonNull(nullAction, "nullAction").run();
            return this;
        }

        @Override
        public <R> R fold(
                final Supplier<R> falseSupplier,
                final Supplier<R> nullSupplier,
                final Supplier<R> trueSupplier) {
            return Objects.requireNonNull(nullSupplier, "nullSupplier").get();
        }

        @Override
        @Nullable
        @SuppressWarnings(SonarLintWarning.JAVA_S2447)
        public Boolean toBoolean() {
            return null;
        }

        @Override
        public NullableBool ifTrue(final Consumer<Bool> trueConsumer) {
            return this;
        }

        @Override
        public NullableBool ifFalse(final Consumer<Bool> falseConsumer) {
            return this;
        }

        @Override
        public NullableBool ifNull(final Consumer<NullableBool> nullConsumer) {
            Objects.requireNonNull(nullConsumer, "nullConsumer").accept(this);
            return this;
        }

        private Object readResolve() {
            return NULL;
        }
    }
}
