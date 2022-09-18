package ispd.gui.iconico;

import java.awt.Toolkit;

/**
 * {@link RulerUnit} is an enumeration that stores the available
 * ruler units.
 */
public enum RulerUnit {
    CENTIMETERS("cm") {
        /**
         * Returns the unit in centimeters unit.
         * @return the unit in centimeters unit
         */
        @Override
        public int getUnit() {
            /* 1 in = 2.54 cm */
            return (int) ((double) RulerUnit.INCH / 2.54D);
        }

        /**
         * Returns the increment in centimeters unit.
         * @return the increment in centimeters unit
         */
        @Override
        public int getIncrement() {
            return this.getUnit();
        }
    },
    INCHES("in") {
        /**
         * Returns the unit in inches unit.
         * @return the unit in inches unit
         */
        @Override
        public int getUnit() {
            return RulerUnit.INCH;
        }

        /**
         * Returns the increment in inches unit.
         * @return the increment in inches unit
         */
        @Override
        public int getIncrement() {
            return this.getUnit() >> 1;
        }
    };

    /**
     * It represents the screen resolution in dots-per-inch.
     */
    private static final int INCH = Toolkit.getDefaultToolkit()
            .getScreenResolution();

    /**
     * It stores the unit symbol. Every symbol must be
     * in English and singular form.
     */
    private final String symbol;

    /**
     * Constructor which specifies the unit symbol.
     *
     * @param symbol the symbol
     */
    /* package-private */ RulerUnit(final String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the ruler unit.
     *
     * @return the ruler unit
     */
    public abstract int getUnit();

    /**
     * Returns the ruler increment.
     *
     * @return the ruler increment
     */
    public abstract int getIncrement();

    /**
     * It returns the next unit described after this one.
     * Further, if it does not have any unit described
     * after this one, then the <em>topmost (or the first)</em>
     * unit is returned, instead.
     * <p>
     * An example of such method operation is given below,
     * first suppose the units is described as
     * <ul>
     *     <li>CENTIMETERS</li>
     *     <li>INCHES</li>
     * </ul>
     * and suppose that this unit is <em>centimeters</em>.
     * Therefore, the next described unit after this one
     * is <em>inches</em>.
     *
     * @return the next unit described after this one
     */
    public RulerUnit nextUnit() {
        final var values = RulerUnit.values();
        return values[(this.ordinal() + 1) % values.length];
    }

    /**
     * Returns the unit symbol.
     *
     * @return the unit symbol
     */
    public String getSymbol() {
        return this.symbol;
    }
}