package ispd.policy.scheduling.grid.impl.util;

public class SlaveStatusControl {
    private static final String OCCUPIED = "Ocupado";
    private static final String FREE = "Livre";
    private static final String BLOCKED = "Bloqueado";
    private static final String UNCERTAIN = "Incerto";
    protected final String ID;//Id da máquina escravo
    protected String status;//Estado da máquina

    public SlaveStatusControl(final String Ident) {
        this.ID = Ident;
        this.status = FREE;
    }

    public String getID() {
        return this.ID;
    }

    public String getStatus() {
        return this.status;
    }

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
}
