package eu.infolead.jtk.test.assertj;

import eu.infolead.jtk.anomaly.ErrorType;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.anomaly.http.ProblemDetail;
import eu.infolead.jtk.fp.either.Maybe;
import java.util.Objects;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

/**
 * Assertions for {@link SystemError}, covering the error-type, error-ID and HTTP-mapping vocabulary
 * of the anomaly model. Combine with {@link eu.infolead.jtk.test.error.TestErrors} fixtures to
 * assert against deterministic errors.
 */
public final class SystemErrorAssert extends AbstractObjectAssert<SystemErrorAssert, SystemError> {

    public SystemErrorAssert(final SystemError actual) {
        super(actual, SystemErrorAssert.class);
    }

    /**
     * Verifies that the actual error carries the expected {@link ErrorType}, as determined by value
     * equality.
     *
     * @param expected the expected error type
     * @return this assertion object
     */
    public SystemErrorAssert hasErrorType(final ErrorType expected) {
        isNotNull();
        objects.assertEqual(info, actual.getErrorType(), expected);
        return myself;
    }

    /**
     * Verifies that the actual error carries the expected error instance ID.
     *
     * @param expected the expected error ID
     * @return this assertion object
     */
    public SystemErrorAssert hasErrorId(final String expected) {
        isNotNull();
        final Maybe<String> errorId = actual.getErrorId();
        if (errorId.isEmpty().toBoolean()) {
            failWithMessage(
                    "Expected the error to carry the error ID <%s> but no ID was assigned",
                    expected);
        }
        objects.assertEqual(info, errorId.orNull(), expected);
        return myself;
    }

    /**
     * Verifies that the actual error maps to the expected HTTP status code.
     *
     * @param expected the expected HTTP status code
     * @return this assertion object
     */
    public SystemErrorAssert hasHttpStatus(final int expected) {
        isNotNull();
        objects.assertEqual(info, actual.getHttpStatus(), expected);
        return myself;
    }

    /**
     * Verifies the RFC&nbsp;9457 {@link ProblemDetail} representation of the actual error with the
     * given requirements.
     *
     * @param requirement the requirements the problem detail must satisfy; MUST NOT be {@code null}
     * @return this assertion object
     */
    public SystemErrorAssert problemDetailSatisfying(
            final Consumer<? super ProblemDetail> requirement) {
        Objects.requireNonNull(requirement, "requirement");
        isNotNull();
        requirement.accept(actual.toProblemDetail());
        return myself;
    }

    /**
     * Returns an object assertion over the RFC&nbsp;9457 {@link ProblemDetail} representation of
     * the actual error, as an escape hatch to the full AssertJ vocabulary.
     *
     * @return an object assertion over the problem detail
     */
    public ObjectAssert<ProblemDetail> extractingProblemDetail() {
        isNotNull();
        return Assertions.assertThat(actual.toProblemDetail());
    }
}
