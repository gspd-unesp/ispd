package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;

public class OSEP_StatusUser extends UserStatus {
    private long ownerShare;//Número de máquinas do usuario

    public OSEP_StatusUser(final String user,
                           final double perfShare,
                           final List<CS_Processamento> slaves) {
        super(user, perfShare);

        this.ownerShare = slaves.stream()
                .filter(s -> s.getProprietario().equals(user))
                .count();
    }

    public long getOwnerShare() {
        return this.ownerShare;
    }
}
