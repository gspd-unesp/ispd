package ispd.policy.escalonador;

import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasUsuarios;

import java.util.List;

/**
 * Classe abstrata ue implementa os escalonadores.
 * <p>
 * lista de atributos:
 * <p>
 * protected {@code List<CS_Processamento>} escravos : Lista de escravos para
 * quem o
 * escalonador dele distribuir tarefas
 * protected {@code List<List>} filaEscravo : Lista que contem informações
 * sobre cada
 * escravo, utilizado em políticas dinâmicas.
 * protected {@code List<Tarefa>} tarefas : Lista de tarefas para serem
 * distribuídas
 * entre os escravos
 * protected MetricasUsuarios metricaUsuarios : Objeto que calcula métricas
 * sobre o escalonamento para os usuários
 * protected Mestre mestre :
 */
public abstract class Escalonador {
    protected List<CS_Processamento> escravos;
    protected List<List> filaEscravo;
    protected List<Tarefa> tarefas;
    protected MetricasUsuarios metricaUsuarios;
    protected Mestre mestre;
    /**
     * Armazena os caminhos possiveis para alcançar cada escravo
     */
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

    /**
     * Indica o intervalo de tempo utilizado pelo escalonador para realizar
     * atualização dos dados dos escravos
     * Retornar null para escalonadores estáticos, nos dinâmicos o method
     * deve ser reescrito
     *
     * @return Intervalo em segundos para atualização
     */
    public Double getTempoAtualizar() {
        return null;
    }

    public void resultadoAtualizar(final Mensagem mensagem) {
        final int index = this.escravos.indexOf(mensagem.getOrigem());
        this.filaEscravo.set(index, mensagem.getFilaEscravo());
    }
}