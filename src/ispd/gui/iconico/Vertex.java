package ispd.gui.iconico;

public abstract class Vertex implements Icon {

    /**
     * It represents the <em>base</em> x-coordinate in cartesian
     * coordinates.
     */
    private int baseX = 0;

    /**
     * It represents the <em>base</em> y-coordinate in cartesian
     * coordinates.
     */
    private int baseY = 0;

    /**
     * It represents the x-coordinate in cartesian coordinates.
     */
    private Integer x;

    /**
     * It represents the y-coordinate in cartesian coordinates.
     */
    private Integer y;

    /**
     * It marks whether the vertex is selected. If this
     * variable is {@code true}, then this vertex is selected;
     * otherwise, it is not selected.
     */
    private boolean selected;

    /**
     * Constructor which specifies the
     * x-coordinate and y-coordinate in the cartesian coordinates.
     * Further, the icon is set as non-selected by default.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @see #Vertex(Integer, Integer, boolean)
     */
    protected Vertex(final Integer x, final Integer y) {
        this(x, y, false);
    }

    /**
     * Constructor which specifies the
     * x-coordinate, y-coordinate and whether the icon
     * is selected.
     *
     * @param x        the x-coordinate
     * @param y        the y-coordinate
     * @param selected whether the icon is selected
     */
    protected Vertex(final Integer x,
                     final Integer y,
                     final boolean selected) {
        this.x = x;
        this.y = y;
        this.selected = selected;
    }

    /**
     * Returns the base x-coordinate.
     *
     * @return the base x-coordinate
     */
    /* package-private */ int getBaseX() {
        return this.baseX;
    }

    /**
     * Returns the base y-coordinate.
     *
     * @return the base y-coordinate
     */
    /* package-private */ int getBaseY() {
        return this.baseY;
    }

    /**
     * It sets the vertex's <em>base</em> X and Y coordinates to the
     * specified coordinates.
     *
     * @param x the base x-coordinate
     * @param y the base y-coordinate
     */
    /* package-private */ void setBase(final Integer x, final Integer y) {
        this.baseX = x;
        this.baseY = y;
    }

    /**
     * It sets the vertex's X and Y coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public void setPosition(final Integer x, final Integer y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns {@code true} if this vertex is selected.
     * Otherwise, returns {@code false}.
     *
     * @return {@code true} if this vertex is selected;
     * otherwise, returns {@code false}.
     */
    @Override
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * It sets the vertex as selected or not.
     *
     * @param selected if {@code true}, set this icon as selected;
     *                 otherwise, set this icon as not selected.
     */
    @Override
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    /**
     * Returns the x-coordinate.
     *
     * @return the x-coordinate
     */
    @Override
    public Integer getX() {
        return this.x;
    }

    /**
     * Returns the y-coordinate.
     *
     * @return the y-coordinate
     */
    @Override
    public Integer getY() {
        return this.y;
    }
}