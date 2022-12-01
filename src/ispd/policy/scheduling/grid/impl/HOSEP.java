package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.ArrayList;
import java.util.Optional;

@Policy
public class HOSEP extends AbstractHOSEP {
    public HOSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    protected boolean shouldTransferMachine(
            final CS_Processamento machine,
            final UserControl machineOwner, final UserControl nextOwner) {
        if (machineOwner.canConcedeProcessingPower(machine)) {
            return true;
        }

        final double machineOwnerPenalty =
                machineOwner.penaltyWithProcessing(-machine.getPoderComputacional());
        final double nextOwnerPenalty =
                nextOwner.penaltyWithProcessing(machine.getPoderComputacional());

        return machineOwnerPenalty >= nextOwnerPenalty;
    }

    @Override
    protected Optional<UserControl> findUserToPreemptFor(final UserControl taskOwner) {
        return Optional.of(this.theBestUser());
    }

}
