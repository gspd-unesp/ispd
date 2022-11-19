package ispd.policy.escalonadorCloud;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.PolicyCondition;
import ispd.policy.scheduling.SchedulingMaster;

import java.util.Set;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be used by the schedulers.
 */
public interface MestreCloud extends SchedulingMaster {
    void sendTask(Tarefa tarefa);

    void processTask(Tarefa tarefa);

    void executeScheduling();

    void sendMessage(Tarefa tarefa, CS_Processamento escravo, int tipo);

    void updateSubordinate(CS_Processamento escravo);

    void freeScheduler();

    Set<PolicyCondition> getSchedulingConditions();

    void setSchedulingConditions(Set<PolicyCondition> tipo);

    Tarefa cloneTask(Tarefa get);

    Simulation getSimulation();

    void setSimulation(Simulation simulacao);
}
