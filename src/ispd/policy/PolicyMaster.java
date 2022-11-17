package ispd.policy;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.EnumSet;

public interface PolicyMaster {
    void executePolicy();

    void sendMessage(Tarefa task, CS_Processamento machine, int messageType);

    void updateSubordinate(CS_Processamento subordinate);

    EnumSet<PolicyCondition> getPolicyCondition();

    void setPolicyCondition(EnumSet<PolicyCondition> newPolicyCondition);

    Simulation getSimulation();

    void setSimulation(Simulation newSimulation);
}
