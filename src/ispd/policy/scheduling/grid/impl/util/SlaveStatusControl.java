package ispd.policy.scheduling.grid.impl.util;

public class SlaveStatusControl {
    protected final String ID;//Id da máquina escravo
    protected String status;//Estado da máquina

    public SlaveStatusControl(final String Ident) {
        this.ID = Ident;
        this.status = "Livre";
    }

    public String getID() {
        return this.ID;
    }

    public String getStatus() {
        return this.status;
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
