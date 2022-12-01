package ispd.policy.scheduling.grid.impl;

import ispd.policy.scheduling.grid.GridSchedulingPolicy;

import java.util.ArrayList;

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
}
