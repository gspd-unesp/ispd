package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;

import java.util.ArrayList;
import java.util.List;

public class SlaveControl {
    private SlaveStatus status = SlaveStatus.FREE;
    private List<Tarefa> tasksInProcessing = new ArrayList<>();

    public void updateStatusIfNeeded() {
        this.status = switch (this.status) {
            case BLOCKED -> SlaveStatus.UNCERTAIN;
            case UNCERTAIN -> this.statusForProcessingQueue();
            default -> this.status;
        };
    }

    private SlaveStatus statusForProcessingQueue() {
        return this.tasksInProcessing.isEmpty()
                ? SlaveStatus.FREE
                : SlaveStatus.OCCUPIED;
    }

    public Tarefa firstTaskInProcessing() {
        return this.tasksInProcessing.get(0);
    }

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

    public List<Tarefa> getTasksInProcessing() {
        return this.tasksInProcessing;
    }

    public void setTasksInProcessing(final List<Tarefa> tasksInProcessing) {
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
