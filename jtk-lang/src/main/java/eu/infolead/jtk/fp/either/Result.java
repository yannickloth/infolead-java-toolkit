package eu.infolead.jtk.fp.either;

import static eu.infolead.jtk.logic.Bool.negate;

import java.util.Objects;

import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.fp.Provider;
import eu.infolead.jtk.fp.either.Result.EmptySuccess;
import eu.infolead.jtk.fp.either.Result.Failure;
import eu.infolead.jtk.fp.either.Result.Success;
import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;

/**
 * This class permits clean error handling.
 */
@SuppressWarnings(CompilerWarning.RAW_TYPES)
public sealed interface Result<F, S> extends Either<F, S> permits EmptySuccess, Success, Failure {
    Result<?, ?> EMPTY_SUCCESS = new EmptySuccess<>();

    /**
     * 
     * @param <T>
     * @param value the successful return value. Must not be {@code null}.
     * @return
     */
    static <F, S> Result<F, S> success(final S value) {
        return new Success<>(value);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    static <F, S> Result<F, S> success() {
        return (Result<F, S>) Result.EMPTY_SUCCESS;
    }

    static <F, S> Result<F, S> failure(final F anomaly) {
        return new Failure<>(anomaly);
    }

    static <F, S> Result<F, S> any(final Result<F, S>... results) {
        Objects.requireNonNull(results);
        Objects.requireNonNull(results[0]);
        for (final var r : results) {
            if (r.isSuccess().toBoolean()) {
                return r;
            }
        }
        return results[0];
    }

    static <F, S> Result<F, S> any(final Provider<Result<F, S>>... resultProviders) {
        Objects.requireNonNull(resultProviders);
        Objects.requireNonNull(resultProviders[0]);
        for (final var resultProvider : resultProviders) {
            final var r = resultProvider.get();
            if (r.isSuccess().toBoolean()) {
                return r;
            }
        }
        return resultProviders[0].get();
    }

    public Bool isSuccess();

    default Bool isFailure() {
        return negate(isSuccess());
    }

    @Override
    default <R1> Result<F, R1> map(Mapper<R1, ? super S> mapper) {
        return fold(
                l -> Result.failure(l),
                r -> Result.success(mapper.map(r)));
    }

    @Override
    default <R1> Result<F, R1> map(final Provider<R1> provider) {
        return fold(Result::failure, r -> Result.success(provider.get()));
    }

    final class EmptySuccess<F, S> extends AbstractRight<F, S> implements Result<F, S> {
        public EmptySuccess() {
            super();
        }

        @Override
        public Bool isSuccess() {
            return isRight();
        }

        @Override
        @SuppressWarnings(CompilerWarning.UNCHECKED)
        public Result<S, F> swap() {
            throw new UnsupportedOperationException(
                    "Swapping an empty Success is not allowed, as there is no failure without an anomaly.");
        }

        @Override
        public String toString() {
            return "{}";
        }
    }

    final class Success<F, S> extends AbstractRight<F, S> implements Result<F, S> {
        public Success(S value) {
            super(value);
        }

        @Override
        public Bool isSuccess() {
            return isRight();
        }

        @Override
        @SuppressWarnings(CompilerWarning.UNCHECKED)
        public Result<S, F> swap() {
            return Result.failure(value());
        }

        @Override
        public String toString() {
            return value().toString();
        }
    }

    final class Failure<F, S> extends AbstractLeft<F, S> implements Result<F, S> {
        public Failure(F value) {
            super(value);
        }

        @Override
        public Bool isSuccess() {
            return isRight();
        }

        @Override
        @SuppressWarnings(CompilerWarning.UNCHECKED)
        public Result<S, F> swap() {
            return Result.success(value());
        }

        @Override
        public String toString() {
            return value().toString();
        }
    }
}
