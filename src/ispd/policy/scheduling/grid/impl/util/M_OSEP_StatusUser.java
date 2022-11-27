package ispd.policy.scheduling.grid.impl.util;

public class M_OSEP_StatusUser extends EHOSEP_StatusUser {
    public M_OSEP_StatusUser(final String user, final Double perfShare) {
        super(user, perfShare);
        this.servedPerf = 0.0;
    }

    public void AtualizaUso(final Double poder, final int opc) {
        if (opc == 1) {
            this.servedPerf = this.servedPerf + poder;
        } else {
            this.servedPerf = this.servedPerf - poder;
        }
    }

    public double getPerfShare() {
        return this.perfShare;
    }

    public double getServedPerf() {
        return this.servedPerf;
    }
}
