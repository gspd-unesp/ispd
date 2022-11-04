package ispd.motor.random;

/**
 * Utility class to contain information about computing or communication sizes.
 * They are expressed as an inclusive range, with an intervalSplit and
 * firstIntervalProbability
 * (parameters for a two-stage uniform distribution).
 */
public record TwoStageUniform(
        double minimum,
        double intervalSplit,
        double maximum,
        double firstIntervalProbability) {
    private static final double EVEN_PROBABILITY = 0.5;

    /**
     * Construct an instance with all values set to 0.
     */
    public TwoStageUniform() {
        this(0, 0, 0, 0);
    }

    /**
     * Construct an instance with given {@code minimum} and {@code maximum}
     * values, and with intervalSplit and firstIntervalProbability to
     * represent a
     * <b>uniform
     * distribution</b>.
     *
     * @param minimum distribution minimum
     * @param maximum distribution maximum
     */
    public TwoStageUniform(final double minimum, final double maximum) {
        this(
                minimum,
                (minimum + maximum) / 2,
                maximum,
                TwoStageUniform.EVEN_PROBABILITY
        );
    }

    /**
     * Make a 'normalized' instance from this one. {@link #intervalSplit} and
     * {@link #firstIntervalProbability}
     * information are copied from the called-from instance (which is left
     * unchanged), but the minimum and maximum values are normalized. See
     * {@link #normalizeValue(double, double)}.
     *
     * @return normalized instance
     */
    public TwoStageUniform rangeNormalized() {
        return new TwoStageUniform(
                TwoStageUniform.normalizeValue(
                        this.intervalSplit(),
                        this.minimum()
                ),
                this.intervalSplit(),
                TwoStageUniform.normalizeValue(
                        this.intervalSplit(),
                        this.minimum()
                ),
                this.firstIntervalProbability()
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
    private static double normalizeValue(
            final double value, final double boundary) {
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
    public double generateValue(final Distribution random) {
        return random.twoStageUniform(
                this.minimum,
                this.intervalSplit,
                this.maximum,
                this.firstIntervalProbability
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
        return "TwoStageUniform(min=%f, med=%f, max=%f, prob=%f)".formatted(
                this.minimum,
                this.intervalSplit,
                this.maximum,
                this.firstIntervalProbability
        );
    }
}