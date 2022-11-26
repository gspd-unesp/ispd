package ispd.policy.scheduling.grid.impl.util;

public class M_OSEP_ControleEscravos extends SlaveStatusControl {

    private static final int OCCUPIED = 1;
    private static final int FREE = 0;
    private static final int BLOCKED = 2;
    private static final int PREEMPTIVE = 3;
    private int contador;

    public M_OSEP_ControleEscravos() {
        this.contador = FREE;
    }
}
