package ispd.policy.scheduling.grid.impl.util;

public class M_OSEP_StatusUser {
    private final Double perfShare;
    private Double servedPerf;

    public M_OSEP_StatusUser(final String user, final Double perfShare) {
        this.servedPerf = 0.0;
        this.perfShare = perfShare;
    }

    public void AtualizaUso(final Double poder, final int opc) {
        if (opc == 1) {
            this.servedPerf = this.servedPerf + poder;
        } else {
            this.servedPerf = this.servedPerf - poder;
        }
    }

    public Double getPerfShare() {
        return this.perfShare;
    }

    public Double getServedPerf() {
        return this.servedPerf;
    }
}
