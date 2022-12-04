package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Implementation of the RoundRobin scheduling algorithm.<br>
 * Hands over the next task on the FIFO queue,
 * for the next resource in a circular queue of resources.
 */
@Policy
public class RoundRobin extends GridSchedulingPolicy {
    private ListIterator<CS_Processamento> resources = null;

    public RoundRobin() {
        this.tarefas = new ArrayList<>(0);
        this.escravos = new LinkedList<>();
    }

    @Override
    public void iniciar() {
        this.resources = this.escravos.listIterator(0);
    }

    @Override
    public Tarefa escalonarTarefa() {
        return this.tarefas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (!this.resources.hasNext()) {
            this.resources = this.escravos.listIterator(0);
        }
        return this.resources.next();
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        final var task = this.escalonarTarefa();
        final var resource = this.escalonarRecurso();
        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
        this.mestre.sendTask(task);
    }
}