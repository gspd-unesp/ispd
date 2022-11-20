package ispd.policy.scheduling.cloud;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.scheduling.SchedulingPolicy;

import java.util.LinkedList;
import java.util.List;

public abstract class CloudSchedulingPolicy extends SchedulingPolicy<CloudMaster> {
    protected List<CS_Processamento> getVMsAdequadas(
            final String usuario,
            final List<? extends CS_Processamento> slaves) {
        final var escravosUsuario = new LinkedList<CS_Processamento>();
        for (final var slave : slaves) {
            final var slaveVM = (CS_VirtualMac) slave;

            if (slave.getProprietario().equals(usuario) && slaveVM.getStatus() == CS_VirtualMac.ALOCADA) {
                escravosUsuario.add(slave);
            }
        }
        return escravosUsuario;
    }
}
