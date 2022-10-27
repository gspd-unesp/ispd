package ispd.motor.carga.task;

/**
 * Utility class to contain information about computing or communication sizes.
 * They are expressed as an inclusive range, with an average and probability
 * (parameters for a two-stage uniform distribution).
 */
public record TaskSize(
        double minimum, double maximum,
        double average, double probability) {
    /**
     * Construct an instance with all values set to 0.
     */
    public TaskSize() {
        this(0, 0, 0, 0);
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
}
