package ispd.policy.escalonadorCloud;

import ispd.motor.Simulation;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

/**
 * Interface que possui métodos implementados penas em um nó Mestre,
 * os métodos desta interface são utilizados pelos escalonadores
 */
public interface MestreCloud {
    int ENQUANTO_HOUVER_TAREFAS = 1;
    int QUANDO_RECEBE_RESULTADO = 2;
    int AMBOS = 3;

    void enviarTarefa(Tarefa tarefa);

    void executarEscalonamento();

    void enviarMensagem(Tarefa tarefa, CS_Processamento escravo, int tipo);

    void atualizar(CS_Processamento escravo);

    //Get e Set
    void liberarEscalonador();

    // TODO: Again, Interpretador
    @SuppressWarnings("unused")
    void setTipoEscalonamento(int tipo);

    Simulation getSimulacao();

    void setSimulacao(Simulation simulacao);
}