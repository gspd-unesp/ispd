package ispd.policy.scheduling.grid.impl.util;

//Classe para armazenar dados sobre as preempções que ainda não terminaram
public class EHOSEP_ControlePreempcao extends PreemptionControl {
    public EHOSEP_ControlePreempcao(final String userP, final int pID,
                                    final String userA, final int aID) {
        super(userP, pID, userA, aID);
    }
}
