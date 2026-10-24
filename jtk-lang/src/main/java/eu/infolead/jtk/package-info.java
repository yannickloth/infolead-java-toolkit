/**
 * Infolead Java Toolkit - Functional programming utilities for Java 17+.
 *
 * <h2>Core Components</h2>
 *
 * <h3>Functional Programming ({@link eu.infolead.jtk.fp})</h3>
 *
 * <ul>
 *   <li><strong>Either types</strong> - {@link eu.infolead.jtk.fp.either.Either}, {@link
 *       eu.infolead.jtk.fp.either.Maybe}, {@link eu.infolead.jtk.fp.either.Result}
 *   <li><strong>Validation</strong> - {@link eu.infolead.jtk.fp.validation} package with
 *       comprehensive error handling
 *   <li><strong>Tail Call Optimization</strong> - {@link eu.infolead.jtk.fp.tailrec.TailCall}
 *   <li><strong>Design by Contract</strong> - {@link eu.infolead.jtk.fp.Contracts}
 * </ul>
 *
 * <h3>Error Handling ({@link eu.infolead.jtk.anomaly})</h3>
 *
 * <ul>
 *   <li><strong>Unified Error System</strong> - {@link eu.infolead.jtk.anomaly.SystemError}
 *       hierarchy
 *   <li><strong>Error Classification</strong> - Parameter, Precondition, Postcondition, Invariant
 *       errors
 *   <li><strong>HTTP Integration</strong> - RFC 7807 ProblemDetail support
 *   <li><strong>Error IDs</strong> - Type-identifying numbers and instance tracking
 * </ul>
 *
 * <h3>Language Extensions ({@link eu.infolead.jtk.lang})</h3>
 *
 * <ul>
 *   <li><strong>Annotations</strong> - {@link eu.infolead.jtk.lang.AliasFor}, compiler warnings
 * </ul>
 *
 * <h3>Logic ({@link eu.infolead.jtk.logic})</h3>
 *
 * <ul>
 *   <li><strong>Boolean Logic</strong> - {@link eu.infolead.jtk.logic.Bool}, {@link
 *       eu.infolead.jtk.logic.NullableBool}
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <h3>Railway Oriented Programming</h3>
 *
 * All Either types support comprehensive Railway-Oriented Programming patterns. Think of your data
 * as trains moving through a railway system:
 *
 * <ul>
 *   <li><strong>Success Track (Right)</strong> - Main railway line where everything works
 *   <li><strong>Error Track (Left)</strong> - Emergency siding for handling failures
 *   <li><strong>Railway Switches</strong> - Conditional operations that can derail trains
 *   <li><strong>Track Junctions</strong> - Combining multiple trains and their cargo
 *   <li><strong>Emergency Recovery</strong> - Getting derailed trains back on track
 * </ul>
 *
 * <pre>{@code
 * import eu.infolead.jtk.fp.Fn;
 *
 * // Complete railway journey with multiple stations
 * Either<String, User> userJourney = Either.fromOptional(
 *         () -> "Email required", getEmail(input))              // Station 1: Load cargo
 *     .map2(Either.fromOptional(() -> "Name required", getName(input)),
 *           (email, name) -> new UserData(email, name))         // Station 2: Merge cargo
 *     .filter(data -> data.isValid(), () -> "Invalid data")    // Station 3: Quality check
 *     .flatMap(this::validateAge)                              // Station 4: Age verification
 *     .flatMap(this::createUser)                               // Station 5: Final processing
 *     .recover(error -> attemptDataRecovery(error));           // Emergency recovery
 * }</pre>
 *
 * <h3>Semantic Error Classification</h3>
 *
 * Errors are classified by semantic meaning rather than "who caused them":
 *
 * <ul>
 *   <li><strong>ParameterError</strong> - Invalid method arguments (HTTP 400)
 *   <li><strong>PreconditionError</strong> - System not ready (HTTP 4xx)
 *   <li><strong>PostconditionError</strong> - Expected state not reached (HTTP 5xx)
 *   <li><strong>InvariantError</strong> - Object consistency violations (HTTP 500)
 * </ul>
 *
 * <h3>Comprehensive Error IDs</h3>
 *
 * Error types include structured IDs for tracking and monitoring:
 *
 * <pre>{@code
 * import eu.infolead.jtk.fp.validation.ValidationError;
 *
 * ValidationError error = ValidationError.requiredWithId("user.email");
 * // error.format() -> [REQUIRED|1001-<processToken>-N] user.email: Required field is missing or null
 * // where <processToken> identifies the process and N is a process-local sequence number
 * }</pre>
 *
 * <h2>Requirements for all sub-packages</h2>
 *
 * The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT",
 * "RECOMMENDED", "MAY", and "OPTIONAL" in this package's documentation (including that of all
 * sub-packages) are to be interpreted as described in <a
 * href="https://www.rfc-editor.org/rfc/rfc2119">RFC 2119</a>.
 *
 * <h2>About serialization</h2>
 *
 * Due to the way the Servlet API works, it is possible for web servers to serialize web sessions,
 * and all objects they reference. Every type in this package MUST either implement {@link
 * java.io.Serializable} or explicitly document that it does not support serialization.
 *
 * <p>All variants of the {@code Either} hierarchy (including {@link
 * eu.infolead.jtk.fp.either.Maybe}, {@link eu.infolead.jtk.fp.either.Result} and {@link
 * eu.infolead.jtk.fp.either.Validation}) are serializable through {@code ValueContainer}, provided
 * their payload is serializable. {@link eu.infolead.jtk.logic.Bool}, {@link
 * eu.infolead.jtk.logic.NullableBool}, {@link eu.infolead.jtk.fp.validation.ValidationResult} and
 * the whole {@link eu.infolead.jtk.anomaly.SystemError} hierarchy are serializable as well, again
 * provided their payloads (context arguments, actual values and causes) are serializable.
 * Operational metadata of a contextual error — cause chains, correlation context and redaction
 * policy — is serializable but is deliberately excluded from the JSON wire contract.
 *
 * <h2>JSON wire contract</h2>
 *
 * The wire shape is defined by Jackson annotations on the types themselves, so it holds with a
 * plain {@code ObjectMapper} and without registering a module. Every {@code Either} is a
 * single-member envelope: {@code {"left": ...}} or {@code {"right": ...}}. Error types are
 * identified by their stable message key (for example {@code
 * error.validation.parameter.null-parameter}), and {@code SystemError} documents carry a {@code
 * kind} discriminator. Modules such as {@code jtk-jackson3} only add what annotations cannot
 * express, and framework integrations (Spring Boot auto-configuration, Quarkus extensions) live in
 * separate artifacts so that this module stays framework-free.
 *
 * @see eu.infolead.jtk.fp.either
 * @see eu.infolead.jtk.fp.validation
 * @see eu.infolead.jtk.anomaly
 */
package eu.infolead.jtk;
