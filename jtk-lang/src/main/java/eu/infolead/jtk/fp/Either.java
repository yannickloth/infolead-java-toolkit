package eu.infolead.jtk.fp;

import java.util.function.Consumer;
import java.util.function.Supplier;

import eu.infolead.jtk.lang.CompilerWarning;

public interface Either<L, R> {
    static <G, D> Either<G, D> right(final D value) {
        return new Right<>(value);
    }

    static <G, D> Either<G, D> left(final G value) {
        return new Left<>(value);
    }

    static <G, D> Either<G, D> right() {
        return new Right<>(null);
    }

    static <G, D> Either<G, D> left() {
        return new Left<>(null);
    }

    Either<R, L> reverse();

    <U> U fold(Mapper<? extends U, ? super L> failureMapper,
            Mapper<? extends U, ? super R> successMapper);

    /**
     * Method which if this is a right instance consumes the right value to produce
     * some side effect(s), or consumes the left value to produce some side
     * effect(s).
     * <p>
     * This is similar to the {@link Either#fold} method, but has no return value
     * and is thus used to produce side effects.
     * <p>
     * In other words, this allows to observe this instance and do something with
     * it.
     * <p>
     * Same as {@link Either#peek(Consumer, Consumer)}.
     * 
     * @param leftConsumer  the consumer applied if this is a left instance.
     * 
     * @param rightConsumer the consumer applied if this is a right instance.
     * @return this.
     */
    Either<L, R> apply(Consumer<? super L> leftConsumer,
            Consumer<? super R> rightConsumer);

    /**
     * Cf. {@link Either#apply(Consumer, Consumer)}.
     * 
     * @param leftConsumer  the consumer applied if this is a left instance.
     * 
     * @param rightConsumer the consumer applied if this is a right instance.
     * @return this.
     * 
     */
    default Either<L, R> peek(Consumer<? super L> leftConsumer,
            Consumer<? super R> rightConsumer) {
        return apply(leftConsumer, rightConsumer);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <G, D, A extends Either<G, D>> A flatMap(final Mapper<A, ? super R> mapper) {
        return fold(l -> (A) this, mapper);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <G, D, A extends Either<G, D>> A flatMap(final Supplier<A> supplier) {
        return fold(l -> (A) this, r -> supplier.get());
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <G, D, A extends Either<G, D>> A flatMapLeft(final Mapper<A, ? super L> mapper) {
        return fold(mapper, r -> (A) this);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <G, D, A extends Either<G, D>> A flatMapLeft(final Supplier<A> supplier) {
        return fold(l -> supplier.get(), r -> (A) this);
    }

    default <G, D, A extends Either<G, D>> A bind(final Mapper<A, ? super R> mapper) {
        return flatMap(mapper);
    }

    default <G, D, A extends Either<G, D>> A bind(final Supplier<A> supplier) {
        return flatMap(supplier);
    }

    default <G, D, A extends Either<G, D>> A andThen(final Mapper<A, ? super R> mapper) {
        return flatMap(mapper);
    }

    default <G, D, A extends Either<G, D>> A andThen(final Supplier<A> supplier) {
        return flatMap(supplier);
    }

    default <G, D, A extends Either<G, D>> A ifRightDo(final Mapper<A, ? super R> mapper) {
        return flatMap(mapper);
    }

    default <E extends Either<L, R>> E ifRightDo(final Supplier<E> supplier) {
        return flatMap(supplier);
    }

    default <E extends Either<L, R>> E bindLeft(final Mapper<E, ? super L> mapper) {
        return flatMapLeft(mapper);
    }

    default <E extends Either<L, R>> E bindLeft(final Supplier<E> supplier) {
        return flatMapLeft(supplier);
    }

    default <E extends Either<L, R>> E leftAndThen(final Mapper<E, ? super L> mapper) {
        return flatMapLeft(mapper);
    }

    default <E extends Either<L, R>> E leftAndThen(final Supplier<E> supplier) {
        return flatMapLeft(supplier);
    }

    default <E extends Either<L, R>> E ifLeftDo(final Mapper<E, ? super L> mapper) {
        return flatMapLeft(mapper);
    }

    default <E extends Either<L, R>> E ifLeftDo(final Supplier<E> supplier) {
        return flatMapLeft(supplier);
    }

    default R or(final R replacement) {
        return fold(anomaly -> replacement, Fn::identity);
    }

    default R or(final Supplier<R> supplier) {
        return fold(anomaly -> supplier.get(), Fn::identity);
    }

    default R orNull() {
        return fold(Fn::toNull, Fn::identity);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <E extends Either<L, R>> E orElse(final E replacement) {
        return fold(anomaly -> replacement, successValue -> (E) this);
    }

    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <E extends Either<L, R>> E orElse(final Supplier<E> supplier) {
        return fold(anomaly -> supplier.get(), successValue -> (E) this);
    }

    /**
     * <h3>Note</h3>
     * It is not currently clear whether this method makes sense: what would be its
     * use-case?
     * 
     * @return
     */
    @SuppressWarnings(CompilerWarning.UNCHECKED)
    default <E extends Either<L, R>> E orElseNull() {
        return fold(Fn::toNull, successValue -> (E) this);
    }

    @SuppressWarnings(CompilerWarning.RAW_TYPES)
    abstract sealed class Wrapper<T> permits Right, Left {
        /**
         * <strong>Note: </strong> Must not be {@code final}, because else
         * deserialization will fail.
         * <p>
         * Cf. <a target="_blank" href=
         * "https://rules.sonarsource.com/java/RSPEC-2055">https://rules.sonarsource.com/java/RSPEC-2055</a>
         */
        private T value;

        Wrapper() {
            this.value = null;
        }

        Wrapper(final T value) {
            this.value = value;
        }

        protected T value() {
            return value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    non-sealed class Right<L, R> extends Wrapper<R> implements Either<L, R> {
        /**
         * The non-serializable super class of a "Serializable" class ({@link Some} and
         * {@link None}) should have a no-argument constructor
         * <p>
         * Cf. <a target="_blank" href=
         * "https://rules.sonarsource.com/java/RSPEC-2055">https://rules.sonarsource.com/java/RSPEC-2055</a>
         */
        Right() {
            super();
        }

        Right(final R value) {
            super(value);
        }

        public Left<R, L> reverse() {
            return new Left<>(value());
        }

        public <U> U fold(final Mapper<? extends U, ? super L> failureMapper,
                final Mapper<? extends U, ? super R> successMapper) {
            return successMapper.map(value());
        }

        @Override
        public Either<L, R> apply(Consumer<? super L> leftConsumer,
                Consumer<? super R> rightConsumer) {
            rightConsumer.accept(value());
            return this;
        }
    }

    non-sealed class Left<L, R> extends Wrapper<L> implements Either<L, R> {
        Left() {
            super();
        }

        Left(final L value) {
            super(value);
        }

        public Right<R, L> reverse() {
            return new Right<>(value());
        }

        public <U> U fold(final Mapper<? extends U, ? super L> failureMapper,
                final Mapper<? extends U, ? super R> successMapper) {
            return failureMapper.map(value());
        }

        @Override
        public Either<L, R> apply(Consumer<? super L> leftConsumer,
                Consumer<? super R> rightConsumer) {
            leftConsumer.accept(value());
            return this;
        }
    }
}
