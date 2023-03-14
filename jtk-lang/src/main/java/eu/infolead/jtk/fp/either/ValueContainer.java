package eu.infolead.jtk.fp.either;

sealed abstract class ValueContainer<T> permits AbstractEither {
    /**
     * <strong>Note: </strong> Must not be {@code final}, because else
     * deserialization will fail.
     * <p>
     * Cf. <a target="_blank" href=
     * "https://rules.sonarsource.com/java/RSPEC-2055">https://rules.sonarsource.com/java/RSPEC-2055</a>
     * 
     */
    private T value;

    ValueContainer() {
        // do nothing, necessary for deserialization of Serializable subclasses
    }

    ValueContainer(final T value) {
        this.value = value;
    }

    public final T value() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
