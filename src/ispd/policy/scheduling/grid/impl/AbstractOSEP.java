package ispd.policy.scheduling.grid.impl;

import ispd.policy.scheduling.grid.GridSchedulingPolicy;

import java.util.ArrayList;

public abstract class AbstractOSEP extends GridSchedulingPolicy {
    public AbstractOSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }
}
