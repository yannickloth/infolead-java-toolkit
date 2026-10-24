package eu.infolead.jtk.async;

import eu.infolead.jtk.fp.either.Maybe;
import eu.infolead.jtk.fp.either.Result;
import eu.infolead.jtk.logic.Bool;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * The outcome of a traversal that ran every item, keeping successes and failures apart.
 *
 * <p>Both lists preserve input order, so the result is deterministic regardless of completion
 * order. Items that were cancelled because a sibling failed are in neither list: they produced
 * neither a value nor a genuine failure.
 *
 * <pre>{@code
 * Partition<SystemError, Invoice> partition = ...;
 *
 * List<Invoice> imported = partition.successes();
 * for (SystemError failure : partition.failures()) {
 *     log.warn("invoice rejected: {}", failure.format());
 * }
 * // Or collapse to the railway: first failure, else all successes.
 * Result<SystemError, List<Invoice>> result = partition.toResult();
 * }</pre>
 *
 * @param <F> the failure type
 * @param <S> the success type
 * @param failures the failures, in input order
 * @param successes the successes, in input order
 */
public record Partition<F, S>(List<F> failures, List<S> successes) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Canonical constructor that makes both lists immutable.
     *
     * @param failures the failures
     * @param successes the successes
     */
    public Partition {
        Objects.requireNonNull(failures, "failures");
        Objects.requireNonNull(successes, "successes");
        failures = List.copyOf(failures);
        successes = List.copyOf(successes);
    }

    /**
     * Collapses this partition into a railway value: the first failure, or all successes when there
     * is no failure.
     *
     * @return the collapsed result
     */
    public Result<F, List<S>> toResult() {
        if (!failures.isEmpty()) {
            return Result.failure(failures.get(0));
        }
        return Result.success(successes);
    }

    /**
     * Returns the first failure, if any.
     *
     * @return the first failure in input order
     */
    public Maybe<F> firstFailure() {
        return failures.isEmpty() ? Maybe.none() : Maybe.of(failures.get(0));
    }

    /**
     * Returns whether every item succeeded.
     *
     * @return {@link Bool#TRUE} when there is no failure
     */
    public Bool isCompleteSuccess() {
        return Bool.of(failures.isEmpty());
    }
}
