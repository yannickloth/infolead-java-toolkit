package eu.infolead.jtk.fp.either;

import java.util.function.Consumer;

import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;

sealed abstract class AbstractRight<L, R> extends AbstractEither<L, R, R>
        permits Right, Maybe.Some, Validation.Valid, Result.EmptySuccess, Result.Success {
    /**
     * The non-serializable super class of a "Serializable" class (e.g. {@link Some}
     * and {@link None}) should have a no-argument constructor
     * <p>
     * Cf. <a target="_blank" href=
     * "https://rules.sonarsource.com/java/RSPEC-2055">https://rules.sonarsource.com/java/RSPEC-2055</a>
     */
    AbstractRight() {
        super();
    }

    AbstractRight(final R value) {
        super(value);
    }

    @Override
    public final Bool isRight() {
        return Bool.TRUE;
    }

    @Override
    public <U> U fold(final Mapper<? extends U, ? super L> leftMapper,
            final Mapper<? extends U, ? super R> rightMapper) {
        return rightMapper.apply(value());
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    @Override
    public <E extends Either<L, R>> E apply(final Consumer<? super L> leftConsumer,
            final Consumer<? super R> rightConsumer) {
        rightConsumer.accept(value());
        return (E) this;
    }
}
