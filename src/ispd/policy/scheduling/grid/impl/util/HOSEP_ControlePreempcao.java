package ispd.policy.scheduling.grid.impl.util;

//Classe para armazenar dados sobre as preempções que ainda não terminaram
public class HOSEP_ControlePreempcao extends PreemptionControl {

    public HOSEP_ControlePreempcao(final String user1, final int pID,
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
