package ispd.gui.iconico.grade;

public class GridItemIdentifier {

    /**
     * It represents the {@link GridItem} local identifier.
     */
    private Integer localId;

    /**
     * It represents the {@link GridItem} global identifier.
     */
    private Integer globalId;

    /**
     * It represents the {@link GridItem} name identifier.
     */
    private String name;

    /**
     * Constructor of {@link GridItemIdentifier} which
     * specifies the local, global and name identifiers.
     *
     * @param localId  the local identifier
     * @param globalId the global identifier
     * @param name     the name identifier
     */
    public GridItemIdentifier(final int localId,
                              final int globalId,
                              final String name) {
        this.localId = localId;
        this.globalId = globalId;
        this.name = name;
    }

    /**
     * Returns the local id.
     *
     * @return the local id
     */
    public Integer getLocalId() {
        return this.localId;
    }

    /**
     * It sets the local identifier
     *
     * @param localId the local identifier to be set
     */
    public void setLocalId(final Integer localId) {
        this.localId = localId;
    }

    /**
     * Returns the global identifier
     *
     * @return the global identifier
     */
    public Integer getGlobalId() {
        return this.globalId;
    }

    /**
     * It sets the global identifier
     *
     * @param globalId the global identifier to be set
     */
    public void setGlobalId(final Integer globalId) {
        this.globalId = globalId;
    }

    /**
     * Returns the name identifier
     *
     * @return the name identifier
     */
    public String getName() {
        return this.name;
    }

    /**
     * It sets the name
     *
     * @param name the name to be set
     */
    public void setName(final String name) {
        this.name = name;
    }
}
