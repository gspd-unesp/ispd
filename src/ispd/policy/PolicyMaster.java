package ispd.policy;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

public interface PolicyMaster {
    int ENQUANTO_HOUVER_VMS = 1;
    int QUANDO_RECEBE_RETORNO = 2;
    int DOISCASOS = 3;

    void executePolicy();

    void sendMessage(Tarefa task, CS_Processamento machine, int messageType);

    void updateSubordinate(CS_Processamento subordinate);

    int getPolicyCondition();

    void setPolicyCondition(int newPolicyCondition);

    Simulation getSimulation();

    void setSimulation(Simulation newSimulation);
}
