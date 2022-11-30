package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
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

@Policy
public class HOSEP extends GridSchedulingPolicy {
    private static final double REFRESH_TIME = 15.0;
    private final Map<String, UserControl> userControls = new HashMap<>();
    private final Map<CS_Processamento, SlaveControl> slaveControls =
            new HashMap<>();
    private final Collection<Tarefa> tasksToSchedule = new HashSet<>();
    private final Collection<PreemptionEntry> preemptionEntries =
            new HashSet<>();

    public HOSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

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

    private UserControl makeUserControlFor(
            final String userId,
            final Collection<CS_Processamento> userOwnedMachines) {
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

    @Override
    public void escalonar() {
        final var sortedUserControls = this.userControls.values().stream()
                .sorted()
                .toList();

        for (final var uc : sortedUserControls) {
            if (this.canScheduleTaskFor(uc)) {
                return;
            }
        }
    }

    private boolean canScheduleTaskFor(final UserControl uc) {
        try {
            this.tryFindTaskAndResourceFor(uc);
            return true;
        } catch (final NoSuchElementException | IllegalStateException ex) {
            return false;
        }
    }

    private void tryFindTaskAndResourceFor(final UserControl uc) {
        final var task = this
                .findTaskSuitableFor(uc)
                .orElseThrow();

        final var machine = this
                .findMachineBestSuitedFor(uc)
                .orElseThrow();

        this.tryHostTaskFromUserWithMachine(task, uc, machine);
    }

    private void tryHostTaskFromUserWithMachine(
            final Tarefa task, final UserControl taskOwner,
            final CS_Processamento machine) {

        if (!this.canMachineHostNewTask(machine)) {
            throw new IllegalStateException("""
                    Machine %s can not host task %s"""
                    .formatted(machine, task));
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
        this.senTaskFromUserToMachine(task, uc, machine);
        uc.decreaseTaskDemand();
    }

    private void senTaskFromUserToMachine(
            final Tarefa task, final UserControl taskOwner,
            final CS_Processamento machine) {
        this.mestre.sendTask(task);
        // TODO: Inherit behavior
        taskOwner.increaseAvailableProcessingPower(machine.getPoderComputacional());
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
            final Tarefa t, final CS_Processamento machine) {
        t.setLocalProcessamento(machine);
        t.setCaminho(this.escalonarRota(machine));
    }

    private boolean canMachineHostNewTask(final CS_Processamento machine) {
        return this.slaveControls.get(machine).canHostNewTask();
    }

    private Optional<Tarefa> findTaskSuitableFor(final UserControl uc) {
        // TODO: UC behaviour difference
        if (uc.currentTaskDemand() == 0) {
            return Optional.empty();
        }

        return this.tasksOwnedBy(uc)
                .min(Comparator.comparingDouble(Tarefa::getTamProcessamento));
    }

    private Stream<Tarefa> tasksOwnedBy(final UserControl uc) {
        return this.tarefas.stream().filter(uc::isOwnerOf);
    }

    private Optional<CS_Processamento> findMachineBestSuitedFor(final UserControl taskOwner) {
        return this
                .findAvailableMachineBestSuitedFor()
                .or(() -> this.findOccupiedMachineBestSuitedFor(taskOwner));
    }

    private Optional<CS_Processamento> findOccupiedMachineBestSuitedFor(final UserControl taskOwner) {
        if (taskOwner.hasExcessProcessingPower() ||
            !this.bestUser().hasExcessProcessingPower()) {
            return Optional.empty();
        }

        final var userToPreempt = this.bestUser();

        return this.escravos.stream()
                .filter(this::isMachineOccupied)
                .filter(machine -> userToPreempt.isOwnerOf(this.taskToPreemptIn(machine)))
                .min(Comparator.comparingDouble(CS_Processamento::getPoderComputacional))
                .filter(m -> this.theTest(m, taskOwner, this.bestUser()));
    }

    private boolean isMachineOccupied(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isOccupied();
    }

    private Tarefa taskToPreemptIn(final CS_Processamento machine) {
        return this.slaveControls.get(machine).firstTaskInProcessing();
    }

    private boolean theTest(
            final CS_Processamento m,
            final UserControl userWithTask, final UserControl userToPreempt) {
        if (userToPreempt.canConcedeProcessingPower(m)) {
            return true;
        }

        final double penalty1 =
                userWithTask.penaltyWithProcessing(m.getPoderComputacional());
        final double penalty2 =
                userToPreempt.penaltyWithProcessing(-m.getPoderComputacional());

        return penalty2 >= penalty1;
    }

    private UserControl bestUser() {
        return this.userControls.values().stream()
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }

    private Optional<CS_Processamento> findAvailableMachineBestSuitedFor() {
        return this.escravos.stream()
                .filter(this::isMachineAvailable)
                .max(Comparator.comparingDouble(CS_Processamento::getPoderComputacional));
    }

    private boolean isMachineAvailable(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isFree();
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
                Do not call method .escalonarRecurso() on instances of HOSEP.""");
    }

    @Override
    public Double getTempoAtualizar() {
        return HOSEP.REFRESH_TIME;
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
                Do not call method .escalonarTarefa() on instances of HOSEP.""");
    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);

        final var maq = tarefa.getCSLProcessamento();
        final var sc = this.slaveControls.get(maq);

        if (sc.isOccupied()) {
            this.userControls
                    .get(tarefa.getProprietario())
                    .decreaseAvailableProcessingPower(maq.getPoderComputacional());

            sc.setAsFree();
        } else if (sc.isBlocked()) {
            this.processPreemptedTask(tarefa);
        }
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
                .filter(HOSEP::hasProcessingCenter)
                .ifPresent(this::processPreemptedTask);
    }

    private static boolean hasProcessingCenter(final Tarefa tarefa) {
        return tarefa.getLocalProcessamento() != null;
    }

    private void processPreemptedTask(final Tarefa task) {
        final var pe = this.findEntryForPreemptedTask(task);

        this.tasksToSchedule.stream()
                .filter(pe::willScheduleTask)
                .findFirst()
                .ifPresent(t -> this
                        .insertTaskIntoPreemptedTaskSlot(t, task));
    }

    private void insertTaskIntoPreemptedTaskSlot(
            final Tarefa scheduled, final Tarefa preempted) {
        this.tasksToSchedule.remove(scheduled);

        final var mach = preempted.getCSLProcessamento();
        final var pe = this.findEntryForPreemptedTask(preempted);


        this.senTaskFromUserToMachine(scheduled,
                this.userControls.get(pe.scheduledTaskUser()), mach);

        this.userControls.get(pe.preemptedTaskUser())
                .decreaseAvailableProcessingPower(mach.getPoderComputacional());

        this.preemptionEntries.remove(pe);
    }

    private PreemptionEntry findEntryForPreemptedTask(final Tarefa t) {
        return this.preemptionEntries.stream()
                .filter(pe -> pe.willPreemptTask(t))
                .findFirst()
                .orElseThrow();
    }
}
