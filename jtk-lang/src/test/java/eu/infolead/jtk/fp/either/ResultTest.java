package eu.infolead.jtk.fp.either;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.Fn;
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

    // ====================== EXCEPTION BOUNDARY TESTS ======================

    @Test
    void fromTryReturnsASuccessWhenTheComputationSucceeds() {
        final Result<String, Integer> result = Result.fromTry(
                ex -> "error: " + ex.getMessage(), () -> 42);

        assertTrue(result.isSuccess().toBoolean());
        assertEquals(Integer.valueOf(42), result.orNull());
    }

    @Test
    void fromTryCapturesCheckedExceptionsWithoutTryCatch() {
        final Result<String, Integer> result = Result.fromTry(
                ex -> "error: " + ex.getMessage(),
                () -> {
                    throw new IOException("file missing");
                });

        assertTrue(result.isFailure().toBoolean());
        assertEquals("error: file missing", result.fold(anomaly -> anomaly, value -> null));
    }

    @Test
    void fromTryCapturesRuntimeExceptions() {
        final Result<String, Integer> result = Result.fromTry(
                ex -> "error: " + ex.getMessage(),
                () -> {
                    throw new IllegalStateException("broken");
                });

        assertTrue(result.isFailure().toBoolean());
        assertEquals("error: broken", result.fold(anomaly -> anomaly, value -> null));
    }

    @Test
    void fromTryRejectsNullSuccessValues() {
        assertThrows(NullPointerException.class,
                () -> Result.fromTry(ex -> "error", () -> null));
    }

    @Test
    void fromTryLetsJvmErrorsEscape() {
        // Error vs exception policy: JVM-level errors escape instead of becoming failures.
        final OutOfMemoryError outOfMemoryError = new OutOfMemoryError("jvm exhausted");

        assertSame(outOfMemoryError, assertThrows(OutOfMemoryError.class,
                () -> Result.fromTry(ex -> "error: " + ex.getMessage(), () -> {
                    throw outOfMemoryError;
                })));
    }

    @Test
    void fromTryRunnableReturnsAnEmptySuccess() {
        final AtomicBoolean ran = new AtomicBoolean();
        final Result<String, Void> result = Result.fromTry(ex -> "error", () -> ran.set(true));

        assertTrue(ran.get());
        assertTrue(result.isSuccess().toBoolean());
    }

    @Test
    void fromTryRunnableCapturesExceptions() {
        final Result<String, Void> result = Result.fromTry(
                ex -> "error: " + ex.getMessage(),
                () -> {
                    throw new IOException("send failed");
                });

        assertTrue(result.isFailure().toBoolean());
        assertEquals("error: send failed", result.fold(anomaly -> anomaly, value -> null));
    }

    @Test
    void liftTryBuildsARailwaySwitch() {
        final Fn.FN1<Result<String, Integer>, String> parse = Result.liftTry(
                ex -> "parse failed: " + ex.getMessage(),
                value -> {
                    if ("NaN".equals(value)) {
                        throw new IllegalArgumentException("no digits");
                    }
                    return Integer.parseInt(value);
                });

        assertTrue(parse.apply("42").isSuccess().toBoolean());
        assertEquals(Integer.valueOf(42), parse.apply("42").orNull());

        final Result<String, Integer> failure = parse.apply("NaN");
        assertTrue(failure.isFailure().toBoolean());
        assertEquals("parse failed: no digits", failure.fold(anomaly -> anomaly, value -> null));
    }

    @Test
    void fromTryResolvesValueReturningMethodReferencesToTheSupplierOverload() {
        // Pins the documented overload resolution: a no-arg value-returning method
        // reference must bind to fromTry(CheckedSupplier), not fromTry(CheckedRunnable).
        final AtomicInteger counter = new AtomicInteger();
        final Result<String, Integer> result = Result.fromTry(
                ex -> "error: " + ex.getMessage(), counter::incrementAndGet);

        assertTrue(result.isSuccess().toBoolean());
        assertEquals(Integer.valueOf(1), result.orNull());
    }

    @Test
    void fromTryResolvesValueProducingStatementExpressionsToTheSupplierOverload() {
        // () -> list.add(x) is compatible with both overloads; the compiler must pick
        // the CheckedSupplier overload and keep the produced value as the success.
        final List<String> consumed = new ArrayList<>();
        final Result<String, Boolean> result = Result.fromTry(
                ex -> "error: " + ex.getMessage(), () -> consumed.add("x"));

        assertTrue(result.isSuccess().toBoolean());
        assertEquals(Boolean.TRUE, result.orNull());
        assertEquals(List.of("x"), consumed);
    }
}
