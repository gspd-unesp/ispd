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
import java.util.ListIterator;

/**
 * Implementação do algoritmo de escalonamento Round-Robin
 * Atribui a proxima tarefa da fila (FIFO)
 * para o proximo recurso de uma fila circular de recursos
 * @author denison_usuario
 */
public class RoundRobin extends Escalonador{
    private ListIterator<CS_Processamento> recursos;
    
    public RoundRobin(){
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new LinkedList<CS_Processamento>();
    }

    @Override
    public void iniciar() {
        recursos = escravos.listIterator(0);
    }

    @Override
    public Tarefa escalonarTarefa() {
        return tarefas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (recursos.hasNext()) {
            return recursos.next();
        }else{
            recursos = escravos.listIterator(0);
            return recursos.next();
        }
    }

    @Override
    public void escalonar() {
        Tarefa trf = escalonarTarefa();
        CS_Processamento rec = escalonarRecurso();
        trf.setLocalProcessamento(rec);
        trf.setCaminho(escalonarRota(rec));
        mestre.enviarTarefa(trf);
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
    }
}
