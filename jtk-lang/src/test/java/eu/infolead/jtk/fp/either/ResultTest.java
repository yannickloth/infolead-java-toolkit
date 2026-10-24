package eu.infolead.jtk.fp.either;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.Provider;

class ResultTest {

    @Test
    void successRejectsNull() {
        assertThrows(NullPointerException.class, () -> Result.success(null));
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void anyRejectsAnEmptyArray() {
        assertThrows(IllegalArgumentException.class, () -> Result.any(new Result[0]));
    }

    @Test
    void anyEvaluatesTheFirstFailingProviderOnlyOnce() {
        final AtomicInteger calls = new AtomicInteger();
        final Provider<Result<String, Integer>> failing = () -> {
            calls.incrementAndGet();
            return Result.failure("failure");
        };

        final Result<String, Integer> result = Result.<String, Integer>any(
                (Provider<Result<String, Integer>>) failing);

        assertTrue(result.isFailure().toBoolean());
        assertEquals(1, calls.get());
    }

    @Test
    void flatMapSupplierRunsForAnEmptySuccess() {
        final java.util.concurrent.atomic.AtomicBoolean called = new java.util.concurrent.atomic.AtomicBoolean();
        final Result<String, Integer> result = Result.<String, Integer>success()
                .flatMap(() -> {
                    called.set(true);
                    return Result.success(1);
                });
        assertTrue(called.get());
        assertTrue(result.isSuccess().toBoolean());
    }

    @Test
    void mappingAnEmptySuccessKeepsItEmpty() {
        final Result<String, Integer> empty = Result.success();
        final Result<String, String> mapped = empty.map(value -> "unexpected " + value);
        assertTrue(mapped.isSuccess().toBoolean());
    }
}
