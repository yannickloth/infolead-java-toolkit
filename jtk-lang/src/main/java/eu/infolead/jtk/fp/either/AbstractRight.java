package eu.infolead.jtk.fp.either;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;
import java.util.function.Consumer;

abstract sealed class AbstractRight<L, R> extends AbstractEither<L, R, R>
        permits Right, Maybe.Some, Validation.Valid, Result.EmptySuccess, Result.Success {
    private static final long serialVersionUID = 1L;

    AbstractRight() {
        super();
    }

    AbstractRight(final R value) {
        super(value);
    }

    /**
     * Exposes the payload under the {@code right} JSON member.
     *
     * <p>This is the serialization-only contract; the value is restored through the {@code
     * fromJson} creators. See {@link Either} for the envelope rules.
     *
     * @return the right value
     */
    @JsonProperty("right")
    public final R rightValue() {
        return value();
    }

    @Override
    public final Bool isRight() {
        return Bool.TRUE;
    }

    @Override
    public <U> U fold(
            final Mapper<? extends U, ? super L> leftMapper,
            final Mapper<? extends U, ? super R> rightMapper) {
        return rightMapper.apply(value());
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    @Override
    public <E extends Either<L, R>> E apply(
            final Consumer<? super L> leftConsumer, final Consumer<? super R> rightConsumer) {
        rightConsumer.accept(value());
        return (E) this;
    }
}
