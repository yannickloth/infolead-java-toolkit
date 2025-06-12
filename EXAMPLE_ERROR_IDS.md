# Error ID System Examples

This document demonstrates the new error ID system with type-identifying numbers and instance identifiers.

## Error Type Numbers

The error system uses a structured numbering scheme:

- **1000-1999**: Validation errors (client input issues)
- **2000-2999**: Resource errors (not found, conflicts)  
- **3000-3999**: Security errors (auth, permissions)
- **4000-4999**: Rate limiting errors
- **5000-5999**: Server errors (internal, external services)
- **6000-6999**: Business logic errors
- **9000-9999**: Contract violations (programming errors)

## Error ID Format

Error IDs follow the pattern: `{typeNumber}-{instanceId}`

Examples:
- `1001-1638123456789` - Required field validation error with timestamp instance ID
- `2001-1638123456790` - Not found error with timestamp instance ID
- `9001-1638123456791` - Precondition violation with timestamp instance ID

## Usage Examples

### Validation Errors with IDs

```java
import eu.infolead.jtk.fp.validation.ValidationError;
import eu.infolead.jtk.fp.validation.ValidationResult;
import eu.infolead.jtk.fp.validation.Validator;

// Basic validation error without ID
ValidationError basicError = ValidationError.required("user.email");
System.out.println(basicError.format());
// Output: [REQUIRED] user.email: Required field is missing or null

// Validation error with auto-generated ID
ValidationError errorWithId = ValidationError.requiredWithId("user.email");
System.out.println(errorWithId.format());
// Output: [REQUIRED|1001-1638123456789] user.email: Required field is missing or null

// Add ID to existing error
ValidationError withGeneratedId = basicError.withGeneratedId();
System.out.println(withGeneratedId.format());
// Output: [REQUIRED|1001-1638123456790] user.email: Required field is missing or null

// Custom error ID
ValidationError withCustomId = basicError.withErrorId("REQ-USER-EMAIL-001");
System.out.println(withCustomId.format());
// Output: [REQUIRED|REQ-USER-EMAIL-001] user.email: Required field is missing or null
```

### Contract Errors with IDs

```java
// Basic contract error without ID
ContractError contractError = ContractError.precondition("value must be positive");
System.out.println(contractError.format());
// Output: [PRECONDITION] value must be positive

// Contract error with auto-generated ID
ContractError contractWithId = contractError.withGeneratedId();
System.out.println(contractWithId.format());
// Output: [PRECONDITION|9001-1638123456791] value must be positive

// Map to standard error type for HTTP responses
ErrorType errorType = contractError.getStandardErrorType();
System.out.println("Error Type: " + errorType.getCode());
System.out.println("Type Number: " + errorType.getTypeNumber());
System.out.println("HTTP Status: " + errorType.getHttpStatus());
// Output:
// Error Type: PRECONDITION_VIOLATION
// Type Number: 9001
// HTTP Status: 500
```

### Working with Error IDs

```java
import eu.infolead.jtk.fp.validation.ValidationError;
import eu.infolead.jtk.anomaly.ErrorType;
import eu.infolead.jtk.anomaly.StandardErrorType;

// Check if error has ID
ValidationError error = ValidationError.required("field");
        Maybe<String> errorId = error.getErrorId();
if(errorId.

        isSome()){
        System.out.

        println("Error ID: "+errorId.get());
        }else{
        System.out.

        println("No error ID assigned");
}

        // Generate error ID manually
        ErrorType errorType = StandardErrorType.INVALID_FORMAT;
        String customInstanceId = "USR-12345";
        String errorId = errorType.generateErrorId(Long.parseLong(customInstanceId.hashCode() + ""));
System.out.

        println("Generated ID: "+errorId);
// Output: Generated ID: 1002-{hash-based-number}

        // Use timestamp-based ID
        String timestampId = errorType.generateErrorId();
System.out.

        println("Timestamp ID: "+timestampId);
// Output: Timestamp ID: 1002-1638123456792
```

### Integration with ProblemDetail

```java
import eu.infolead.jtk.fp.validation.ValidationError;
import eu.infolead.jtk.anomaly.ErrorType;
import eu.infolead.jtk.anomaly.StandardErrorType;
import eu.infolead.jtk.anomaly.http.ProblemDetail;

// Error types automatically include their type numbers in problem details
ErrorType errorType = StandardErrorType.TOO_LONG;
        ProblemDetail problem = errorType.toProblemDetail("Field exceeds maximum length");

System.out.

        println("Problem Type: "+problem.getProblemType());
        System.out.

        println("Title: "+problem.getProblemTitle());
        System.out.

        println("Status: "+problem.getHttpStatus());
// Error type number is embedded in the problem type URI

        // Validation errors with IDs can be converted to ProblemDetail
        ValidationError error = ValidationError.tooLongWithId("description", "very long text", 100);
        ProblemDetail problemFromError = error.toProblemDetail();
```

## Monitoring and Logging Benefits

With error IDs, you can:

1. **Track specific error instances** across logs and monitoring systems
2. **Correlate errors** between different services and components  
3. **Analyze error patterns** by type number groupings
4. **Provide users with trackable references** for support requests
5. **Implement error tracking dashboards** grouped by error type ranges

## Custom Error Types

When creating custom error type enums, follow the numbering convention:

```java
public enum CustomErrorType implements ErrorType {
    CUSTOM_VALIDATION_ERROR(
        1100,  // Custom validation errors in 1100-1199 range
        "CUSTOM_VALIDATION_ERROR",
        "Custom validation failed: %s",
        400,
        "https://mycompany.com/errors/custom-validation",
        "Custom Validation Error",
        ErrorCategory.VALIDATION
    );
    
    // Implementation details...
}
```

This ensures error IDs remain globally unique across your application ecosystem.