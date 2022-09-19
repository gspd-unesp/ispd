package ispd.motor.queueNetworks;

import java.util.PriorityQueue;

public class Filas {
    private int idFila;
    private int numTarefas;
    private PriorityQueue<NoFila> filaTarefas;


    public Filas(int id) {
        filaTarefas = new PriorityQueue<NoFila>();
        setIdFila(id);
        numTarefas = 0;
    }

    public int getIdFila() {
        return idFila;
    }

    public void setIdFila(int id) {
        idFila = (id >= 0) ? id : 0;
    }

    public int size() {
        return numTarefas;
    }

    public Double adicionaTarefaFila(NoFila tarefa) {
        Double retornoFuncao = 0.0;  //Eh o tempo que o sistema levou para
		// adicionar uma tarefa na fila.
        //Como considera-se que isso eh feito instantaneamente, o retorno e 0.0
        filaTarefas.offer(tarefa);
        numTarefas = numTarefas + 1;
        System.out.printf("| Tarefa Id: %2d Adicionada na Fila ID: %2d do CS " +
						  "%2d\n", tarefa.getIdTarefa(), tarefa.getIdFila(),
				tarefa.getIdCSAtual());
        System.out.printf("size = %d\n", numTarefas);
        return retornoFuncao;
    }

    public NoFila peek() {
        NoFila novaTarefa = null;

        if (numTarefas == 0)
            System.out.printf("ERRO no peek da classe Filas.java\n");
        else
            novaTarefa = filaTarefas.peek();
        return novaTarefa;
    }

    public void poll() {
        filaTarefas.poll();
        numTarefas = numTarefas - 1;
        System.out.printf("numTarefas %d\n", numTarefas);
    }

} // fim de public class Fila