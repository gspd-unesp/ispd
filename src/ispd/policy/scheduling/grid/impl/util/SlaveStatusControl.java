package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;

import java.util.ArrayList;
import java.util.List;

public class SlaveStatusControl {
    private SlaveStatus status = SlaveStatus.FREE;
    private List<Tarefa> tasksOnHold = new ArrayList<>();
    private ArrayList<Tarefa> tasksInProcessing = new ArrayList<>();

    public boolean isOccupied() {
        return this.status == SlaveStatus.OCCUPIED;
    }

    public boolean isFree() {
        return this.status == SlaveStatus.FREE;
    }

    public boolean isBlocked() {
        return this.status == SlaveStatus.BLOCKED;
    }

    public boolean isUncertain() {
        return this.status == SlaveStatus.UNCERTAIN;
    }

    public boolean isPreempted() {
        return this.status == SlaveStatus.PREEMPTED;
    }

    public void setAsOccupied() {
        this.status = SlaveStatus.OCCUPIED;
    }

    public void setAsFree() {
        this.status = SlaveStatus.FREE;
    }

    public void setAsBlocked() {
        this.status = SlaveStatus.BLOCKED;
    }

    public void setAsUncertain() {
        this.status = SlaveStatus.UNCERTAIN;
    }

    public void setAsPreempted() {
        this.status = SlaveStatus.PREEMPTED;
    }

    public List<Tarefa> getTasksOnHold() {
        return this.tasksOnHold;
    }

    public void setTasksOnHold(final List<Tarefa> tasksOnHold) {
        this.tasksOnHold = tasksOnHold;
    }

    public ArrayList<Tarefa> getTasksInProcessing() {
        return this.tasksInProcessing;
    }

    public void setTasksInProcessing(final ArrayList<Tarefa> tasksInProcessing) {
        this.tasksInProcessing = tasksInProcessing;
    }

    private enum SlaveStatus {
        FREE,
        OCCUPIED,
        BLOCKED,
        UNCERTAIN,
        PREEMPTED,
    }
}
