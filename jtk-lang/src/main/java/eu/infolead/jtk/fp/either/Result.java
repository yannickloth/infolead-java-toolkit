package eu.infolead.jtk.fp.either;

import static eu.infolead.jtk.logic.Bool.negate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.fp.Provider;
import eu.infolead.jtk.fp.either.Result.EmptySuccess;
import eu.infolead.jtk.fp.either.Result.Failure;
import eu.infolead.jtk.fp.either.Result.Success;
import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;

/**
 * A result that is either a success carrying a value, or a failure carrying an
 * anomaly. Both the success value and the failure anomaly must be non-null.
 *
 * @param <F> the type of the failure anomaly
 * @param <S> the type of the success value
 */
@SuppressWarnings(CompilerWarning.RAW_TYPES)
public sealed interface Result<F, S> extends Either<F, S> permits EmptySuccess, Success, Failure {
    Result<?, ?> EMPTY_SUCCESS = new EmptySuccess<>();

    /**
     * Creates a successful result with the specified value.
     *
     * @param <F>   the failure type
     * @param <S>   the success type
     * @param value the successful return value. Must not be {@code null}.
     * @return a successful result carrying {@code value}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    static <F, S> Result<F, S> success(final S value) {
        return new Success<>(Objects.requireNonNull(value, "value"));
    }

    /**
     * Creates an empty successful result, for operations whose success carries
     * no value.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @return the shared empty-success instance
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    static <F, S> Result<F, S> success() {
        return (Result<F, S>) Result.EMPTY_SUCCESS;
    }

    /**
     * Creates a failed result with the specified anomaly.
     *
     * @param <F>     the failure type
     * @param <S>     the success type
     * @param anomaly the failure anomaly. Must not be {@code null}.
     * @return a failed result carrying {@code anomaly}
     * @throws NullPointerException if {@code anomaly} is {@code null}
     */
    static <F, S> Result<F, S> failure(final F anomaly) {
        return new Failure<>(Objects.requireNonNull(anomaly, "anomaly"));
    }

    /**
     * Returns the first successful result, or the first failure when all results
     * fail.
     *
     * @param <F>     the failure type
     * @param <S>     the success type
     * @param results the results to inspect; must contain at least one element
     * @return the first successful result, or the first failure
     * @throws IllegalArgumentException if {@code results} is empty
     */
    @SafeVarargs
    static <F, S> Result<F, S> any(final Result<F, S>... results) {
        Objects.requireNonNull(results, "results");
        if (results.length == 0) {
            throw new IllegalArgumentException("results must contain at least one element");
        }
        Result<F, S> firstFailure = null;
        for (final Result<F, S> result : results) {
            Objects.requireNonNull(result, "results must not contain null");
            if (result.isSuccess().toBoolean()) {
                return result;
            }
            if (firstFailure == null) {
                firstFailure = result;
            }
        }
        return firstFailure;
    }

    /**
     * Returns the first successful result produced by the providers, or the first
     * failure when all results fail. Every provider is invoked at most once.
     *
     * @param <F>             the failure type
     * @param <S>             the success type
     * @param resultProviders the result providers; must contain at least one
     * @return the first successful result, or the first failure
     * @throws IllegalArgumentException if {@code resultProviders} is empty
     */
    @SafeVarargs
    static <F, S> Result<F, S> any(final Provider<Result<F, S>>... resultProviders) {
        Objects.requireNonNull(resultProviders, "resultProviders");
        if (resultProviders.length == 0) {
            throw new IllegalArgumentException("resultProviders must contain at least one element");
        }
        Result<F, S> firstFailure = null;
        for (final Provider<Result<F, S>> provider : resultProviders) {
            Objects.requireNonNull(provider, "resultProviders must not contain null");
            final Result<F, S> result = Objects.requireNonNull(provider.get(), "provider returned null");
            if (result.isSuccess().toBoolean()) {
                return result;
            }
            if (firstFailure == null) {
                firstFailure = result;
            }
        }
        return firstFailure;
    }

    /**
     * Checks whether this result is successful.
     *
     * @return {@link Bool#TRUE} for a success, {@link Bool#FALSE} for a failure
     */
    Bool isSuccess();

    default Bool isFailure() {
        return negate(isSuccess());
    }

    @Override
    default <R1> Result<F, R1> map(Mapper<R1, ? super S> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return fold(
                l -> Result.failure(l),
                r -> Result.success(mapper.map(r)));
    }

    @Override
    default <R1> Result<F, R1> map(final Provider<R1> provider) {
        Objects.requireNonNull(provider, "provider");
        return fold(Result::failure, r -> Result.success(provider.get()));
    }

    /**
     * Successful result without a value. Mapping, flat-mapping, and applying an
     * empty success keep it empty instead of inventing a value or invoking a
     * success consumer with {@code null}.
     * <p>
     * {@link #fold} has no empty branch, so its right mapper still receives
     * {@code null}; callers that need empty-aware handling should use
     * {@link #isSuccess()} together with {@link #map}.
     */
    final class EmptySuccess<F, S> extends AbstractRight<F, S> implements Result<F, S> {
        private static final long serialVersionUID = 1L;

        private EmptySuccess() {
            super();
        }

        @Override
        public Bool isSuccess() {
            return isRight();
        }

        @Override
        public <R1> Result<F, R1> map(final Mapper<R1, ? super S> mapper) {
            Objects.requireNonNull(mapper, "mapper");
            return Result.success();
        }

        @Override
        public <R1> Result<F, R1> map(final Provider<R1> provider) {
            Objects.requireNonNull(provider, "provider");
            return Result.success();
        }

        @SuppressWarnings(CompilerWarning.UNCHECKED)
        @Override
        public <G extends F, D, E extends Either<G, D>> E flatMap(
                final Mapper<E, ? super S> mapper) {
            Objects.requireNonNull(mapper, "mapper");
            return (E) this;
        }

        @SuppressWarnings(CompilerWarning.UNCHECKED)
        @Override
        public <G extends F, D, E extends Either<G, D>> E flatMap(final Supplier<E> supplier) {
            Objects.requireNonNull(supplier, "supplier");
            return Objects.requireNonNull(supplier.get(), "supplier returned null");
        }

        @SuppressWarnings(CompilerWarning.UNCHECKED)
        @Override
        public <E extends Either<F, S>> E apply(final Consumer<? super F> leftConsumer,
                final Consumer<? super S> rightConsumer) {
            Objects.requireNonNull(leftConsumer, "leftConsumer");
            Objects.requireNonNull(rightConsumer, "rightConsumer");
            return (E) this;
        }

        @Override
        public Result<S, F> swap() {
            throw new UnsupportedOperationException(
                    "Swapping an empty Success is not allowed, as there is no failure without an anomaly.");
        }

        @Override
        public String toString() {
            return "EmptySuccess";
        }

        private Object readResolve() {
            return Result.success();
        }
    }

    final class Success<F, S> extends AbstractRight<F, S> implements Result<F, S> {
        private static final long serialVersionUID = 1L;

        private Success(final S value) {
            super(Objects.requireNonNull(value, "value"));
        }

        private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
            input.defaultReadObject();
            Objects.requireNonNull(value(), "deserialized Success value must not be null");
        }

        @Override
        public Bool isSuccess() {
            return isRight();
        }

        @Override
        public Result<S, F> swap() {
            return Result.failure(value());
        }
    }

    final class Failure<F, S> extends AbstractLeft<F, S> implements Result<F, S> {
        private static final long serialVersionUID = 1L;

        private Failure(final F value) {
            super(Objects.requireNonNull(value, "anomaly"));
        }

        private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
            input.defaultReadObject();
            Objects.requireNonNull(value(), "deserialized Failure anomaly must not be null");
        }

        @Override
        public Bool isSuccess() {
            return isRight();
        }

        @Override
        public Result<S, F> swap() {
            return Result.success(value());
        }
    }
}
