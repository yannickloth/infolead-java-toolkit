package eu.infolead.jtk.anomaly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;

class SystemErrorTest {

    @Test
    void parameterErrorsFormatContextAndActualValues() {
        final ParameterError withContext = ParameterError.outOfRange("age", 42, 18, 65);
        assertEquals(Maybe.of(42), withContext.getActualValue());
        assertTrue(withContext.getMessage().contains("18"));
        assertTrue(withContext.getMessage().contains("65"));
        assertFalse(withContext.getMessage().contains("%s"));
        assertTrue(withContext.format().contains("42"));

        final ParameterError withActual = ParameterError.withActualValue(
                "age", ParameterErrorType.OUT_OF_RANGE, 42);
        assertEquals(Maybe.of(42), withActual.getActualValue());
        assertTrue(withActual.getMessage().startsWith("age:"));
        assertFalse(withActual.getMessage().contains("%"));
    }

    @Test
    void errorContextArraysAreDefensivelyCopied() {
        final ParameterError error = ParameterError.outOfRange("age", 42, 18, 65);
        final Object[] first = error.contextArgs();
        final Object[] second = error.contextArgs();
        assertNotSame(first, second);
        first[0] = "mutated";
        assertEquals(18, error.contextArgs()[0]);
        assertEquals(18, error.getContextArgs()[0]);
    }

    @Test
    void recordsUseValueEqualityIncludingContextArrays() {
        assertEquals(
                ParameterError.outOfRange("age", 42, 18, 65),
                ParameterError.outOfRange("age", 42, 18, 65));
        assertEquals(
                ParameterError.outOfRange("age", 42, 18, 65).hashCode(),
                ParameterError.outOfRange("age", 42, 18, 65).hashCode());
        assertFalse(ParameterError.outOfRange("age", 42, 18, 65)
                .equals(ParameterError.outOfRange("age", 43, 18, 65)));
    }

    @Test
    void preconditionAndPostconditionMessagesDoNotLeakRawTemplates() {
        final PreconditionError precondition = PreconditionError.withActualValue(
                "state must be ready", PreconditionErrorType.CONFLICTING_STATE, "busy");
        assertTrue(precondition.getMessage().contains("state must be ready"));
        assertFalse(precondition.getMessage().contains("%s"));
        assertTrue(precondition.format().contains("busy"));

        final PostconditionError postcondition = PostconditionError.withActualValue(
                "value must be positive", PostconditionErrorType.EXPECTED_STATE_NOT_REACHED, -1);
        assertTrue(postcondition.getMessage().contains("value must be positive"));
        assertFalse(postcondition.getMessage().contains("%s"));
        assertTrue(postcondition.format().contains("-1"));
    }

    @Test
    void resourceErrorsTolerateNullIdentifiers() {
        final ResourceError notFound = ResourceError.notFound("User", null);
        assertTrue(notFound.getActualValue().isEmpty().toBoolean());
        assertTrue(notFound.getMessage().contains("User"));
        assertFalse(notFound.getMessage().contains("%s"));

        final ResourceError alreadyExists = ResourceError.alreadyExists("User", null);
        assertTrue(alreadyExists.getMessage().contains("User"));
        assertFalse(alreadyExists.getMessage().contains("%s"));
    }

    @Test
    void systemErrorsMapCategoriesToExceptionTypes() {
        assertTrue(ParameterError.nullParameter("id").toException() instanceof IllegalArgumentException);
        assertTrue(PreconditionError.conflictingState("busy").toException() instanceof IllegalStateException);
        assertTrue(PostconditionError.expectedStateNotReached("ready", "busy").toException()
                instanceof IllegalStateException);
        assertTrue(ResourceError.notFound("User", 1).toException() instanceof IllegalStateException);
        assertTrue(AuthorizationError.notAuthenticated().toException() instanceof SecurityException);
        assertTrue(BusinessRuleError.insufficientFunds("a", 10, 5).toException()
                instanceof IllegalStateException);
        assertTrue(InvariantError.objectConsistency("broken").toException()
                instanceof IllegalStateException);
    }

    @Test
    void generatedAndCustomErrorIdsArePreserved() {
        final ParameterError error = ParameterError.nullParameter("id");
        final SystemError generated = error.withGeneratedId();
        assertTrue(generated.getErrorId().isPresent().toBoolean());
        assertEquals(generated.getErrorId(), generated.withGeneratedId().getErrorId());

        final SystemError custom = error.withErrorId("custom-id");
        assertEquals(Maybe.of("custom-id"), custom.getErrorId());
        assertTrue(custom.toProblemDetail().getDetail().contains("custom-id"));
    }

    @Test
    void problemDetailsExposeTypeStatusAndMessage() {
        final ParameterError error = ParameterError.nullParameter("id");
        final ProblemDetail detail = error.toProblemDetail(Maybe.some(URI.create("/request/1")));

        assertEquals(error.getErrorType().getProblemType(), detail.getProblemType());
        assertEquals(error.getHttpStatus(), detail.getStatusValue());
        assertEquals(error.getMessage(), detail.getDetail());
        assertEquals(URI.create("/request/1"), detail.getInstance());
        assertTrue(detail.isValid());
    }

    @Test
    void renderValueIsBoundedArrayAwareAndSafe() {
        assertEquals("null", SystemError.renderValue(null));
        assertEquals("[1, 2]", SystemError.renderValue(new int[] { 1, 2 }));
        assertEquals("[a, null]", SystemError.renderValue(new Object[] { "a", null }));
        assertEquals("short", SystemError.renderValue("short"));

        final String longValue = "x".repeat(300);
        final String renderedLong = SystemError.renderValue(longValue);
        assertEquals(259, renderedLong.length());

        final Object broken = new Object() {
            @Override
            public String toString() {
                throw new IllegalStateException("boom");
            }
        };
        assertEquals("<unrenderable " + broken.getClass().getName() + ">",
                SystemError.renderValue(broken));

        assertEquals("{password=[REDACTED]}",
                SystemError.renderValue(Map.of("password", "hunter2")));
        assertEquals("[a, [REDACTED]]",
                SystemError.renderValue(List.of("a", Sensitive.of("hunter2"))));
    }

    @Test
    void formatMessageFallsBackToTheTemplateOnBadArguments() {
        assertEquals(ParameterErrorType.TOO_LONG.getMessageTemplate(),
                ParameterErrorType.TOO_LONG.formatMessage("not-a-number"));
        assertEquals(ParameterErrorType.TOO_LONG.getProblemTitle(),
                ParameterErrorType.TOO_LONG.messageWithoutArgs());
    }
}
