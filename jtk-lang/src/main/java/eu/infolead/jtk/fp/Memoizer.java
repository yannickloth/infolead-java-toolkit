package eu.infolead.jtk.fp;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import eu.infolead.jtk.fp.either.Maybe;
import jakarta.annotation.Nonnull;

public interface Memoizer {
    <I, O> Function<I, O> memoize(@Nonnull Maybe<Supplier<Map<I, O>>> mapSupplier,
            @Nonnull Function<I, O> f);
}
