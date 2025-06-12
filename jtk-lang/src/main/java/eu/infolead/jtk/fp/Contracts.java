package eu.infolead.jtk.fp;

import java.util.function.Predicate;
import java.util.function.Supplier;

import eu.infolead.jtk.anomaly.InvariantError;
import eu.infolead.jtk.anomaly.InvariantErrorType;
import eu.infolead.jtk.anomaly.ParameterError;
import eu.infolead.jtk.anomaly.PostconditionError;
import eu.infolead.jtk.anomaly.PostconditionErrorType;
import eu.infolead.jtk.anomaly.PreconditionError;
import eu.infolead.jtk.anomaly.PreconditionErrorType;
import eu.infolead.jtk.anomaly.SystemError;
import eu.infolead.jtk.fp.either.Either;
import eu.infolead.jtk.fp.either.Result;

/**
 * Utility class for Design by Contract programming.
 * Provides methods for checking preconditions, postconditions, and invariants.
 * 
 * <p>Uses the unified SystemError hierarchy to provide both exception-based
 * and functional error handling approaches for contract violations.</p>
 */
public final class Contracts {

    private Contracts() {
        // Utility class
    }

    // ============= PRECONDITION CHECKS =============

    /**
     * Checks a precondition and throws an exception if violated.
     * 
     * @param condition the condition that must be true
     * @param message the error message if condition is false
     * @throws IllegalStateException if condition is false
     */
    public static void preCond(boolean condition, String message) {
        if (!condition) {
            throw new PreconditionError(message, PreconditionErrorType.PRECONDITION_FAILED, message).toException();
        }
    }

    /**
     * Checks a precondition with formatted message.
     * 
     * @param condition the condition that must be true
     * @param messageFormat the error message format
     * @param args arguments for message formatting
     * @throws IllegalStateException if condition is false
     */
    public static void preCond(boolean condition, String messageFormat, Object... args) {
        if (!condition) {
            String message = String.format(messageFormat, args);
            throw new PreconditionError(message, PreconditionErrorType.PRECONDITION_FAILED, message).toException();
        }
    }

    /**
     * Checks that an argument is not null.
     * 
     * @param <T> the type of the argument
     * @param argument the argument to check
     * @param parameterName the name of the parameter
     * @return the argument if not null
     * @throws IllegalArgumentException if argument is null
     */
    public static <T> T requireNonNull(T argument, String parameterName) {
        if (argument == null) {
            throw ParameterError.nullParameter(parameterName).toException();
        }
        return argument;
    }

    /**
     * Checks that an argument satisfies a predicate.
     * 
     * @param <T> the type of the argument
     * @param argument the argument to check
     * @param predicate the condition the argument must satisfy
     * @param parameterName the name of the parameter
     * @return the argument if valid
     * @throws IllegalArgumentException if predicate fails
     */
    public static <T> T requireArgument(T argument, Predicate<T> predicate, String parameterName) {
        if (!predicate.test(argument)) {
            throw ParameterError.unsupportedValue(parameterName, argument).toException();
        }
        return argument;
    }

    /**
     * Checks that an argument satisfies a predicate with custom message.
     * 
     * @param <T> the type of the argument
     * @param argument the argument to check
     * @param predicate the condition the argument must satisfy
     * @param message the error message if predicate fails
     * @return the argument if valid
     * @throws IllegalArgumentException if predicate fails
     */
    public static <T> T requireArgumentWithMessage(T argument, Predicate<T> predicate, String message) {
        if (!predicate.test(argument)) {
            throw ParameterError.unsupportedValue("argument", argument).toException();
        }
        return argument;
    }

    // ============= STATE CHECKS =============

    /**
     * Checks an invariant or state condition.
     * 
     * @param condition the condition that must be true
     * @param message the error message if condition is false
     * @throws IllegalStateException if condition is false
     */
    public static void requireState(boolean condition, String message) {
        if (!condition) {
            throw new InvariantError(message, InvariantErrorType.OBJECT_CONSISTENCY, message).toException();
        }
    }

    /**
     * Checks an invariant with formatted message.
     * 
     * @param condition the condition that must be true
     * @param messageFormat the error message format
     * @param args arguments for message formatting
     * @throws IllegalStateException if condition is false
     */
    public static void requireState(boolean condition, String messageFormat, Object... args) {
        if (!condition) {
            String message = String.format(messageFormat, args);
            throw new InvariantError(message, InvariantErrorType.OBJECT_CONSISTENCY, message).toException();
        }
    }

    // ============= POSTCONDITION CHECKS =============

    /**
     * Checks a postcondition.
     * 
     * @param condition the condition that must be true
     * @param message the error message if condition is false
     * @throws IllegalStateException if condition is false
     */
    public static void ensureThat(boolean condition, String message) {
        if (!condition) {
            throw new PostconditionError(message, PostconditionErrorType.EXPECTED_STATE_NOT_REACHED, message).toException();
        }
    }

    /**
     * Checks a postcondition with formatted message.
     * 
     * @param condition the condition that must be true
     * @param messageFormat the error message format
     * @param args arguments for message formatting
     * @throws IllegalStateException if condition is false
     */
    public static void ensureThat(boolean condition, String messageFormat, Object... args) {
        if (!condition) {
            String message = String.format(messageFormat, args);
            throw new PostconditionError(message, PostconditionErrorType.EXPECTED_STATE_NOT_REACHED, message).toException();
        }
    }

    /**
     * Ensures a result is not null before returning it.
     * 
     * @param <T> the type of the result
     * @param result the result to check
     * @param message the error message if result is null
     * @return the result if not null
     * @throws IllegalStateException if result is null
     */
    public static <T> T ensureNonNull(T result, String message) {
        if (result == null) {
            throw new PostconditionError(message, PostconditionErrorType.EXPECTED_STATE_NOT_REACHED).toException();
        }
        return result;
    }

    // ============= FUNCTIONAL CONTRACT CHECKS =============

    /**
     * Checks a precondition and returns Either.left if violated, Either.right if satisfied.
     * This allows for functional error handling instead of exceptions.
     * 
     * @param <T> the type of the value
     * @param value the value to return if condition is satisfied
     * @param condition the condition that must be true
     * @param message the error message if condition is false
     * @return Either.right(value) if condition is true, Either.left(error) otherwise
     */
    public static <T> Either<SystemError, T> checkPrecondition(T value, boolean condition, String message) {
        return condition ? Either.right(value) : 
               Either.left(new PreconditionError(message, PreconditionErrorType.PRECONDITION_FAILED, message));
    }

    /**
     * Checks a precondition using a predicate.
     * 
     * @param <T> the type of the value
     * @param value the value to check and return
     * @param predicate the condition the value must satisfy
     * @param errorMessage the error message if predicate fails
     * @return Either.right(value) if predicate passes, Either.left(error) otherwise
     */
    public static <T> Either<SystemError, T> checkPrecondition(T value, Predicate<T> predicate, String errorMessage) {
        return predicate.test(value) 
            ? Either.right(value) 
            : Either.left(new PreconditionError(errorMessage, PreconditionErrorType.PRECONDITION_FAILED, value));
    }

    /**
     * Checks a postcondition functionally.
     * 
     * @param <T> the type of the result
     * @param result the result to check and return
     * @param condition the condition that must be true
     * @param errorMessage the error message if condition is false
     * @return Either.right(result) if condition is true, Either.left(error) otherwise
     */
    public static <T> Either<SystemError, T> checkPostcondition(T result, boolean condition, String errorMessage) {
        return condition 
            ? Either.right(result) 
            : Either.left(new PostconditionError(errorMessage, PostconditionErrorType.EXPECTED_STATE_NOT_REACHED, result));
    }

    /**
     * Wraps a computation with automatic postcondition checking.
     * 
     * @param <T> the type of the result
     * @param computation the computation to perform
     * @param postcondition the condition the result must satisfy
     * @param errorMessage the error message if postcondition fails
     * @return Result with the computation result or contract error
     */
    public static <T> Result<SystemError, T> withPostcondition(Supplier<T> computation, 
                                                               Predicate<T> postcondition, 
                                                               String errorMessage) {
        try {
            T result = computation.get();
            return postcondition.test(result) 
                ? Result.success(result)
                : Result.failure(new PostconditionError(errorMessage, PostconditionErrorType.EXPECTED_STATE_NOT_REACHED, result));
        } catch (Exception e) {
            return Result.failure(new PostconditionError("Computation failed: " + e.getMessage(), PostconditionErrorType.EXPECTED_STATE_NOT_REACHED));
        }
    }
}