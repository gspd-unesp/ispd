package ispd.policy.allocation.vm.impl.util.util;

import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

public class ComparaRequisitos extends MachineComparator<CS_VirtualMac> {
    private static final int MULTIPLIER = 100_000;

    protected int calculateMachineValue(final CS_VirtualMac m) {
        return ComparaRequisitos.MULTIPLIER * (
                m.getProcessadoresDisponiveis()
                + (int) m.getMemoriaDisponivel()
                + (int) m.getDiscoDisponivel()
        );
    }
}
