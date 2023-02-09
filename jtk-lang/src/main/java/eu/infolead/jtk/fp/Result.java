package eu.infolead.jtk.fp;

import static eu.infolead.jtk.logic.Bool.negate;

import eu.infolead.jtk.fp.Result.EmptySuccess;
import eu.infolead.jtk.fp.Result.Failure;
import eu.infolead.jtk.fp.Result.Success;
import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;

/**
 * This class permits clean error handling.
 */
@SuppressWarnings(CompilerWarning.RAW_TYPES)
public sealed interface Result<F, S> extends Either<F, S> permits EmptySuccess, Success, Failure {
    Result<?, ?> EMPTY_SUCCESS = new EmptySuccess<Void, Void>();

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

    // <U> U fold(Mapper<? extends U, ? super F> failureMapper,
    // Mapper<? extends U, ? super S> successMapper);

    default Bool isSuccess() {
        return Bool.FALSE;
    }

    default Bool isFailure() {
        return negate(isSuccess());
    }

    // /**
    // * This uses the specified {@link Mapper} to map the resulting {@link Result}
    // to
    // * {@code this} successful {@link Result}.
    // * If {@code this} is a {@link Failure}, the specified {@link Mapper} is not
    // * applied, and this {@link Failure} is returned (and thus its contents are
    // * passed downstream).
    // *
    // * @param mapper the mapping function
    // *
    // * @return the new {@link Result} if {@code this} is a {@link Success}, else
    // * {@code this}.
    // * @param <U> the type of the result's successful value.
    // */
    // @SuppressWarnings(CompilerWarning.UNCHECKED)
    // default <F1, S1> Result<F1, S1> flatMap(final Mapper<Result<F1, S1>, ? super
    // S1> mapper) {
    // return fold(a -> (Result<F1, S1>) this, mapper);
    // }

    // /**
    // * This replaces this successful {@link Result} with the instance received
    // from
    // * the specified {@link Supplier}.
    // * If {@code this} is a {@link Failure}, the specified {@link Supplier} is not
    // * used, and this {@link Failure} is returned (and thus its contents are
    // passed
    // * downstream).
    // *
    // * @param mapper the mapping function
    // * @return the new {@link Result} if {@code this} is a {@link Success}, else
    // * {@code this}.
    // * @param <U> the type of the result's successful value.
    // */
    // @SuppressWarnings(CompilerWarning.UNCHECKED)
    // default <U> Result<U> flatMap(final Supplier<Result<U>> resultSupplier) {
    // return fold(a -> (Result<U>) this, a -> resultSupplier.get());
    // }

    // default S or(final S replacement) {
    // return fold(anomaly -> replacement, Fn::identity);
    // }

    // default S or(final Supplier<S> supplier) {
    // return fold(anomaly -> supplier.get(), Fn::identity);
    // }

    // @Override
    // default S orNull() {
    // return fold(Fn::toNull, Fn::identity);
    // }

    // default Result<F, S> orElse(final Result<F, S> replacement) {
    // return fold(anomaly -> replacement, successValue -> this);
    // }

    // default Result<F, S> orElse(final Supplier<Result<F, S>> supplier) {
    // return fold(anomaly -> supplier.get(), successValue -> this);
    // }

    final class EmptySuccess<F, S> extends Either.Right<F, S> implements Result<F, S> {
        public EmptySuccess() {
            super(null);
        }

        @Override
        public Bool isSuccess() {
            return Bool.TRUE;
        }

        @Override
        public String toString() {
            return "{}";
        }

        // @Override
        // public <U> U fold(final Mapper<? extends U, ? super F> failureMapper,
        // final Mapper<? extends U, ? super S> successMapper) {
        // return successMapper.map(null);
        // }
    }

    final class Success<F, S> extends Either.Right<F, S> implements Result<F, S> {
        public Success(S value) {
            super(value);
        }

        @Override
        public Bool isSuccess() {
            return Bool.TRUE;
        }

        @Override
        public String toString() {
            return value().toString();
        }

        // @Override
        // public <U> U fold(final Mapper<? extends U, ? super Anomaly> failureMapper,
        // final Mapper<? extends U, ? super T> successMapper) {
        // return successMapper.map(value);
        // }
    }

    final class Failure<F, S> extends Either.Left<F, S> implements Result<F, S> {
        public Failure(F value) {
            super(value);
        }

        @Override
        public String toString() {
            return value().toString();
        }

        // @Override
        // public <U> U fold(final Mapper<? extends U, ? super Anomaly> failureMapper,
        // final Mapper<? extends U, ? super T> successMapper) {
        // return failureMapper.map(anomaly);
        // }
    }
}
