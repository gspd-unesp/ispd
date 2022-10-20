package ispd.motor.carga;

/**
 * A simple {@link Generator} which generates integers in arithmetic sequence.
 */
public class SequentialIntegerGenerator implements Generator<Integer> {
    private int nextAvailableId;

    /**
     * Create an instance that will start producing integers from 0, including.
     */
    public SequentialIntegerGenerator() {
        this(0);
    }

    /**
     * Create an instance that will start producing integers from {@code
     * start}, including.
     *
     * @param start first integer to be produced by the instantiated generator.
     */
    public SequentialIntegerGenerator(final int start) {
        this.nextAvailableId = start;
    }

    @Override
    public Integer next() {
        final var returnId = this.nextAvailableId;
        this.nextAvailableId++;
        return returnId;
    }
}