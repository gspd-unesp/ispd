package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

@Policy
public class HOSEP extends AbstractHOSEP {
    public HOSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    protected Optional<CS_Processamento> findOccupiedMachineBestSuitedFor(final UserControl taskOwner) {
        if (taskOwner.hasExcessProcessingPower() ||
            !this.bestUser().hasExcessProcessingPower()) {
            return Optional.empty();
        }

        return this.findMachineToPreemptFor(taskOwner);
    }

    private Optional<CS_Processamento> findMachineToPreemptFor(final UserControl taskOwner) {
        return this.findUserToPreemptFor().flatMap(
                userToPreempt -> this.findMachineToTransferBetween(userToPreempt, taskOwner));
    }

    private Optional<UserControl> findUserToPreemptFor() {
        return Optional.of(this.bestUser());
    }

    private Optional<CS_Processamento> findMachineToTransferBetween(
            final UserControl userToPreempt, final UserControl taskOwner) {
        return this.machinesOccupiedBy(userToPreempt)
                .min(this.compareAvailableMachinesFor(null))
                .filter(machine -> this.shouldTransferMachine(
                        machine, userToPreempt, taskOwner));
    }

    private boolean shouldTransferMachine(
            final CS_Processamento m,
            final UserControl machineOwner, final UserControl nextOwner) {
        if (machineOwner.canConcedeProcessingPower(m)) {
            return true;
        }

        final double machineOwnerPenalty =
                machineOwner.penaltyWithProcessing(-m.getPoderComputacional());
        final double nextOwnerPenalty =
                nextOwner.penaltyWithProcessing(m.getPoderComputacional());

        return machineOwnerPenalty >= nextOwnerPenalty;
    }

    private Stream<CS_Processamento> machinesOccupiedBy(final UserControl userToPreempt) {
        return this.escravos.stream()
                .filter(this::isMachineOccupied)
                .filter(machine -> userToPreempt.isOwnerOf(this.taskToPreemptIn(machine)));
    }

    private UserControl bestUser() {
        return this.userControls.values().stream()
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }
}
