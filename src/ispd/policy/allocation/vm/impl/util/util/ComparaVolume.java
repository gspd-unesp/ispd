package ispd.policy.allocation.vm.impl.util.util;

import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;

public class ComparaVolume extends MachineComparator<CS_MaquinaCloud> {
    private static final int VOLUME = 100 * 100 * 100;

    protected int calculateMachineValue(final CS_MaquinaCloud m) {
        return ComparaVolume.VOLUME * (
                m.getProcessadoresDisponiveis()
                * (int) m.getMemoriaDisponivel()
                * (int) m.getDiscoDisponivel()
        );
    }
}
