package ispd.policy.alocacaoVM;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.PolicyCondition;
import ispd.policy.allocation.AllocationMaster;

import java.util.Set;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be used by the schedulers.
 */
public interface VMM extends AllocationMaster {
    void enviarVM(CS_VirtualMac vm);

    void executarAlocacao();

    void enviarMensagemAlloc(Tarefa tarefa, CS_Processamento maquina, int tipo);

    void atualizarAlloc(CS_Processamento maquina);

    Set<PolicyCondition> getTipoAlocacao();

    void setTipoAlocacao(Set<PolicyCondition> tipo);

    Simulation getSimulacaoAlloc();

    void setSimulacaoAlloc(Simulation simulacao);
}