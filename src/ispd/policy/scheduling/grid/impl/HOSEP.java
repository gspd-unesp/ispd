package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.scheduling.grid.impl.util.PreemptionEntry;
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

    protected void hostTaskFromUserInMachine(
            final Tarefa task, final UserControl taskOwner,
            final CS_Processamento machine) {
        this.sendTaskToResource(task, machine);
        this.tarefas.remove(task);

        if (this.isMachineAvailable(machine)) {
            this.hostTaskNormally(task, taskOwner, machine);
        } else if (this.isMachineOccupied(machine)) {
            this.hostTaskWithPreemption(task, taskOwner, machine);
        }

        this.slaveControls.get(machine).setAsBlocked();
    }

    private void hostTaskNormally(
            final Tarefa task, final UserControl uc,
            final CS_Processamento machine) {
        this.sendTaskFromUserToMachine(task, uc, machine);
        uc.decreaseTaskDemand();
    }


    private void hostTaskWithPreemption(
            final Tarefa taskToSchedule, final UserControl taskOwner,
            final CS_Processamento machine) {
        final var taskToPreempt = this.taskToPreemptIn(machine);

        this.preemptionEntries.add(
                new PreemptionEntry(taskToPreempt, taskToSchedule)
        );

        this.tasksToSchedule.add(taskToSchedule);

        this.mestre.sendMessage(
                taskToPreempt,
                machine,
                Mensagens.DEVOLVER_COM_PREEMPCAO
        );

        taskOwner.decreaseTaskDemand();
    }

    private Tarefa taskToPreemptIn(final CS_Processamento machine) {
        return this.slaveControls.get(machine).firstTaskInProcessing();
    }

    private void sendTaskToResource(
            final Tarefa task, final CentroServico resource) {
        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
    }

    @Override
    protected Optional<Tarefa> findTaskSuitableFor(final UserControl uc) {
        if (!HOSEP.isUserEligibleForTask(uc)) {
            return Optional.empty();
        }

        return this.tasksOwnedBy(uc)
                .min(Comparator.comparingDouble(Tarefa::getTamProcessamento));
    }

    private static boolean isUserEligibleForTask(final UserControl uc) {
        return uc.hasTaskDemand();
    }

    private Stream<Tarefa> tasksOwnedBy(final UserControl uc) {
        return this.tarefas.stream().filter(uc::isOwnerOf);
    }

    @Override
    protected Optional<CS_Processamento> findMachineBestSuitedFor(
            final Tarefa task, final UserControl taskOwner) {
        return this
                .findAvailableMachineBestSuitedFor(task, taskOwner)
                .or(() -> this.findOccupiedMachineBestSuitedFor(taskOwner));
    }

    private Optional<CS_Processamento> findAvailableMachineBestSuitedFor(
            final Tarefa task, final UserControl taskOwner) {
        return this.availableMachinesFor(taskOwner)
                .max(HOSEP.bestAvailableMachines(task));
    }

    private static Comparator<CS_Processamento> bestAvailableMachines(final Tarefa task) {
        return HOSEP.bestComputationalPower();
    }

    private static Comparator<CS_Processamento> bestComputationalPower() {
        return Comparator.comparingDouble(CS_Processamento::getPoderComputacional);
    }

    private Stream<CS_Processamento> availableMachinesFor(final UserControl taskOwner) {
        return this.escravos.stream()
                .filter(this::isMachineAvailable);
    }

    private Optional<CS_Processamento> findOccupiedMachineBestSuitedFor(final UserControl taskOwner) {
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
                .min(HOSEP.bestComputationalPower())
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

    private boolean isMachineAvailable(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isFree();
    }

    private boolean isMachineOccupied(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isOccupied();
    }
}
