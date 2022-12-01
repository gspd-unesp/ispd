package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.scheduling.grid.impl.util.EnergyUserControl;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

@Policy
public class EHOSEP extends AbstractHOSEP<EnergyUserControl> {
    @Override
    protected EnergyUserControl makeUserControlFor(final String userId) {
        return new EnergyUserControl(
                userId, this.escravos,
                this.metricaUsuarios.getLimites().get(userId)
        );
    }

    @Override
    protected Comparator<EnergyUserControl> getUserComparator() {
        return super.getUserComparator()
                .thenComparing(EnergyUserControl::getOwnedMachinesEnergyConsumption);
    }

    @Override
    protected Comparator<CS_Processamento> compareAvailableMachinesFor(final Tarefa task) {
        // Extracted as a variable to aid type inference
        final ToDoubleFunction<CS_Processamento> energyConsumption =
                m -> EHOSEP.calculateEnergyConsumptionForTask(m, task);

        return Comparator
                .comparingDouble(energyConsumption)
                .reversed()
                .thenComparing(super.compareAvailableMachinesFor(task));
    }

    private static double calculateEnergyConsumptionForTask(
            final CS_Processamento machine, final Tarefa task) {
        return task.getTamProcessamento()
               / machine.getPoderComputacional()
               * machine.getConsumoEnergia();
    }

    @Override
    protected Stream<CS_Processamento> availableMachinesFor(final EnergyUserControl taskOwner) {
        return super.availableMachinesFor(taskOwner)
                .filter(taskOwner::canUseMachineWithoutExceedingEnergyLimit);
    }

    @Override
    protected Optional<CS_Processamento> findMachineToPreemptFor(final EnergyUserControl taskOwner) {
        return this.findUserToPreemptFor(taskOwner).flatMap(
                userToPreempt -> this.findMachineToTransferBetween(userToPreempt, taskOwner));
    }

    @Override
    protected Optional<EnergyUserControl> findUserToPreemptFor(final EnergyUserControl taskOwner) {
        return this.userControls.values().stream()
                .filter(EnergyUserControl::hasExcessProcessingPower)
                .max(EHOSEP.compareConsumptionWeightedByEfficiency())
                .filter(taskOwner::hasLessEnergyConsumptionThan);
    }

    @Override
    protected Stream<CS_Processamento> machinesTransferableBetween(final EnergyUserControl userToPreempt, final EnergyUserControl taskOwner) {
        return super.machinesTransferableBetween(userToPreempt, taskOwner)
                .filter(taskOwner::canUseMachineWithoutExceedingEnergyLimit);
    }

    @Override
    protected Comparator<CS_Processamento> compareOccupiedMachines() {
        return Comparator
                .comparingDouble(this::wastedProcessingIfPreempted)
                .thenComparing(super.compareOccupiedMachines());
    }

    private double wastedProcessingIfPreempted(final CS_Processamento machine) {
        final var preemptedTask = this.taskToPreemptIn(machine);
        final var startTimeList = preemptedTask.getTempoInicial();
        final var taskStartTime = startTimeList.get(startTimeList.size() - 1);
        final var currTime = this.mestre.getSimulation().getTime(this);
        final var processingSize =
                (currTime - taskStartTime) * machine.getPoderComputacional();

        if (preemptedTask.getCheckPoint() > 0.0) {
            return processingSize % preemptedTask.getCheckPoint();
        } else {
            return processingSize;
        }
    }

    private static Comparator<EnergyUserControl> compareConsumptionWeightedByEfficiency() {
        return Comparator
                .comparingDouble(EnergyUserControl::currentConsumptionWeightedByEfficiency)
                .thenComparing(EnergyUserControl::excessProcessingPower);
    }
}
