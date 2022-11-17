package ispd.policy.alocacaoVM;

import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;

import java.io.Serializable;
import java.util.Comparator;

public class ComparaVolume implements
        Comparator<CS_MaquinaCloud>, Serializable {
    @Override
    public int compare(final CS_MaquinaCloud t1, final CS_MaquinaCloud t2) {
        return (ComparaVolume.getValue(t1) - ComparaVolume.getValue(t2));
    }

    private static int getValue(final CS_MaquinaCloud m) {
        return m.getProcessadoresDisponiveis() * 100
               * (int) m.getMemoriaDisponivel() * 100
               * (int) m.getDiscoDisponivel() * 100;
    }
}
