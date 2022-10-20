package ispd.motor.carga;

/**
 * Simple interface for an infinite generator of elements.
 * Similar to a {@link java.util.function.Supplier}, but whose functional
 * method is {@link #next()}.
 *
 * @param <T> Type to be returned by the generation method {@link #next()}
 */
@FunctionalInterface
public interface Generator <T> {
    /**
     * Generate and return the next element.
     *
     * @return a generated element.
     */
    T next();
}