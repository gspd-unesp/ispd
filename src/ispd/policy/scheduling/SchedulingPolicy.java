package ispd.policy.scheduling;

import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.metricas.MetricasUsuarios;
import ispd.policy.Policy;

import java.util.List;

public abstract class SchedulingPolicy <T extends SchedulingMaster> implements Policy<T> {
    protected List<List> filaEscravo = null;
    protected List<CS_Processamento> escravos = null;
    protected List<Tarefa> tarefas = null;
    protected MetricasUsuarios metricaUsuarios = null;
    protected List<List> caminhoEscravo = null;
    protected T mestre = null;

    public void setMestre(final T mestre) {
        this.mestre = mestre;
    }

    public abstract Tarefa escalonarTarefa();

    public List<CS_Processamento> getEscravos() {
        return this.escravos;
    }

    public void addEscravo(final CS_Processamento newSlave) {
        this.escravos.add(newSlave);
    }

    public void addTarefaConcluida(final Tarefa tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasConcluidas(tarefa);
        }
    }

    public MetricasUsuarios getMetricaUsuarios() {
        return this.metricaUsuarios;
    }

    public void setMetricaUsuarios(final MetricasUsuarios metricaUsuarios) {
        this.metricaUsuarios = metricaUsuarios;
    }

    public List<List> getCaminhoEscravo() {
        return this.caminhoEscravo;
    }

    public void setCaminhoEscravo(final List<List> caminhoEscravo) {
        this.caminhoEscravo = caminhoEscravo;
    }

    public List<Tarefa> getFilaTarefas() {
        return this.tarefas;
    }

    public void resultadoAtualizar(final Mensagem mensagem) {
        final int index = this.escravos.indexOf(mensagem.getOrigem());
        this.filaEscravo.set(index, mensagem.getFilaEscravo());
    }

    public void adicionarTarefa(final Tarefa tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasSubmetidas(tarefa);
        }
        this.tarefas.add(tarefa);
    }
}
