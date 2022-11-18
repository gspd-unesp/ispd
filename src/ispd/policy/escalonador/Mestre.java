package ispd.policy.escalonador;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be use by the schedulers.
 */
public interface Mestre {
    int ENQUANTO_HOUVER_TAREFAS = 1;
    int QUANDO_RECEBE_RESULTADO = 2;
    int AMBOS = 3;

    void enviarTarefa(Tarefa tarefa);

    void processarTarefa(Tarefa tarefa);

    void executePolicy();

    void sendMessage(Tarefa task, CS_Processamento machine, int messageType);

    void updateSubordinate(CS_Processamento subordinate);

    int getPolicyCondition();

    void setPolicyCondition(int newPolicyCondition);

    Tarefa criarCopia(Tarefa get);

    Simulation getSimulation();

    void setSimulation(Simulation newSimulation);
}
