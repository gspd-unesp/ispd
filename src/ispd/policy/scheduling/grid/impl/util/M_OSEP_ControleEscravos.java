package ispd.policy.scheduling.grid.impl.util;

public class M_OSEP_ControleEscravos {

    private int contador;

    public M_OSEP_ControleEscravos() {
        this.contador = 0;
    }

    public boolean Ocupado() {
        return this.contador == 1;
    }

    public boolean Livre() {
        return this.contador == 0;
    }

    public boolean Bloqueado() {
        return this.contador == 2;
    }

    public boolean Preemp() {
        return this.contador == 3;
    }

    public void SetOcupado() {
        this.contador = 1;
    }

    public void SetLivre() {
        this.contador = 0;
    }

    public void SetBloqueado() {
        this.contador = 2;
    }

    public void setPreemp() {
        this.contador = 3;
    }
}
