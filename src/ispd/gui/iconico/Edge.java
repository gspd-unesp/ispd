package ispd.gui.iconico;

public abstract class Edge implements Icon {
    /**
     * It represents the source vertex. Mathematically speaking,
     * this edge is <em>incident from</em> this vertex.
     */
    private Vertex source;

    /**
     * It represents the destination vertex. Mathematically
     * speaking, this edge is <em>incident to</em> this vertex.
     */
    private Vertex destination;

    /**
     * It marks whether the edge is selected. If this variable
     * is {@code true}, then this edge is selected; otherwise,
     * it is not selected.
     */
    private boolean selected;

    /**
     * Constructor of {@link Edge} which specifies the source,
     * destination and whether the edge is selected. Mathematically
     * speaking, the source vertex is which this edge is
     * <em>incident from</em> and the destination vertex is
     * whcih this edge is <em>incident to</em>.
     *
     * @param source      the source vertex
     * @param destination the destination vertex
     * @param selected    whether the edge is selected
     */
    public Edge(final Vertex source,
                final Vertex destination,
                final boolean selected) {
        this.source = source;
        this.destination = destination;
        this.selected = selected;
    }

    /**
     * Constructor of {@link Edge} which specifies the source
     * and destination vertices. Mathematically speaking, the
     * source vertex is which this edge is <em>incident
     * from</em> and the destination vertex is which this
     * edge is <em>incident to</em>.
     *
     * @param source      the source vertex
     * @param destination the destination vertex
     */
    public Edge(final Vertex source,
                final Vertex destination) {
        this(source, destination, false);
    }

    /**
     * Returns the source vertex.
     *
     * @return the source vertex
     */
    public Vertex getSource() {
        return this.source;
    }

    /**
     * Returns the destination vertex.
     *
     * @return the destination vertex
     */
    public Vertex getDestination() {
        return this.destination;
    }

    /**
     * It sets the source and destination vertices.
     *
     * @param source      the source vertex
     * @param destination the destination vertex
     */
    public void setPosition(final Vertex source,
                            final Vertex destination) {
        this.source = source;
        this.destination = destination;
    }

    /**
     * Returns {@code true} if this edge is selected.
     * Otherwise, returns {@code false}.
     *
     * @return {@code true} if this edge is selected;
     *         otherwise, returns {@code false}.
     */
    @Override
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * It sets the edge as selected or not.
     *
     * @param selected if {@code true}, set this edge as selected;
     *                 otherwise, set this edge as not selected.
     */
    @Override
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    /**
     * Returns the central edge position in x-axis in cartesian coordinates.
     *
     * @return the central edge position in x-axis in cartesian coordinates.
     */
    @Override
    public Integer getX() {
        return Edge.midPoint(this.source.getX(), this.destination.getX());
    }

    /**
     * Returns the central edge position in y-axis in cartesian coordinates.
     *
     * @return the central edge position in y-axis in cartesian coordinates.
     */
    @Override
    public Integer getY() {
        return Edge.midPoint(this.source.getY(), this.destination.getY());
    }

    /**
     * It calculates the midpoint between the specified
     * points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the midpoint between the specified points
     */
    private static int midPoint(final int p1, final int p2) {
        return p1 + (p2 - p1) / 2;
    }
}