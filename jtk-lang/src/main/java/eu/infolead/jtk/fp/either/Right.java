package eu.infolead.jtk.fp.either;

import eu.infolead.jtk.lang.CompilerWarning;

final class Right<L, R> extends AbstractRight<L, R> {
    Right() {
        super();
    }

    Right(final R value) {
        super(value);
    }

    @Override
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    public Left<R, L> swap() {
        return new Left<>(value());
    }
}