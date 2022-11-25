package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;

import java.util.ArrayList;
import java.util.List;

//Classe para arnazenar o estado das máquinas no sistema
public class SlaveControl extends SlaveStatusControl {

    protected final int index;//Índice na lista de escravos
    protected ArrayList<Tarefa> fila;
    protected ArrayList<Tarefa> processador;

    public SlaveControl(final String Ident, final int ind,
                        final ArrayList<Tarefa> F,
                        final ArrayList<Tarefa> P) {
        super(Ident);
        this.index = ind;
        this.fila = F;
        this.processador = P;
    }

    public int GetIndex() {
        return this.index;
    }

    public List<Tarefa> GetFila() {
        return this.fila;
    }

    public ArrayList<Tarefa> GetProcessador() {
        return this.processador;
    }

    public void setFila(final ArrayList<Tarefa> F) {
        this.fila = F;
    }

    public void setProcessador(final ArrayList<Tarefa> P) {
        this.processador = P;
    }
}
