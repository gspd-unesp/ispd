package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;

public class M_OSEP_StatusUser {

    private final Double Cota;
    private Double PoderEmUso;
    private int numCota;
    private int numUso;

    public M_OSEP_StatusUser(final String usuario, final Double poder,
                             final List<CS_Processamento> slaves) {
        this.PoderEmUso = 0.0;
        this.Cota = poder;
        this.numCota = 0;
        this.numUso = 0;

        for (final CS_Processamento escravo : slaves) {
            if (escravo.getProprietario().equals(usuario)) {
                this.numCota++;
            }
        }


    }

    public void AtualizaUso(final Double poder, final int opc) {
        if (opc == 1) {
            this.PoderEmUso = this.PoderEmUso + poder;
            this.numUso++;
        } else {
            this.PoderEmUso = this.PoderEmUso - poder;
            this.numUso--;
        }
    }

    public Double GetCota() {
        return this.Cota;
    }

    public Double GetUso() {
        return this.PoderEmUso;
    }

    public int GetNumCota() {
        return this.numCota;
    }

    public int GetNumUso() {
        return this.numUso;
    }
}
