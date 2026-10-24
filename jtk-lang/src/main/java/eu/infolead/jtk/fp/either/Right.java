package eu.infolead.jtk.fp.either;

final class Right<L, R> extends AbstractRight<L, R> {
    private static final long serialVersionUID = 1L;

    Right(final R value) {
        super(value);
    }

    @Override
    public Left<R, L> swap() {
        return new Left<>(value());
    }
}
