package eu.infolead.jtk.fp.either;

import eu.infolead.jtk.lang.CompilerWarning;

final class Left<L, R> extends AbstractLeft<L, R> {
    Left() {
        super();
    }

    Left(final L value) {
        super(value);
    }

    @Override
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    public Right<R, L> swap() {
        return new Right<>(value());
    }
}