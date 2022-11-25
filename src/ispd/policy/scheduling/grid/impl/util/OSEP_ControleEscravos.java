package ispd.policy.scheduling.grid.impl.util;

public class OSEP_ControleEscravos extends SlaveStatusControl {

    public OSEP_ControleEscravos(final String ID) {
        super(ID);
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
