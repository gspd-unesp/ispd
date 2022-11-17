package ispd.policy.alocacaoVM;

import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;

public class ComparaVolume extends MachineComparator<CS_MaquinaCloud> {
    protected int calculateMachineValue(final CS_MaquinaCloud m) {
        return m.getProcessadoresDisponiveis() * 100
               * (int) m.getMemoriaDisponivel() * 100
               * (int) m.getDiscoDisponivel() * 100;
    }
}
