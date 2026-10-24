package eu.infolead.jtk.fp.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.either.Result;
import eu.infolead.jtk.fp.either.Validation;

class ValidationApplicativeTest {

    private static final ValidationError NAME_REQUIRED = ValidationError.required("name");
    private static final ValidationError EMAIL_REQUIRED = ValidationError.required("email");

    @Test
    void zipCombinesBothValidValues() {
        final Validation<List<String>, String> combined = Validation.<String, String>valid("Ada")
                .zip(Validation.<String, Integer>valid(36), (name, age) -> name + "/" + age);

        assertTrue(combined.isValid().toBoolean());
        assertEquals("Ada/36", combined.fold(errors -> "", value -> value));
    }

    @Test
    void zipAccumulatesEveryInvalidValue() {
        final Validation<List<String>, String> bothInvalid = Validation.<String, String>invalid("no name")
                .zip(Validation.<String, Integer>invalid("no age"), (name, age) -> name + age);

        assertTrue(bothInvalid.isInvalid().toBoolean());
        assertEquals(List.of("no name", "no age"),
                bothInvalid.fold(errors -> errors, value -> List.of()));

        final Validation<List<String>, String> oneInvalid = Validation.<String, String>valid("Ada")
                .zip(Validation.<String, Integer>invalid("no age"), (name, age) -> name + age);
        assertEquals(List.of("no age"), oneInvalid.fold(errors -> errors, value -> List.of()));
    }

    @Test
    void combineIsAnAliasForZip() {
        final Validation<List<String>, String> combined = Validation.<String, String>invalid("a")
                .combine(Validation.<String, Integer>invalid("b"), (left, right) -> left + right);
        assertEquals(List.of("a", "b"), combined.fold(errors -> errors, value -> List.of()));
    }

    @Test
    void andKeepsTheLeftValueWhenBothAreValid() {
        final Validation<List<String>, String> kept = Validation.<String, String>valid("user")
                .and(Validation.<String, Integer>valid(42));
        assertTrue(kept.isValid().toBoolean());
        assertEquals("user", kept.fold(errors -> "", value -> value));

        final Validation<List<String>, String> failed = Validation.<String, String>valid("user")
                .and(Validation.<String, Integer>invalid("no age"));
        assertEquals(List.of("no age"), failed.fold(errors -> errors, value -> List.of()));
    }

    @Test
    void staticZipAccumulatesValidationErrorsIntoAValidationResult() {
        final ValidationResult<String> bothInvalid = Validation.zip(
                Validation.<ValidationError, String>invalid(NAME_REQUIRED),
                Validation.<ValidationError, String>invalid(EMAIL_REQUIRED),
                (name, email) -> name + email);

        assertTrue(bothInvalid.isFailure().toBoolean());
        assertEquals(List.of(NAME_REQUIRED, EMAIL_REQUIRED), bothInvalid.getErrors());

        final ValidationResult<String> success = Validation.zip(
                Validation.<ValidationError, String>valid("Ada"),
                Validation.<ValidationError, Integer>valid(36),
                (name, age) -> name + "/" + age);
        assertEquals(ValidationResult.success("Ada/36"), success);
    }

    @Test
    void staticCombineAndAndDelegateToZip() {
        final ValidationResult<String> combined = Validation.combine(
                Validation.<ValidationError, String>invalid(NAME_REQUIRED),
                Validation.<ValidationError, String>invalid(EMAIL_REQUIRED),
                (left, right) -> left + right);
        assertEquals(2, combined.getErrors().size());

        final ValidationResult<String> and = Validation.and(
                Validation.<ValidationError, String>valid("user"),
                Validation.<ValidationError, Integer>invalid(ValidationError.required("age")));
        assertEquals(1, and.getErrors().size());

        final ValidationResult<String> kept = Validation.and(
                Validation.<ValidationError, String>valid("user"),
                Validation.<ValidationError, Integer>valid(42));
        assertEquals(ValidationResult.success("user"), kept);
    }

    @Test
    void toResultBridgesToTheRailway() {
        final Result<ValidationError, String> success = ValidationResult.<String>success("value").toResult();
        assertTrue(success.isSuccess().toBoolean());
        assertEquals("value", success.fold(error -> null, value -> value));

        final Result<ValidationError, String> failure = ValidationResult
                .<String>failure(NAME_REQUIRED, EMAIL_REQUIRED)
                .toResult();
        assertTrue(failure.isFailure().toBoolean());
        assertEquals(NAME_REQUIRED, failure.fold(error -> error, value -> null));
    }
}
