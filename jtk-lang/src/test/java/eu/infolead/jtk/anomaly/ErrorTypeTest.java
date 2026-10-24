package eu.infolead.jtk.anomaly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ErrorTypeTest {

    @Test
    void problemTypeUrisAreUnique() {
        final List<ErrorType> types = allTypes();
        final Set<URI> uris = new HashSet<>();
        for (final ErrorType type : types) {
            assertTrue(uris.add(type.getProblemType()),
                    () -> "Duplicate problem type URI: " + type.getProblemType());
        }
        assertEquals(types.size(), uris.size());
    }

    @Test
    void httpStatusesAreValid() {
        for (final ErrorType type : allTypes()) {
            assertTrue(type.getHttpStatus() >= 100 && type.getHttpStatus() <= 599,
                    () -> "Invalid HTTP status for " + type.getCode() + ": " + type.getHttpStatus());
        }
    }

    @Test
    void infrastructureFailuresRemainErrors() {
        assertEquals(ErrorType.ErrorSeverity.ERROR, StandardErrorType.INTERNAL_ERROR.getSeverity());
        assertEquals(ErrorType.ErrorSeverity.ERROR, StandardErrorType.SERVICE_UNAVAILABLE.getSeverity());
        assertEquals(ErrorType.ErrorSeverity.ERROR, StandardErrorType.TIMEOUT.getSeverity());
        assertEquals(ErrorType.ErrorSeverity.CRITICAL, InvariantErrorType.OBJECT_CONSISTENCY.getSeverity());
    }

    @Test
    void malformedMessageArgumentsFallBackToTheTemplate() {
        final String template = StandardErrorType.TOO_LONG.getMessageTemplate();
        assertEquals(template, StandardErrorType.TOO_LONG.formatMessage("not-a-number"));
    }

    @Test
    void generatedErrorIdsAreUnique() {
        final ErrorType type = StandardErrorType.REQUIRED;
        final Set<String> ids = new HashSet<>();
        for (int index = 0; index < 100; index++) {
            assertTrue(ids.add(type.generateErrorId()), "generated error ids must be unique");
        }
    }

    private static List<ErrorType> allTypes() {
        final List<ErrorType> types = new ArrayList<>();
        types.addAll(Arrays.asList(StandardErrorType.values()));
        types.addAll(Arrays.asList(ParameterErrorType.values()));
        types.addAll(Arrays.asList(PreconditionErrorType.values()));
        types.addAll(Arrays.asList(PostconditionErrorType.values()));
        types.addAll(Arrays.asList(InvariantErrorType.values()));
        types.addAll(Arrays.asList(AuthorizationErrorType.values()));
        types.addAll(Arrays.asList(ResourceErrorType.values()));
        types.addAll(Arrays.asList(BusinessRuleErrorType.values()));
        return types;
    }
}
