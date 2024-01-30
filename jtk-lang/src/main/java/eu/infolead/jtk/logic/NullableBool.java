package eu.infolead.jtk.logic;

import eu.infolead.jtk.lang.SonarLintWarning;
import eu.infolead.jtk.logic.NullableBool.False;
import eu.infolead.jtk.logic.NullableBool.Null;
import eu.infolead.jtk.logic.NullableBool.True;
import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This represents a three-valued boolean, which accepts the values {@link NullableBool.True},
 * {@link NullableBool.False} and {@link NullableBool.Null}.
 *
 * <h3>Notes</h3>
 *
 * <ol>
 *   <li>The API of this class SHOULD always consume and return boxed {@link Boolean} instances
 *       instead of unboxed {@code boolean} instances: only the boxed ones are able to represent the
 *       three states {@link Boolean#TRUE}, {@link Boolean#FALSE} and {@code null}.
 * </ol>
 */
public abstract sealed class NullableBool implements Serializable permits True, False, Null {
  static final True TRUE = new True();
  static final False FALSE = new False();
  static final Null NULL = new Null();

  public static NullableBool of(final Bool bool) {
    return bool.fold(() -> NullableBool.FALSE, () -> NullableBool.TRUE);
  }

  public abstract NullableBool apply(
      Runnable falseAction, Runnable nullAction, Runnable trueAction);

  public NullableBool ifTrue(
      @SuppressWarnings(SonarLintWarning.JAVA_S1172) final Consumer<Bool> trueConsumer) {
    return this;
  }

  public NullableBool ifFalse(
      @SuppressWarnings(SonarLintWarning.JAVA_S1172) final Consumer<Bool> falseConsumer) {
    return this;
  }

  public NullableBool ifNull(
      @SuppressWarnings(SonarLintWarning.JAVA_S1172) final Consumer<Bool> nullConsumer) {
    return this;
  }

  public abstract <R> R fold(
      Supplier<R> falseSupplier, Supplier<R> nullSupplier, Supplier<R> trueSupplier);

  @Nullable
  public abstract Boolean toBoolean();

  static final class True extends NullableBool {

    @Override
    public NullableBool apply(
        final Runnable falseAction, final Runnable nullAction, final Runnable trueAction) {
      trueAction.run();
      return this;
    }

    @Override
    public <R> R fold(
        final Supplier<R> falseSupplier,
        final Supplier<R> nullSupplier,
        final Supplier<R> trueSupplier) {
      return trueSupplier.get();
    }

    @Override
    public Boolean toBoolean() {
      return Boolean.TRUE;
    }

    @Override
    public NullableBool ifTrue(final Consumer<Bool> trueConsumer) {
      trueConsumer.accept(Bool.TRUE);
      return this;
    }
  }

  static final class False extends NullableBool {

    @Override
    public NullableBool apply(
        final Runnable falseAction, final Runnable nullAction, final Runnable trueAction) {
      falseAction.run();
      return this;
    }

    @Override
    public <R> R fold(
        final Supplier<R> falseSupplier,
        final Supplier<R> nullSupplier,
        final Supplier<R> trueSupplier) {
      return falseSupplier.get();
    }

    @Override
    public Boolean toBoolean() {
      return Boolean.FALSE;
    }

    @Override
    public NullableBool ifFalse(final Consumer<Bool> falseConsumer) {
      falseConsumer.accept(Bool.FALSE);
      return this;
    }
  }

  static final class Null extends NullableBool {

    @Override
    public NullableBool apply(
        final Runnable falseAction, final Runnable nullAction, final Runnable trueAction) {
      nullAction.run();
      return this;
    }

    @Override
    public <R> R fold(
        final Supplier<R> falseSupplier,
        final Supplier<R> nullSupplier,
        final Supplier<R> trueSupplier) {
      return nullSupplier.get();
    }

    @Override
    @Nullable
    @SuppressWarnings(SonarLintWarning.JAVA_S2447)
    public Boolean toBoolean() {
      return null;
    }

    @Override
    public NullableBool ifNull(final Consumer<Bool> nullConsumer) {
      nullConsumer.accept(null);
      return this;
    }
  }
}
