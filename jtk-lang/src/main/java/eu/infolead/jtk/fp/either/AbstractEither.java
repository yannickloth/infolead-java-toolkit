package eu.infolead.jtk.fp.either;

import eu.infolead.jtk.logic.Bool;

sealed abstract class AbstractEither<L, R, X> extends ValueContainer<X> implements Either<L, R>
        permits AbstractLeft, AbstractRight {
    /**
     * The non-serializable super class of a "Serializable" class (e.g. {@link Some}
     * and {@link None}) should have a no-argument constructor
     * <p>
     * Cf. <a target="_blank" href=
     * "https://rules.sonarsource.com/java/RSPEC-2055">https://rules.sonarsource.com/java/RSPEC-2055</a>
     */
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
