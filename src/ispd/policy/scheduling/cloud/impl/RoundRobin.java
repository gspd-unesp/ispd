package ispd.policy.scheduling.cloud.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.scheduling.cloud.CloudSchedulingPolicy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Implementation of the RoundRobin scheduling algorithm.
 * Hands over the next task on the FIFO queue,
 * for the next resource in a circular queue of resources.
 */
@Policy
public class RoundRobin extends CloudSchedulingPolicy {
    private ListIterator<CS_Processamento> resources = null;
    private LinkedList<CS_Processamento> slavesUser = null;

    public RoundRobin() {
        this.tarefas = new ArrayList<>(0);
        this.escravos = new LinkedList<>();
    }

    @Override
    public void iniciar() {
        System.out.println("iniciou escalonamento RR");
        this.slavesUser = new LinkedList<>();
        this.resources = this.slavesUser.listIterator(0);
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final var destination = (CS_Processamento) destino;
        final int index = this.escravos.indexOf(destination);

        System.out.println("traçando rota para a VM: " + destination.getId());
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));

    }

    @Override
    public void escalonar() {
        System.out.println("---------------------------");
        final var task = this.escalonarTarefa();
        final var taskOwner = task.getProprietario();
        this.slavesUser = (LinkedList<CS_Processamento>)
                this.getVMsAdequadas(taskOwner);

        if (this.slavesUser.isEmpty()) {
            this.noAllocatedVms(task);
        } else {
            this.scheduleTask(task);
        }

        System.out.println("---------------------------");
    }

    @Override
    public Tarefa escalonarTarefa() {
        return this.tarefas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (!this.resources.hasNext()) {
            this.resources = this.slavesUser.listIterator(0);
        }
        return this.resources.next();
    }

    private void noAllocatedVms(final Tarefa task) {
        System.out.printf(
                "Não existem VMs alocadas ainda, devolvendo tarefa %d%n",
                task.getIdentificador());
        this.adicionarTarefa(task);
        this.mestre.freeScheduler();
    }

    private void scheduleTask(final Tarefa task) {
        final var resource = this.escalonarRecurso();
        System.out.printf("escalonando tarefa %d para:%s%n",
                task.getIdentificador(), resource.getId());
        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
        this.mestre.sendTask(task);
    }
}