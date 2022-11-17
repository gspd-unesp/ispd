package ispd.policy.alocacaoVM;

import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

import java.io.Serializable;
import java.util.Comparator;

public class ComparaRequisitos implements
        Comparator<CS_VirtualMac>, Serializable {
    private static final int MULTIPLIER = 100000;

    @Override
    public int compare(final CS_VirtualMac t1, final CS_VirtualMac t2) {
        return (ComparaRequisitos.calculateValue(t1) - ComparaRequisitos.calculateValue(t2));
    }

    private static int calculateValue(final CS_VirtualMac aux1) {
        return aux1.getProcessadoresDisponiveis() * ComparaRequisitos.MULTIPLIER
               + (int) aux1.getMemoriaDisponivel() * ComparaRequisitos.MULTIPLIER
               + (int) aux1.getDiscoDisponivel() * ComparaRequisitos.MULTIPLIER;
    }
}
