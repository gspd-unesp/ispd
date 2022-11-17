package ispd.policy.alocacaoVM;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be used by the schedulers.
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