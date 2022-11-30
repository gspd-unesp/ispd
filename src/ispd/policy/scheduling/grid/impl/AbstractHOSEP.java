package ispd.policy.scheduling.grid.impl;

import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.SchedulingPolicy;
import ispd.policy.scheduling.grid.GridMaster;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.PreemptionEntry;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractHOSEP extends GridSchedulingPolicy {
    protected static final double REFRESH_TIME = 15.0;
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

    protected void processPreemptedTask(final Tarefa task) {
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

    protected void sendTaskFromUserToMachine(
            final Tarefa task, final UserControl taskOwner,
            final CS_Processamento machine) {
        this.mestre.sendTask(task);
        taskOwner.startTaskFrom(machine);
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        final var sc = this.slaveControls
                .get((CS_Processamento) mensagem.getOrigem());

        sc.setTasksInProcessing(mensagem.getProcessadorEscravo());
        sc.updateStatusIfNeeded();
    }
}
