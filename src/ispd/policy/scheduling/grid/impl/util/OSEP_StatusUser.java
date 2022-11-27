package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.Collection;

public class OSEP_StatusUser extends UserStatus {
    private final long userMachineCount;

    public OSEP_StatusUser(
            final String user, final double perfShare,
            final Collection<? extends CS_Processamento> slaves) {
        super(user, perfShare);

        this.userMachineCount = slaves.stream()
                .filter(s -> s.getProprietario().equals(user))
                .count();
    }

    public long getOwnerShare() {
        return this.userMachineCount;
    }
}
