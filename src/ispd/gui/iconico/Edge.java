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
     * Constructor which specifies the source and destination vertices.
     * Mathematically speaking, the source vertex is which this edge is
     * <em>incident from</em> and the destination vertex is which this
     * edge is <em>incident to</em>.
     *
     * @param source      the source vertex
     * @param destination the destination vertex
     */
    protected Edge(final Vertex source,
                   final Vertex destination) {
        this.source = source;
        this.destination = destination;
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
     * Returns the central edge position in x-axis in cartesian coordinates.
     *
     * @return the central edge position in x-axis in cartesian coordinates.
     */
    @Override
    public Integer getX() {
        return Edge.midPoint(this.source.getX(), this.destination.getX());
    }

    private static int midPoint(final int p1, final int p2) {
        return p1 + (p2 - p1) / 2;
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
}