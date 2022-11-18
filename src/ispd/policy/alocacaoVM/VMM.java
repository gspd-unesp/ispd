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
    int ENQUANTO_HOUVER_VMS = 1;
    int QUANDO_RECEBE_RETORNO = 2;
    int DOISCASOS = 3;

    void enviarVM(CS_VirtualMac vm);

    void executePolicy();

    void sendMessage(Tarefa task, CS_Processamento machine, int messageType);

    void updateSubordinate(CS_Processamento subordinate);

    int getPolicyCondition();

    void setPolicyCondition(int newPolicyCondition);

    Simulation getSimulation();

    void setSimulation(Simulation newSimulation);
}