package ispd.gui.iconico.grade;

import ispd.gui.iconico.Vertex;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

/* package-private */ abstract class VertexGridItem
        extends Vertex implements GridItem {

    /**
     * It contains the grid item identifier.
     */
    protected final GridItemIdentifier id;

    /**
     * It stores all of inbound connections.
     */
    protected final Set<GridItem> inboundConnections;

    /**
     * It stores all of outbound connections.
     */
    protected final Set<GridItem> outboundConnections;

    /**
     * It stores the state of whether this vertex is
     * configured or not. Therefore, this stores {@code true}
     * if vertex is configured; otherwise, it stores
     * {@code false}.
     * <p>
     * The state of this variable is {@code false} by default,
     * however, it can be changed by attribution or at
     * construction.
     */
    protected boolean configured;

    /**
     * Constructor of {@link VertexGridItem} which specifies
     * the local, global and name identifiers, as well as,
     * the X and Y coordinates.
     *
     * @param localId  the local id
     * @param globalId the global id
     * @param name     the name
     * @param x        the vertex grid item x-coordinate
     *                 in cartesian coordinates
     * @param y        the vertex grid item y-coordinate
     *                 in cartesian coordinates
     */
    public VertexGridItem(
            final int localId,
            final int globalId,
            final String name,
            final Integer x,
            final Integer y) {
        this(localId, globalId, name, x, y, false);
    }

    /**
     * Constructor of {@link VertexGridItem} which specifies
     * the local, global and name identifiers, as well as,
     * the X and Y coordinates and whether is selected.
     *
     * @param localId  the local id
     * @param globalId the global id
     * @param name     the name
     * @param x        the vertex grid item x-coordinate
     *                 in cartesian coordinates
     * @param y        the vertex grid item y-coordinate
     *                 in cartesian coordinates
     * @param selected whether is selected
     */
    public VertexGridItem(
            final int localId,
            final int globalId,
            final String name,
            final Integer x,
            final Integer y,
            final boolean selected) {
        super(x, y, selected);
        this.id = new GridItemIdentifier(localId, globalId, name + globalId);
        this.inboundConnections = new HashSet<>();
        this.outboundConnections = new HashSet<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(final Graphics g) {
        final var configuredStatusImage = this.configured ?
                DesenhoGrade.greenIcon : DesenhoGrade.redIcon;

        g.drawImage(this.getImage(), this.getX() - 15,
                this.getY() - 15, null);
        g.drawImage(configuredStatusImage, this.getX() + 15,
                this.getY() + 15, null);

        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(this.id.getGlobalId()),
                this.getX(), this.getY() + 30);

        /* If the icon is active, then a margin is drawn */
        if (this.isSelected()) {
            final var offset = this.getOffset();
            final var squareSize = 34;

            g.setColor(Color.RED);
            g.drawRect(this.getX() - offset,
                    this.getY() - offset,
                    squareSize, squareSize);
        }
    }

    /**
     * Returns this grid item offset.<br>
     * The offset represents a <i>margin of error</i> to state whether this
     * grid item is contained at a given x-coordinate and y-coordinate in
     * {@link #contains(int, int)} method.
     *
     * @return this grid item offset
     */
    protected int getOffset() {
        return 17;
    }

    /**
     * Returns {@code true} if this grid item is contained at
     * the given x-coordinate and y-coordinate (in cartesian
     * coordinates) plus a <em>offset</em>. Otherwise, {@code
     * false} is returned.
     *
     * @param x the X-coordinate
     * @param y the Y-coordinate
     * @return {@code true} if this grid item is contained at
     * the given coordinates; otherwise {@code false}
     * is returned.
     */
    @Override
    public boolean contains(final int x, final int y) {
        final var offset = this.getOffset();
        return (x > this.getX() - offset && x < this.getX() + offset) &&
               (y > this.getY() - offset && y < this.getY() + offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridItemIdentifier getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<GridItem> getInboundConnections() {
        return this.inboundConnections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<GridItem> getOutboundConnections() {
        return this.outboundConnections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigured() {
        return this.configured;
    }
}