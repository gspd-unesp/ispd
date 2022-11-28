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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

@Policy
public class HOSEP extends GridSchedulingPolicy {
    private static final double REFRESH_TIME = 15.0;
    private final Map<String, UserControl> userControls = new HashMap<>();
    private final Map<CS_Processamento, SlaveControl> slaveControls =
            new HashMap<>();
    private final List<Tarefa> tasksToSchedule = new ArrayList<>();
    private final List<PreemptionEntry> preemptionEntries = new ArrayList<>();

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
        final var t = this
                .findTaskSuitableFor(uc)
                .orElseThrow();

        final int resourceIndex = this.buscarRecurso(uc);
        if (resourceIndex == -1) {
            throw new NoSuchElementException("");
        }

        final var machine = this.escravos.get(resourceIndex);
        final var sc = this.slaveControls.get(machine);

        if (!sc.canHostNewTask()) {
            throw new IllegalStateException("""
                    Machine %s can not host task %s"""
                    .formatted(machine, t));
        }

        this.tarefas.remove(t);
        this.sendTaskToResource(t, machine);

        if (sc.isFree()) {

            this.mestre.sendTask(t);

            uc.decreaseTaskDemand();
            uc.increaseAvailableProcessingPower(machine.getPoderComputacional());

        } else if (sc.isOccupied()) {

            final var taskToPreempt = sc.firstTaskInProcessing();

            this.preemptionEntries.add(new PreemptionEntry(taskToPreempt, t));

            this.tasksToSchedule.add(t);

            this.mestre.sendMessage(
                    taskToPreempt,
                    machine,
                    Mensagens.DEVOLVER_COM_PREEMPCAO
            );

            uc.decreaseTaskDemand();
        }

        sc.setAsBlocked();
    }

    private void sendTaskToResource(
            final Tarefa t, final CS_Processamento machine) {
        t.setLocalProcessamento(machine);
        t.setCaminho(this.escalonarRota(machine));
    }

    private Optional<Tarefa> findTaskSuitableFor(final UserControl uc) {
        // TODO: UC behaviour difference
        if (uc.currentTaskDemand() == 0) {
            return Optional.empty();
        }

        return this.tarefas.stream()
                .filter(uc::isOwnerOf)
                .min(Comparator.comparingDouble(Tarefa::getTamProcessamento));
    }

    private int buscarRecurso(final UserControl uc) {
        //Índice da máquina escolhida, na lista de máquinas
        int indexSelec = -1;

        for (int i = 0; i < this.escravos.size(); i++) {
            final var s = this.escravos.get(i);

            if (this.slaveControls.get(s).isFree()) {
                if (indexSelec == -1 || this.escravos.get(i).getPoderComputacional() > this.escravos.get(indexSelec).getPoderComputacional()) {
                    indexSelec = i;
                }
            }
        }

        if (indexSelec != -1) {
            return indexSelec;
        }

        if (!this.lastUc().hasExcessProcessingPower() || uc.hasExcessProcessingPower()) {
            return indexSelec;
        }

        for (int i = 0; i < this.escravos.size(); i++) {
            final var s = this.escravos.get(i);
            final var sc = this.slaveControls.get(s);

            if (!sc.isOccupied()) {
                continue;
            }

            if (!sc.firstTaskInProcessing().getProprietario().equals(this.lastUc().getUserId())) {
                continue;
            }

            if (indexSelec == -1 ||
                s.getPoderComputacional() < this.escravos.get(indexSelec).getPoderComputacional()) {
                indexSelec = i;
            }
        }

        if (indexSelec == -1) {
            return -1;
        }

        final double penalidaUserEsperaPosterior =
                (uc.currentlyAvailableProcessingPower() + this.escravos.get(indexSelec).getPoderComputacional() - uc.getOwnedMachinesProcessingPower()) / uc.getOwnedMachinesProcessingPower();
        final double penalidaUserEscravoPosterior =
                (this.lastUc().currentlyAvailableProcessingPower() - this.escravos.get(indexSelec).getPoderComputacional() - this.lastUc().getOwnedMachinesProcessingPower()) / this.lastUc().getOwnedMachinesProcessingPower();

        if (penalidaUserEscravoPosterior >= penalidaUserEsperaPosterior || penalidaUserEscravoPosterior > 0) {
            return indexSelec;
        } else {
            return -1;
        }

    }

    private UserControl lastUc() {
        return this.userControls.values().stream()
                .max(Comparator.naturalOrder())
                .orElseThrow();
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
            this.userControls.get(tarefa.getProprietario())
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
                        .inserTaskIntoPreemptedTaskSlot(t, task));
    }

    private void inserTaskIntoPreemptedTaskSlot(
            final Tarefa scheduled, final Tarefa preempted) {
        this.tasksToSchedule.remove(scheduled);

        final var mach = preempted.getCSLProcessamento();
        final var pe = this.findEntryForPreemptedTask(preempted);


        this.mestre.sendTask(scheduled);

        this.userControls.get(pe.scheduledTaskUser())
                .increaseAvailableProcessingPower(mach.getPoderComputacional());

        this.userControls.get(pe.preemptedTaskUser())
                .decreaseAvailableProcessingPower(mach.getPoderComputacional());

        this.preemptionEntries.remove(pe);
    }

    private PreemptionEntry findEntryForPreemptedTask(final Tarefa preempted) {
        return this.preemptionEntries.stream()
                .filter(pe1 -> pe1.willPreemptTask(preempted))
                .findFirst()
                .orElseThrow();
    }
}
