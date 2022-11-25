package ispd.policy.scheduling.grid.impl.util;

public class OSEP_ControleEscravos {

    private final String ID;//Id da máquina escravo
    private String status;//Estado da máquina

    public OSEP_ControleEscravos(final String ID) {
        this.status = "Livre";
        this.ID = ID;
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
