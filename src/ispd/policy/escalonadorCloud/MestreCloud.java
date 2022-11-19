package ispd.policy.escalonadorCloud;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.PolicyCondition;

import java.util.Set;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be used by the schedulers.
 */
public interface MestreCloud {
    void enviarTarefa(Tarefa tarefa);

    void processarTarefa(Tarefa tarefa);

    void executarEscalonamento();

    void enviarMensagem(Tarefa tarefa, CS_Processamento escravo, int tipo);

    void atualizar(CS_Processamento escravo);

    void liberarEscalonador();

    Set<PolicyCondition> getTipoEscalonamento();

    void setTipoEscalonamento(Set<PolicyCondition> tipo);

    Tarefa criarCopia(Tarefa get);

    Simulation getSimulacao();

    void setSimulacao(Simulation simulacao);
}
