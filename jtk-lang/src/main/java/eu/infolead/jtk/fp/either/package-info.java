/**
 * Railway-Oriented Programming implementation for Java using Either types.
 * 
 * <h1>Railway-Oriented Programming Guide</h1>
 * 
 * <p>This package implements Scott Wlaschin's Railway-Oriented Programming pattern
 * (see <a href="https://fsharpforfunandprofit.com/rop/">F# for Fun and Profit</a>)
 * using Java's Either types. Think of your data processing as trains moving through
 * a railway network with two tracks:</p>
 * 
 * <ul>
 *   <li><strong>Success Track (Right)</strong> - Main railway line where everything works</li>
 *   <li><strong>Error Track (Left)</strong> - Emergency siding for handling failures</li>
 * </ul>
 * 
 * <h2>Core Railway Operations</h2>
 * 
 * <h3>1. Switch Functions - Converting Single-Track to Two-Track</h3>
 * <p>Convert regular functions (that might fail) into railway-compatible functions.</p>
 * 
 * <h4>Pattern: Single-Track Function → Two-Track Function</h4>
 * <pre>{@code
 * // Traditional approach (throws exceptions)
 * public User parseUser(String input) throws ValidationException {
 *     if (input == null) throw new ValidationException("Input required");
 *     return new User(input);
 * }
 * 
 * // Railway approach (returns Either)
 * public Either<String, User> parseUserSafely(String input) {
 *     return Either.fromBoolean(
 *         input != null,
 *         () -> "Input required",          // Error track (first parameter)
 *         () -> new User(input)            // Success track (second parameter)
 *     );
 * }
 * 
 * // Or using try-catch conversion
 * public Either<String, User> parseUserFromTry(String input) {
 *     return Either.fromTry(
 *         ex -> "Parsing failed: " + ex.getMessage(),  // Error mapper (first parameter)
 *         () -> parseUser(input)                        // Computation (second parameter)
 *     );
 * }
 * }</pre>
 * 
 * <h3>2. Railway Chaining - Composing Switch Functions</h3>
 * <p>Chain multiple operations where each can fail, using {@code flatMap} (also called {@code bind}).</p>
 * 
 * <h4>Pattern: Railway Switch Chain</h4>
 * <pre>{@code
 * // Each function is a railway switch: Input → Either<Error, Output>
 * public Either<String, String> validateEmail(String email) {
 *     return Either.fromBoolean(
 *         email.contains("@"),
 *         () -> email,
 *         () -> "Invalid email format"
 *     );
 * }
 * 
 * public Either<String, User> createUser(String email) {
 *     return Either.fromTry(
 *         ex -> "User creation failed: " + ex.getMessage(),
 *         () -> new User(email)
 *     );
 * }
 * 
 * // Chain them together - train goes through multiple stations
 * Either<String, User> userResult = parseUserSafely(input)
 *     .flatMap(this::validateEmail)    // Station 1: Email validation
 *     .flatMap(this::createUser);      // Station 2: User creation
 * 
 * // If any station fails, the train derails to error track
 * }</pre>
 * 
 * <h3>3. Parallel Track Operations - Independent Validations</h3>
 * <p>Combine multiple independent validations using applicative operations.</p>
 * 
 * <h4>Pattern: Multiple Train Convergence</h4>
 * <pre>{@code
 * // Validate different fields independently
 * Either<String, String> emailResult = validateEmail(user.getEmail());
 * Either<String, String> nameResult = validateName(user.getName());
 * Either<String, Integer> ageResult = validateAge(user.getAge());
 * 
 * // Combine all validations - all trains must arrive successfully
 * Either<String, User> userResult = emailResult.map3(nameResult, ageResult,
 *     (email, name, age) -> new User(email, name, age)
 * );
 * 
 * // Alternative using map2 for two validations
 * Either<String, UserData> userData = emailResult.map2(nameResult,
 *     (email, name) -> new UserData(email, name)
 * );
 * }</pre>
 * 
 * <h3>4. Railway Switches - Conditional Track Changes</h3>
 * <p>Add quality gates that can switch trains from success to error track.</p>
 * 
 * <h4>Pattern: Quality Inspection Points</h4>
 * <pre>{@code
 * Either<String, User> processUser(String input) {
 *     return parseUserSafely(input)
 *         .filter(user -> user.getAge() >= 18, 
 *                () -> "User must be 18 or older")              // Age checkpoint
 *         .filter(user -> user.getEmail().endsWith(".com"), 
 *                () -> "Only .com email addresses allowed")     // Email checkpoint
 *         .ensure(user -> user.getName().length() > 0,
 *                () -> "Name cannot be empty");                 // Name safety check
 * }
 * }</pre>
 * 
 * <h3>5. Track Bypass Functions - Transform Both Tracks</h3>
 * <p>Operations that work on both success and error tracks simultaneously.</p>
 * 
 * <h4>Pattern: Dual-Track Processing</h4>
 * <pre>{@code
 * // Transform both success and error values
 * Either<ErrorReport, SuccessReport> result = processData(input)
 *     .bimap(
 *         error -> new ErrorReport("Processing failed", error),    // Error track transform
 *         data -> new SuccessReport("Processing succeeded", data)  // Success track transform
 *     );
 * 
 * // Both tracks get transformed, but train stays on same track
 * }</pre>
 * 
 * <h3>6. Dead-End Functions - Terminal Operations</h3>
 * <p>Extract final values from the railway system using {@code fold}.</p>
 * 
 * <h4>Pattern: Railway Terminal</h4>
 * <pre>{@code
 * // End of the railway line - extract final result
 * String finalMessage = processUser(input).fold(
 *     error -> "❌ Error: " + error,           // Handle error track
 *     user -> "✅ Success: Welcome " + user    // Handle success track
 * );
 * 
 * // Or get just the success value with default for errors
 * User finalUser = processUser(input).or(User.defaultUser());
 * }</pre>
 * 
 * <h3>7. Recovery Operations - Emergency Repairs</h3>
 * <p>Attempt to recover from failures and get back on the success track.</p>
 * 
 * <h4>Pattern: Emergency Response System</h4>
 * <pre>{@code
 * Either<String, User> userWithRecovery = processUser(input)
 *     .recover(error -> {
 *         if (error.contains("email")) {
 *             // Try to fix email issues
 *             return Either.right(User.withDefaultEmail(input));
 *         } else if (error.contains("age")) {
 *             // Try to fix age issues  
 *             return Either.right(User.withDefaultAge(input));
 *         } else {
 *             // Can't recover from this error
 *             return Either.left("Unrecoverable error: " + error);
 *         }
 *     });
 * }</pre>
 * 
 * <h3>8. Collection Operations - Train Convoys</h3>
 * <p>Process collections of data through the railway system.</p>
 * 
 * <h4>Pattern: Train Fleet Management</h4>
 * <pre>{@code
 * List<String> inputs = Arrays.asList("user1@example.com", "user2@example.com", "invalid");
 * 
 * // Process all inputs, stop at first failure (fail-fast)
 * Either<String, List<User>> allUsersOrFirstError = Either.traverse(inputs, this::processUser);
 * 
 * // Process all inputs, collect both successes and failures
 * Either.Tuple2<List<String>, List<User>> separated = Either.partition(
 *     inputs.stream()
 *           .map(this::processUser)
 *           .collect(toList())
 * );
 * 
 * List<String> errors = separated.first();      // All the failures
 * List<User> successes = separated.second();    // All the successes
 * }</pre>
 * 
 * <h3>9. Function Lifting - Converting Regular Functions</h3>
 * <p>Convert regular functions to work with railway types.</p>
 * 
 * <h4>Pattern: Function Railway Conversion</h4>
 * <pre>{@code
 * import eu.infolead.jtk.fp.Fn;
 * 
 * // Regular function
 * public String formatUser(User user) {
 *     return user.getName() + " <" + user.getEmail() + ">";
 * }
 * 
 * // Lift it to work with Either
 * Fn.FN1<Either<String, String>, Either<String, User>> formatUserLifted = 
 *     Either.lift(this::formatUser);
 * 
 * // Now it works on railway types
 * Either<String, String> formattedResult = formatUserLifted.apply(userResult);
 * 
 * // Lift binary functions  
 * Fn.FN2<Either<String, String>, Either<String, User>, Either<String, String>> addEmailSuffix =
 *     Either.lift2((user, suffix) -> user.getEmail() + suffix);
 * }</pre>
 * 
 * <h2>Complete Railway System Example</h2>
 * 
 * <pre>{@code
 * public class UserRegistrationRailway {
 *     
 *     // Station 1: Input loading
 *     private Either<String, RegistrationData> loadRegistrationData(String input) {
 *         return Either.fromOptional(
 *             () -> "Invalid JSON input",
 *             parseJson(input)
 *         );
 *     }
 *     
 *     // Station 2: Email validation  
 *     private Either<String, RegistrationData> validateEmail(RegistrationData data) {
 *         return Either.right(data)
 *             .filter(d -> d.getEmail().contains("@"), 
 *                    () -> "Invalid email format")
 *             .filter(d -> !d.getEmail().contains("+"), 
 *                    () -> "Plus signs not allowed in email");
 *     }
 *     
 *     // Station 3: Age verification
 *     private Either<String, RegistrationData> validateAge(RegistrationData data) {
 *         return Either.right(data)
 *             .filter(d -> d.getAge() >= 13, 
 *                    () -> "Must be 13 or older")
 *             .filter(d -> d.getAge() <= 120, 
 *                    () -> "Age seems unrealistic");
 *     }
 *     
 *     // Station 4: External service validation
 *     private Either<String, RegistrationData> checkEmailNotTaken(RegistrationData data) {
 *         return Either.fromTry(
 *             ex -> "Email already registered",
 *             () -> {
 *                 if (emailService.isEmailTaken(data.getEmail())) {
 *                     throw new EmailTakenException();
 *                 }
 *                 return data;
 *             }
 *         );
 *     }
 *     
 *     // Station 5: User creation
 *     private Either<String, User> createUser(RegistrationData data) {
 *         return Either.fromTry(
 *             ex -> "Database error: " + ex.getMessage(),
 *             () -> userRepository.save(new User(data))
 *         );
 *     }
 *     
 *     // Station 6: Welcome email
 *     private Either<String, User> sendWelcomeEmail(User user) {
 *         return Either.fromTry(
 *             ex -> "Welcome email failed: " + ex.getMessage(),
 *             () -> {
 *                 emailService.sendWelcome(user);
 *                 return user;
 *             }
 *         );
 *     }
 *     
 *     // Complete railway journey
 *     public Either<String, User> registerUser(String input) {
 *         return loadRegistrationData(input)                    // Station 1: Load cargo
 *             .flatMap(this::validateEmail)                     // Station 2: Email check
 *             .flatMap(this::validateAge)                       // Station 3: Age check  
 *             .flatMap(this::checkEmailNotTaken)                // Station 4: Uniqueness check
 *             .flatMap(this::createUser)                        // Station 5: Creation
 *             .flatMap(this::sendWelcomeEmail)                  // Station 6: Welcome
 *             .recover(error -> {                               // Emergency recovery
 *                 if (error.contains("email failed")) {
 *                     // User was created but email failed - that's OK
 *                     return Either.right(userRepository.findByEmail(
 *                         extractEmailFromError(error)));
 *                 }
 *                 return Either.left(error);  // Can't recover
 *             });
 *     }
 *     
 *     // Railway terminal - extract final HTTP response
 *     public HttpResponse handleRegistration(String input) {
 *         return registerUser(input).fold(
 *             error -> HttpResponse.badRequest()
 *                 .body("Registration failed: " + error),      // Error track terminal
 *             user -> HttpResponse.ok()  
 *                 .body("Welcome " + user.getName() + "!")     // Success track terminal
 *         );
 *     }
 * }
 * }</pre>
 * 
 * <h2>Railway Error Accumulation</h2>
 * 
 * <p>For scenarios where you want to collect ALL validation errors (not just the first),
 * use {@link eu.infolead.jtk.fp.validation.ValidationResult} instead of {@code Either}:</p>
 * 
 * <pre>{@code
 * // Collect all validation errors
 * ValidationResult<User> userValidation = Validator.ObjectValidator.forObject(new User())
 *     .validate(User::getEmail, emailValidator)     // Validate email
 *     .validate(User::getName, nameValidator)       // Validate name  
 *     .validate(User::getAge, ageValidator)         // Validate age
 *     .result();
 * 
 * if (userValidation.isFailure()) {
 *     List<ValidationError> allErrors = userValidation.getErrors();
 *     // Show all errors to user at once
 * }
 * }</pre>
 * 
 * <h2>Key Railway Principles</h2>
 * 
 * <ol>
 *   <li><strong>Fail Fast</strong> - Errors short-circuit the pipeline</li>
 *   <li><strong>Composability</strong> - Operations chain together naturally</li>
 *   <li><strong>Type Safety</strong> - Compiler ensures error handling</li>
 *   <li><strong>Explicit Errors</strong> - No hidden exceptions</li>
 *   <li><strong>Functional Style</strong> - Immutable, side-effect-free operations</li>
 * </ol>
 * 
 * <h2>Common Railway Patterns</h2>
 * 
 * <table border="1">
 * <tr><th>Pattern</th><th>Method</th><th>Use Case</th></tr>
 * <tr><td>Single validation</td><td>{@code filter()}</td><td>Check one condition</td></tr>
 * <tr><td>Sequential processing</td><td>{@code flatMap()}</td><td>Chain operations that can fail</td></tr>
 * <tr><td>Parallel validation</td><td>{@code map2()}, {@code map3()}</td><td>Combine independent checks</td></tr>
 * <tr><td>Error recovery</td><td>{@code recover()}</td><td>Try to fix errors</td></tr>
 * <tr><td>Fallback values</td><td>{@code or()}, {@code orElse()}</td><td>Provide defaults</td></tr>
 * <tr><td>Collection processing</td><td>{@code traverse()}</td><td>Process lists with error handling</td></tr>
 * <tr><td>Function conversion</td><td>{@code fromTry()}</td><td>Wrap exception-throwing code</td></tr>
 * <tr><td>Final extraction</td><td>{@code fold()}</td><td>Get final result</td></tr>
 * </table>
 * 
 * @see Either
 * @see eu.infolead.jtk.fp.validation.ValidationResult
 * @see <a href="https://fsharpforfunandprofit.com/rop/">Railway-Oriented Programming</a>
 */
package eu.infolead.jtk.fp.either;