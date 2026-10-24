package eu.infolead.jtk.fp.either;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;
import java.util.function.Consumer;

abstract sealed class AbstractLeft<L, R> extends AbstractEither<L, R, L>
        permits Left, Validation.Invalid, Maybe.None, Result.Failure {
    private static final long serialVersionUID = 1L;

    AbstractLeft() {
        super();
    }

    AbstractLeft(final L value) {
        super(value);
    }

    /**
     * Exposes the payload under the {@code left} JSON member.
     *
     * <p>This is the serialization-only contract; the value is restored through the {@code
     * fromJson} creators. See {@link Either} for the envelope rules.
     *
     * @return the left value
     */
    @JsonProperty("left")
    public final L leftValue() {
        return value();
    }

    @Override
    public final Bool isRight() {
        return Bool.FALSE;
    }

    @Override
    public <U> U fold(
            final Mapper<? extends U, ? super L> leftMapper,
            final Mapper<? extends U, ? super R> rightMapper) {
        return leftMapper.apply(value());
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    @Override
    public <E extends Either<L, R>> E apply(
            final Consumer<? super L> leftConsumer, final Consumer<? super R> rightConsumer) {
        leftConsumer.accept(value());
        return (E) this;
    }
}
