package ispd.policy.scheduling.grid.impl.util;

public class M_OSEP_StatusUser {

    private final Double Cota;
    private Double PoderEmUso;

    public M_OSEP_StatusUser(final String usuario, final Double poder) {
        this.PoderEmUso = 0.0;
        this.Cota = poder;
    }

    public void AtualizaUso(final Double poder, final int opc) {
        if (opc == 1) {
            this.PoderEmUso = this.PoderEmUso + poder;
        } else {
            this.PoderEmUso = this.PoderEmUso - poder;
        }
    }

    public Double GetCota() {
        return this.Cota;
    }

    public Double GetUso() {
        return this.PoderEmUso;
    }
}
