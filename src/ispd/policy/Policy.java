package ispd.policy;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;

import java.util.List;

public abstract class Policy <T extends PolicyMaster> {
    protected T mestre = null;

    public abstract void iniciar();

    public abstract List<CentroServico> escalonarRota(CentroServico destino);

    public abstract void escalonar();

    public abstract CS_Processamento escalonarRecurso();

    public Double getTempoAtualizar() {
        return null;
    }

    public T getMestre() {
        return this.mestre;
    }

    public void setMestre(final T mestre) {
        this.mestre = mestre;
    }
}
