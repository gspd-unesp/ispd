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
import ispd.policy.scheduling.grid.impl.util.PreemptionControl;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

@Policy
public class EHOSEP extends GridSchedulingPolicy {
    private static final double REFRESH_TIME = 15.0;
    private final List<UserControl> userControls = new ArrayList<>();
    private final Map<CS_Processamento, SlaveControl> slaveControls =
            new HashMap<>();
    private final List<Tarefa> tasksInWaiting = new ArrayList<>();
    private final List<PreemptionControl> preemptionControls =
            new ArrayList<>();

    public EHOSEP() {
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
            this.userControls.add(uc);
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

        final double energyCons = userOwnedMachines.stream()
                .mapToDouble(CS_Processamento::getConsumoEnergia)
                .sum();

        final var uc = new UserControl(userId, compPower, this.escravos);
        uc.setOwnedMachinesEnergyConsumption(energyCons);
        uc.calculateEnergyConsumptionLimit(this.metricaUsuarios);
        return uc;
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        Collections.sort(this.userControls);

        for (final var uc : this.userControls) {
            try {
                this.tryFindTaskAndResourceFor(uc);
                return;
            } catch (final NoSuchElementException | IllegalStateException ex) {
                // Try again with next user control
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
                Do not call method .escalonarRecurso() on instances of EHOSEP.""");
    }

    @Override
    public Double getTempoAtualizar() {
        return EHOSEP.REFRESH_TIME;
    }

    /**
     * @param uc
     * @throws NoSuchElementException
     * @throws IllegalStateException
     */
    private void tryFindTaskAndResourceFor(final UserControl uc) {
        final var task = this
                .findTaskSuitableFor(uc)
                .orElseThrow();

        final var machine = this
                .findMachineBestSuitedFor(task, uc)
                .orElseThrow();

        this.tryAcceptTask(machine, task, uc);
    }

    private void tryAcceptTask(
            final CS_Processamento machine, final Tarefa task,
            final UserControl taskOwner) {
        if (!this.canAcceptSomeTask(machine)) {
            throw new IllegalStateException("""
                    Machine %s can not host task %s"""
                    .formatted(machine, task));
        }

        this.sendTaskToResource(task, machine);

        if (this.isMachineAvailable(machine)) {
            this.hostTaskNormally(machine, task, taskOwner);
        }

        if (this.isMachineOccupied(machine)) {
            this.hostTaskWithPreemption(machine, task, taskOwner);
        }

        this.slaveControls.get(machine).setAsBlocked();
    }

    private boolean canAcceptSomeTask(final CS_Processamento machine) {
        return this.isMachineAvailable(machine) ||
               this.isMachineOccupied(machine);
    }

    private void hostTaskNormally(
            final CS_Processamento machine,
            final Tarefa task, final UserControl taskOwner) {
        this.mestre.sendTask(task);
        taskOwner.sentTaskTo(machine);
    }

    private void hostTaskWithPreemption(
            final CS_Processamento machine,
            final Tarefa task, final UserControl taskOwner) {
        final var preemptedTask = this.taskToPreemptIn(machine);

        this.preemptionControls.add(new PreemptionControl(preemptedTask, task));

        this.tasksInWaiting.add(task);

        this.mestre.sendMessage(
                preemptedTask,
                machine,
                Mensagens.DEVOLVER_COM_PREEMPCAO
        );

        taskOwner.decreaseTaskDemand();
    }

    private void sendTaskToResource(
            final Tarefa task, final CentroServico resource) {
        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
        this.tarefas.remove(task);
    }

    private Optional<Tarefa> findTaskSuitableFor(final UserControl uc) {
        if (!uc.isEligibleForTask()) {
            return Optional.empty();
        }

        return this.tarefas.stream()
                .filter(uc::isOwnerOf)
                .min(Comparator.comparingDouble(Tarefa::getTamProcessamento));
    }

    private Optional<CS_Processamento> findMachineBestSuitedFor(
            final Tarefa task, final UserControl taskOwner) {
        return this
                .findAvailableMachineBestSuitedFor(task, taskOwner)
                .or(() -> this.findMachineBestSuitedWithPreemption(taskOwner));
    }

    private Optional<CS_Processamento> findAvailableMachineBestSuitedFor(
            final Tarefa task, final UserControl taskOwner) {
        // Attempts to find a machine that can host the task 'normally'
        return this.escravos.stream()
                .filter(this::isMachineAvailable)
                .filter(taskOwner::canUseMachineWithoutExceedingEnergyLimit)
                .max(EHOSEP.bestConsumptionForTaskSize(task));
    }

    private static Comparator<CS_Processamento> bestConsumptionForTaskSize(final Tarefa task) {
        // Extracted as a variable to aid type inference
        final ToDoubleFunction<CS_Processamento> criterionFunction =
                m -> EHOSEP.calculateEnergyConsumptionForTask(m, task);

        return Comparator
                .comparingDouble(criterionFunction)
                .reversed()
                .thenComparing(CS_Processamento::getPoderComputacional);
    }

    private static double calculateEnergyConsumptionForTask(
            final CS_Processamento machine, final Tarefa task) {
        return task.getTamProcessamento()
               / machine.getPoderComputacional()
               * machine.getConsumoEnergia();
    }

    private boolean isMachineAvailable(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isFree();
    }

    private Optional<CS_Processamento> findMachineBestSuitedWithPreemption(final UserControl taskOwner) {
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
                preemptedUser -> this.findMachineToTransferBetween(preemptedUser, userWithTask));
    }

    private Optional<UserControl> findUserToPreemptFor(final UserControl userWithTask) {
        return this.userControls.stream()
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
        return this.escravos.stream()
                .filter(this::isMachineOccupied)
                .filter(userWithTask::canUseMachineWithoutExceedingEnergyLimit)
                .filter(machine -> userToPreempt.isOwnerOf(this.taskToPreemptIn(machine)))
                .min(this.leastWastedProcessingIfPreempted())
                .filter(userToPreempt::canConcedeProcessingPower);
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

    private Tarefa taskToPreemptIn(final CS_Processamento machine) {
        return this.slaveControls.get(machine).firstTaskInProcessing();
    }

    private boolean isMachineOccupied(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isOccupied();
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
                Do not call method .escalonarTarefa() on instances of EHOSEP.""");
    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);

        final var maq = (CS_Processamento) tarefa.getLocalProcessamento();
        final var sc = this.slaveControls.get(maq);

        if (sc.isOccupied()) {
            this.userControls.stream()
                    .filter(uc -> uc.isOwnerOf(tarefa))
                    .findFirst()
                    .orElseThrow()
                    .stopTaskFrom(maq);

            sc.setAsFree();
        } else if (sc.isBlocked()) {
            final var pc = this.findControlForPreemptedTask(tarefa);

            this.tasksInWaiting.stream()
                    .filter(pc::hasScheduledTask)
                    .findFirst()
                    .ifPresent(this::doPreemptionForScheduledTask);
        }
    }

    private void doPreemptionForScheduledTask(final Tarefa t) {
        this.tasksInWaiting.remove(t);
        this.mestre.sendTask(t);

        final var maq = (CS_Processamento) t.getLocalProcessamento();
        final var pc = this.findControlForPreemptedTask(t);

        this.userControls.stream()
                .filter(EHOSEP.controlHasUser(pc.scheduledTaskUser()))
                .findFirst()
                .orElseThrow()
                .startTaskFrom(maq);

        this.userControls.stream()
                .filter(EHOSEP.controlHasUser(pc.preemptedTaskUser()))
                .findFirst()
                .orElseThrow()
                .stopTaskFrom(maq);

        this.preemptionControls.remove(pc);
    }

    private static Predicate<UserControl> controlHasUser(final String pc) {
        return userControl -> userControl.hasUser(pc);
    }

    private PreemptionControl findControlForPreemptedTask(final Tarefa t) {
        return this.preemptionControls.stream()
                .filter(EHOSEP.controlHasPreemptedTask(t))
                .findFirst()
                .orElseThrow();
    }

    private static Predicate<PreemptionControl> controlHasPreemptedTask(final Tarefa task) {
        return preemptionControl -> preemptionControl.hasPreemptedTask(task);
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        //Localizar máquina que enviou estado atualizado

        //Atualizar listas de espera e processamento da máquina
        final var sc =
                this.slaveControls.get((CS_Processamento) mensagem.getOrigem());

        sc.setTasksInProcessing((ArrayList<Tarefa>) mensagem.getProcessadorEscravo());
        sc.setTasksOnHold(mensagem.getFilaEscravo());

        //Tanto alocação para recurso livre como a preempção levam dois
        // ciclos de atualização para que a máquina possa ser considerada
        // para esacalonamento novamente

        //Primeiro ciclo
        if (sc.isBlocked()) {
            sc.setAsUncertain();
            //Segundo ciclo
        } else if (sc.isUncertain()) {
            //Se não está executando nada
            if (sc.getTasksInProcessing().isEmpty()) {

                sc.setAsFree();
                //Se está executando uma tarefa
            } else if (sc.getTasksInProcessing().size() == 1) {

                sc.setAsOccupied();
                //Se há mais de uma tarefa e a máquina tem mais de um núcleo
            }
        }
    }

    @Override
    public void adicionarTarefa(final Tarefa tarefa) {
        super.adicionarTarefa(tarefa);

        this.userControls.stream()
                .filter(uc -> uc.isOwnerOf(tarefa))
                .forEach(UserControl::increaseTaskDemand);

        //Em caso de preempção
        if (tarefa.getLocalProcessamento() != null) {

            //Localizar informações de estado de máquina que executou a
            // tarefa (se houver)
            final var maq = (CS_Processamento) tarefa.getLocalProcessamento();

            //Localizar informações armazenadas sobre a preempção em particular

            final PreemptionControl pc =
                    this.findControlForPreemptedTask(tarefa);

            int indexStatusUserAlloc = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(pc.scheduledTaskUser())) {
                    indexStatusUserAlloc = k;
                }
            }

            int indexStatusUserPreemp = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(pc.preemptedTaskUser())) {
                    indexStatusUserPreemp = k;
                }
            }

            //Localizar tarefa em espera deseignada para executar
            for (int i = 0; i < this.tasksInWaiting.size(); i++) {

                if (pc.hasScheduledTask(this.tasksInWaiting.get(i))) {
                    this.mestre.sendTask(this.tasksInWaiting.remove(i));

                    this.userControls.get(indexStatusUserAlloc).startTaskFrom(maq);
                    this.userControls.get(indexStatusUserPreemp).stopTaskFrom(maq);

                    this.preemptionControls.remove(pc);

                    break;
                }
            }
        }
    }
}
