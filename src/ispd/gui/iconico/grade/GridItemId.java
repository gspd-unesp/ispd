package ispd.gui.iconico.grade;

public class GridItemId {
    private final Integer localId;
    private final Integer globalId;
    private String name;

    /* package-private */ GridItemId(final int localId,
                                     final int globalId,
                                     final String name) {
        this.localId = localId;
        this.globalId = globalId;
        this.name = name;
    }

    /* package-private */ Integer getLocalId() {
        return this.localId;
    }

    public Integer getGlobalId() {
        return this.globalId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}