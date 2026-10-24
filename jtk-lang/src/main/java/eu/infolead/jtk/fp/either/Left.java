package eu.infolead.jtk.fp.either;

final class Left<L, R> extends AbstractLeft<L, R> {
    private static final long serialVersionUID = 1L;

    Left(final L value) {
        super(value);
    }

    @Override
    public Right<R, L> swap() {
        return new Right<>(value());
    }
}
