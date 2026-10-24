package eu.infolead.jtk.fp.either;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

final class Right<L, R> extends AbstractRight<L, R> {
    private static final long serialVersionUID = 1L;

    @JsonCreator
    Right(@JsonProperty("right") final R value) {
        super(value);
    }

    @Override
    public Left<R, L> swap() {
        return new Left<>(value());
    }
}
