package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.Collection;

public class EnergyUserControl extends UserControl {
    public EnergyUserControl(
            final String userId, final double ownedProcPower,
            final Collection<? extends CS_Processamento> systemMachines) {
        super(userId, ownedProcPower, systemMachines);
    }
}
