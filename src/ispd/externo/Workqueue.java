/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo;

import ispd.escalonador.Escalonador;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementação do algoritmo de escalonamento Workqueue
 * Atribui a proxima tarefa da fila (FIFO)
 * para um recurso que está livre
 * @author denison_usuario
 */
public class Workqueue extends Escalonador {

    private LinkedList<Tarefa> ultimaTarefaConcluida;
    private List<Tarefa> tarefaEnviada;

    public Workqueue() {
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
        this.ultimaTarefaConcluida = new LinkedList<Tarefa>();
    }

    @Override
    public void iniciar() {
        tarefaEnviada = new ArrayList<Tarefa>(escravos.size());
        for (int i = 0; i < escravos.size(); i++) {
            tarefaEnviada.add(null);
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        if (tarefas.size() > 0) {
            return tarefas.remove(0);
        }
        return null;
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (!ultimaTarefaConcluida.isEmpty() && !ultimaTarefaConcluida.getLast().isCopy()) {
            int index = tarefaEnviada.indexOf(ultimaTarefaConcluida.getLast());
            return this.escravos.get(index);
        } else {
            for (int i = 0; i < tarefaEnviada.size(); i++) {
                if (tarefaEnviada.get(i) == null) {
                    return this.escravos.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        CS_Processamento rec = escalonarRecurso();
        if (rec != null) {
            Tarefa trf = escalonarTarefa();
            if (trf != null) {
                tarefaEnviada.set(escravos.indexOf(rec), trf);
                if(!ultimaTarefaConcluida.isEmpty()){
                    ultimaTarefaConcluida.removeLast();
                }
                trf.setLocalProcessamento(rec);
                trf.setCaminho(escalonarRota(rec));
                mestre.enviarTarefa(trf);
            }
        }
    }
    
    @Override
    public void adicionarTarefa(Tarefa tarefa){
        super.adicionarTarefa(tarefa);
        if(tarefaEnviada.contains(tarefa)){
            int index = tarefaEnviada.indexOf(tarefa);
            tarefaEnviada.set(index, null);
            mestre.executarEscalonamento();
        }
    }
    
    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        ultimaTarefaConcluida.add(tarefa);
        if (!tarefas.isEmpty()) {
            mestre.executarEscalonamento();
        }
    }
}
