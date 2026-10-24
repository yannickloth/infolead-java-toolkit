package eu.infolead.jtk.anomaly;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Strategy used to redact sensitive values before they reach logs or {@code ProblemDetail}
 * payloads.
 *
 * <p>Three policies are available:
 *
 * <ul>
 *   <li>{@link #none()} – preserve values as-is;
 *   <li>{@link #sensitive()} – redact explicit {@link Sensitive} wrappers, recursively through
 *       maps, collections and arrays;
 *   <li>{@link #standard()} – also redact map entries whose key looks like a credential ({@code
 *       password}, {@code token}, {@code authorization}, and similar).
 * </ul>
 *
 * <p>Redaction preserves list/set semantics, keeps array component types when possible, detects
 * reference cycles, and bounds recursion depth. A policy can be attached to an error with {@link
 * SystemError#withRedaction(RedactionPolicy)}. The policy is applied to {@link
 * SystemError#getActualValue()} and {@link SystemError#getContextArgs()}, which are the values used
 * to build log lines and problem details.
 *
 * <pre>{@code
 * Map<String, Object> payload = Map.of(
 *         "user", "alice",
 *         "password", "hunter2",
 *         "accessToken", "token-abc");
 *
 * SystemError error = ParameterError.invalidFormat("payload", payload)
 *         .withRedaction(RedactionPolicy.standard());
 *
 * error.format();
 * // ... (actual value: {user=alice, password=[REDACTED], accessToken=[REDACTED]})
 * }</pre>
 */
@FunctionalInterface
public interface RedactionPolicy extends java.io.Serializable {

    /** Leaves all values untouched. */
    RedactionPolicy NONE = value -> value;

    /** Redacts explicit {@link Sensitive} wrappers, recursively. */
    RedactionPolicy SENSITIVE = value -> redactValue(value, key -> false, 0, newIdentitySet());

    /** Redacts explicit {@link Sensitive} wrappers and map entries whose key names a credential. */
    RedactionPolicy STANDARD =
            value -> redactValue(value, RedactionPolicy::isSensitiveKey, 0, newIdentitySet());

    /**
     * Redacts a single value.
     *
     * @param value the value to redact
     * @return the redacted value, which may be a deep copy
     */
    Object redact(Object value);

    /**
     * Returns a policy that leaves values untouched.
     *
     * @return the no-op policy
     */
    static RedactionPolicy none() {
        return NONE;
    }

    /**
     * Returns a policy that redacts {@link Sensitive} wrappers only.
     *
     * @return the sensitive-wrapper policy
     */
    static RedactionPolicy sensitive() {
        return SENSITIVE;
    }

    /**
     * Returns a policy that redacts {@link Sensitive} wrappers and credential-looking map keys.
     *
     * @return the standard policy
     */
    static RedactionPolicy standard() {
        return STANDARD;
    }

    /**
     * Indicates whether a map key names a credential or secret.
     *
     * <p>Matching is fragment-based after stripping punctuation, so {@code userPassword}, {@code
     * new-password}, {@code accessToken} and {@code x-api-key} are all recognized. Short fragments
     * such as {@code pin} or {@code cvv} require an exact match to avoid false positives.
     *
     * <pre>{@code
     * RedactionPolicy.isSensitiveKey("password");      // true
     * RedactionPolicy.isSensitiveKey("userPassword");  // true
     * RedactionPolicy.isSensitiveKey("x-api-key");     // true
     * RedactionPolicy.isSensitiveKey("shipping");      // false
     * }</pre>
     *
     * @param key the map key
     * @return {@code true} when the value under this key must be redacted
     */
    static boolean isSensitiveKey(final String key) {
        final String normalized = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return RedactionKeys.SENSITIVE.stream()
                .anyMatch(
                        fragment ->
                                fragment.length() >= 4
                                        ? normalized.contains(fragment)
                                        : normalized.equals(fragment));
    }

    /**
     * Redacts every element of an array.
     *
     * @param values the values to redact
     * @return a new array with redacted elements
     */
    default Object[] redactAll(final Object[] values) {
        Objects.requireNonNull(values, "values cannot be null");
        final Object[] redacted = new Object[values.length];
        for (int index = 0; index < values.length; index++) {
            redacted[index] = redact(values[index]);
        }
        return redacted;
    }

    /**
     * Replaces rendered sensitive arguments inside an already formatted message. This keeps the
     * detail text safe even when the message was built before the policy was attached.
     *
     * @param message the formatted message, possibly {@code null}
     * @param rawValues the arguments that produced the message
     * @return the message with sensitive fragments replaced
     */
    default String redactMessage(final String message, final Object[] rawValues) {
        if (message == null || rawValues == null || rawValues.length == 0 || this == NONE) {
            return message;
        }
        final Object[] redactedValues = redactAll(rawValues);
        String result = message;
        for (int index = 0; index < rawValues.length; index++) {
            if (rawValues[index] == redactedValues[index]) {
                continue;
            }
            final String rawRendered = rawRendered(rawValues[index]);
            final String redactedRendered = SystemError.renderValue(redactedValues[index]);
            if (rawRendered != null
                    && !rawRendered.isEmpty()
                    && !rawRendered.equals(redactedRendered)) {
                result = result.replace(rawRendered, redactedRendered);
            }
        }
        return result;
    }

    private static Object redactValue(
            final Object value,
            final Predicate<String> sensitiveKey,
            final int depth,
            final Set<Object> visited) {
        if (value == null) {
            return null;
        }
        if (value instanceof Sensitive) {
            return Sensitive.REDACTED;
        }
        if (value instanceof Map<?, ?> map) {
            if (depth > 8 || !visited.add(value)) {
                return Sensitive.REDACTED;
            }
            try {
                final Map<Object, Object> redacted = new LinkedHashMap<>(map.size());
                map.forEach(
                        (key, entryValue) -> {
                            if (key != null && sensitiveKey.test(String.valueOf(key))) {
                                redacted.put(key, Sensitive.REDACTED);
                            } else {
                                redacted.put(
                                        key,
                                        redactValue(entryValue, sensitiveKey, depth + 1, visited));
                            }
                        });
                return redacted;
            } finally {
                visited.remove(value);
            }
        }
        if (value instanceof Collection<?> collection) {
            if (depth > 8 || !visited.add(value)) {
                return Sensitive.REDACTED;
            }
            try {
                final Collection<Object> redacted =
                        value instanceof Set
                                ? new LinkedHashSet<>(collection.size())
                                : new ArrayList<>(collection.size());
                for (final Object element : collection) {
                    redacted.add(redactValue(element, sensitiveKey, depth + 1, visited));
                }
                return redacted;
            } finally {
                visited.remove(value);
            }
        }
        if (value.getClass().isArray()) {
            if (value.getClass().getComponentType().isPrimitive()) {
                return value;
            }
            if (depth > 8 || !visited.add(value)) {
                return Sensitive.REDACTED;
            }
            try {
                return redactArray(value, sensitiveKey, depth, visited);
            } finally {
                visited.remove(value);
            }
        }
        return value;
    }

    private static Object redactArray(
            final Object array,
            final Predicate<String> sensitiveKey,
            final int depth,
            final Set<Object> visited) {
        final int length = Array.getLength(array);
        final Object[] untyped = new Object[length];
        final Object typed = Array.newInstance(array.getClass().getComponentType(), length);
        boolean typeCompatible = true;
        for (int index = 0; index < length; index++) {
            final Object redactedElement =
                    redactValue(Array.get(array, index), sensitiveKey, depth + 1, visited);
            untyped[index] = redactedElement;
            if (typeCompatible) {
                try {
                    Array.set(typed, index, redactedElement);
                } catch (final IllegalArgumentException exception) {
                    typeCompatible = false;
                }
            }
        }
        return typeCompatible ? typed : untyped;
    }

    private static Set<Object> newIdentitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private static String rawRendered(final Object value) {
        try {
            if (value instanceof Sensitive sensitive) {
                return String.valueOf(sensitive.reveal());
            }
            return String.valueOf(value);
        } catch (final RuntimeException exception) {
            return null;
        }
    }
}

/**
 * Normalized key fragments that designate credentials or secrets. Kept package-private so that the
 * policy interface does not expose mutable-looking configuration.
 */
final class RedactionKeys {

    static final Set<String> SENSITIVE =
            Set.of(
                    "password",
                    "passwd",
                    "pwd",
                    "passphrase",
                    "secret",
                    "clientsecret",
                    "secretkey",
                    "token",
                    "accesstoken",
                    "refreshtoken",
                    "idtoken",
                    "apikey",
                    "authorization",
                    "cookie",
                    "setcookie",
                    "credential",
                    "credentials",
                    "privatekey",
                    "ssn",
                    "socialsecuritynumber",
                    "cardnumber",
                    "cvv",
                    "pin");

    private RedactionKeys() {
        throw new UnsupportedOperationException("Utility class");
    }
}
