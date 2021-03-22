/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor;

import ispd.motor.filas.Mensagem;

/**
 *
 * @author denison
 */
public interface Mensagens {

    public static final int CANCELAR = 1;
    public static final int PARAR = 2;
    public static final int DEVOLVER = 3;
    public static final int DEVOLVER_COM_PREEMPCAO = 4;
    public static final int ATUALIZAR = 5;
    public static final int RESULTADO_ATUALIZAR = 6;
    public static final int FALHAR = 7;
    public static final int ALOCAR_ACK = 8;
    public static final int DESLIGAR = 9; 

    public void atenderCancelamento(Simulacao simulacao, Mensagem mensagem);
    public void atenderParada(Simulacao simulacao, Mensagem mensagem);
    public void atenderDevolucao(Simulacao simulacao, Mensagem mensagem);
    public void atenderDevolucaoPreemptiva(Simulacao simulacao, Mensagem mensagem);
    public void atenderAtualizacao(Simulacao simulacao, Mensagem mensagem);
    public void atenderRetornoAtualizacao(Simulacao simulacao, Mensagem mensagem);
    public void atenderFalha(Simulacao simulacao, Mensagem mensagem);
    public void atenderAckAlocacao(Simulacao simulacao, Mensagem mensagem);
    public void atenderDesligamento(Simulacao simulacao, Mensagem mensagem);
}