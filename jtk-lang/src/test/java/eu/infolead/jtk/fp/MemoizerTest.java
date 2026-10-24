package eu.infolead.jtk.fp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.either.Maybe;

class MemoizerTest {

    private final Memoizer memoizer = new ThreadSafeRecursionSafeMemoizer();

    @Test
    void memoizesSequentialCalls() {
        final AtomicInteger calls = new AtomicInteger();
        final Function<Integer, Integer> memoized = memoizer.memoize(Maybe.none(), value -> {
            calls.incrementAndGet();
            return value * 2;
        });

        assertEquals(4, memoized.apply(2));
        assertEquals(4, memoized.apply(2));
        assertEquals(1, calls.get());
    }

    @Test
    void doesNotCacheNullResults() {
        final AtomicInteger calls = new AtomicInteger();
        final Function<Integer, Object> memoized = memoizer.memoize(Maybe.none(), value -> {
            calls.incrementAndGet();
            return null;
        });

        assertNull(memoized.apply(1));
        assertNull(memoized.apply(1));
        assertEquals(2, calls.get());
    }

    @Test
    void supportsRecursionAcrossDifferentKeys() {
        final AtomicReference<Function<Integer, Integer>> reference = new AtomicReference<>();
        final Function<Integer, Integer> factorial = memoizer.memoize(Maybe.none(),
                value -> value == 0 ? 1 : value * reference.get().apply(value - 1));
        reference.set(factorial);

        assertEquals(120, factorial.apply(5));
    }

    @Test
    void rejectsRecursionOnTheSameKey() {
        final AtomicReference<Function<Integer, Integer>> reference = new AtomicReference<>();
        final Function<Integer, Integer> recursive = memoizer.memoize(Maybe.none(),
                value -> reference.get().apply(value));
        reference.set(recursive);

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> recursive.apply(1));
        assertTrue(exception.getMessage().contains("Recursive memoization"));
    }
}
