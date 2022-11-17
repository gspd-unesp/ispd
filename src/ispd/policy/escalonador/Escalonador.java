package ispd.policy.escalonador;

import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasUsuarios;

import java.util.List;

/**
 * Abstract class to represent a scheduling policy
 */
public abstract class Escalonador {
    protected List<CS_Processamento> escravos;
    protected List<List> filaEscravo;
    protected List<Tarefa> tarefas;
    protected MetricasUsuarios metricaUsuarios;
    protected Mestre mestre;
    protected List<List> caminhoEscravo;

    public abstract void iniciar();

    public abstract Tarefa escalonarTarefa();

    public abstract CS_Processamento escalonarRecurso();

    public abstract List<CentroServico> escalonarRota(CentroServico destino);

    public abstract void escalonar();

    public void adicionarTarefa(final Tarefa tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasSubmetidas(tarefa);
        }
        this.tarefas.add(tarefa);
    }

    //Get e Set

    public List<CS_Processamento> getEscravos() {
        return this.escravos;
    }

    public void setCaminhoEscravo(final List<List> caminhoEscravo) {
        this.caminhoEscravo = caminhoEscravo;
    }

    public void addEscravo(final CS_Processamento maquina) {
        this.escravos.add(maquina);
    }

    public void addTarefaConcluida(final Tarefa tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasConcluidas(tarefa);
        }
    }

    public List<Tarefa> getFilaTarefas() {
        return this.tarefas;
    }

    public MetricasUsuarios getMetricaUsuarios() {
        return this.metricaUsuarios;
    }

    public void setMetricaUsuarios(final MetricasUsuarios metricaUsuarios) {
        this.metricaUsuarios = metricaUsuarios;
    }

    public void setMestre(final Mestre mestre) {
        this.mestre = mestre;
    }

    public Double getTempoAtualizar() {
        return null;
    }

    public void resultadoAtualizar(final Mensagem mensagem) {
        final int index = this.escravos.indexOf(mensagem.getOrigem());
        this.filaEscravo.set(index, mensagem.getFilaEscravo());
    }
}