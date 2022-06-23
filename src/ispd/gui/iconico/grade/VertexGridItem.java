package ispd.gui.iconico.grade;

import ispd.gui.iconico.Vertex;

import java.util.HashSet;
import java.util.Set;

/* package-private */ abstract class VertexGridItem
        extends Vertex implements GridItem {

    /**
     * It contains the grid item identifier.
     */
    private final GridItemIdentifier id;

    /**
     * It stores all of inbound connections.
     */
    private final Set<GridItem> inboundConnections;

    /**
     * It stores all of outbound connections.
     */
    private final Set<GridItem> outboundConnections;

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
    public boolean configured;

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
     * It returns the offset of this item, that is,
     * @return
     */
    public abstract int getOffset();

    @Override
    public boolean contains(int x, int y) {
        return false;
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
