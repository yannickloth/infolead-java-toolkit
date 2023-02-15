package eu.infolead.jtk.fp;

import java.io.Serializable;
import java.util.function.Consumer;

class None<T> extends Either.Left<Void, T> implements Maybe<T>, Serializable {
    private static final long serialVersionUID = -1L;
    static final None<?> INSTANCE = new None<>();

    @Override
    public Maybe<T> apply(final Runnable emptyAction, final Consumer<? super T> presentConsumer) {
        emptyAction.run();
        return this;
    }

    @Override
    public String toString() {
        return "{}";
    }
}