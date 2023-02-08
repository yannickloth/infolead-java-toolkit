package eu.infolead.jtk.fp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Maybe<T> {

    @SuppressWarnings("unchecked")
    static <U> Maybe<U> none() {
        return (Maybe<U>) None.INSTANCE;
    }

    /**
     * Converts the non-null value into a {@link Maybe}.
     * 
     * @param <U>   the type of the value
     * @param value the value. Must not be {@code null}.
     * @return a Maybe with the specified non-null value.
     */
    static <U> Maybe<U> of(final U value) {
        return new Some<>(value);
    }

    static <U> Maybe<U> when(final Bool condition, final U value) {
        return condition.fold(Maybe::none, () -> Maybe.of(value));
    }

    static <U> Maybe<U> when(final Bool condition, final Supplier<U> valueSupplier) {
        return condition.fold(Maybe::none, () -> Maybe.of(valueSupplier.get()));
    }

    /**
     * Static method that just returns another value if the specified {@code value}
     * argument is {@code null}.
     * 
     * @param value
     * @param other
     * @return
     */
    static <U> U or(final U value, final U other) {
        return Bool.of(value != null).fold(() -> other, () -> value);
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

    <U> U fold(Supplier<? extends U> emptyMapper, Mapper<? extends U, ? super T> presentMapper);

    /**
     * Method which either consumes the present value to produce some side
     * effect(s), or performs a specific action when none present.
     * <p>
     * This is similar to the {@link Maybe#fold} method, but has no return value and
     * is thus used to produce side effects.
     */
    Maybe<T> apply(Runnable emptyAction, Consumer<? super T> presentConsumer);

    default <U> Maybe<U> map(final Mapper<U, ? super T> mapper) {
        return fold(Maybe::none, t -> Maybe.of(mapper.map(t)));
    }

    default <U> Maybe<U> map(final Supplier<U> supplier) {
        return fold(Maybe::none, t -> of(supplier.get()));
    }

    default <U> Maybe<U> flatMap(final Mapper<Maybe<U>, ? super T> mapper) {
        return fold(Maybe::none, mapper);
    }

    default <U> Maybe<U> flatMap(final Supplier<Maybe<U>> supplier) {
        return fold(Maybe::none, t -> supplier.get());
    }

    default Maybe<T> filter(final Predicate<? super T> predicate) {
        return fold(Maybe::none, v -> predicate.test(v) ? this : none());
    }

    default Stream<T> stream() {
        return fold(Stream::empty, Stream::of);
    }

    default Bool isPresent() {
        return Bool.FALSE;
    }

    default Bool isEmpty() {
        return isPresent().negate();
    }

    static <U> U identity(final U value) {
        return value;
    }

    default T or(final T replacement) {
        return fold(() -> replacement, Maybe::identity);
    }

    default T orNull() {
        return fold(Fn::toNull, t -> t);
    }

    default T or(final Supplier<T> supplier) {
        return fold(supplier, Maybe::identity);
    }

    default Maybe<T> orElse(final T replacement) {
        return fold(() -> Maybe.ofNullable(replacement), t -> this);
    }

    default Maybe<T> orElse(final Maybe<T> replacement) {
        return fold(() -> replacement, t -> this);
    }

    default Maybe<T> orElse(final Supplier<Maybe<T>> supplier) {
        return fold(supplier, t -> this);
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
        return fold(Optional::empty, Optional::of);
    }

    /**
     * Wraps the value of this{@link Maybe} in a {@link Result}.
     * 
     * @return an instance of {@link Result} wrapping the value of this
     *         {@link Maybe}.
     */
    default Result<T> toResult(final Anomaly anomaly) {
        return fold(() -> Result.failure(anomaly), Result::success);
    }

    @SafeVarargs
    static <U> Maybe<U> any(final Maybe<U>... maybes) {
        return Arrays.stream(maybes).filter(m -> m.isPresent().toBoolean()).findFirst().orElse(Maybe.none());
    }

    @SafeVarargs
    static <U> Maybe<U> any(final Supplier<Maybe<U>>... maybeSuppliers) {
        return Arrays.stream(maybeSuppliers).map(Supplier::get).filter(m -> m.isPresent().toBoolean()).findFirst()
                .orElse(Maybe.none());
    }

    @SafeVarargs
    static <U> Maybe<U> any(final Optional<U>... optionals) {
        return Arrays.stream(optionals).map(Maybe::of).filter(m -> m.isPresent().toBoolean()).findFirst()
                .orElse(Maybe.none());
    }

    @SafeVarargs
    static <U> Maybe<U> anyOptional(final Supplier<Optional<U>>... optionalSuppliers) {
        return Arrays.stream(optionalSuppliers).map(Supplier::get).map(Maybe::of).filter(m -> m.isPresent().toBoolean())
                .findFirst()
                .orElse(Maybe.none());
    }

}
