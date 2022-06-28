package ispd.gui.iconico.grade;

import java.util.ResourceBundle;
import java.util.Set;

public interface GridItem {
    GridItemId getId();

    Set<GridItem> getConnectionsIn();

    Set<GridItem> getConnectionsOut();

    String makeDescription(ResourceBundle translator);

    // TODO: Maybe encapsulate both parameters in a GridItemId object?
    GridItem makeCopy(int mousePosX,
                      int mousePosY,
                      int copyGlobalId,
                      int copyLocalId);

    boolean isCorrectlyConfigured();
}