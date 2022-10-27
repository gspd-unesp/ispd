package ispd.motor.carga.task;

import ispd.motor.random.Distribution;

/**
 * Utility class to contain information about computing or communication sizes.
 * They are expressed as an inclusive range, with an average and probability
 * (parameters for a two-stage uniform distribution).
 */
public record TaskSize(
        double minimum,
        double maximum,
        double average,
        double probability) {
    private static final double EVEN_PROBABILITY = 0.5;

    /**
     * Construct an instance with all values set to 0.
     */
    public TaskSize() {
        this(0, 0, 0, 0);
    }

    /**
     * Construct an instance with given {@code minimum} and {@code maximum}
     * values, and with average and probability to represent a <b>uniform
     * distribution</b>.
     *
     * @param minimum distribution minimum
     * @param maximum distribution maximum
     */
    public TaskSize(final double minimum, final double maximum) {
        this(
                minimum, maximum,
                TaskSize.averageOf(minimum, maximum),
                TaskSize.EVEN_PROBABILITY
        );
    }

    private static double averageOf(final double a, final double b) {
        return (a + b) / 2;
    }

    /**
     * Make a 'normalized' instance from this one. Average and probability
     * information are copied from the called-from instance (which is left
     * unchanged), but the minimum and maximum values are normalized. See
     * {@link #normalizeValue(double, double)}.
     *
     * @return normalized instance
     */
    public TaskSize rangeNormalized() {
        return new TaskSize(
                TaskSize.normalizeValue(this.average(), this.minimum()),
                TaskSize.normalizeValue(this.average(), this.minimum()),
                this.average(),
                this.probability()
        );
    }

    /**
     * Normalize a value to respect the given boundary and to be within the
     * range [0, 1].
     *
     * @param value    value to be normalized
     * @param boundary boundary for normalization
     * @return normalized value, within [0, 1]
     */
    private static double normalizeValue(final double value,
                                         final double boundary) {
        final var d = Math.abs(value - boundary) / value;
        return Math.min(1.0, d);
    }

    /**
     * Generate a two-stage uniform with parameters from the fields of the
     * instance, using the given {@link Distribution}.
     *
     * @param random {@link Distribution} to generate a random value.
     * @return value in the interval from [{@link #minimum()},
     * {@link #maximum()}, with probability following a two-stage uniform
     * distribution.
     */
    public double rollTwoStageUniform(final Distribution random) {
        return random.twoStageUniform(
                this.minimum, this.average,
                this.maximum, this.probability
        );
    }

    /**
     * Makes a string with the values of the fields of this instance, in a
     * human-readable manner.
     *
     * @return {@link String} representing how this instance is initialized,
     * in a human-readable format.
     */
    @Override
    public String toString() {
        return "TaskSize(min=%f, max=%f, avg=%f, prob=%f)".formatted(
                this.minimum, this.maximum, this.average, this.probability
        );
    }
}