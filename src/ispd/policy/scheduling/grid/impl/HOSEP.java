package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.Optional;

@Policy
public class HOSEP extends AbstractHOSEP {
    @Override
    protected Optional<UserControl> findUserToPreemptFor(final UserControl taskOwner) {
        return Optional.of(this.theBestUser());
    }

    @Override
    protected boolean shouldTransferMachine(
            final CS_Processamento machine,
            final UserControl machineOwner, final UserControl nextOwner) {
        if (super.shouldTransferMachine(machine, machineOwner, nextOwner)) {
            return true;
        }

        final double machineOwnerPenalty =
                machineOwner.penaltyWithProcessing(-machine.getPoderComputacional());
        final double nextOwnerPenalty =
                nextOwner.penaltyWithProcessing(machine.getPoderComputacional());

        return machineOwnerPenalty >= nextOwnerPenalty;
    }
}
