package ispd.arquivo.xml.utils;

/**
 * Utility class to contain information about computing or communication sizes.
 * Sizes are usually expressed as ranges, with an average and probability.
 */
public record SizeInfo(
        double minimum, double maximum,
        double average, double probability) {
    public SizeInfo() {
        this(0, 0, 0, 0);
    }

    /**
     * Construct an instance from a {@link WrappedElement} object, copying
     * all information from it.
     */
    public SizeInfo(final WrappedElement e) {
        this(e.minimum(), e.maximum(), e.average(), e.probability());
    }

    /**
     * Construct an instance from a {@link WrappedElement} object, copying
     * only minimum and maximum information (no average or probability).
     */
    public static SizeInfo rangeFrom(final WrappedElement e) {
        return new SizeInfo(e.minimum(), e.maximum(), 0, 0);
    }

    /**
     * Construct an instance from a {@link WrappedElement} object, copying
     * minimum, maximum and average information, leaving out probability.
     */
    public static SizeInfo noProbability(final WrappedElement e) {
        return new SizeInfo(e.minimum(), e.maximum(), e.average(), 0);
    }

    /**
     * Make a 'normalized' instance from this one. Average and probability
     * information are copied from the called-from instance (which is left
     * unchanged), but the minimum and maximum values are normalized. See
     * {@link #normalizeValue(double, double)}.
     *
     * @return normalized instance
     */
    public SizeInfo rangeNormalized() {
        return new SizeInfo(
                SizeInfo.normalizeValue(this.average(), this.minimum()),
                SizeInfo.normalizeValue(this.average(), this.minimum()),
                this.average(),
                this.probability()
        );
    }

    /**
     * Normalize a value to respect the given boundary and to be within the
     * range (0, 1).
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
