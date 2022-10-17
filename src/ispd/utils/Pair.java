package ispd.utils;

/**
 * The {@link Pair} is used to store two, possibly, different type values.
 * <p>
 * This class provide an easy way to access the stored values in this pair. For
 * instance, let be the following pair
 * <pre>{@code
 *      Pair pair = new Pair<Integer, Integer>(2, 3);
 * }</pre>
 * Therefore, we say that the first pair's value is {@code 2} and the second
 * pair's value is {@code 3}. Moreover, these values can be accessed by
 * {@link #getFirst()} and {@link #getSecond()} methods, respectively.
 *
 * @param <T> the first value type
 * @param <U> the second value type
 */
public final class Pair<T, U> {

    /**
     * The first pair's value.
     */
    private final T first;

    /**
     * The second pair's value.
     */
    private final U second;

    /**
     * Constructor which specifies the first and second value.
     *
     * @param first  the first value.
     * @param second the second value.
     */
    public Pair(final T first, final U second) {
        this.first = first;
        this.second = second;
    }

    /**
     * It returns the first pair's value.
     *
     * @return the first pair's value.
     */
    public T getFirst() {
        return this.first;
    }

    /**
     * It returns the second pair's value.
     *
     * @return the second pair's value.
     */
    public U getSecond() {
        return this.second;
    }

}
