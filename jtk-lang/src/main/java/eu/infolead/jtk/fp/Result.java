package eu.infolead.jtk.fp;

import static eu.infolead.jtk.logic.Bool.negate;

import java.util.Objects;
import java.util.function.Supplier;

import eu.infolead.jtk.fp.Result.Failure;
import eu.infolead.jtk.fp.Result.Success;
import eu.infolead.jtk.fp.Result.EmptySuccess;
import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;

@SuppressWarnings(CompilerWarning.RAW_TYPES)
public sealed interface Result<T> extends Mappable<Result<T>> permits EmptySuccess, Success, Failure {
    Result<?> EMPTY_SUCCESS = new EmptySuccess<>();

    /**
     * 
     * @param <T>
     * @param value the successful return value. Must not be {@code null}.
     * @return
     */
    static <T> Result<T> success(final T value) {
        return new Success<>(value);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    static <T> Result<T> success() {
        return (Result<T>) Result.EMPTY_SUCCESS;
    }

    static <T> Result<T> failure(final Anomaly anomaly) {
        return new Failure<>(anomaly);
    }

    <U> U fold(Mapper<? extends U, ? super Anomaly> failureMapper,
            Mapper<? extends U, ? super T> successMapper);

    default Bool isSuccess() {
        return Bool.FALSE;
    }

    default Bool isFailure() {
        return negate(isSuccess());
    }

    /**
     * This uses the specified {@link Mapper} to map the resulting {@link Result} to
     * {@code this} successful {@link Result}.
     * If {@code this} is a {@link Failure}, the specified {@link Mapper} is not
     * applied.
     *
     * @param mapper the mapping function
     *
     * @return the new {@link Result} if {@code this} is a {@link Success}, else
     *         {@code this}.
     * @param <U> the type of the result's successful value.
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <U> Result<U> flatMap(final Mapper<Result<U>, ? super T> mapper) {
        return fold(a -> (Result<U>) this, mapper);
    }

    /**
     * This replaces this successful {@link Result} with the instance received from
     * the specified {@link Supplier}.
     * If {@code this} is a {@link Failure}, the specified {@link Supplier} is not
     * used.
     *
     * @param mapper the mapping function
     * @return the new {@link Result} if {@code this} is a {@link Success}, else
     *         {@code this}.
     * @param <U> the type of the result's successful value.
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <U> Result<U> flatMap(final Supplier<Result<U>> resultSupplier) {
        return fold(a -> (Result<U>) this, a -> resultSupplier.get());
    }

    default T or(final T replacement) {
        return fold(anomaly -> replacement, Fn::identity);
    }

    default T or(final Supplier<T> supplier) {
        return fold(anomaly -> supplier.get(), Fn::identity);
    }

    default T orNull() {
        return fold(Fn::toNull, Fn::identity);
    }

    default Result<T> orElse(final Result<T> replacement) {
        return fold(anomaly -> replacement, successValue -> this);
    }

    default Result<T> orElse(final Supplier<Result<T>> supplier) {
        return fold(anomaly -> supplier.get(), successValue -> this);
    }

    default Result<T> orElseNull() {
        return fold(Fn::toNull, successValue -> this);
    }

    record EmptySuccess<T>() implements Result<T> {
        @Override
        public Bool isSuccess() {
            return Bool.TRUE;
        }

        @Override
        public String toString() {
            return "{}";
        }

        @Override
        public <U> U fold(final Mapper<? extends U, ? super Anomaly> failureMapper,
                final Mapper<? extends U, ? super T> successMapper) {
            return successMapper.map(null);
        }
    }

    record Success<T>(T value) implements Result<T> {
        public Success(T value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public Bool isSuccess() {
            return Bool.TRUE;
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public <U> U fold(final Mapper<? extends U, ? super Anomaly> failureMapper,
                final Mapper<? extends U, ? super T> successMapper) {
            return successMapper.map(value);
        }
    }

    record Failure<T>(Anomaly anomaly) implements Result<T> {

        @Override
        public String toString() {
            return anomaly.toString();
        }

        @Override
        public <U> U fold(final Mapper<? extends U, ? super Anomaly> failureMapper,
                final Mapper<? extends U, ? super T> successMapper) {
            return failureMapper.map(anomaly);
        }
    }
}
