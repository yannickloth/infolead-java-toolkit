package eu.infolead.jtk.fp;

import static eu.infolead.jtk.fp.Bool.negate;

import java.util.function.Supplier;

import eu.infolead.jtk.fp.Result.Failure;
import eu.infolead.jtk.fp.Result.Success;

public sealed interface Result<T> permits Success, Failure {
    static <T> Result<T> success(final T value) {
        return new Success<>(value);
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    default <U> Result<U> flatMap(final Supplier<Result<U>> resultSupplier) {
        return fold(a -> (Result<U>) this, a -> resultSupplier.get());
    }

    record Success<T>(T value) implements Result<T> {

        @Override
        public String toString() {
            return "Success(" + value.toString() + ")";
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
            return "Failure(" + anomaly + ")";
        }

        @Override
        public <U> U fold(final Mapper<? extends U, ? super Anomaly> failureMapper,
                final Mapper<? extends U, ? super T> successMapper) {
            return failureMapper.map(anomaly);
        }
    }
}
