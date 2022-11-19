package ispd.policy.alocacaoVM;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.PolicyCondition;

import java.util.EnumSet;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be used by the schedulers.
 */
public interface VMM {
    void enviarVM(CS_VirtualMac vm);

    void executarAlocacao();

    void enviarMensagemAlloc(Tarefa tarefa, CS_Processamento maquina, int tipo);

    void atualizarAlloc(CS_Processamento maquina);

    EnumSet<PolicyCondition> getTipoAlocacao();

    void setTipoAlocacao(EnumSet<PolicyCondition> tipo);

    Simulation getSimulacaoAlloc();

    void setSimulacaoAlloc(Simulation simulacao);
}