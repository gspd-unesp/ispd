package ispd.policy.scheduling.cloud;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.scheduling.SchedulingPolicy;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CloudSchedulingPolicy extends SchedulingPolicy<CloudMaster> {
    protected List<CS_Processamento> getVMsAdequadas(final String user) {
        return this.escravos.stream()
                .filter(s -> s.getProprietario().equals(user))
                .map(CS_VirtualMac.class::cast)
                .filter(s -> s.getStatus() == CS_VirtualMac.ALOCADA)
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
