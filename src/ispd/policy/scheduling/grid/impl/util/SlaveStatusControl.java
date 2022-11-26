package ispd.policy.scheduling.grid.impl.util;

public class SlaveStatusControl {
    private static final SlaveStatus PREEMPTED = SlaveStatus.PREEMPTED;
    private enum SlaveStatus {
        FREE,
        OCCUPIED,
        BLOCKED,
        UNCERTAIN,
        PREEMPTED,
    };
    private static final SlaveStatus OCCUPIED = SlaveStatus.OCCUPIED;
    private static final SlaveStatus FREE = SlaveStatus.FREE;
    private static final SlaveStatus BLOCKED = SlaveStatus.BLOCKED;
    private static final SlaveStatus UNCERTAIN = SlaveStatus.UNCERTAIN;
    private SlaveStatus status = FREE;//Estado da máquina

    public boolean Ocupado() {
        return OCCUPIED.equals(this.status);
    }

    public boolean Livre() {
        return FREE.equals(this.status);
    }

    public boolean Bloqueado() {
        return BLOCKED.equals(this.status);
    }

    public boolean Incerto() {
        return UNCERTAIN.equals(this.status);
    }
    public boolean Preemp() {return PREEMPTED.equals(this.status);}

    public void setOcupado() {
        this.status = OCCUPIED;
    }

    public void setLivre() {
        this.status = FREE;
    }

    public void setBloqueado() {
        this.status = BLOCKED;
    }

    public void setIncerto() {
        this.status = UNCERTAIN;
    }

    public void setPreemp() {this.status = PREEMPTED;}
}
