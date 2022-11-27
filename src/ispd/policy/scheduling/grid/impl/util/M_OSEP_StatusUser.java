package ispd.policy.scheduling.grid.impl.util;

public class M_OSEP_StatusUser extends EHOSEP_StatusUser {
    public M_OSEP_StatusUser(final String user, final Double perfShare) {
        super(user, perfShare);
    }

    public void AtualizaUso(final Double poder, final int opc) {
        if (opc == 1) {
            this.addServedPerf(poder);
        } else {
            this.rmServedPerf(poder);
        }
    }
}
