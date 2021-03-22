/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author denison
 */
package ispd.escalonador;

import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasUsuarios;
import java.util.List;

/**
 * Classe abstrata ue implementa os escalonadores.
 * 
 * lista de atributos:
 * 
 *  protected List<CS_Processamento> escravos : Lista de escravos para quem o escalonador dele distribuir tarefas
 *  protected List<List> filaEscravo : Lista que contem informações sobre cada escravo, utilizado em políticas dinâmicas.
 *  protected List<Tarefa> tarefas : Lista de tarefas para serem distribuídas entre os escravos
 *  protected MetricasUsuarios metricaUsuarios : Objeto que calcula métricas sobre o escalonamento para os usuários
 *  protected Mestre mestre : 
 
 * @author Diogo Tavares
 */

public abstract class Escalonador {
    //Atributos
    
    
    protected List<CS_Processamento> escravos;
    protected List<List> filaEscravo;
    protected List<Tarefa> tarefas;
    protected MetricasUsuarios metricaUsuarios;
    protected Mestre mestre;
    /**
     * Armazena os caminhos possiveis para alcançar cada escravo
     */
    protected List<List> caminhoEscravo;

    //Métodos

    public abstract void iniciar();

    public abstract Tarefa escalonarTarefa();

    public abstract CS_Processamento escalonarRecurso();

    public abstract List<CentroServico> escalonarRota(CentroServico destino);

    public abstract void escalonar();

    public void adicionarTarefa(Tarefa tarefa){
        if(tarefa.getOrigem().equals(mestre)){
            this.metricaUsuarios.incTarefasSubmetidas(tarefa);
        }
        this.tarefas.add(tarefa);
    }

    //Get e Set

    public List<CS_Processamento> getEscravos() {
        return escravos;
    }

    public void setCaminhoEscravo(List<List> caminhoEscravo) {
        this.caminhoEscravo = caminhoEscravo;
    }

    public void addEscravo(CS_Processamento maquina) {
        this.escravos.add(maquina);
    }
    
    public void addTarefaConcluida(Tarefa tarefa) {
        if(tarefa.getOrigem().equals(mestre)){
            this.metricaUsuarios.incTarefasConcluidas(tarefa);
        }
    }
    
    public List<Tarefa> getFilaTarefas() {
        return this.tarefas;
    }

    public MetricasUsuarios getMetricaUsuarios() {
        return metricaUsuarios;
    }

    public void setMetricaUsuarios(MetricasUsuarios metricaUsuarios) {
        this.metricaUsuarios = metricaUsuarios;
    }

    public void setMestre(Mestre mestre) {
        this.mestre = mestre;
    }

    public List<List> getCaminhoEscravo() {
        return caminhoEscravo;
    }
    
    /**
     * Indica o intervalo de tempo utilizado pelo escalonador para realizar atualização dos dados dos escravos
     * Retornar null para escalonadores estáticos, nos dinâmicos o método deve ser reescrito
     * @return Intervalo em segundos para atualização
     */
    public Double getTempoAtualizar(){
        return null;
    }

    public void resultadoAtualizar(Mensagem mensagem) {
        int index = escravos.indexOf(mensagem.getOrigem());
        filaEscravo.set(index, mensagem.getFilaEscravo());
    }
}
