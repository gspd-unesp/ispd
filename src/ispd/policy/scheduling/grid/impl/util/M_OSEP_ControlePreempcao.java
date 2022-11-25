package ispd.policy.scheduling.grid.impl.util;

public class M_OSEP_ControlePreempcao extends PreemptionControl {
    public M_OSEP_ControlePreempcao(final String user1, final int pID,
                                    final String user2, final int aID) {
        super(user1, pID, user2, aID);
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
