package eu.infolead.jtk.anomaly;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Process-local sequence used to generate unique error instance identifiers.
 * <p>
 * Package-private on purpose: this is an implementation detail of
 * {@link ErrorType}, not part of the public API.
 */
final class ErrorIdSequence {

    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final String PROCESS_TOKEN = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    private ErrorIdSequence() {
        throw new UnsupportedOperationException("Utility class");
    }

    static String next() {
        return PROCESS_TOKEN + "-" + SEQUENCE.incrementAndGet();
    }
}
