package ispd.gui.iconico.grade;

import java.util.ResourceBundle;
import java.util.Set;

public interface GridItem {

    /**
     * Returns its identifier.
     *
     * @return its identifier
     */
    GridItemIdentifier getId();

    /**
     * Returns the inbound connections.
     * <p>
     * The inbound connections are those {@link GridItem}s
     * whose edges are incident to this grid item.
     *
     * @return the inbound connections
     */
    Set<GridItem> getInboundConnections();

    /**
     * Returns the outbound connections.
     * <p>
     * The outbound connections are those {@link GridItem}s
     * whose edges are incident from this grid item.
     *
     * @return the outbound connections
     */
    Set<GridItem> getOutboundConnections();

    /**
     * It makes the grid item description relative to the
     * specified translator.
     *
     * @param translator the translator containing the
     *                   translated messages
     * @return the grid item description
     */
    String makeDescription(ResourceBundle translator);

    /**
     * It returns a copy of this grid item relative to the
     * specified parameters.
     *
     * @param mousePosX the mouse x-coordinate in
     *                  cartesian coordinates
     * @param mousePosY the mouse y-coordinate in
     *                  cartesian coordinates
     * @param globalId  the global identifier
     * @param localId   the local identifier
     * @return a copy of this {@link GridItem}
     */
    GridItem makeCopy(int mousePosX,
                      int mousePosY,
                      int globalId,
                      int localId);

    /**
     * Returns {@code true} since this {@link GridItem} is
     * configured. Otherwise, {@code false} is returned.
     *
     * @return {@code true} since this {@link GridItem} is
     *         configured; otherwise, {@code false}.
     */
    boolean isConfigured();
}
