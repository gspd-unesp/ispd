package ispd.policy;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;

import java.util.List;

public abstract class Policy <T extends PolicyMaster> {
    protected T mestre = null;
    protected List<List> caminhoEscravo = null;
    protected List<CS_Processamento> escravos = null;

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

    public List<List> getCaminhoEscravo() {
        return this.caminhoEscravo;
    }

    public void setCaminhoEscravo(final List<List> caminhoEscravo) {
        this.caminhoEscravo = caminhoEscravo;
    }

    public List<CS_Processamento> getEscravos() {
        return this.escravos;
    }

    public void addEscravo(final CS_Processamento newSlave) {
        this.escravos.add(newSlave);
    }
}
