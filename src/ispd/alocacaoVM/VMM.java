/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.alocacaoVM;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

/**
 * Interface que possui métodos implementados penas em um nó Mestre,
 * os métodos desta interface são utilizados pelos escalonadores
 * @author denison
 */
public interface VMM {
    //Tipos de escalonamentos
    public static final int ENQUANTO_HOUVER_VMS = 1;
    public static final int QUANDO_RECEBE_RETORNO = 2;
    public static final int DOISCASOS = 3;
    //Métodos que geram eventos
    public void enviarVM(CS_VirtualMac vm);
    
    public void executarAlocacao();
    public void enviarMensagemAlloc(Tarefa tarefa, CS_Processamento maquina, int tipo); //tarefa com VM encapsulada
    public void atualizarAlloc(CS_Processamento maquina);    
    //Get e Set
    public void setSimulacaoAlloc(Simulation simulacao);
    public int getTipoAlocacao();
    public void setTipoAlocacao(int tipo);

    public Simulation getSimulacaoAlloc();
}