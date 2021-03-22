/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor;

import ispd.motor.filas.Cliente;
import ispd.motor.filas.servidores.CentroServico;

/**
 * Classe que representa os eventos que alteram o estado do modelo simulado
 * @author denison_usuario
 */
public class EventoFuturo implements Comparable<EventoFuturo> {

    public static final int CHEGADA = 1;
    public static final int ATENDIMENTO = 2;
    public static final int SAÍDA = 3;
    public static final int ESCALONAR = 4;
    public static final int MENSAGEM = 5;
    public static final int SAIDA_MENSAGEM = 6;
    public static final int ALOCAR_VMS = 7;
    
    
    private Double tempoOcorrencia;
    private int tipoDeEvento;
    private CentroServico recurso;
    private Cliente cliente;

    /**
     * Criacao de novo evento
     * @param time tempo do relógio em que foi criada
     * @param tipoDeEvento tipo do evento criado
     * @param servidor servidor que executará o evento
     * @param cliente cliente do evento
     */
    public EventoFuturo(double time, int tipoDeEvento, CentroServico servidor, Cliente cliente) {
        this.tempoOcorrencia = time;
        this.recurso = servidor;
        this.tipoDeEvento = tipoDeEvento;
        this.cliente = cliente;
    }

    /**
     * Informa o tipo do evento
     * @return Retorna o tipo do evento de acordo com as constantes da classe
     */
    public int getTipo() {
        return this.tipoDeEvento;
    }

    /**
     * Retorna recurso que realiza a ação
     * @return recurso que deve executar ação
     */
    public CentroServico getServidor() {
        return this.recurso;
    }

    /**
     * Retorna tarefa alvo da ação
     * @return cliente do evento
     */
    public Cliente getCliente() {
        return this.cliente;
    }

    public Double getTempoOcorrencia() {
        return tempoOcorrencia;
    }

    /**
     * Comparação necessaria para utilizar PriorityQueue
     * @param o evento que será comparado
     * @return 0 se valores iguais, um menor que 0 se "o" inferior, e maior que 0 se "o" for maior.
     */
    public int compareTo(EventoFuturo o) {
        return tempoOcorrencia.compareTo(o.tempoOcorrencia);
    }
}
