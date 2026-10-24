package eu.infolead.jtk.fp.either;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.infolead.jtk.fp.Mapper;
import eu.infolead.jtk.fp.Provider;
import eu.infolead.jtk.fp.either.Maybe.None;
import eu.infolead.jtk.fp.either.Maybe.Some;
import eu.infolead.jtk.logic.Bool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An optional value: either {@link Some} (a present, non-null value) or {@link None} (absence of a
 * value).
 *
 * <p>Unlike {@link Optional}, a {@code Maybe} participates in the railway-oriented API exposed by
 * {@link Either}.
 *
 * @param <T> the type of the present value
 */
public sealed interface Maybe<T> extends Either<Void, T> permits None, Some {

    /**
     * Reads the {@code left}/{@code right} JSON envelope.
     *
     * <p>A present value is written as {@code {"right": value}}; absence is written as {@code
     * {"left": null}} because the left type is {@link Void}. Because {@link Some} rejects a null
     * payload, a missing or null right member unambiguously means {@link #none()}.
     *
     * @param <T> the present-value type
     * @param left the always-null left member
     * @param right the present value, or {@code null}
     * @return the envelope as a {@code Maybe}
     */
    @JsonCreator
    static <T> Maybe<T> fromJson(
            @JsonProperty("left") final Void left, @JsonProperty("right") final T right) {
        return right == null ? Maybe.none() : Maybe.of(right);
    }

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
     * @param <U> the type of the value
     * @param value the value. Must not be {@code null}.
     * @return a Maybe with the specified non-null value.
     * @throws NullPointerException if {@code value} is {@code null}
     */
    static <U> Maybe<U> of(final U value) {
        return new Maybe.Some<>(
                Objects.requireNonNull(value, "value must not be null; use ofNullable instead"));
    }

    /**
     * Same as {@link Maybe#of(Object)}.
     *
     * @param <U> the type of the value
     * @param value the value. Must not be {@code null}.
     * @return a Maybe with the specified non-null value.
     */
    static <U> Maybe<U> some(final U value) {
        return of(value);
    }

    /**
     * Converts the non-null value into a {@link Maybe}.
     *
     * @param <U> the type of the value
     * @param valueProvider provides the value. MUST NOT be {@code null} and MUST NOT provide a
     *     {@code null} value.
     * @return a {@link Maybe} with the specified non-null value.
     */
    static <U> Maybe<U> of(final Provider<U> valueProvider) {
        Objects.requireNonNull(valueProvider, "valueProvider");
        return of(valueProvider.get());
    }

    static <U> Maybe<U> when(final Bool condition, final U value) {
        Objects.requireNonNull(condition, "condition");
        return condition.fold(Maybe::none, () -> Maybe.of(value));
    }

    static <U> Maybe<U> when(final Bool condition, final Provider<U> valueProvider) {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(valueProvider, "valueProvider");
        return condition.fold(Maybe::none, () -> Maybe.of(valueProvider.get()));
    }

    static <U> Maybe<U> when(final boolean condition, final U value) {
        return when(Bool.of(condition), value);
    }

    static <U> Maybe<U> when(final boolean condition, final Provider<U> valueProvider) {
        Objects.requireNonNull(valueProvider, "valueProvider");
        return when(Bool.of(condition), valueProvider);
    }

    static <U extends Collection<?>> Maybe<U> ofCollection(final U collection) {
        Objects.requireNonNull(collection, "collection");
        return collection.isEmpty() ? Maybe.none() : Maybe.of(collection);
    }

    /**
     * Converts the non-null map into an {@link Maybe}. This allows to use methods like {@link
     * Maybe#apply} and {@link Maybe#fold} on a map and obtain code with greater
     * functional-programming style (instead of using {@code if(map.isEmpty()){...}else{...}}).
     *
     * <p>Ideally, methods like {@link Maybe#fold} and {@link Maybe#apply} would be provided
     * directly by the collections.
     *
     * @param <U> the type of the map
     * @param map the value. Must not be {@code null}.
     * @return a Maybe with the specified non-null map: if the map is empty, a none Maybe is
     *     returned, else a Maybe with the map is returned.
     */
    static <U extends Map<?, ?>> Maybe<U> ofMap(final U map) {
        Objects.requireNonNull(map, "map");
        return map.isEmpty() ? Maybe.none() : Maybe.of(map);
    }

    /**
     * Converts a nullable collection into a {@link Maybe}.
     *
     * <p>Both a {@code null} collection and an empty collection map to {@link #none()}. Use {@link
     * #ofNullable(Object)} when an empty collection must be preserved.
     *
     * @param <U> the collection type
     * @param collection the collection, possibly {@code null}
     * @return a Maybe containing a non-empty collection, or {@link #none()}
     */
    static <U extends Collection<?>> Maybe<U> ofNullableCollection(final U collection) {
        return collection == null || collection.isEmpty() ? Maybe.none() : Maybe.of(collection);
    }

    /**
     * Converts a nullable map into a {@link Maybe}.
     *
     * <p>Both a {@code null} map and an empty map map to {@link #none()}. Use {@link
     * #ofNullable(Object)} when an empty map must be preserved.
     *
     * @param <U> the map type
     * @param map the map, possibly {@code null}
     * @return a Maybe containing a non-empty map, or {@link #none()}
     */
    static <U extends Map<?, ?>> Maybe<U> ofNullableMap(final U map) {
        return map == null || map.isEmpty() ? Maybe.none() : Maybe.of(map);
    }

    /**
     * Converts the value into an {@link Maybe}.
     *
     * @param <T> the type of the value
     * @param value the value
     * @return a {@link Maybe} with the specified value. Returns {@code Maybe.none()} if the
     *     specified value is {@code null}.
     */
    static <T> Maybe<T> ofNullable(final T value) {
        return value == null ? Maybe.none() : Maybe.of(value);
    }

    static Maybe<String> ofBlankable(final String value) {
        if (value == null || value.isBlank()) {
            return Maybe.none();
        }
        return Maybe.of(value);
    }

    static <U> Maybe<U> of(final Optional<U> optional) {
        Objects.requireNonNull(optional, "optional");
        return ofNullable(optional.orElse(null));
    }

    default <U> U fold(
            final Provider<? extends U> emptyProvider,
            final Mapper<? extends U, ? super T> presentMapper) {
        Objects.requireNonNull(emptyProvider, "emptyProvider");
        Objects.requireNonNull(presentMapper, "presentMapper");
        return fold(emptyProvider.asMapper(), presentMapper);
    }

    /**
     * Method which either consumes the present value to produce some side effect(s), or performs a
     * specific action when none present.
     *
     * <p>This is similar to the {@link Maybe#fold} method, but has no return value and is thus used
     * to produce side effects.
     */
    Maybe<T> apply(Runnable emptyAction, Consumer<? super T> presentConsumer);

    /**
     * Transforms the value using the specified mapper.
     *
     * <p>Replaces this instance with another {@link Maybe} instance having a different value from a
     * different type. The new value is computed by mapping the current value using the specified
     * mapper.
     *
     * @param <U> the type of the new value
     * @param mapper the mapping function which maps from {@code T} to {@code U} and transforms the
     *     current value into the new value.
     * @return {@link Maybe#none()} if this is empty or a new {@link Maybe} with the
     *     mapped/transformed value.
     */
    @Override
    default <U> Maybe<U> map(final Mapper<U, ? super T> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return fold(n -> Maybe.none(), t -> Maybe.ofNullable(mapper.map(t)));
    }

    /**
     * Replaces this instance with another {@link Maybe} instance containing the value received from
     * the specified {@link Provider}.
     *
     * @param <U> the type of the new value
     * @param provider the provider of the new value
     * @return an empty instance ({@link Maybe#none()}) if this instance is empty, or else a new
     *     {@link Maybe} instance with the value received from the specified provider.
     */
    default <U> Maybe<U> map(final Provider<U> provider) {
        Objects.requireNonNull(provider, "provider");
        return fold(Maybe::none, t -> ofNullable(provider.get()));
    }

    default <U> Maybe<U> ifSomeDo(final Provider<Maybe<U>> provider) {
        Objects.requireNonNull(provider, "provider");
        return ifRightDo(t -> provider.get());
    }

    default Maybe<T> filter(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return fold(v -> Maybe.none(), v -> predicate.test(v) ? this : none());
    }

    default Stream<T> stream() {
        return fold(v -> Stream.empty(), Stream::of);
    }

    default Bool isPresent() {
        return fold(v -> Bool.FALSE, v -> Bool.TRUE);
    }

    default Bool isEmpty() {
        return isPresent().negate();
    }

    default Maybe<T> orElse(final T replacement) {
        return fold(v -> Maybe.ofNullable(replacement), t -> this);
    }

    default Maybe<T> orElse(final Maybe<T> replacement) {
        Objects.requireNonNull(replacement, "replacement");
        return fold(v -> replacement, t -> this);
    }

    /**
     * Wraps the value of this{@link Maybe} in an {@link Optional}. <strong>NOTE: </strong>One
     * should try to avoid this method, as well as {@link Optional}, because {@link Optional} easily
     * enables a non-functional programming style (using if/else, throwing exceptions...
     *
     * @return an instance of {@link Optional} wrapping the value of this {@link Maybe}.
     */
    default Optional<T> toOptional() {
        return fold(v -> Optional.empty(), Optional::of);
    }

    @SafeVarargs
    static <U> Maybe<U> any(final Maybe<U>... maybes) {
        Objects.requireNonNull(maybes, "maybes");
        return Arrays.stream(maybes)
                .map(m -> Objects.requireNonNull(m, "maybes must not contain null"))
                .filter(m -> m.isPresent().toBoolean())
                .findFirst()
                .orElseGet(Maybe::none);
    }

    @SafeVarargs
    static <U> Maybe<U> any(final Provider<Maybe<U>>... maybeProviders) {
        Objects.requireNonNull(maybeProviders, "maybeProviders");
        return Arrays.stream(maybeProviders)
                .map(
                        p ->
                                Objects.requireNonNull(
                                        Objects.requireNonNull(
                                                        p, "maybeProviders must not contain null")
                                                .get(),
                                        "provider returned null"))
                .filter(m -> m.isPresent().toBoolean())
                .findFirst()
                .orElseGet(Maybe::none);
    }

    @SafeVarargs
    static <U> Maybe<U> any(final Optional<U>... optionals) {
        Objects.requireNonNull(optionals, "optionals");
        return Arrays.stream(optionals)
                .map(o -> of(Objects.requireNonNull(o, "optionals must not contain null")))
                .filter(m -> m.isPresent().toBoolean())
                .findFirst()
                .orElseGet(Maybe::none);
    }

    @SafeVarargs
    static <U> Maybe<U> anyOptional(final Provider<Optional<U>>... optionalProviders) {
        Objects.requireNonNull(optionalProviders, "optionalProviders");
        return Arrays.stream(optionalProviders)
                .map(
                        p ->
                                of(
                                        Objects.requireNonNull(
                                                Objects.requireNonNull(
                                                                p,
                                                                "optionalProviders must not contain null")
                                                        .get(),
                                                "provider returned null")))
                .filter(m -> m.isPresent().toBoolean())
                .findFirst()
                .orElseGet(Maybe::none);
    }

    final class None<T> extends AbstractLeft<Void, T> implements Maybe<T>, Serializable {
        private static final long serialVersionUID = 459735266617L;
        static final None<?> INSTANCE = new None<>();

        private None() {
            super();
        }

        @JsonCreator
        static None<?> fromJson(@JsonProperty("left") final Void ignored) {
            return (None<?>) INSTANCE;
        }

        private Object readResolve() {
            return INSTANCE;
        }

        @Override
        public Maybe<T> apply(
                final Runnable emptyAction, final Consumer<? super T> presentConsumer) {
            Objects.requireNonNull(emptyAction, "emptyAction");
            Objects.requireNonNull(presentConsumer, "presentConsumer");
            emptyAction.run();
            return this;
        }

        @Override
        public <E extends Either<T, Void>> E swap() {
            throw new UnsupportedOperationException("Swapping a Maybe does not make sense.");
        }

        @Override
        public String toString() {
            return "None";
        }
    }

    final class Some<T> extends AbstractRight<Void, T> implements Maybe<T>, Serializable {
        private static final long serialVersionUID = 459735266617L;

        @JsonCreator
        private Some(@JsonProperty("right") final T value) {
            super(Objects.requireNonNull(value, "value must not be null"));
        }

        private void readObject(final ObjectInputStream input)
                throws IOException, ClassNotFoundException {
            input.defaultReadObject();
            Objects.requireNonNull(value(), "deserialized Some value must not be null");
        }

        @Override
        public String toString() {
            return "Some";
        }

        @Override
        public Maybe<T> apply(
                final Runnable emptyAction, final Consumer<? super T> presentConsumer) {
            Objects.requireNonNull(emptyAction, "emptyAction");
            Objects.requireNonNull(presentConsumer, "presentConsumer");
            presentConsumer.accept(value());
            return this;
        }

        @Override
        public <E extends Either<T, Void>> E swap() {
            throw new UnsupportedOperationException("Swapping a Maybe does not make sense.");
        }
    }
}
