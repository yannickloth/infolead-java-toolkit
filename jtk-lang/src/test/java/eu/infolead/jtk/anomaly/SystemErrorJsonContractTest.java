package eu.infolead.jtk.anomaly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Result;
import eu.infolead.jtk.fp.either.Validation;
import eu.infolead.jtk.fp.validation.ValidationError;
import eu.infolead.jtk.fp.validation.ValidationResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pins the error wire contract and the session-replication contract.
 *
 * <p>Two independent contracts are asserted here on purpose: the JSON shape (RFC 9457-adjacent,
 * self-describing through the error type key) and plain {@link Serializable} round-trips, which is
 * what a Servlet container needs when it replicates a web session.
 */
class SystemErrorJsonContractTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void errorJsonIsSelfDescribingAndRoundTrips() {
        final ParameterError error = ParameterError.invalidFormat("password", "hunter2");

        final String json = mapper.writeValueAsString(error);
        assertTrue(json.contains("\"kind\":\"parameter\""), json);
        assertTrue(
                json.contains("\"errorType\":\"error.validation.parameter.invalid-format\""), json);
        assertFalse(json.contains("hunter2"), json);

        final SystemError parsed = mapper.readValue(json, SystemError.class);
        assertInstanceOf(ParameterError.class, parsed);
        assertEquals(error.getErrorType(), parsed.getErrorType());
        assertEquals(error.getMessage(), parsed.getMessage());
        assertEquals(Maybe.none(), parsed.getActualValue());
        assertEquals(Maybe.none(), parsed.getErrorId());
    }

    @Test
    void everyErrorKindRoundTripsThroughItsDiscriminator() {
        final List<SystemError> errors =
                List.of(
                        ParameterError.nullParameter("id"),
                        PreconditionError.systemNotReady("maintenance"),
                        PostconditionError.sideEffectFailed("email"),
                        BusinessRuleError.ageRestriction(18, 16),
                        AuthorizationError.insufficientPermissions("orders:write"),
                        ResourceError.notFound("User", 7),
                        InvariantError.dataCorruption("balance"));

        for (final SystemError error : errors) {
            final String json = mapper.writeValueAsString(error);
            final SystemError parsed = mapper.readValue(json, SystemError.class);
            assertEquals(error.getClass(), parsed.getClass(), json);
            assertEquals(error.getMessage(), parsed.getMessage(), json);
        }
    }

    @Test
    void operationalMetadataIsNotPartOfTheWireContract() {
        final SystemError error =
                ResourceError.notFound("User", 7)
                        .withCause(new IOException("disk"))
                        .withErrorId("err-1")
                        .withCorrelationId("corr-1")
                        .withRedaction(RedactionPolicy.standard());

        final String json = mapper.writeValueAsString(error);
        assertFalse(json.contains("corr-1"), json);
        assertFalse(json.contains("disk"), json);

        final SystemError parsed = mapper.readValue(json, SystemError.class);
        assertInstanceOf(ContextualSystemError.class, parsed);
        final ContextualSystemError contextual = (ContextualSystemError) parsed;
        assertEquals(ResourceError.class, contextual.delegate().getClass());
        assertSame(RedactionPolicy.NONE, contextual.redaction());
        assertEquals(Maybe.none(), contextual.getCause());
        assertEquals(ErrorContext.empty(), contextual.getErrorContext());
    }

    @Test
    void errorTypeIsIdentifiedByItsStableMessageKey() {
        final String json = mapper.writeValueAsString(ParameterErrorType.NULL_PARAMETER);
        assertEquals("\"error.validation.parameter.null-parameter\"", json);
        assertSame(
                ParameterErrorType.NULL_PARAMETER,
                mapper.readValue(json, ParameterErrorType.class));
        assertSame(ParameterErrorType.NULL_PARAMETER, mapper.readValue(json, ErrorType.class));

        final String standard = mapper.writeValueAsString(StandardErrorType.REQUIRED);
        assertSame(StandardErrorType.REQUIRED, mapper.readValue(standard, ErrorType.class));
    }

    @Test
    void eitherHierarchySurvivesJavaSerialization() throws Exception {
        assertEquals(Maybe.of("value"), roundTrip(Maybe.of("value")));
        assertSame(Maybe.none(), roundTrip(Maybe.none()));

        final Result<SystemError, Integer> success = Result.success(7);
        assertEquals(Result.success(7), roundTrip(success));
        assertSame(Result.success(), roundTrip(Result.success()));

        final Validation<String, Integer> invalid = Validation.invalid("bad");
        assertEquals(Validation.invalid("bad"), roundTrip(invalid));

        final ValidationResult<Integer> failure =
                ValidationResult.failure(List.of(ValidationError.required("email")));
        assertEquals(failure, roundTrip(failure));
    }

    @Test
    void errorsAndTheirDecoratorsSurviveJavaSerialization() throws Exception {
        final SystemError plain = ParameterError.nullParameter("id");
        final SystemError plainBack = roundTrip(plain);
        assertEquals(plain, plainBack);

        final SystemError decorated =
                ResourceError.notFound("User", 7)
                        .withCause(new IOException("disk"))
                        .withSuppressedException(new IllegalStateException("retry"))
                        .withErrorId("err-1")
                        .withCorrelationId("corr-1")
                        .withTenantId("tenant-a")
                        .withRedaction(RedactionPolicy.standard())
                        .withRetryAfter(Duration.ofSeconds(5));

        final SystemError back = roundTrip(decorated);
        assertInstanceOf(ContextualSystemError.class, back);
        final ContextualSystemError contextual = (ContextualSystemError) back;
        assertEquals(ResourceError.class, contextual.delegate().getClass());
        assertEquals("err-1", contextual.getErrorId().orNull());
        assertEquals("corr-1", contextual.getErrorContext().correlationId().orNull());
        assertEquals("tenant-a", contextual.getErrorContext().tenantId().orNull());
        assertEquals("disk", contextual.getCause().orNull().getMessage());
        assertEquals(1, contextual.getSuppressed().size());
        assertEquals(Maybe.of(Duration.ofSeconds(5)), contextual.retryAfter());
        assertEquals(ErrorContext.class, contextual.getErrorContext().getClass());

        final Result<SystemError, Integer> inResult = Result.failure(decorated);
        final Result<SystemError, Integer> resultBack = roundTrip(inResult);
        assertEquals(
                ResourceError.class,
                resultBack.fold(
                        error -> ((ContextualSystemError) error).delegate().getClass(),
                        value -> value.getClass()));
    }

    @Test
    void contextAndPolicyAreSerializableValueObjects() throws Exception {
        final ErrorContext context =
                ErrorContext.empty()
                        .withCorrelationId("corr-1")
                        .withTraceId("trace-1")
                        .withRequestId("req-1")
                        .withTenantId("tenant-1");
        assertEquals(context, roundTrip(context));
        final RedactionPolicy policy = roundTrip(RedactionPolicy.STANDARD);
        assertEquals(Sensitive.REDACTED, policy.redact(Sensitive.of("secret")));
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(final T value) throws Exception {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in =
                new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }
}
