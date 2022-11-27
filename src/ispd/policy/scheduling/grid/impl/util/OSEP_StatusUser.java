package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.Collection;

public class OSEP_StatusUser extends UserStatus {

    public OSEP_StatusUser(
            final String user, final double perfShare,
            final Collection<? extends CS_Processamento> slaves) {
        super(user, perfShare, slaves);
    }
}
