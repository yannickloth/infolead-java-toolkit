package eu.infolead.jtk.fp.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.anomaly.StandardErrorType;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Validation;

class ValidationResultTest {

    private static final ValidationError NAME_REQUIRED = ValidationError.required("name");
    private static final ValidationError EMAIL_REQUIRED = ValidationError.required("email");
    private static final ValidationError SERVICE_DOWN = new ValidationError(
            "service", StandardErrorType.SERVICE_UNAVAILABLE, new Object[0], Maybe.none(), Maybe.none());

    @Test
    void successAndFailureFactoriesBehaveConsistently() {
        final ValidationResult<String> success = ValidationResult.success("value");
        assertTrue(success.isSuccess().toBoolean());
        assertFalse(success.isFailure().toBoolean());
        assertEquals(Maybe.of("value"), success.getValue());
        assertTrue(success.getErrors().isEmpty());

        final ValidationResult<String> failure = ValidationResult.failure(NAME_REQUIRED);
        assertTrue(failure.isFailure().toBoolean());
        assertEquals(Maybe.none(), failure.getValue());
        assertEquals(List.of(NAME_REQUIRED), failure.getErrors());

        assertThrows(IllegalArgumentException.class,
                () -> ValidationResult.failure(List.of()));
        assertThrows(NullPointerException.class,
                () -> ValidationResult.failure((ValidationError) null));
    }

    @Test
    void failureErrorListIsDefensivelyCopied() {
        final ValidationResult<String> failure = ValidationResult.failure(NAME_REQUIRED);
        failure.getErrors().clear();
        assertEquals(1, failure.getErrors().size());
    }

    @Test
    void mapAndFlatMapShortCircuitOnFailure() {
        final AtomicBoolean called = new AtomicBoolean();
        final ValidationResult<String> failure = ValidationResult.failure(NAME_REQUIRED);

        assertTrue(failure.map(value -> {
            called.set(true);
            return value;
        }).isFailure().toBoolean());
        assertFalse(called.get());

        assertTrue(failure.flatMap(value -> {
            called.set(true);
            return ValidationResult.success(value);
        }).isFailure().toBoolean());
        assertFalse(called.get());
    }

    @Test
    void combineAccumulatesBothValuesAndErrors() {
        final ValidationResult<String> success = ValidationResult.success("a");
        final ValidationResult<Integer> otherSuccess = ValidationResult.success(2);
        final ValidationResult<String> combined = success.combine(
                otherSuccess, left -> right -> left + right);
        assertEquals(ValidationResult.success("a2"), combined);

        final ValidationResult<String> firstFailure = ValidationResult.failure(NAME_REQUIRED);
        final ValidationResult<Integer> secondFailure = ValidationResult.failure(EMAIL_REQUIRED);

        assertEquals(1, success.combine(secondFailure, a -> b -> a).getErrors().size());
        assertEquals(1, firstFailure.combine(otherSuccess, a -> b -> a).getErrors().size());
        assertEquals(2, firstFailure.combine(secondFailure, a -> b -> a).getErrors().size());
    }

    @Test
    void validateThatAndValidateRespectExistingFailures() {
        final ValidationResult<String> success = ValidationResult.success("value");
        final ValidationResult<String> failed = success.validateThat(
                value -> false, NAME_REQUIRED);
        assertEquals(ValidationResult.failure(NAME_REQUIRED), failed);

        final ValidationResult<String> unchanged = failed.validate(
                value -> ValidationResult.success("changed"));
        assertEquals(failed, unchanged);
    }

    @Test
    void convertsToEitherAndValidation() {
        final Either<String, String> success = ValidationResult.<String>success("value").toEither();
        assertTrue(success.isRight().toBoolean());
        assertEquals("value", success.orNull());

        final Either<String, String> failure = ValidationResult.<String>failure(
                NAME_REQUIRED, EMAIL_REQUIRED).toEither();
        assertTrue(failure.isLeft().toBoolean());
        final String message = failure.fold(error -> error, value -> "");
        assertTrue(message.contains("name"));
        assertTrue(message.contains("email"));

        assertTrue(ValidationResult.success("value").toValidation().isValid().toBoolean());
        assertEquals(NAME_REQUIRED,
                ValidationResult.failure(NAME_REQUIRED, EMAIL_REQUIRED).toValidation()
                        .fold(error -> error, value -> null));
    }

    @Test
    void toProblemDetailAggregatesErrorsAndPreservesStatus() {
        final ValidationResult<String> failure = ValidationResult.failure(
                NAME_REQUIRED, EMAIL_REQUIRED);
        final ProblemDetail detail = failure.toProblemDetail();

        assertEquals(ValidationResult.DEFAULT_PROBLEM_TYPE, detail.getProblemType());
        assertEquals("Validation Error", detail.getTitle());
        assertEquals(400, detail.getStatusValue());
        assertTrue(detail.getDetail().contains("name"));
        assertTrue(detail.getDetail().contains("email"));
        assertTrue(detail.isValid());

        final ProblemDetail fromErrorType = failure.toProblemDetailFromErrorType();
        assertEquals(NAME_REQUIRED.errorType().getProblemType(), fromErrorType.getProblemType());
        assertEquals(400, fromErrorType.getStatusValue());

        assertThrows(IllegalStateException.class,
                () -> ValidationResult.success("value").toProblemDetail());
        assertThrows(IllegalArgumentException.class,
                () -> failure.toProblemDetail(URI.create("about:blank"), "Invalid", 99));
    }

    @Test
    void groupsErrorsAndClassifiesClientAndServerErrors() {
        final ValidationResult<String> mixed = ValidationResult.failure(
                NAME_REQUIRED, SERVICE_DOWN);
        final Map<Integer, List<ValidationError>> groups = mixed.groupByHttpStatus();

        assertEquals(2, groups.size());
        assertTrue(groups.containsKey(400));
        assertTrue(groups.containsKey(503));

        assertTrue(ValidationResult.failure(NAME_REQUIRED, EMAIL_REQUIRED).areAllClientErrors());
        assertFalse(mixed.areAllClientErrors());
        assertTrue(mixed.hasServerErrors());
        assertFalse(ValidationResult.failure(NAME_REQUIRED).hasServerErrors());
        assertFalse(ValidationResult.success("value").areAllClientErrors());
        assertFalse(ValidationResult.success("value").hasServerErrors());
    }

    @Test
    void sequenceAndValidateAllAccumulateErrors() {
        final ValidationResult<List<String>> allSuccess = ValidationResult.sequence(
                List.of(ValidationResult.success("a"), ValidationResult.success("b")));
        assertEquals(ValidationResult.success(List.of("a", "b")), allSuccess);

        final ValidationResult<List<String>> oneFailure = ValidationResult.sequence(
                List.of(ValidationResult.success("a"), ValidationResult.failure(NAME_REQUIRED)));
        assertEquals(1, oneFailure.getErrors().size());
        assertTrue(ValidationResult.sequence(List.<ValidationResult<String>>of())
                .isSuccess().toBoolean());

        final ValidationResult<List<String>> validated = ValidationResult.validateAll(
                List.of("alpha", "beta", "gamma"),
                value -> value.startsWith("a")
                        ? ValidationResult.success(value)
                        : ValidationResult.failure(ValidationError.required(value)));
        assertEquals(2, validated.getErrors().size());
    }

    @Test
    void convertsFromSimpleValidation() {
        final Validation<ValidationError, String> valid = Validation.valid("value");
        final Validation<ValidationError, String> invalid = Validation.invalid(NAME_REQUIRED);

        assertEquals(ValidationResult.success("value"),
                ValidationResult.from(valid, error -> error));
        assertEquals(ValidationResult.failure(NAME_REQUIRED),
                ValidationResult.from(invalid, error -> error));
    }
}
