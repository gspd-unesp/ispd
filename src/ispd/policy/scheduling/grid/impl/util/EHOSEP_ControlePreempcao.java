package ispd.policy.scheduling.grid.impl.util;

//Classe para armazenar dados sobre as preempções que ainda não terminaram
public class EHOSEP_ControlePreempcao {

    private final String usuarioPreemp;
    private final String usuarioAlloc;
    private final int preempID;//ID da tarefa que sofreu preempção
    private final int allocID;//ID da tarefa alocada

    public EHOSEP_ControlePreempcao(final String userP, final int pID,
                                    final String userA, final int aID) {
        this.usuarioPreemp = userP;
        this.preempID = pID;
        this.usuarioAlloc = userA;
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
