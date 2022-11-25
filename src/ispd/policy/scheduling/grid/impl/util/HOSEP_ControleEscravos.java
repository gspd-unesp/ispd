package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;

import java.util.ArrayList;
import java.util.List;

//Classe para arnazenar o estado das máquinas no sistema
public class HOSEP_ControleEscravos extends SlaveControl {

    public HOSEP_ControleEscravos(final String Ident, final int ind,
                                  final ArrayList<Tarefa> F,
                                  final ArrayList<Tarefa> P) {
        super(Ident, ind, F, P);
    }

    public String getID() {
        return this.ID;
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

    public String getStatus() {
        return this.status;
    }

    public void setFila(final ArrayList<Tarefa> F) {
        this.fila = F;
    }

    public void setProcessador(final ArrayList<Tarefa> P) {
        this.processador = P;
    }

    public void setOcupado() {
        this.status = "Ocupado";
    }

    public void setLivre() {
        this.status = "Livre";
    }

    public void setBloqueado() {
        this.status = "Bloqueado";
    }

    public void setIncerto() {
        this.status = "Incerto";
    }
}
