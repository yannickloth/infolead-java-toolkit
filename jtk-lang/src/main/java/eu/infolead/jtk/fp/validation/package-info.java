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
 * ValidationResult<User> result = Validator.ObjectValidator.forObject(new User())
 *     .validate(User::getEmail, Validator.field("email").required())
 *     .validate(User::getName, Validator.field("name").lengthBetween(2, 50))
 *     .validate(User::getAge, Validator.field("age").rangeBetween(0, 120))
 *     .result();
 * }</pre>
 * 
 * <h3>Error ID Tracking</h3>
 * <pre>{@code
 * ValidationError error = ValidationError.required("field")
 *     .withErrorId("REQ-USER-FIELD-001");
 * 
 * String errorId = error.getErrorId().orElse("No ID");
 * String formatted = error.format(); // [REQUIRED|REQ-USER-FIELD-001] field: ...
 * }</pre>
 * 
 * @see eu.infolead.jtk.anomaly.ErrorType
 * @see eu.infolead.jtk.anomaly.StandardErrorType
 * @see eu.infolead.jtk.anomaly.http.ProblemDetail
 */
package eu.infolead.jtk.fp.validation;