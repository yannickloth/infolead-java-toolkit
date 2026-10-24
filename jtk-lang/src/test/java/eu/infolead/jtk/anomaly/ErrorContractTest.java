package eu.infolead.jtk.anomaly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.anomaly.http.StandardProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ErrorContractTest {

    @Test
    void asyncDeadlinesAreRetryableAndCancellationsAreNot() {
        final SystemError timedOut = PostconditionError.operationTimedOut("fetchUser");
        assertEquals(504, timedOut.getHttpStatus());
        assertEquals(PostconditionErrorType.OPERATION_TIMED_OUT, timedOut.getErrorType());
        assertTrue(timedOut.isRetryable());
        assertTrue(timedOut.getMessage().contains("fetchUser"));

        final SystemError cancelled =
                PostconditionError.operationCancelled("fetchUser")
                        .withCause(new java.util.concurrent.CancellationException("caller"));
        assertEquals(408, cancelled.getHttpStatus());
        assertEquals(PostconditionErrorType.OPERATION_CANCELLED, cancelled.getErrorType());
        assertFalse(cancelled.isRetryable());
        assertInstanceOf(
                java.util.concurrent.CancellationException.class,
                cancelled.toException().getCause());

        final SystemError asyncFailure = PostconditionError.asyncOperationFailed("boom");
        assertEquals(500, asyncFailure.getHttpStatus());
        assertEquals(PostconditionErrorType.ASYNC_OPERATION_FAILED, asyncFailure.getErrorType());

        assertEquals(
                "error.external-service.postcondition.operation-timed-out",
                PostconditionErrorType.OPERATION_TIMED_OUT.getMessageKey());
        assertEquals(
                "error.system.postcondition.operation-cancelled",
                PostconditionErrorType.OPERATION_CANCELLED.getMessageKey());
    }

    @Test
    void causeAndSuppressedExceptionsSurviveToException() {
        final IOException cause = new IOException("disk unavailable");
        final IllegalStateException suppressed = new IllegalStateException("retry failed");

        final SystemError error =
                PostconditionError.expectedStateNotReached("READY", "BUSY")
                        .withCause(cause)
                        .withSuppressedException(suppressed);

        final RuntimeException exception = error.toException();
        assertInstanceOf(IllegalStateException.class, exception);
        assertSame(cause, exception.getCause());
        assertEquals(List.of(suppressed), List.of(exception.getSuppressed()));
        assertTrue(exception.getMessage().contains("Expected state not reached"));
    }

    @Test
    void causeCanBeClearedAndSuppressedReplaced() {
        final IllegalStateException first = new IllegalStateException("first");
        final IllegalStateException second = new IllegalStateException("second");
        final SystemError error =
                ResourceError.notFound("User", 1)
                        .withCause(first)
                        .withSuppressed(second)
                        .withCause(null);

        assertTrue(error.getCause().isEmpty().toBoolean());
        assertEquals(List.of(second), error.getSuppressed());
        assertTrue(error.toException().getCause() == null);
        assertEquals(1, error.toException().getSuppressed().length);
    }

    @Test
    void rateLimitsAndUnavailableServicesExposeRetryMetadata() {
        final SystemError rateLimited = PreconditionError.rateLimitExceeded("100/min");
        assertTrue(rateLimited.isRetryable());
        assertEquals(Maybe.of(Duration.ofSeconds(1)), rateLimited.retryAfter());
        assertEquals(Maybe.of("1"), rateLimited.retryAfterHeader());

        final SystemError unavailable =
                PreconditionError.systemNotReady("maintenance")
                        .withCause(new IllegalStateException("down"));
        assertTrue(unavailable.isRetryable());
        assertEquals(
                Maybe.of(Duration.ofSeconds(5)),
                StandardErrorType.SERVICE_UNAVAILABLE.retryAfter());

        final SystemError overridden =
                rateLimited.withRetryAfter(Duration.ofSeconds(30)).withRetryable(false);
        assertEquals(Maybe.of(Duration.ofSeconds(30)), overridden.retryAfter());
        assertFalse(overridden.isRetryable());

        final ProblemDetail detail = rateLimited.toProblemDetail();
        assertTrue(detail.getRetryable().orNull());
        assertEquals(1L, detail.getRetryAfterSeconds().orNull());
    }

    @Test
    void contextIsPropagatedToProblemDetailExtensions() {
        final SystemError error =
                ResourceError.notFound("User", 7)
                        .withGeneratedId()
                        .withCorrelationId("corr-123")
                        .withTraceId("trace-456")
                        .withRequestId("req-789")
                        .withTenantId("tenant-a")
                        .withCause(new IOException("missing"));

        final ProblemDetail detail = error.toProblemDetail(Maybe.some(URI.create("/users/7")));

        assertEquals(URI.create("/users/7"), detail.getInstance());
        assertEquals("corr-123", detail.getCorrelationId().orNull());
        assertEquals("trace-456", detail.getTraceId().orNull());
        assertEquals("req-789", detail.getRequestId().orNull());
        assertEquals("tenant-a", detail.getTenantId().orNull());
        assertTrue(detail.getDetail().contains("error ID"));
        assertTrue(error.getErrorId().isPresent().toBoolean());
        assertSame(IOException.class, error.getCause().orNull().getClass());
    }

    @Test
    void generatedIdsKeepOperationalMetadata() {
        final SystemError error =
                ResourceError.notFound("User", 7)
                        .withCorrelationId("corr-123")
                        .withCause(new IOException("missing"))
                        .withGeneratedId();

        assertTrue(error.getErrorId().isPresent().toBoolean());
        assertEquals("corr-123", error.getErrorContext().correlationId().orNull());
        assertSame(IOException.class, error.getCause().orNull().getClass());
        assertTrue(error.toProblemDetail().getCorrelationId().isPresent().toBoolean());
    }

    @Test
    void sensitiveValuesAreRedactedFromMessagesLogsAndDetails() {
        final ParameterError error =
                new ParameterError(
                        "password",
                        ParameterErrorType.INVALID_COMBINATION,
                        new Object[] {Sensitive.of("hunter2")},
                        Maybe.some(Sensitive.of("hunter2")),
                        Maybe.none());

        assertFalse(error.getMessage().contains("hunter2"));
        assertFalse(error.format().contains("hunter2"));
        assertFalse(error.toString().contains("hunter2"));
        assertTrue(error.getMessage().contains(Sensitive.REDACTED));
        assertTrue(error.format().contains(Sensitive.REDACTED));
        assertFalse(error.toProblemDetail().getDetail().contains("hunter2"));
    }

    @Test
    void sensitiveValuesNeverReachJacksonSerialization() throws Exception {
        final JsonMapper mapper = JsonMapper.builder().build();
        assertEquals("\"[REDACTED]\"", mapper.writeValueAsString(Sensitive.of("hunter2")));

        final ParameterError error =
                ParameterError.invalidFormat("password", Sensitive.of("hunter2"));
        final String json = mapper.writeValueAsString(error);
        assertFalse(json.contains("hunter2"), json);

        final Sensitive parsed = mapper.readValue("\"[REDACTED]\"", Sensitive.class);
        assertEquals(Sensitive.REDACTED, parsed.reveal());
    }

    @Test
    void standardPolicyRedactsCredentialMapEntriesInValuesAndMessages() {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", "alice");
        payload.put("password", "hunter2");
        payload.put("access_token", "token-abc");

        final SystemError error =
                ParameterError.invalidFormat("payload", payload)
                        .withRedaction(RedactionPolicy.standard());

        assertFalse(error.format().contains("hunter2"));
        assertFalse(error.format().contains("token-abc"));
        assertTrue(error.format().contains(Sensitive.REDACTED));
        assertFalse(error.getActualValue().orNull().toString().contains("hunter2"));

        final SystemError withContext =
                new ParameterError(
                                "payload",
                                ParameterErrorType.INVALID_COMBINATION,
                                new Object[] {payload},
                                Maybe.none(),
                                Maybe.none())
                        .withRedaction(RedactionPolicy.standard());
        assertFalse(withContext.getMessage().contains("hunter2"));
        assertFalse(withContext.getMessage().contains("token-abc"));

        final ParameterError plainContext =
                new ParameterError(
                        "payload",
                        ParameterErrorType.INVALID_COMBINATION,
                        new Object[] {payload},
                        Maybe.none(),
                        Maybe.none());
        assertFalse(plainContext.getMessage().contains("hunter2"));
        assertFalse(plainContext.getMessage().contains("token-abc"));
    }

    @Test
    void acceptLanguageResolvesLocalizedTitlesAndMessages() {
        final SystemError error = ParameterError.nullParameter("userId");

        final ProblemDetail french =
                error.toProblemDetail(
                        Maybe.none(), ErrorLocalizer.acceptLanguage("fr-FR,fr;q=0.9,en;q=0.4"));
        assertEquals("Paramètre nul", french.getTitle());
        assertTrue(french.getDetail().startsWith("userId:"));
        assertFalse(french.getDetail().contains("cannot be null"));

        final ProblemDetail german =
                error.toProblemDetail(Maybe.none(), ErrorLocalizer.acceptLanguage("de"));
        assertEquals("Nullparameter", german.getTitle());

        final ProblemDetail english =
                error.toProblemDetail(Maybe.none(), ErrorLocalizer.acceptLanguage("en-GB"));
        assertEquals("Bad Request - Null Parameter", english.getTitle());
    }

    @Test
    void unsupportedOrMalformedAcceptLanguageFallsBackToEnglish() {
        assertEquals(Locale.ENGLISH, ErrorLocalizer.preferredLocale(null));
        assertEquals(Locale.ENGLISH, ErrorLocalizer.preferredLocale(""));
        assertEquals(Locale.ENGLISH, ErrorLocalizer.preferredLocale("zh-CN"));
        assertEquals(Locale.ENGLISH, ErrorLocalizer.preferredLocale("not a locale ;;"));
        assertEquals(Locale.FRENCH, ErrorLocalizer.preferredLocale("fr-CA"));
        assertEquals(Locale.ENGLISH, ErrorLocalizer.preferredLocale("fr;q=0, en;q=0.5"));
    }

    @Test
    void aboutBlankTitlesUseLocalizedHttpReasonPhrases() {
        final StandardProblemDetail aboutBlank =
                StandardProblemDetail.aboutBlank(404, Locale.FRENCH);
        assertEquals("Introuvable", aboutBlank.getTitle());

        final ProblemDetail localized =
                StandardProblemDetail.aboutBlank("Not Found", 404)
                        .withExtensions(
                                Maybe.some("corr"),
                                Maybe.none(),
                                Maybe.none(),
                                Maybe.none(),
                                true,
                                Maybe.of(2L))
                        .localized(Locale.GERMAN);
        assertEquals("Nicht gefunden", localized.getTitle());
        assertEquals("corr", localized.getCorrelationId().orNull());
        assertEquals(2L, localized.getRetryAfterSeconds().orNull());
        assertTrue(localized.getRetryable().orNull());

        final ProblemDetail typed =
                StandardErrorType.REQUIRED.toProblemDetail(
                        Maybe.none(), Maybe.none(), ErrorLocalizer.of(Locale.FRENCH));
        assertEquals("Champ obligatoire manquant", typed.getTitle());
    }

    @Test
    void messageKeysAreStableAndNamespaceAware() {
        assertEquals(
                "error.validation.standard.required", StandardErrorType.REQUIRED.getMessageKey());
        assertEquals(
                "error.validation.parameter.null-parameter",
                ParameterErrorType.NULL_PARAMETER.getMessageKey());
        assertTrue(
                StandardErrorType.REQUIRED
                        .getMessageKey()
                        .equals(StandardErrorType.REQUIRED.getMessageKey()));
    }

    @Test
    void transientPostconditionFailuresAreRetryableButContractFailuresAreNot() {
        assertEquals(
                ErrorType.ErrorSeverity.ERROR,
                PostconditionErrorType.PERSISTENCE_FAILED.getSeverity());
        assertTrue(PostconditionErrorType.PERSISTENCE_FAILED.isRetryable());
        assertTrue(PostconditionErrorType.INTEGRATION_FAILED.isRetryable());
        assertTrue(PostconditionErrorType.SIDE_EFFECT_FAILED.isRetryable());
        assertTrue(PostconditionErrorType.ASYNC_OPERATION_FAILED.isRetryable());

        assertEquals(
                ErrorType.ErrorSeverity.CRITICAL,
                PostconditionErrorType.INVARIANT_VIOLATED.getSeverity());
        assertFalse(PostconditionErrorType.INVARIANT_VIOLATED.isRetryable());
        assertFalse(PostconditionErrorType.EXPECTED_STATE_NOT_REACHED.isRetryable());
        assertFalse(InvariantErrorType.DATA_CORRUPTION.isRetryable());
    }

    @Test
    void englishResolutionKeepsTheDeclaredTitle() {
        assertEquals(
                StandardErrorType.REQUIRED.getProblemTitle(),
                StandardErrorType.REQUIRED.toProblemDetail().getTitle());
        assertEquals(
                StandardErrorType.REQUIRED.getProblemTitle(),
                StandardErrorType.REQUIRED
                        .toProblemDetail(Maybe.none(), Maybe.none(), ErrorLocalizer.english())
                        .getTitle());
        assertEquals(
                InvariantErrorType.OBJECT_CONSISTENCY.getProblemTitle(),
                ErrorLocalizer.of(Locale.ENGLISH).title(InvariantErrorType.OBJECT_CONSISTENCY));
    }

    @Test
    void dataCorruptionTranslationsUseTheSystemNamespace() {
        assertEquals(
                "error.system.invariant.data-corruption",
                InvariantErrorType.DATA_CORRUPTION.getMessageKey());
        assertEquals(
                "Datenbeschädigung",
                ErrorLocalizer.of(Locale.GERMAN).title(InvariantErrorType.DATA_CORRUPTION));
        assertEquals(
                "Corruption de données",
                ErrorLocalizer.of(Locale.FRENCH).title(InvariantErrorType.DATA_CORRUPTION));
    }

    @Test
    void localizedDetailsTranslateTheErrorIdSuffix() {
        final ProblemDetail french =
                ParameterError.nullParameter("userId")
                        .withGeneratedId()
                        .toProblemDetail(Maybe.none(), ErrorLocalizer.of(Locale.FRENCH));
        assertTrue(french.getDetail().contains("(ID d'erreur : "), french.getDetail());

        final ProblemDetail german =
                ParameterError.nullParameter("userId")
                        .withGeneratedId()
                        .toProblemDetail(Maybe.none(), ErrorLocalizer.of(Locale.GERMAN));
        assertTrue(german.getDetail().contains("(Fehler-ID: "), german.getDetail());

        final ProblemDetail english =
                ParameterError.nullParameter("userId").withGeneratedId().toProblemDetail();
        assertTrue(english.getDetail().contains("(error ID: "), english.getDetail());
    }

    @Test
    void unknownStatusCodesKeepTheDeclaredTitleWhenLocalizing() {
        final ProblemDetail localized =
                StandardProblemDetail.aboutBlank("Upstream gave up", 599).localized(Locale.FRENCH);
        assertEquals("Upstream gave up", localized.getTitle());
    }

    @Test
    void sensitiveNumericArgumentsDoNotLeakTheRawTemplate() {
        final String message = StandardErrorType.TOO_LONG.formatMessage(Sensitive.of(42));
        assertEquals(StandardErrorType.TOO_LONG.getProblemTitle(), message);
        assertFalse(message.contains("%"));
    }

    @Test
    void renderValueBoundsNestedArrays() {
        final String leaf = "x".repeat(200);
        final String[][][] nested = new String[16][16][16];
        for (int a = 0; a < 16; a++) {
            for (int b = 0; b < 16; b++) {
                for (int c = 0; c < 16; c++) {
                    nested[a][b][c] = leaf;
                }
            }
        }
        final String rendered = SystemError.renderValue(nested);
        assertTrue(rendered.length() <= 259, "length was " + rendered.length());
    }

    @Test
    void retryabilityIsNotPartOfTheRecordJsonContract() throws Exception {
        final String json =
                JsonMapper.builder().build().writeValueAsString(ParameterError.nullParameter("id"));
        assertFalse(json.contains("\"retryable\""), json);
        assertFalse(json.contains("\"retryAfter\""), json);
    }

    @Test
    void localizerWithoutTranslationFallsBackToDeclaredEnglish() {
        final ErrorLocalizer localizer = ErrorLocalizer.of(Locale.FRENCH);
        // Postcondition translations are intentionally not bundled.
        assertEquals(
                PostconditionErrorType.PERSISTENCE_FAILED.getProblemTitle(),
                localizer.title(PostconditionErrorType.PERSISTENCE_FAILED));
        assertEquals(
                PostconditionErrorType.PERSISTENCE_FAILED.getMessageTemplate(),
                localizer.messageTemplate(PostconditionErrorType.PERSISTENCE_FAILED));
    }

    @Test
    void zeroArgumentErrorsDoNotRenderRawFormatPlaceholders() {
        final AuthorizationError authorization =
                new AuthorizationError(
                        "admin role", AuthorizationErrorType.INSUFFICIENT_PERMISSIONS);
        assertFalse(authorization.getMessage().contains("%s"));
        assertFalse(authorization.getLocalizedMessage(Locale.GERMAN).contains("%s"));

        final BusinessRuleError businessRule =
                new BusinessRuleError("Currency mismatch", BusinessRuleErrorType.CURRENCY_MISMATCH);
        assertFalse(businessRule.getMessage().contains("%s"));
        assertFalse(businessRule.getLocalizedMessage(Locale.FRENCH).contains("%s"));
    }

    @Test
    void compoundCredentialKeysAreRedactedWithoutMaskingOrdinaryKeys() {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userPassword", "secret-one");
        payload.put("accessToken", "secret-two");
        payload.put("x-api-key", "secret-three");
        payload.put("pin", "1234");
        payload.put("shipping", "standard");

        final SystemError error =
                ParameterError.invalidFormat("payload", payload)
                        .withRedaction(RedactionPolicy.standard());
        final String rendered = error.format();

        assertFalse(rendered.contains("secret-one"));
        assertFalse(rendered.contains("secret-two"));
        assertFalse(rendered.contains("secret-three"));
        assertFalse(rendered.contains("1234"));
        assertTrue(rendered.contains("shipping=standard"));
    }

    @Test
    void redactionHandlesCyclesAndPreservesContainerTypes() {
        final Map<String, Object> cyclic = new LinkedHashMap<>();
        cyclic.put("self", cyclic);
        final Object redactedCycle = RedactionPolicy.standard().redact(cyclic);
        assertTrue(redactedCycle.toString().contains(Sensitive.REDACTED));

        final Set<String> set = new LinkedHashSet<>(List.of("a", "b"));
        assertInstanceOf(Set.class, RedactionPolicy.sensitive().redact(set));

        final String[] array = {"a", "b"};
        assertInstanceOf(String[].class, RedactionPolicy.sensitive().redact(array));
    }

    @Test
    void retryDelaysAreRoundedUpAndContractFailuresAreNotRetryable() {
        final SystemError unavailable = PreconditionError.systemNotReady("maintenance");
        final SystemError subSecond = unavailable.withRetryAfter(Duration.ofMillis(500));
        assertEquals(Maybe.of("1"), subSecond.retryAfterHeader());
        assertEquals(1L, subSecond.toProblemDetail().getRetryAfterSeconds().orNull());

        final SystemError cleared = unavailable.withRetryAfter(null);
        assertTrue(cleared.retryAfter().isEmpty().toBoolean());
        assertTrue(cleared.toProblemDetail().getRetryAfterSeconds().isEmpty().toBoolean());

        assertFalse(InvariantErrorType.DATA_CORRUPTION.isRetryable());
        assertFalse(StandardErrorType.PRECONDITION_VIOLATION.isRetryable());
        assertTrue(StandardErrorType.SERVICE_UNAVAILABLE.isRetryable());
    }

    @Test
    void localizedApisTreatNullLocaleAsEnglish() {
        final StandardProblemDetail aboutBlank = StandardProblemDetail.aboutBlank(404, null);
        assertEquals("Not Found", aboutBlank.getTitle());
        assertEquals("Not Found", aboutBlank.localized(null).getTitle());
        assertEquals("Not Found", aboutBlank.getLocalizedTitle(null));
    }

    @Test
    void extensionRetryabilityCanRemainUnknown() {
        final StandardProblemDetail detail =
                StandardProblemDetail.aboutBlank("Not Found", 404)
                        .withExtensions(
                                Maybe.none(),
                                Maybe.none(),
                                Maybe.none(),
                                Maybe.none(),
                                Maybe.none(),
                                Maybe.none());
        assertTrue(detail.getRetryable().isEmpty().toBoolean());
    }
}
