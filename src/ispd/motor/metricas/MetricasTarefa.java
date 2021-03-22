/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.metricas;

/**
 *
 * @author denison_usuario
 */
public class MetricasTarefa {
    /**
     * Recebe tempo total que a tarefa permaneceu em um fila de um recurso de computação
     */
    private double tempoEsperaProc = 0;
    /**
     * Recebe tempo total que a tarefa permaneceu em um fila de um recurso de comunicação
     */
    private double tempoEsperaComu = 0;
    /**
     * Recebe tempo total que a tarefa gastou sendo computada no modelo
     */
    private double tempoProcessamento = 0;
    /**
     * Recebe tempo total que a tarefa gastou sendo transferida na rede modelada
     */
    private double tempoComunicacao = 0;
    
    private double eficiencia = 0;

    public void incTempoComunicacao(double tempoComunicacao) {
        this.tempoComunicacao += tempoComunicacao;
    }

    public void incTempoEsperaComu(double tempoEsperaComu) {
        this.tempoEsperaComu += tempoEsperaComu;
    }

    public void incTempoEsperaProc(double tempoEsperaProc) {
        this.tempoEsperaProc += tempoEsperaProc;
    }

    public void incTempoProcessamento(double tempoProcessamento) {
        this.tempoProcessamento += tempoProcessamento;
    }
    
    public void calcEficiencia(double capacidadeRecebida, double tamanhoTarefa){
        eficiencia = capacidadeRecebida / (tamanhoTarefa * tempoProcessamento);
    }

    public double getTempoComunicacao() {
        return tempoComunicacao;
    }

    public double getTempoEsperaComu() {
        return tempoEsperaComu;
    }

    public double getTempoEsperaProc() {
        return tempoEsperaProc;
    }

    public double getTempoProcessamento() {
        return tempoProcessamento;
    }

    public double getEficiencia() {
        return eficiencia;
    }
}
