/**
 * Infolead Java Toolkit - Functional programming utilities for Java 17+.
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Functional Programming ({@link eu.infolead.jtk.fp})</h3>
 * <ul>
 *   <li><strong>Either types</strong> - {@link eu.infolead.jtk.fp.either.Either}, 
 *       {@link eu.infolead.jtk.fp.either.Maybe}, {@link eu.infolead.jtk.fp.either.Result}</li>
 *   <li><strong>Validation</strong> - {@link eu.infolead.jtk.fp.validation} package with 
 *       comprehensive error handling</li>
 *   <li><strong>Tail Call Optimization</strong> - {@link eu.infolead.jtk.fp.tailrec.TailCall}</li>
 *   <li><strong>Design by Contract</strong> - {@link eu.infolead.jtk.fp.Contracts}</li>
 * </ul>
 * 
 * <h3>Error Handling ({@link eu.infolead.jtk.anomaly})</h3>
 * <ul>
 *   <li><strong>Unified Error System</strong> - {@link eu.infolead.jtk.anomaly.SystemError} hierarchy</li>
 *   <li><strong>Error Classification</strong> - Parameter, Precondition, Postcondition, Invariant errors</li>
 *   <li><strong>HTTP Integration</strong> - RFC 7807 ProblemDetail support</li>
 *   <li><strong>Error IDs</strong> - Type-identifying numbers and instance tracking</li>
 * </ul>
 * 
 * <h3>Language Extensions ({@link eu.infolead.jtk.lang})</h3>
 * <ul>
 *   <li><strong>Annotations</strong> - {@link eu.infolead.jtk.lang.AliasFor}, compiler warnings</li>
 *   <li><strong>Boolean Logic</strong> - {@link eu.infolead.jtk.logic.Bool}, nullable booleans</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * 
 * <h3>Railway Oriented Programming</h3>
 * All Either types support comprehensive Railway-Oriented Programming patterns.
 * Think of your data as trains moving through a railway system:
 * <ul>
 *   <li><strong>Success Track (Right)</strong> - Main railway line where everything works</li>
 *   <li><strong>Error Track (Left)</strong> - Emergency siding for handling failures</li>
 *   <li><strong>Railway Switches</strong> - Conditional operations that can derail trains</li>
 *   <li><strong>Track Junctions</strong> - Combining multiple trains and their cargo</li>
 *   <li><strong>Emergency Recovery</strong> - Getting derailed trains back on track</li>
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
 * Errors are classified by semantic meaning rather than "who caused them":
 * <ul>
 *   <li><strong>ParameterError</strong> - Invalid method arguments (HTTP 400)</li>
 *   <li><strong>PreconditionError</strong> - System not ready (HTTP 4xx)</li>
 *   <li><strong>PostconditionError</strong> - Expected state not reached (HTTP 5xx)</li>
 *   <li><strong>InvariantError</strong> - Object consistency violations (HTTP 500)</li>
 * </ul>
 * 
 * <h3>Comprehensive Error IDs</h3>
 * Error types include structured IDs for tracking and monitoring:
 * <pre>{@code
 * ValidationError error = ValidationError.requiredWithId("user.email");
 * // Format: [REQUIRED|1001-1638123456789] user.email: Required field is missing
 * }</pre>
 * 
 * <h2>Requirements for all sub-packages</h2>
 * The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
 * "SHOULD", "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this
 * document are to be interpreted as described in
 * <a href="https://www.rfc-editor.org/rfc/rfc2119">RFC 2119</a>.
 * 
 * <h2>About serialization</h2>
 * Due to the way the Servlet API works, it is possible for web servers
 * to serialize web sessions, and all objects they reference. For this reason,
 * it is essential to ensure that APIs in this package either support serialization
 * by default or explicitly state that they don't support it.
 * 
 * @see eu.infolead.jtk.fp.either
 * @see eu.infolead.jtk.fp.validation
 * @see eu.infolead.jtk.anomaly
 */
package eu.infolead.jtk;
