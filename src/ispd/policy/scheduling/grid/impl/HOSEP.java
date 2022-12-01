package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.Collection;
import java.util.Optional;

@Policy
public class HOSEP extends AbstractHOSEP<UserControl> {
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

    @Override
    protected UserControl makeUserControlFor(
            final String userId,
            final Collection<? extends CS_Processamento> userOwnedMachines) {
        final double compPower = userOwnedMachines.stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .sum();

        return new UserControl(userId, compPower, this.escravos);
    }
}
