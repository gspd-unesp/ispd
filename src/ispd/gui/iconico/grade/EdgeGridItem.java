package ispd.gui.iconico.grade;

import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;

/* package-private */ abstract class EdgeGridItem
        extends Edge implements GridItem {

    /**
     * It contains the edge grid item identifier.
     */
    protected final GridItemIdentifier id;

    /**
     * It stores the state of whether the edge is configured
     * or not. Therefore, this stores {@code true} if the
     * edge is configured; otherwise, it stores {@code false}.
     * <p>
     * The state of this variable is {@code false} by default,
     * however, it cna be changed by attribution or at
     * construction.
     */
    protected boolean configured;

    /**
     * Constructor of {@link EdgeGridItem} which specifies
     * the local, global and name identifiers, as well as,
     * the source and destination vertices and whether this
     * edge is selected or not.
     *
     * @param localId     the local identifier
     * @param globalId    the global identifier
     * @param name        the name
     * @param source      the source vertex
     * @param destination the destination vertex
     * @param selected    whether is selected
     */
    public EdgeGridItem(final int localId,
                        final int globalId,
                        final String name,
                        final Vertex source,
                        final Vertex destination,
                        final boolean selected) {
        super(source, destination, selected);
        this.id = new GridItemIdentifier(localId, globalId, name + globalId);
    }

    /**
     * Constructor of {@link EdgeGridItem} which specifies
     * the local, global and name identifiers, as well as,
     * the source and destination.
     *
     * @param localId     the local identifier
     * @param globalId    the global identifier
     * @param name        the name
     * @param source      the source vertex
     * @param destination the destination vertex
     */
    public EdgeGridItem(final int localId,
                        final int globalId,
                        final String name,
                        final Vertex source,
                        final Vertex destination) {
        this(localId, globalId, name, source, destination, false);
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
    public boolean isConfigured() {
        return this.configured;
    }
}
