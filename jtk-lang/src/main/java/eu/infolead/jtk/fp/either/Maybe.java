package eu.infolead.jtk.fp.either;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import eu.infolead.jtk.fp.Anomaly;
import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.fp.Provider;
import eu.infolead.jtk.fp.either.Maybe.None;
import eu.infolead.jtk.fp.either.Maybe.Some;
import eu.infolead.jtk.logic.Bool;

public sealed interface Maybe<T> extends Either<Void, T> permits None, Some {
    /**
     * Provides an empty instance of {@link Maybe}.
     * 
     * @param <U> the type of the thing that is not present
     * @return an empty instance of {@link Maybe}.
     */
    @SuppressWarnings("unchecked")
    static <U> Maybe<U> none() {
        return (Maybe<U>) None.INSTANCE;
    }

    /**
     * Same as {@link Maybe#none()}.
     * 
     * @param <U> the type of the thing that is not present
     * @return an empty instance of {@link Maybe}.
     */
    static <U> Maybe<U> nothing() {
        return none();
    }

    /**
     * Converts the non-null value into a {@link Maybe}.
     * 
     * @param <U>   the type of the value
     * @param value the value. Must not be {@code null}.
     * @return a Maybe with the specified non-null value.
     */
    static <U> Maybe<U> of(final U value) {
        return new Maybe.Some<>(value);
    }

    /**
     * Same as {@link Maybe#of()}.
     * 
     * @param <U>   the type of the value
     * @param value the value. Must not be {@code null}.
     * @return a Maybe with the specified non-null value.
     */
    static <U> Maybe<U> some(final U value) {
        return of(value);
    }

    /**
     * Converts the non-null value into a {@link Maybe}.
     * 
     * @param <U>           the type of the value
     * @param valueProvider provides the value. MUST NOT be {@code null} and MUST
     *                      NOT provide a {@code null} value.
     * @return a {@link Maybe} with the specified non-null value.
     */
    static <U> Maybe<U> of(final Provider<U> valueProvider) {
        return new Some<>(valueProvider.get());
    }

    static <U> Maybe<U> when(final Bool condition, final U value) {
        return condition.fold(Maybe::none, () -> Maybe.of(value));
    }

    static <U> Maybe<U> when(final Bool condition, final Provider<U> valueProvider) {
        return condition.fold(Maybe::none, () -> Maybe.of(valueProvider.get()));
    }

    static <U> Maybe<U> when(final boolean condition, final U value) {
        return when(Bool.of(condition), value);
    }

    static <U> Maybe<U> when(final boolean condition, final Provider<U> valueProvider) {
        return when(Bool.of(condition), valueProvider.get());
    }

    static <U extends Collection<?>> Maybe<U> ofCollection(final U collection) {
        return Bool.of(collection.isEmpty()).fold(() -> Maybe.of(collection), Maybe::none);
    }

    /**
     * Converts the non-null map into an {@link Maybe}. This allows to use methods
     * like {@link Maybe#apply} and {@link Maybe#fold} on a map and obtain code with
     * greater functional-programming style (instead of using
     * {@code if(map.isEmpty()){...}else{...}}).
     * 
     * <p>
     * Ideally, methods like {@link Maybe#fold} and {@link Maybe#apply} would be
     * provided directly by the collections.
     * 
     * @param <U> the type of the map
     * @param map the value. Must not be {@code null}.
     * @return a Maybe with the specified non-null map: if the map is empty, a none
     *         Maybe is returned, else a Maybe with the map is returned.
     */
    static <U extends Map<?, ?>> Maybe<U> ofMap(final U map) {
        return map.isEmpty() ? Maybe.none() : Maybe.of(map);
    }

    static <U extends Collection<?>> Maybe<U> ofNullableCollection(final U collection) {
        return Bool.of(collection == null).fold(() -> Maybe.of(collection), Maybe::none);
    }

    static <U extends Map<?, ?>> Maybe<U> ofNullableMap(final U map) {
        return map == null ? Maybe.none() : Maybe.of(map);
    }

    /**
     * Converts the value into an {@link Maybe}.
     * 
     * @param <T>   the type of the value
     * @param value the value
     * @return a {@link Maybe} with the specified value. Returns
     *         {@code Maybe.none()} if the specified value is {@code null}.
     */
    static <T> Maybe<T> ofNullable(final T value) {
        return value == null ? Maybe.none() : Maybe.of(value);
    }

    @SuppressWarnings("null")
    static Maybe<String> ofBlankable(final String value) {
        return Bool.of(value == null).or(value.trim().isEmpty()).fold(() -> Maybe.of(value), Maybe::none);
    }

    static <U> Maybe<U> of(final Optional<U> optional) {
        return ofNullable(optional.orElse(null));
    }

    default <U> U fold(final Provider<? extends U> emptyProvider,
            final Mapper<? extends U, ? super T> presentMapper) {
        return fold(emptyProvider.asMapper(), presentMapper);
    }

    /**
     * Method which either consumes the present value to produce some side
     * effect(s), or performs a specific action when none present.
     * <p>
     * This is similar to the {@link Maybe#fold} method, but has no return value and
     * is thus used to produce side effects.
     */
    Maybe<T> apply(Runnable emptyAction, Consumer<? super T> presentConsumer);

    /**
     * Transforms the value using the specified mapper.
     * <p>
     * Replaces this instance with another {@link Maybe} instance having a different
     * value from a different type.
     * The new value is computed by mapping the current value using the specified
     * mapper.
     * 
     * @param <U>    the type of the new value
     * @param mapper the mapping function which maps from {@code T} to {@code U} and
     *               transforms the current value into the new value.
     * @return {@link Maybe#none()} if this is empty or a new {@link Maybe} with the
     *         mapped/transformed value.
     */
    @Override
    default <U> Maybe<U> map(final Mapper<U, ? super T> mapper) {
        return fold(n -> Maybe.none(), t -> Maybe.of(mapper.map(t)));
    }

    /**
     * Replaces this instance with another {@link Maybe} instance containing the
     * value received from the specified {@link Provider}.
     * 
     * @param <U>      the type of the new value
     * @param provider the provider of the new value
     * @return an empty instance ({@link Maybe#none()}) if this instance is empty,
     *         or else a new
     *         {@link Maybe} instance with the value received from the specified
     *         provider.
     */
    default <U> Maybe<U> map(final Provider<U> provider) {
        return fold(Maybe::none, t -> of(provider.get()));
    }

    default <U> Maybe<U> ifSomeDo(final Provider<Maybe<U>> provider) {
        return ifRightDo(t -> provider.get());
    }

    default Maybe<T> filter(final Predicate<? super T> predicate) {
        return fold(v -> Maybe.none(), v -> predicate.test(v) ? this : none());
    }

    default Stream<T> stream() {
        return fold(v -> Stream.empty(), Stream::of);
    }

    default Bool isPresent() {
        return Bool.FALSE;
    }

    default Bool isEmpty() {
        return isPresent().negate();
    }

    default Maybe<T> orElse(final T replacement) {
        return fold(v -> Maybe.ofNullable(replacement), t -> this);
    }

    default Maybe<T> orElse(final Maybe<T> replacement) {
        return fold(v -> replacement, t -> this);
    }

    /**
     * Wraps the value of this{@link Maybe} in an {@link Optional}. <strong>NOTE:
     * </strong>One should try to avoid this method, as well as {@link Optional},
     * because {@link Optional} easily enables a non-functional programming style
     * (using if/else, throwing exceptions...
     * 
     * @return an instance of {@link Optional} wrapping the value of this
     *         {@link Maybe}.
     */
    default Optional<T> toOptional() {
        return fold(v -> Optional.empty(), Optional::of);
    }

    /**
     * Wraps the value of this{@link Maybe} in a {@link Result}.
     * 
     * @return an instance of {@link Result} wrapping the value of this
     *         {@link Maybe}.
     */
    default Result<Void, T> toResult(final Anomaly anomaly) {
        return fold(v -> Result.failure(null), Result::success);
    }

    @SafeVarargs
    static <U> Maybe<U> any(final Maybe<U>... maybes) {
        return Arrays.stream(maybes).filter(m -> m.isPresent().toBoolean()).findFirst().orElse(Maybe.none());
    }

    @SafeVarargs
    static <U> Maybe<U> any(final Provider<Maybe<U>>... maybeProviders) {
        return Arrays.stream(maybeProviders).map(Provider::get).filter(m -> m.isPresent().toBoolean()).findFirst()
                .orElse(Maybe.none());
    }

    @SafeVarargs
    static <U> Maybe<U> any(final Optional<U>... optionals) {
        return Arrays.stream(optionals).map(Maybe::of).filter(m -> m.isPresent().toBoolean()).findFirst()
                .orElse(Maybe.none());
    }

    @SafeVarargs
    static <U> Maybe<U> anyOptional(final Provider<Optional<U>>... optionalProviders) {
        return Arrays.stream(optionalProviders).map(Provider::get).map(Maybe::of).filter(m -> m.isPresent().toBoolean())
                .findFirst()
                .orElse(Maybe.none());
    }

    final class None<T> extends AbstractLeft<Void, T> implements Maybe<T>, Serializable {
        private static final long serialVersionUID = 459735266617L;
        static final None<?> INSTANCE = new None<>();

        @Override
        public Maybe<T> apply(final Runnable emptyAction, final Consumer<? super T> presentConsumer) {
            emptyAction.run();
            return this;
        }

        @Override
        public <E extends Either<T, Void>> E swap() {
            throw new UnsupportedOperationException("Swapping a Maybe does not make sense.");
        }

        @Override
        public String toString() {
            return "{}";
        }
    }

    final class Some<T> extends AbstractRight<Void, T> implements Maybe<T>, Serializable {
        private static final long serialVersionUID = 459735266617L;

        public Some() {
            super();
        }

        public Some(final T value) {
            super(value);
        }

        @Override
        public Maybe<T> apply(final Runnable emptyAction, final Consumer<? super T> presentConsumer) {
            presentConsumer.accept(value());
            return this;
        }

        @Override
        public <E extends Either<T, Void>> E swap() {
            throw new UnsupportedOperationException("Swapping a Maybe does not make sense.");
        }

        @Override
        public String toString() {
            return "{}";
        }
    }

}
