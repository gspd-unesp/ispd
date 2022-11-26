package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;

import java.util.ArrayList;
import java.util.List;

public class SlaveControl extends SlaveStatusControl {
    private List<Tarefa> tasksOnHold = new ArrayList<>();
    private ArrayList<Tarefa> tasksInProcessing = new ArrayList<>();

    public List<Tarefa> GetFila() {
        return this.tasksOnHold;
    }

    public ArrayList<Tarefa> GetProcessador() {
        return this.tasksInProcessing;
    }

    public void setFila(final List<Tarefa> tasksOnHold) {
        this.tasksOnHold = tasksOnHold;
    }

    public void setProcessador(final ArrayList<Tarefa> tasksInProcessing) {
        this.tasksInProcessing = tasksInProcessing;
    }
}
