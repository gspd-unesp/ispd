package ispd.policy.escalonador;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

/**
 * Interface que possui métodos implementados penas em um nó Mestre,
 * os métodos desta interface são utilizados pelos escalonadores
 */
public interface Mestre {
    int ENQUANTO_HOUVER_TAREFAS = 1;
    int QUANDO_RECEBE_RESULTADO = 2;
    int AMBOS = 3;

    void enviarTarefa(Tarefa tarefa);

    void executarEscalonamento();

    void enviarMensagem(Tarefa tarefa, CS_Processamento escravo, int tipo);

    void atualizar(CS_Processamento escravo);

    // TODO: Figure out how to deal with Interpretador's shenanigans
    @SuppressWarnings("unused")
    void setTipoEscalonamento(int tipo);

    Simulation getSimulacao();

    //Get e Set
    void setSimulacao(Simulation simulacao);
}