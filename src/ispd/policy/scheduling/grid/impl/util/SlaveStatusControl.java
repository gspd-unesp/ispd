package ispd.policy.scheduling.grid.impl.util;

public class SlaveStatusControl {
    private SlaveStatus status = SlaveStatus.FREE;

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

    private enum SlaveStatus {
        FREE,
        OCCUPIED,
        BLOCKED,
        UNCERTAIN,
        PREEMPTED,
    }
}
