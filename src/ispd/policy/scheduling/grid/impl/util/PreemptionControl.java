package ispd.policy.scheduling.grid.impl.util;

public class PreemptionControl {
    protected final String usuarioPreemp;
    protected final String usuarioAlloc;
    protected final int preempID;//ID da tarefa que sofreu preempção
    protected final int allocID;//ID da tarefa alocada

    public PreemptionControl(final String user1, final int pID,
                             final String user2, final int aID) {
        this.usuarioPreemp = user1;
        this.preempID = pID;
        this.usuarioAlloc = user2;
        this.allocID = aID;
    }

    public String getUsuarioPreemp() {
        return this.usuarioPreemp;
    }

    public int getPreempID() {
        return this.preempID;
    }

    public String getUsuarioAlloc() {
        return this.usuarioAlloc;
    }

    public int getAllocID() {
        return this.allocID;
    }
}
