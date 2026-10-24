package eu.infolead.jtk.fp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Result;

class ContractsTest {

    @Test
    void postconditionFailureKeepsANullResultWithoutThrowing() {
        final Either<SystemError, Object> result = Contracts.checkPostcondition(null, false, "expected state");

        assertTrue(result.isLeft().toBoolean());
        result.fold(error -> {
            assertTrue(error.getMessage().contains("expected state"));
            return null;
        }, value -> {
            throw new AssertionError("expected a failure");
        });
    }

    @Test
    void postconditionFailurePreservesTheActualValue() {
        final Either<SystemError, Integer> result = Contracts.checkPostcondition(42, false, "expected state");

        assertTrue(result.isLeft().toBoolean());
        result.fold(error -> {
            assertTrue(error.getActualValue().isPresent().toBoolean());
            return null;
        }, value -> {
            throw new AssertionError("expected a failure");
        });
    }

    @Test
    void withPostconditionConvertsANullResultToAFailure() {
        final Result<SystemError, Object> result = Contracts.withPostcondition(
                () -> null, Objects::nonNull, "non-null result");

        assertTrue(result.isFailure().toBoolean());
    }

    @Test
    void withPostconditionConvertsPredicateExceptionsToFailures() {
        final Result<SystemError, String> result = Contracts.withPostcondition(
                () -> "value",
                value -> {
                    throw new IllegalStateException("predicate exploded");
                },
                "valid value");

        assertFalse(result.isSuccess().toBoolean());
        result.fold(error -> {
            assertTrue(error.getMessage().contains("predicate exploded"));
            return null;
        }, value -> {
            throw new AssertionError("expected a failure");
        });
    }
}
