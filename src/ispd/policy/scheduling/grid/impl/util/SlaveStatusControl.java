package ispd.policy.scheduling.grid.impl.util;

public class SlaveStatusControl {
    private enum SlaveStatus {
        FREE,
        OCCUPIED,
        BLOCKED,
        UNCERTAIN,
        PREEMPTED,
    };
    private SlaveStatus status = SlaveStatus.FREE;//Estado da máquina

    public boolean Ocupado() {
        return SlaveStatus.OCCUPIED.equals(this.status);
    }

    public boolean Livre() {
        return SlaveStatus.FREE.equals(this.status);
    }

    public boolean Bloqueado() {
        return SlaveStatus.BLOCKED.equals(this.status);
    }

    public boolean Incerto() {
        return SlaveStatus.UNCERTAIN.equals(this.status);
    }
    public boolean Preemp() {return SlaveStatus.PREEMPTED.equals(this.status);}

    public void setOcupado() {
        this.status = SlaveStatus.OCCUPIED;
    }

    public void setLivre() {
        this.status = SlaveStatus.FREE;
    }

    public void setBloqueado() {
        this.status = SlaveStatus.BLOCKED;
    }

    public void setIncerto() {
        this.status = SlaveStatus.UNCERTAIN;
    }

    public void setPreemp() {this.status = SlaveStatus.PREEMPTED;}
}
