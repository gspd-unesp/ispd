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

    void executarEscalonamento();

    void enviarMensagem(Tarefa tarefa, CS_Processamento escravo, int tipo);

    void atualizar(CS_Processamento escravo);

    int getTipoEscalonamento();

    void setTipoEscalonamento(int tipo);

    Tarefa criarCopia(Tarefa get);

    Simulation getSimulacao();

    void setSimulacao(Simulation simulacao);
}
