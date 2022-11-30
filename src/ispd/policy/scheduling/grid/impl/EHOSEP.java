package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.scheduling.grid.impl.util.PreemptionEntry;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

@Policy
public class EHOSEP extends AbstractHOSEP {
    public EHOSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    protected UserControl makeUserControlFor(
            final String userId,
            final Collection<? extends CS_Processamento> userOwnedMachines) {
        final double energyConsumption = userOwnedMachines.stream()
                .mapToDouble(CS_Processamento::getConsumoEnergia)
                .sum();

        final var uc = super.makeUserControlFor(userId, userOwnedMachines);
        uc.setOwnedMachinesEnergyConsumption(energyConsumption);
        uc.calculateEnergyConsumptionLimit(this.metricaUsuarios);
        return uc;
    }


    /**
     * Attempts to initiate the execution (host) of the given {@link Tarefa
     * task} in the given {@link CS_Processamento processing center}.<br>
     * If it is determined that the given {@code machine}'s <i>status</i>
     * {@link SlaveControl#canHostNewTask() is not suited} for hosting a new
     * task, an {@link IllegalStateException} is thrown; otherwise, will
     * host the task in the given machine.<br>
     * Once it is determined that the machine is suitable for receiving a new
     * task, the hosting process is <i>guaranteed to succeed</i>.<br>
     *
     * @param task      {@link Tarefa task} to host in the given
     *                  {@link CS_Processamento machine}
     * @param taskOwner {@link UserControl} representing the owner of the
     *                  given {@link Tarefa task}
     * @param machine   {@link CS_Processamento processing center} that may
     *                  host the task; it must be in a valid state to do so
     * @throws IllegalStateException if the given {@link CS_Processamento
     *                               machine} is not in a suitable state for
     *                               hosting a new task
     * @see #canMachineHostNewTask(CS_Processamento) Machine Status Validation
     */
    @Override
    protected void tryHostTaskFromUserInMachine(
            final Tarefa task, final UserControl taskOwner,
            final CS_Processamento machine) {
        if (!this.canMachineHostNewTask(machine)) {
            throw new IllegalStateException("""
                    Scheduled machine %s can not host tasks"""
                    .formatted(machine));
        }

        this.hostTaskFromUserInMachine(task, taskOwner, machine);
    }


    private void hostTaskFromUserInMachine(
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
            final Tarefa task, final UserControl taskOwner,
            final CS_Processamento machine) {
        this.sendTaskFromUserToMachine(task, taskOwner, machine);
        taskOwner.decreaseTaskDemand();
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

    private boolean isMachineAvailable(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isFree();
    }

    private boolean isMachineOccupied(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isOccupied();
    }

    private boolean canMachineHostNewTask(final CS_Processamento machine) {
        return this.slaveControls.get(machine).canHostNewTask();
    }

    @Override
    protected Optional<Tarefa> findTaskSuitableFor(final UserControl uc) {
        if (!EHOSEP.isUserEligibleForTask(uc)) {
            return Optional.empty();
        }

        return this.tasksOwnedBy(uc)
                .min(Comparator.comparingDouble(Tarefa::getTamProcessamento));
    }

    private static boolean isUserEligibleForTask(final UserControl uc) {
        return uc.hasTaskDemand() && !uc.hasExceededEnergyLimit();
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
                .max(EHOSEP.bestAvailableMachines(task));
    }

    private static Comparator<CS_Processamento> bestAvailableMachines(final Tarefa task) {
        // Extracted as a variable to aid type inference
        final ToDoubleFunction<CS_Processamento> energyConsumption =
                m -> EHOSEP.calculateEnergyConsumptionForTask(m, task);

        return Comparator
                .comparingDouble(energyConsumption)
                .reversed()
                .thenComparing(EHOSEP.bestComputationalPower());
    }

    private static Comparator<CS_Processamento> bestComputationalPower() {
        return Comparator.comparingDouble(CS_Processamento::getPoderComputacional);
    }

    private static double calculateEnergyConsumptionForTask(
            final CS_Processamento machine, final Tarefa task) {
        return task.getTamProcessamento()
               / machine.getPoderComputacional()
               * machine.getConsumoEnergia();
    }

    private Stream<CS_Processamento> availableMachinesFor(final UserControl taskOwner) {
        return this.escravos.stream()
                .filter(this::isMachineAvailable)
                .filter(taskOwner::canUseMachineWithoutExceedingEnergyLimit);
    }

    private Optional<CS_Processamento> findOccupiedMachineBestSuitedFor(final UserControl taskOwner) {
        // If no available machine is found, preemption may be used to force
        // the task into one. However, if the task owner has excess
        // processing power, preemption will NOT be used to accommodate them
        if (taskOwner.hasExcessProcessingPower()) {
            return Optional.empty();
        }

        return this.findMachineToPreemptFor(taskOwner);
    }

    private Optional<CS_Processamento> findMachineToPreemptFor(final UserControl userWithTask) {
        return this.findUserToPreemptFor(userWithTask).flatMap(
                userToPreempt -> this.findMachineToTransferBetween(userToPreempt, userWithTask));
    }

    private Optional<UserControl> findUserToPreemptFor(final UserControl userWithTask) {
        return this.userControls.values().stream()
                .filter(UserControl::hasExcessProcessingPower)
                .max(EHOSEP.bestConsumptionWeightedByEfficiency())
                .filter(userWithTask::hasLessEnergyConsumptionThan);
    }

    private static Comparator<UserControl> bestConsumptionWeightedByEfficiency() {
        return Comparator
                .comparingDouble(UserControl::currentConsumptionWeightedByEfficiency)
                .thenComparing(UserControl::excessProcessingPower);
    }

    private Optional<CS_Processamento> findMachineToTransferBetween(
            final UserControl userToPreempt, final UserControl userWithTask) {
        return this.machinesOccupiedBy(userToPreempt)
                .filter(userWithTask::canUseMachineWithoutExceedingEnergyLimit)
                .min(this.leastWastedProcessingIfPreempted())
                .filter(machine -> EHOSEP.shouldTransferMachine(
                        machine, userToPreempt, userWithTask));
    }

    private static boolean shouldTransferMachine(
            final CS_Processamento machine,
            final UserControl machineOwner, final UserControl nextOwner) {
        return machineOwner.canConcedeProcessingPower(machine);
    }

    private Stream<CS_Processamento> machinesOccupiedBy(final UserControl userToPreempt) {
        return this.escravos.stream()
                .filter(this::isMachineOccupied)
                .filter(machine -> userToPreempt.isOwnerOf(this.taskToPreemptIn(machine)));
    }

    private Comparator<CS_Processamento> leastWastedProcessingIfPreempted() {
        return Comparator
                .comparingDouble(this::wastedProcessingIfPreempted)
                .thenComparing(CS_Processamento::getPoderComputacional);
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
}
