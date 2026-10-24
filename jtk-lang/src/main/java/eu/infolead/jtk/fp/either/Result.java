package eu.infolead.jtk.fp.either;

import static eu.infolead.jtk.logic.Bool.negate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.CheckedFunction;
import eu.infolead.jtk.fp.CheckedRunnable;
import eu.infolead.jtk.fp.CheckedSupplier;
import eu.infolead.jtk.fp.Fn;
import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.fp.Provider;
import eu.infolead.jtk.fp.either.Result.EmptySuccess;
import eu.infolead.jtk.fp.either.Result.Failure;
import eu.infolead.jtk.fp.either.Result.Success;
import eu.infolead.jtk.lang.CompilerWarning;
import eu.infolead.jtk.logic.Bool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A result that is either a success carrying a value, or a failure carrying an anomaly. Both the
 * success value and the failure anomaly must be non-null.
 *
 * @param <F> the type of the failure anomaly
 * @param <S> the type of the success value
 */
@SuppressWarnings(CompilerWarning.RAW_TYPES)
public sealed interface Result<F, S> extends Either<F, S> permits EmptySuccess, Success, Failure {

    /**
     * Reads the {@code left}/{@code right} JSON envelope.
     *
     * <p>A failure is written as {@code {"left": anomaly}}. A success is written as {@code
     * {"right": value}}, or as {@code {"right": null}} when the success carries no value ({@link
     * EmptySuccess}). Because a non-empty {@link Success} rejects a null payload, the null right
     * member unambiguously identifies the empty success.
     *
     * @param <F> the anomaly type
     * @param <S> the success type
     * @param failure the anomaly, or {@code null} for a success
     * @param success the success value, or {@code null} for a failure or empty success
     * @return the envelope as a {@code Result}
     * @throws IllegalArgumentException if both branches are present
     */
    @JsonCreator
    static <F, S> Result<F, S> fromJson(
            @JsonProperty("left") final F failure, @JsonProperty("right") final S success) {
        if (failure != null) {
            if (success != null) {
                throw new IllegalArgumentException(
                        "Result JSON envelope must contain exactly one of 'left' and 'right'");
            }
            return Result.failure(failure);
        }
        return success == null ? Result.success() : Result.success(success);
    }

    Result<?, ?> EMPTY_SUCCESS = new EmptySuccess<>();

    /**
     * Creates a successful result with the specified value.
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param value the successful return value. Must not be {@code null}.
     * @return a successful result carrying {@code value}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    static <F, S> Result<F, S> success(final S value) {
        return new Success<>(Objects.requireNonNull(value, "value"));
    }

    /**
     * Creates an empty successful result, for operations whose success carries no value.
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
     * @param <F> the failure type
     * @param <S> the success type
     * @param anomaly the failure anomaly. Must not be {@code null}.
     * @return a failed result carrying {@code anomaly}
     * @throws NullPointerException if {@code anomaly} is {@code null}
     */
    static <F, S> Result<F, S> failure(final F anomaly) {
        return new Failure<>(Objects.requireNonNull(anomaly, "anomaly"));
    }

    /**
     * Returns the first successful result, or the first failure when all results fail.
     *
     * @param <F> the failure type
     * @param <S> the success type
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
     * Returns the first successful result produced by the providers, or the first failure when all
     * results fail. Every provider is invoked at most once.
     *
     * @param <F> the failure type
     * @param <S> the success type
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
            final Result<F, S> result =
                    Objects.requireNonNull(provider.get(), "provider returned null");
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
     * Executes a computation that might throw an exception, converting it to a result: the
     * computation's value becomes a success, and a thrown exception becomes a failure carrying the
     * mapped anomaly.
     *
     * <p>The computation is a {@link CheckedSupplier}, so lambdas and method references may declare
     * checked exceptions; callers never wrap the computation in try/catch just to call this method.
     * The computation must not return {@code null}, as successes always carry a value.
     *
     * <p><strong>Error vs exception policy:</strong> only {@link Exception} instances are captured
     * &mdash; checked exceptions and runtime exceptions alike. {@link Error} instances (such as
     * {@link OutOfMemoryError}, {@link StackOverflowError} or {@link LinkageError}) are
     * deliberately not captured: they signal JVM-level failures from which the process cannot
     * reliably continue, and converting them to failure anomalies would let the program resume in
     * an undefined state. They propagate to the caller, as does any {@link Throwable} that is
     * neither an {@link Exception} nor an {@link Error}. This matches the anomaly model of {@code
     * eu.infolead.jtk.anomaly}, whose {@code SystemError} values represent recoverable domain and
     * system failures, never JVM failures.
     *
     * <p>Example &mdash; capture a checked exception without any try/catch:
     *
     * <pre>{@code
     * Result<String, String> config = Result.fromTry(
     *     ex -> "Cannot read config: " + ex.getMessage(),
     *     () -> Files.readString(Path.of("config.properties")));
     * }</pre>
     *
     * @param <F> the failure type
     * @param <S> the success type
     * @param errorMapper function to convert exceptions to failure anomalies
     * @param computation the computation that might throw
     * @return a success with the computation's value, or a failure with the mapped exception
     * @throws NullPointerException if {@code errorMapper} or {@code computation} is {@code null},
     *     or the computation returns {@code null}
     */
    static <F, S> Result<F, S> fromTry(
            final Fn.FN1<F, Exception> errorMapper, final CheckedSupplier<S> computation) {
        Objects.requireNonNull(errorMapper, "errorMapper");
        Objects.requireNonNull(computation, "computation");
        final S value;
        try {
            value = computation.get();
        } catch (Exception e) {
            return Result.failure(errorMapper.apply(e));
        }
        // Outside the try: a null success value must surface as a NullPointerException,
        // not be captured as a failure by this very boundary.
        return Result.success(value);
    }

    /**
     * Executes a computation without a return value that might throw an exception, converting it to
     * a result: success becomes an {@linkplain #success() empty success}, and a thrown exception
     * becomes a failure carrying the mapped anomaly. The error-vs-exception policy of {@link
     * #fromTry(Fn.FN1, CheckedSupplier)} applies.
     *
     * <p><strong>Overload resolution:</strong> this overload and {@link #fromTry(Fn.FN1,
     * CheckedSupplier)} share the same argument shape, so when a computation is compatible with
     * both &mdash; a value-producing statement expression such as {@code () -> list.add(value)}, a
     * value-returning method reference, or an always-throwing block &mdash; the compiler selects
     * the {@link CheckedSupplier} overload and the produced value becomes the success value. A
     * computation only resolves to this overload when its body produces no value; cast to {@code
     * CheckedRunnable} to force this overload for any other body.
     *
     * <p>Example &mdash; run a throwing side effect:
     *
     * <pre>{@code
     * Result<String, Void> sent = Result.fromTry(
     *     ex -> "Welcome email failed: " + ex.getMessage(),
     *     () -> mailer.sendWelcome(user));
     * }</pre>
     *
     * @param <F> the failure type
     * @param errorMapper function to convert exceptions to failure anomalies
     * @param computation the computation that might throw
     * @return an empty success, or a failure with the mapped exception
     * @throws NullPointerException if {@code errorMapper} or {@code computation} is {@code null}
     */
    static <F> Result<F, Void> fromTry(
            final Fn.FN1<F, Exception> errorMapper, final CheckedRunnable computation) {
        Objects.requireNonNull(errorMapper, "errorMapper");
        Objects.requireNonNull(computation, "computation");
        try {
            computation.run();
            return Result.success();
        } catch (Exception e) {
            return Result.failure(errorMapper.apply(e));
        }
    }

    /**
     * Lifts a function that might throw an exception into a railway switch: the returned function
     * applies the computation to its argument and wraps the outcome in a result, using the same
     * error-vs-exception policy as {@link #fromTry(Fn.FN1, CheckedSupplier)}. The lifted
     * computation must not return {@code null}: the returned function then throws a {@link
     * NullPointerException} instead of producing a failure, exactly as {@link #fromTry(Fn.FN1,
     * CheckedSupplier)} does.
     *
     * <p>Example &mdash; turn a throwing reader into a railway switch:
     *
     * <pre>{@code
     * Fn.FN1<Result<String, String>, Path> read = Result.liftTry(
     *     ex -> "Cannot read: " + ex.getMessage(),
     *     path -> Files.readString(path));
     *
     * Result<String, String> content = read.apply(Path.of("config.properties"));
     * }</pre>
     *
     * @param <F> the failure type
     * @param <T> the input type
     * @param <S> the success type
     * @param errorMapper function to convert exceptions to failure anomalies
     * @param computation the function that might throw
     * @return a function producing a success with the result, or a failure with the mapped
     *     exception
     * @throws NullPointerException if {@code errorMapper} or {@code computation} is {@code null}
     */
    static <F, T, S> Fn.FN1<Result<F, S>, T> liftTry(
            final Fn.FN1<F, Exception> errorMapper, final CheckedFunction<T, S> computation) {
        Objects.requireNonNull(errorMapper, "errorMapper");
        Objects.requireNonNull(computation, "computation");
        return value -> fromTry(errorMapper, () -> computation.apply(value));
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
        return fold(l -> Result.failure(l), r -> Result.success(mapper.map(r)));
    }

    @Override
    default <R1> Result<F, R1> map(final Provider<R1> provider) {
        Objects.requireNonNull(provider, "provider");
        return fold(Result::failure, r -> Result.success(provider.get()));
    }

    /**
     * Successful result without a value. Mapping, flat-mapping, and applying an empty success keep
     * it empty instead of inventing a value or invoking a success consumer with {@code null}.
     *
     * <p>{@link #fold} has no empty branch, so its right mapper still receives {@code null};
     * callers that need empty-aware handling should use {@link #isSuccess()} together with {@link
     * #map}.
     */
    final class EmptySuccess<F, S> extends AbstractRight<F, S> implements Result<F, S> {
        private static final long serialVersionUID = 1L;

        private EmptySuccess() {
            super();
        }

        @JsonCreator
        static EmptySuccess<?, ?> fromJson(@JsonProperty("right") final Void ignored) {
            return (EmptySuccess<?, ?>) EMPTY_SUCCESS;
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
        public <E extends Either<F, S>> E apply(
                final Consumer<? super F> leftConsumer, final Consumer<? super S> rightConsumer) {
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

        @JsonCreator
        private Success(@JsonProperty("right") final S value) {
            super(Objects.requireNonNull(value, "value"));
        }

        private void readObject(final ObjectInputStream input)
                throws IOException, ClassNotFoundException {
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

        @JsonCreator
        private Failure(@JsonProperty("left") final F value) {
            super(Objects.requireNonNull(value, "anomaly"));
        }

        private void readObject(final ObjectInputStream input)
                throws IOException, ClassNotFoundException {
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
