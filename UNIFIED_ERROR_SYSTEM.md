# Unified Error System

The new unified error system provides a principled approach to error classification based on **what the error represents** rather than **who caused it**. All errors map cleanly to HTTP status codes and RFC 7807 ProblemDetail format.

## Error Hierarchy

```java
public sealed interface SystemError permits 
    ParameterError,      // Invalid method arguments (HTTP 400)
    PreconditionError,   // System not ready for operation (HTTP 4xx)  
    PostconditionError,  // System failed to reach expected state (HTTP 5xx)
    BusinessRuleError,   // Domain constraint violations (HTTP 422)
    AuthorizationError,  // Permission/access violations (HTTP 401/403)
    ResourceError,       // Resource availability issues (HTTP 404/409)
    InvariantError       // Object/system consistency violations (HTTP 500)
```

## Error Type Number Ranges

- **1000-1999**: Parameter validation errors
- **2000-2999**: Precondition errors (system state)
- **3000-3999**: Postcondition errors (expected state not reached)  
- **4000-4999**: Business rule violations
- **5000-5999**: Authorization/security errors
- **6000-6999**: Resource errors
- **7000-7999**: Invariant violations

## Usage Examples

### Parameter Validation (HTTP 400)

```java
// Null parameter
ParameterError nullError = ParameterError.nullParameter("userId");
// Format: [NULL_PARAMETER|1001-timestamp] userId: Parameter cannot be null

// Out of range
ParameterError rangeError = ParameterError.outOfRange("age", -5, 0, 150);
// Format: [OUT_OF_RANGE|1002-timestamp] age: Parameter value must be between 0 and 150

// Invalid format with generated ID
ParameterError formatError = ParameterError.invalidFormat("email", "not-an-email")
    .withGeneratedId();
```

### Precondition Errors (HTTP 4xx - Client Error)

```java
// System not ready (HTTP 503)
PreconditionError systemError = PreconditionError.systemNotReady("Database maintenance in progress");

// Conflicting state (HTTP 409)
PreconditionError conflictError = PreconditionError.conflictingState("User already logged in");

// Insufficient permissions (HTTP 403)
PreconditionError permissionError = PreconditionError.insufficientPermissions("admin role required");

// Rate limiting (HTTP 429)
PreconditionError rateLimitError = PreconditionError.rateLimitExceeded("100 requests per minute");
```

### Postcondition Errors (HTTP 5xx - Server Error)

```java
// Expected state not reached (HTTP 500)
PostconditionError stateError = PostconditionError.expectedStateNotReached("ACTIVE", "PENDING");

// Invariant violated (HTTP 500)
PostconditionError invariantError = PostconditionError.invariantViolated("Account balance cannot be negative");

// Side effect failed (HTTP 500)
PostconditionError sideEffectError = PostconditionError.sideEffectFailed("Email notification not sent");

// External integration failed (HTTP 502)
PostconditionError integrationError = PostconditionError.integrationFailed("PaymentService", "processPayment");
```

### Business Rule Violations (HTTP 422)

```java
// Insufficient funds
BusinessRuleError fundsError = BusinessRuleError.insufficientFunds("ACC123", 1000.0, 500.0);

// Age restriction
BusinessRuleError ageError = BusinessRuleError.ageRestriction(18, 16);

// Currency mismatch (see jtk-currency Money)
BusinessRuleError currencyError = BusinessRuleError.currencyMismatch("EUR", "USD");
```

### Authorization Errors (HTTP 401/403)

```java
// Not authenticated (HTTP 401)
AuthorizationError authError = AuthorizationError.notAuthenticated();

// Insufficient permissions (HTTP 403)
AuthorizationError permError = AuthorizationError.insufficientPermissions("ADMIN");
```

### Resource Errors (HTTP 404/409)

```java
// Resource not found (HTTP 404)
ResourceError notFoundError = ResourceError.notFound("User", "123");

// Resource already exists (HTTP 409)
ResourceError existsError = ResourceError.alreadyExists("User", "email@example.com");
```

### Invariant Violations (HTTP 500)

```java
// Object consistency violated
InvariantError consistencyError = InvariantError.objectConsistency("User has email but no email_verified flag");

// Data corruption detected
InvariantError corruptionError = InvariantError.dataCorruption("Checksum mismatch in user data");
```

## ProblemDetail Integration

All errors automatically convert to RFC 7807 compliant ProblemDetail:

```java
SystemError error = ParameterError.nullParameter("userId").withGeneratedId();

// Convert to ProblemDetail
ProblemDetail problem = error.toProblemDetail();
System.out.println("Type: " + problem.getProblemType());
System.out.println("Title: " + problem.getProblemTitle());
System.out.println("Status: " + problem.getHttpStatus());
System.out.println("Detail: " + problem.getProblemDetail());

// JSON output (when serialized):
{
  "type": "https://errors.infolead.eu/problems/validation/parameter/null-parameter",
  "title": "Bad Request - Null Parameter", 
  "status": 400,
  "detail": "userId: Parameter cannot be null"
}
```

## Exception Integration

All errors can be converted to appropriate Java exceptions:

```java
SystemError error = PostconditionError.invariantViolated("Balance negative");

// Convert to exception based on error category
RuntimeException ex = error.toException();
// Returns: IllegalStateException("[INVARIANT_VIOLATED|3002-timestamp] Invariant 'Balance negative': System invariant violated: Balance negative")

// Exception type mapping:
// - ParameterError → IllegalArgumentException
// - PreconditionError → IllegalStateException  
// - PostconditionError → IllegalStateException
// - BusinessRuleError → IllegalStateException
// - AuthorizationError → SecurityException
// - ResourceError → IllegalStateException
// - InvariantError → IllegalStateException

### Capturing exceptions into results (`fromTry`)

The exception boundary goes both ways: `Either.fromTry`, `Result.fromTry` and
`liftTry` capture exceptions from throwing code into railway values, mapping
them to error/anomaly values via a caller-supplied mapper. The computations
are passed as `CheckedSupplier`, `CheckedFunction` and `CheckedRunnable`
(`eu.infolead.jtk.fp`), so lambdas and method references that declare checked
exceptions need no try/catch wrapping:

```java
// The lambda throws IOException (checked) — no try/catch needed:
Result<String, String> config = Result.fromTry(
    ex -> "Cannot read config: " + ex.getMessage(),   // map Exception → error value
    () -> Files.readString(Path.of("config.properties")));
```

**Error vs Exception policy.** These boundaries capture `java.lang.Exception`
instances — checked exceptions and runtime exceptions alike. `java.lang.Error`
instances (`OutOfMemoryError`, `StackOverflowError`, `LinkageError`, ...) are
deliberately **not** captured and keep propagating:

- Errors signal JVM-level failures from which the process cannot reliably
  continue; converting them to values would let the program resume in an
  undefined state.
- The toolkit's `SystemError` anomaly model represents recoverable domain and
  system failures (with HTTP mappings), not JVM failures.
- Handle `Error`s only at thread or process boundaries, such as an
  application-wide exception handler.

```

## Operational Metadata

Errors can be enriched without changing their concrete record type. The
`with...` methods return a `ContextualSystemError` decorator that carries the
extra metadata.

### Cause chaining

```java
SystemError error = PostconditionError.persistenceFailed("Order", 42)
    .withCause(new IOException("disk full"))
    .withSuppressedException(new IllegalStateException("retry failed"));

RuntimeException exception = error.toException();
// exception.getCause()       -> IOException
// exception.getSuppressed()  -> IllegalStateException
```

### Retryability

Server errors and HTTP 429 are retryable. Deterministic contract violations and
critical integrity failures are not, because repeating the request cannot make
them succeed. Rate limits suggest a one-second delay and service-unavailable
responses suggest five seconds; a caller can override either value.

```java
SystemError error = PreconditionError.rateLimitExceeded("100/min")
    .withRetryAfter(Duration.ofSeconds(30));

error.isRetryable();                 // true
error.retryAfter();                  // Maybe.of(30s)
error.retryAfterHeader();            // Maybe.of("30")
```

### Correlation context

```java
SystemError error = ResourceError.notFound("User", 7)
    .withCorrelationId("corr-123")
    .withTraceId("trace-456")
    .withRequestId("req-789")
    .withTenantId("tenant-a");
```

These identifiers are emitted as RFC 7807 extension members
(`correlationId`, `traceId`, `requestId`, `tenantId`), alongside
`retryable` and `retryAfter`.

### Redaction

Wrap secrets in `Sensitive`, or attach a redaction policy:

```java
ParameterError.invalidFormat("password", Sensitive.of(rawPassword));

Map<String, Object> payload = Map.of("user", "alice", "password", "hunter2");
SystemError safe = ParameterError.invalidFormat("payload", payload)
    .withRedaction(RedactionPolicy.standard());
```

`Sensitive` renders as `[REDACTED]` everywhere: messages, `format()`,
`toString()` and problem details. `RedactionPolicy.standard()` also masks map
entries whose key looks like a credential (`password`, `token`,
`authorization`, ...).

### Localization

Every error type has a stable message key, for example
`error.validation.parameter.null-parameter`. The resolver picks a language from
`Accept-Language` and falls back to the declared English title/template when a
translation is missing.

```java
ErrorLocalizer localizer =
    ErrorLocalizer.acceptLanguage(request.getHeader("Accept-Language"));
ProblemDetail problem = error.toProblemDetail(instance, localizer);
```

`about:blank` problem titles are resolved to the localized HTTP reason phrase
through `ProblemDetail.localized(Locale)` and
`StandardProblemDetail.aboutBlank(status, locale)`. Translations live in
`jtk-lang/src/main/resources/eu/infolead/jtk/anomaly/messages*.properties` and
`.../http/http-status*.properties`.

## Error Classification

```java
SystemError error = PreconditionError.rateLimitExceeded("100/min");

// HTTP status information
int status = error.getHttpStatus();           // 429
boolean isClient = error.isClientError();     // true
boolean isServer = error.isServerError();     // false

// Error metadata
ErrorType.ErrorCategory category = error.getCategory();  // RATE_LIMIT
ErrorType.ErrorSeverity severity = error.getSeverity();  // WARNING
String code = error.getErrorType().getCode();            // "RATE_LIMIT_EXCEEDED"
int typeNumber = error.getErrorType().getTypeNumber();   // 2006
```

## Benefits

### 1. **Clear Semantic Distinction**
- **Parameter errors**: Invalid arguments to methods
- **Precondition errors**: System not ready for operation (client's fault)
- **Postcondition errors**: System failed to work correctly (server's fault)
- **Business rule errors**: Domain-specific constraint violations
- **Authorization errors**: Permission and access control
- **Resource errors**: Resource availability and conflicts
- **Invariant errors**: Object/system consistency violations

### 2. **Proper HTTP Status Mapping**
- Parameter validation → 400 Bad Request
- Preconditions → 4xx (client error - operation not allowed now)
- Postconditions → 5xx (server error - system didn't work correctly)
- Business rules → 422 Unprocessable Entity
- Authorization → 401 Unauthorized / 403 Forbidden
- Resources → 404 Not Found / 409 Conflict
- Invariants → 500 Internal Server Error

### 3. **Comprehensive Error Tracking**
- Unique error IDs for tracking across systems
- Type numbers for error classification and monitoring
- Rich contextual information and actual values
- Automatic ProblemDetail generation for APIs

### 4. **Flexible Handling**
- Exception-based for fail-fast scenarios
- Functional Result types for error accumulation
- Consistent API across all error types
- Easy integration with existing validation frameworks

This unified approach eliminates the artificial distinction between "contract" and "validation" errors, providing a more principled classification based on error semantics and proper HTTP status mapping.