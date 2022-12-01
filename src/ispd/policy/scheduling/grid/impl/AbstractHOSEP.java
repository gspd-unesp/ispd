package ispd.policy.scheduling.grid.impl;

import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.SchedulingPolicy;
import ispd.policy.scheduling.grid.GridMaster;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.PreemptionEntry;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractHOSEP extends GridSchedulingPolicy {
    private static final double REFRESH_TIME = 15.0;
    protected final Map<String, UserControl> userControls = new HashMap<>();
    protected final Map<CS_Processamento, SlaveControl> slaveControls =
            new HashMap<>();
    protected final Collection<Tarefa> tasksToSchedule = new HashSet<>();
    protected final Collection<PreemptionEntry> preemptionEntries =
            new HashSet<>();

    @Override
    public void iniciar() {
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);

        final var nonMasters = this.escravos.stream()
                .filter(Predicate.not(GridMaster.class::isInstance))
                .toList();

        for (final var userId : this.metricaUsuarios.getUsuarios()) {
            final var userOwnedMachines = nonMasters.stream()
                    .filter(machine -> userId.equals(machine.getProprietario()))
                    .toList();

            final var uc = this.makeUserControlFor(userId, userOwnedMachines);
            this.userControls.put(userId, uc);
        }

        for (final var s : this.escravos)
            this.slaveControls.put(s, new SlaveControl());
    }

    protected UserControl makeUserControlFor(
            final String userId,
            final Collection<? extends CS_Processamento> userOwnedMachines) {
        final double compPower = userOwnedMachines.stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .sum();

        return new UserControl(userId, compPower, this.escravos);
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    /**
     * Attempts to schedule a task and a suitable machine for one of the
     * users, giving preference to users "first" in a sorted list according
     * to the {@link UserControl#compareTo(UserControl) comparison criteria} of
     * {@link UserControl}.<br>
     * <p>
     * The method stops immediately upon any successful scheduling of a task
     * in a resource, be it 'normally' or through preemption.
     * </p>
     * For details on scheduling criteria, see:
     * <ul>
     * <li>{@link #findTaskSuitableFor(UserControl) Task selection}</li>
     * <li>{@link #findMachineBestSuitedFor(Tarefa, UserControl) Machine
     * selection}</li>
     * </ul>
     */
    @Override
    public void escalonar() {
        for (final var uc : this.sortedUserControls()) {
            if (this.canScheduleTaskFor(uc)) {
                return;
            }
        }
    }

    /**
     * This algorithm's resource scheduling does not conform to the standard
     * {@link SchedulingPolicy} interface.<br>
     * Therefore, calling this method on instances of this algorithm will
     * result in an {@link UnsupportedOperationException} being thrown.
     *
     * @return not applicable in this context, an exception is thrown instead.
     * @throws UnsupportedOperationException whenever called.
     */
    @Override
    public CS_Processamento escalonarRecurso() {
        throw new UnsupportedOperationException("""
                Do not call method .escalonarRecurso() on HOSEP-like algorithms.""");
    }

    @Override
    public Double getTempoAtualizar() {
        return AbstractHOSEP.REFRESH_TIME;
    }

    private List<UserControl> sortedUserControls() {
        return this.userControls.values().stream()
                .sorted()
                .toList();
    }

    /**
     * Attempts to find a task and a resource to execute such task, for the
     * user represented in {@code uc}. If successful, will initiate the
     * execution of the selected task in the selected resource and return
     * {@code true} if such procedure succeeds; otherwise, won't do anything
     * and will return {@code false}.<br>
     *
     * @param uc {@link UserControl} for the user whose tasks may need
     *           scheduling
     * @return {@code true} if a task and resource were selected
     * successfully, and the task was sent to be executed in the resource
     * successfully; {@code false} otherwise
     */
    private boolean canScheduleTaskFor(final UserControl uc) {
        try {
            this.tryFindTaskAndResourceFor(uc);
            return true;
        } catch (final NoSuchElementException | IllegalStateException ex) {
            return false;
        }
    }

    /**
     * Attempts to find a task and a resource for the user represented in
     * {@code uc}, and initiate the process of hosting the selected task in
     * the selected resource.<br>
     * If it fails in finding either an appropriate task or a suitable
     * resource for the selected task, will throw a
     * {@link NoSuchElementException}.<br>
     * If hosting the selected task in the selected resource fails, will echo
     * the exception thrown in the process. Namely,
     * {@link IllegalStateException}.<br>
     *
     * @param uc {@link UserControl} representing the user whose tasks may
     *           need scheduling
     * @throws NoSuchElementException if it cannot select either an
     *                                appropriate task or a suitable resource
     *                                for a selected task, for the
     *                                given {@link UserControl}
     * @throws IllegalStateException  if hosting the selected task in the
     *                                selected resource fails
     */
    private void tryFindTaskAndResourceFor(final UserControl uc) {
        final var task = this
                .findTaskSuitableFor(uc)
                .orElseThrow();

        final var machine = this
                .findMachineBestSuitedFor(task, uc)
                .orElseThrow();

        this.tryHostTaskFromUserInMachine(task, uc, machine);
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
    private void tryHostTaskFromUserInMachine(
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
            final Tarefa task, final UserControl uc,
            final CS_Processamento machine) {
        this.sendTaskFromUserToMachine(task, uc, machine);
        uc.decreaseTaskDemand();
    }

    private void sendTaskFromUserToMachine(
            final Tarefa task, final UserControl taskOwner,
            final CS_Processamento machine) {
        this.mestre.sendTask(task);
        taskOwner.startTaskFrom(machine);
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

    private void sendTaskToResource(
            final Tarefa task, final CentroServico resource) {
        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
    }

    private boolean canMachineHostNewTask(final CS_Processamento machine) {
        return this.slaveControls.get(machine).canHostNewTask();
    }

    protected Optional<Tarefa> findTaskSuitableFor(final UserControl uc) {
        if (!this.isUserEligibleForTask(uc)) {
            return Optional.empty();
        }

        return this.tasksOwnedBy(uc)
                .min(Comparator.comparingDouble(Tarefa::getTamProcessamento));
    }

    protected boolean isUserEligibleForTask(final UserControl uc) {
        return uc.hasTaskDemand();
    }

    protected Stream<Tarefa> tasksOwnedBy(final UserControl uc) {
        return this.tarefas.stream().filter(uc::isOwnerOf);
    }

    protected Optional<CS_Processamento> findMachineBestSuitedFor(
            final Tarefa task, final UserControl taskOwner) {
        return this
                .findAvailableMachineBestSuitedFor(task, taskOwner)
                .or(() -> this.findOccupiedMachineBestSuitedFor(taskOwner));
    }

    protected Optional<CS_Processamento> findAvailableMachineBestSuitedFor(
            final Tarefa task, final UserControl taskOwner) {
        return this.availableMachinesFor(taskOwner)
                .max(this.compareAvailableMachinesFor(task));
    }

    protected Comparator<CS_Processamento> compareAvailableMachinesFor(final Tarefa task) {
        return Comparator.comparingDouble(CS_Processamento::getPoderComputacional);
    }

    protected Stream<CS_Processamento> availableMachinesFor(final UserControl taskOwner) {
        return this.escravos.stream().filter(this::isMachineAvailable);
    }

    protected boolean isMachineAvailable(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isFree();
    }

    protected Optional<CS_Processamento> findOccupiedMachineBestSuitedFor(final UserControl taskOwner) {
        // If no available machine is found, preemption may be used to force
        // the task into one. However, if the task owner has excess
        // processing power, preemption will NOT be used to accommodate them
        if (taskOwner.hasExcessProcessingPower() ||
            !this.theBestUser().hasExcessProcessingPower()) {
            return Optional.empty();
        }

        return this.findMachineToPreemptFor(taskOwner);
    }

    protected Optional<CS_Processamento> findMachineToPreemptFor(final UserControl taskOwner) {
        return this.findUserToPreemptFor(taskOwner).flatMap(
                userToPreempt -> this.findMachineToTransferBetween(userToPreempt, taskOwner));
    }

    protected abstract Optional<UserControl> findUserToPreemptFor(UserControl taskOwner);

    protected Optional<CS_Processamento> findMachineToTransferBetween(
            final UserControl userToPreempt, final UserControl taskOwner) {
        return this.machinesTransferableBetween(userToPreempt, taskOwner)
                .min(this.compareOccupiedMachines())
                .filter(machine -> this.shouldTransferMachine(
                        machine, userToPreempt, taskOwner));
    }

    protected abstract boolean shouldTransferMachine(
            CS_Processamento machine,
            UserControl machineOwner, UserControl nextOwner);

    protected Stream<CS_Processamento> machinesTransferableBetween(
            final UserControl userToPreempt, final UserControl taskOwner) {
        return this.machinesOccupiedBy(userToPreempt);
    }

    protected Stream<CS_Processamento> machinesOccupiedBy(final UserControl userToPreempt) {
        return this.escravos.stream()
                .filter(this::isMachineOccupied)
                .filter(machine -> userToPreempt.isOwnerOf(this.taskToPreemptIn(machine)));
    }

    protected Tarefa taskToPreemptIn(final CS_Processamento machine) {
        return this.slaveControls.get(machine).firstTaskInProcessing();
    }

    protected boolean isMachineOccupied(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isOccupied();
    }

    protected Comparator<CS_Processamento> compareOccupiedMachines() {
        return Comparator.comparingDouble(CS_Processamento::getPoderComputacional);
    }

    protected UserControl theBestUser() {
        return this.userControls.values().stream()
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }

    /**
     * This algorithm's task scheduling does not conform to the standard
     * {@link SchedulingPolicy} interface.<br>
     * Therefore, calling this method on instances of this algorithm will
     * result in an {@link UnsupportedOperationException} being thrown.
     *
     * @return not applicable in this context, an exception is thrown instead.
     * @throws UnsupportedOperationException whenever called.
     */
    @Override
    public Tarefa escalonarTarefa() {
        throw new UnsupportedOperationException("""
                Do not call method .escalonarTarefa() on HOSEP-like algorithms.""");
    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);

        final var maq = tarefa.getCSLProcessamento();
        final var sc = this.slaveControls.get(maq);

        if (sc.isOccupied()) {
            this.userControls
                    .get(tarefa.getProprietario())
                    .stopTaskFrom(maq);

            sc.setAsFree();
        } else if (sc.isBlocked()) {
            this.processPreemptedTask(tarefa);
        }
    }

    private void processPreemptedTask(final Tarefa task) {
        final var pe = this.findEntryForPreemptedTask(task);

        this.tasksToSchedule.stream()
                .filter(pe::willScheduleTask)
                .findFirst()
                .ifPresent(t -> this
                        .insertTaskIntoPreemptedTaskSlot(t, task));
    }

    private PreemptionEntry findEntryForPreemptedTask(final Tarefa t) {
        return this.preemptionEntries.stream()
                .filter(pe -> pe.willPreemptTask(t))
                .findFirst()
                .orElseThrow();
    }

    private void insertTaskIntoPreemptedTaskSlot(
            final Tarefa scheduled, final Tarefa preempted) {
        this.tasksToSchedule.remove(scheduled);

        final var mach = preempted.getCSLProcessamento();
        final var pe = this.findEntryForPreemptedTask(preempted);

        final var user = this.userControls.get(pe.scheduledTaskUser());
        this.sendTaskFromUserToMachine(scheduled, user, mach);

        this.userControls
                .get(pe.preemptedTaskUser())
                .stopTaskFrom(mach);

        this.preemptionEntries.remove(pe);
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        final var sc = this.slaveControls
                .get((CS_Processamento) mensagem.getOrigem());

        sc.setTasksInProcessing(mensagem.getProcessadorEscravo());
        sc.updateStatusIfNeeded();
    }

    @Override
    public void adicionarTarefa(final Tarefa tarefa) {
        super.adicionarTarefa(tarefa);

        this.userControls
                .get(tarefa.getProprietario())
                .increaseTaskDemand();

        Optional.of(tarefa)
                .filter(AbstractHOSEP::hasProcessingCenter)
                .ifPresent(this::processPreemptedTask);
    }

    private static boolean hasProcessingCenter(final Tarefa t) {
        return t.getLocalProcessamento() != null;
    }
}
