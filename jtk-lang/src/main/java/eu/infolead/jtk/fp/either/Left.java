package eu.infolead.jtk.fp.either;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

final class Left<L, R> extends AbstractLeft<L, R> {
    private static final long serialVersionUID = 1L;

    @JsonCreator
    Left(@JsonProperty("left") final L value) {
        super(value);
    }

    @Override
    public Right<R, L> swap() {
        return new Right<>(value());
    }
}
