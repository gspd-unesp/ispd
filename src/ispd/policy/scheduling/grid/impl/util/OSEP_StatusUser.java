package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;

public class OSEP_StatusUser extends EHOSEP_StatusUser {
    private int ownerShare;//Número de máquinas do usuario

    public OSEP_StatusUser(final String user,
                           final double perfShare,
                           final List<CS_Processamento> slaves) {
        super(user, perfShare);

        this.ownerShare = 0;
        int i;
        int j = 0;
        for (i = 0; i < slaves.size(); i++) {
            if (slaves.get(i).getProprietario().equals(user)) {
                j++;
            }
        }
        this.ownerShare = j;
    }

    public String getUser() {
        return this.user;
    }

    public int getOwnerShare() {
        return this.ownerShare;
    }
}
