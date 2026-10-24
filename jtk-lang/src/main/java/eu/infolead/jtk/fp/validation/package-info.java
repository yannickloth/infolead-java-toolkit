/**
 * Validation utilities and error handling for user input and business rule validation.
 * 
 * <p>This package contains classes for handling expected validation errors that arise
 * from user input validation and business rule enforcement. Unlike contract errors
 * which indicate programming bugs, validation errors represent expected scenarios
 * that should be handled gracefully.</p>
 * 
 * <h2>Key Classes</h2>
 * 
 * <h3>{@link eu.infolead.jtk.fp.validation.ValidationError}</h3>
 * <p>Represents individual validation errors with:</p>
 * <ul>
 *   <li>Field paths for precise error location</li>
 *   <li>Error types with HTTP status mapping</li>
 *   <li>Contextual information and rejected values</li>
 *   <li>Unique error IDs for tracking</li>
 * </ul>
 * 
 * <h3>{@link eu.infolead.jtk.fp.validation.ValidationResult}</h3>
 * <p>Accumulates multiple validation errors for comprehensive reporting:</p>
 * <ul>
 *   <li>Collects all validation failures instead of short-circuiting</li>
 *   <li>Provides monadic operations for chaining validations</li>
 *   <li>Converts to ProblemDetail for RFC 7807 compliance</li>
 *   <li>Supports error classification and HTTP status determination</li>
 * </ul>
 * 
 * <h3>{@link eu.infolead.jtk.fp.validation.Validator}</h3>
 * <p>Utility class for creating and combining validators:</p>
 * <ul>
 *   <li>Fluent API for building complex validation logic</li>
 *   <li>Field-level validators with path tracking</li>
 *   <li>Object-level validation builders</li>
 *   <li>Common validation patterns (required, length, range)</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Single Field Validation</h3>
 * <pre>{@code
 * ValidationError emailError = ValidationError.invalidFormat("user.email", invalidEmail);
 * ValidationError requiredError = ValidationError.requiredWithId("user.name");
 * }</pre>
 * 
 * <h3>Multiple Field Validation</h3>
 * <pre>{@code
 * ValidationResult<User> result = ValidationResult.success(new User())
 *     .combine(validateEmail(user.getEmail()))
 *     .combine(validateAge(user.getAge()))
 *     .combine(validateName(user.getName()));
 * 
 * if (result.isFailure()) {
 *     List<ValidationError> errors = result.getErrors();
 *     ProblemDetail problem = result.toProblemDetail();
 * }
 * }</pre>
 * 
 * <h3>Fluent Validator API</h3>
 * <pre>{@code
 * ValidationResult<User> result = Validator.object(new User())
 *     .validate(User::getEmail, email -> Validator.field("email").required(email))
 *     .validate(User::getName, name -> Validator.field("name").lengthBetween(name, 2, 50))
 *     .validate(User::getAge, age -> Validator.field("age").rangeBetween(age, 0, 120))
 *     .result();
 * }</pre>
 * 
 * <h3>Applicative Validation (Accumulating)</h3>
 * <p>{@link eu.infolead.jtk.fp.either.Validation} short-circuits when used as a
 * monad ({@code flatMap} stops at the first invalid value). The applicative
 * combinators evaluate every operand and accumulate all errors into a
 * {@link eu.infolead.jtk.fp.validation.ValidationResult}:</p>
 * <pre>{@code
 * ValidationResult<Registration> result = Validation.zip(
 *         Validation.required("email", email),
 *         Validation.lengthBetween("name", name, 2, 50),
 *         Registration::new);
 *
 * if (result.isFailure()) {
 *     // every problem, not just the first one
 *     List<ValidationError> errors = result.getErrors();
 * }
 * }</pre>
 * <p>{@code Validation.combine(...)} is an alias for {@code zip}, and
 * {@code Validation.and(...)} keeps the left value when both operands are valid.
 * The generic instance methods
 * {@link eu.infolead.jtk.fp.either.Validation#zip(eu.infolead.jtk.fp.either.Validation, java.util.function.BiFunction)}
 * and
 * {@link eu.infolead.jtk.fp.either.Validation#and(eu.infolead.jtk.fp.either.Validation)}
 * accumulate any invalid type into a {@code Validation<List<I>, R>}.</p>
 *
 * <h3>Interop with the Railway</h3>
 * <p>The two worlds compose in both directions:</p>
 * <pre>{@code
 * // Validation -> ValidationResult
 * ValidationResult<User> result = ValidationResult.from(validation, error -> error);
 *
 * // ValidationResult -> either railway form
 * Validation<ValidationError, User> validation = result.toValidation(); // first error
 * Result<ValidationError, User> railway = result.toResult();            // first error
 * }</pre>
 *
 * <h3>Nested Field Paths</h3>
 * <p>{@link eu.infolead.jtk.fp.validation.FieldPath} parses dotted and indexed
 * paths, and {@link eu.infolead.jtk.fp.validation.ValidationError#under(String)}
 * re-nests an error without duplicating the prefix:</p>
 * <pre>{@code
 * ValidationError required = ValidationError.required("city").under("user.address");
 * required.fieldPath(); // "user.address.city"
 * required.path().segments(); // ["user", "address", "city"]
 *
 * FieldPath.parse("items[0].price").segments(); // ["items", "[0]", "price"]
 * }</pre>
 *
 * <h3>Cross-Field Invariants</h3>
 * <p>Constraints that span several fields belong to the object, not to any one
 * field. {@code ObjectValidator.invariant(...)} evaluates them against the whole
 * object and accumulates the failure with the field errors:</p>
 * <pre>{@code
 * ValidationResult<Booking> result = Validator.object(booking)
 *         .validateAndAccumulate(Booking::start,
 *                 start -> Validator.<LocalDate>field("start").required(start))
 *         .validateAndAccumulate(Booking::end,
 *                 end -> Validator.<LocalDate>field("end").required(end))
 *         .invariant("period",
 *                 value -> value.start() != null && value.start().isBefore(value.end()),
 *                 "start must be before end")
 *         .result();
 * }</pre>
 * <p>Invariant failures use
 * {@link eu.infolead.jtk.anomaly.StandardErrorType#CROSS_FIELD_VIOLATION} (HTTP
 * 400). An empty path attaches the error to the whole object and omits the field
 * prefix from the formatted message.</p>
 *
 * <h3>Error ID Tracking</h3>
 * <pre>{@code
 * ValidationError error = ValidationError.required("field")
 *     .withErrorId("REQ-USER-FIELD-001");
 * 
 * String errorId = error.getErrorId().fold(() -> "No ID", id -> id);
 * String formatted = error.format(); // [REQUIRED|REQ-USER-FIELD-001] field: ...
 * }</pre>
 * 
 * @see eu.infolead.jtk.anomaly.ErrorType
 * @see eu.infolead.jtk.anomaly.StandardErrorType
 * @see eu.infolead.jtk.anomaly.http.ProblemDetail
 */
package eu.infolead.jtk.fp.validation;