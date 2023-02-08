package eu.infolead.jtk.fp;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

record None<T>() implements Maybe<T>, Serializable {
    private static final long serialVersionUID = -1L;
    static final None<?> INSTANCE = new None<>();

    @Override
    public <U> U fold(final Supplier<? extends U> emptyMapper,
            final Mapper<? extends U, ? super T> presentMapper) {
        return emptyMapper.get();
    }

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