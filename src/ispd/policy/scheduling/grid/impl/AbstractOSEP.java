package ispd.policy.scheduling.grid.impl;

import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOSEP extends GridSchedulingPolicy {
    private static final double REFRESH_TIME = 15.0;

    public AbstractOSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    public Double getTempoAtualizar() {
        return AbstractOSEP.REFRESH_TIME;
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }
}
