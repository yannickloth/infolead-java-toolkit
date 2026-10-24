package eu.infolead.jtk.fp.either;

import eu.infolead.jtk.logic.Bool;

sealed abstract class AbstractEither<L, R, X> extends ValueContainer<X> implements Either<L, R>
        permits AbstractLeft, AbstractRight {
    private static final long serialVersionUID = 1L;

    AbstractEither() {
        super();
    }

    AbstractEither(final X value) {
        super(value);
    }

    @Override
    public final Bool isLeft() {
        return isRight().negate();
    }
}
