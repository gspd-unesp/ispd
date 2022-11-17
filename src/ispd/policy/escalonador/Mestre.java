package ispd.policy.escalonador;

import ispd.motor.filas.Tarefa;
import ispd.policy.PolicyMaster;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be use by the schedulers.
 */
public interface Mestre extends PolicyMaster {
    void enviarTarefa(Tarefa tarefa);

    void processarTarefa(Tarefa tarefa);

    Tarefa criarCopia(Tarefa get);
}
