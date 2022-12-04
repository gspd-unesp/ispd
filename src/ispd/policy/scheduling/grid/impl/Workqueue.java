package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Policy
public class Workqueue extends GridSchedulingPolicy {
    private final LinkedList<Tarefa> ultimaTarefaConcluida = new LinkedList<>();
    private List<Tarefa> tarefaEnviada = null;

    public Workqueue() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        this.tarefaEnviada = new ArrayList<>(this.escravos.size());
        for (int i = 0; i < this.escravos.size(); i++) {
            this.tarefaEnviada.add(null);
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        if (!this.tarefas.isEmpty()) {
            return this.tarefas.remove(0);
        }
        return null;
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (!this.ultimaTarefaConcluida.isEmpty() && !this.ultimaTarefaConcluida.getLast().isCopy()) {
            final int index =
                    this.tarefaEnviada.indexOf(this.ultimaTarefaConcluida.getLast());
            return this.escravos.get(index);
        } else {
            for (int i = 0; i < this.tarefaEnviada.size(); i++) {
                if (this.tarefaEnviada.get(i) == null) {
                    return this.escravos.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        final CS_Processamento rec = this.escalonarRecurso();

        if (rec == null) {
            return;
        }

        final Tarefa trf = this.escalonarTarefa();

        if (trf == null) {
            return;
        }

        this.tarefaEnviada.set(this.escravos.indexOf(rec), trf);
        if (!this.ultimaTarefaConcluida.isEmpty()) {
            this.ultimaTarefaConcluida.removeLast();
        }
        trf.setLocalProcessamento(rec);
        trf.setCaminho(this.escalonarRota(rec));
        this.mestre.sendTask(trf);
    }

    @Override
    public void adicionarTarefa(final Tarefa tarefa) {
        super.adicionarTarefa(tarefa);
        if (this.tarefaEnviada.contains(tarefa)) {
            final int index = this.tarefaEnviada.indexOf(tarefa);
            this.tarefaEnviada.set(index, null);
            this.mestre.executeScheduling();
        }
    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        this.ultimaTarefaConcluida.add(tarefa);
        if (!this.tarefas.isEmpty()) {
            this.mestre.executeScheduling();
        }
    }
}
