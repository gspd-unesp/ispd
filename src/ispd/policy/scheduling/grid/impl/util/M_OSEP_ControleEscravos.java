package ispd.policy.scheduling.grid.impl.util;

public class M_OSEP_ControleEscravos {

    private static final int OCCUPIED = 1;
    private static final int FREE = 0;
    private static final int BLOCKED = 2;
    private static final int PREEMPTIVE = 3;
    private int contador;

    public M_OSEP_ControleEscravos() {
        this.contador = FREE;
    }

    public boolean Ocupado() {
        return this.contador == OCCUPIED;
    }

    public boolean Livre() {
        return this.contador == FREE;
    }

    public boolean Bloqueado() {
        return this.contador == BLOCKED;
    }

    public boolean Preemp() {
        return this.contador == PREEMPTIVE;
    }

    public void SetOcupado() {
        this.contador = OCCUPIED;
    }

    public void SetLivre() {
        this.contador = FREE;
    }

    public void SetBloqueado() {
        this.contador = BLOCKED;
    }

    public void setPreemp() {
        this.contador = PREEMPTIVE;
    }
}
