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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

@Policy
public class EHOSEP extends GridSchedulingPolicy {
    private static final double REFRESH_TIME = 15.0;
    private final List<UserControl> userControls = new ArrayList<>();
    private final Map<CS_Processamento, SlaveControl> slaveControls =
            new HashMap<>();
    private final List<Tarefa> esperaTarefas = new ArrayList<>();
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

        for (final var userControl : this.userControls) {

            final var optTask = this.findTaskSuitableFor(userControl);

            if (optTask.isEmpty()) {
                continue;
            }

            final var task = optTask.get();

            final int resourceIndex = this.findIndexOfResourceBestSuitedFor(
                    userControl,
                    task.getTamProcessamento()
            );

            if (resourceIndex == -1) {
                continue;
            }

            final var resource = this.escravos.get(resourceIndex);
            final var control = this.slaveControls.get(resource);

            if (control.isFree()) {
                this.sendTaskToResource(task, resource);
                this.mestre.sendTask(task);

                userControl.gotTaskFrom(resource);

                control.setAsBlocked();
                return;
            }

            if (control.isOccupied()) {
                this.sendTaskToResource(task, resource);

                final var preemptedTask = control.firstTaskInProcessing();

                this.preemptionControls.add(
                        new PreemptionControl(preemptedTask, task)
                );

                this.esperaTarefas.add(task);

                this.mestre.sendMessage(
                        preemptedTask,
                        resource,
                        Mensagens.DEVOLVER_COM_PREEMPCAO
                );

                control.setAsBlocked();
                userControl.decreaseTaskDemand();

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
                Do not call method .escalonarRecurso() on instances of EHOSEP.""");
    }

    @Override
    public Double getTempoAtualizar() {
        return EHOSEP.REFRESH_TIME;
    }

    private Optional<Tarefa> findTaskSuitableFor(final UserControl uc) {
        if (!uc.isEligibleForTask()) {
            return Optional.empty();
        }

        return this.tarefas.stream()
                .filter(uc::isOwnerOf)
                .min(Comparator.comparingDouble(Tarefa::getTamProcessamento));
    }

    private void sendTaskToResource(
            final Tarefa task, final CentroServico resource) {
        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
        this.tarefas.remove(task);
    }

    private int findIndexOfResourceBestSuitedFor(
            final UserControl uc, final double taskSize) {
        // Attempts to find a machine that can host the task 'normally'
        final var availableMachine = this.escravos.stream()
                .filter(this::isMachineAvailable)
                .filter(uc::canUseMachineWithoutExceedingEnergyLimit)
                .max(EHOSEP.bestConsumptionForTaskSize(taskSize));

        if (availableMachine.isPresent()) {
            return this.escravos.indexOf(availableMachine.get());
        }

        // If no available machine is found, preemption may be used to force
        // the task into one

        // However, if the task owner (user in uc) has excess processing
        // power, preemption will NOT be used to accommodate their tasks
        if (uc.hasExcessProcessingPower()) {
            return -1;
        }

        // Otherwise, we must find a user whose tasks should be preempted
        final var optPreemptedUser = this.userControls.stream()
                .filter(UserControl::hasExcessProcessingPower)
                .max(EHOSEP.bestConsumptionWeightedByEfficiency());

        if (optPreemptedUser.isEmpty()) {
            return -1;
        }

        final var preemptedUser = optPreemptedUser.get();

        if (preemptedUser.getEnergyConsumptionLimit() <= uc.getEnergyConsumptionLimit()) {
            return -1;
        }

        // And a machine that should be preempted
        final var optPreemptedMachine = this.escravos.stream()
                .filter(this::isMachineOccupied)
                .filter(uc::canUseMachineWithoutExceedingEnergyLimit)
                .filter(machine -> preemptedUser.isOwnerOf(this.taskToPreemptIn(machine)))
                .min(this.leastWastedProcessingIfPreempted());

        if (optPreemptedMachine.isEmpty()) {
            return -1;
        }

        final var preemptedMachine = optPreemptedMachine.get();

        if (!preemptedUser.canFailyUsePreemptedMachine(preemptedMachine)) {
            return -1;
        }

        return this.escravos.indexOf(preemptedMachine);
    }

    private static Comparator<UserControl> bestConsumptionWeightedByEfficiency() {
        return Comparator
                .comparingDouble(UserControl::currentConsumptionWeightedByEfficiency)
                .thenComparing(UserControl::excessProcessingPower);
    }

    private static Comparator<CS_Processamento> bestConsumptionForTaskSize(final double taskProcessingSize) {
        final ToDoubleFunction<CS_Processamento> criterionFunction =
                m -> EHOSEP.machineConsumptionForTask(m, taskProcessingSize);

        return Comparator
                .comparingDouble(criterionFunction)
                .reversed()
                .thenComparing(CS_Processamento::getPoderComputacional);
    }

    private static double machineConsumptionForTask(
            final CS_Processamento machine, final double taskProcessingSize) {
        return taskProcessingSize
               / machine.getPoderComputacional()
               * machine.getConsumoEnergia();
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

    private boolean isMachineAvailable(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isFree();
    }

    private UserControl bestUserControl() {
        return this.userControls.get(this.userControls.size() - 1);
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
    //Chegada de tarefa concluida
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);

        //Localizar informações sobre máquina que executou a tarefa e usuário
        // proprietário da tarefa
        final var maq = (CS_Processamento) tarefa.getLocalProcessamento();
        final var sc = this.slaveControls.get(maq);

        if (sc.isOccupied()) {

            int statusIndex = -1;

            for (int i = 0; i < this.userControls.size(); i++) {
                if (this.userControls.get(i).getUserId().equals(tarefa.getProprietario())) {
                    statusIndex = i;
                }
            }

            //Atualização das informações de estado do proprietario da tarefa
            // terminada.
            this.userControls.get(statusIndex).decreaseAvailableMachines();
            this.userControls.get(statusIndex).decreaseAvailableProcessingPower(maq.getPoderComputacional());
            this.userControls.get(statusIndex).decreaseEnergyConsumption(maq.getConsumoEnergia());

            sc.setAsFree();
        } else if (sc.isBlocked()) {

            int indexControlePreemp = -1;
            for (int j = 0; j < this.preemptionControls.size(); j++) {
                if (this.preemptionControls.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.preemptionControls.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            int indexStatusUserAlloc = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(this.preemptionControls.get(indexControlePreemp).scheduledTaskUser())) {
                    indexStatusUserAlloc = k;
                    break;
                }
            }

            int indexStatusUserPreemp = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(this.preemptionControls.get(indexControlePreemp).preemptedTaskUser())) {
                    indexStatusUserPreemp = k;
                    break;
                }
            }

            //Localizar tarefa em espera designada para executar
            for (int i = 0; i < this.esperaTarefas.size(); i++) {
                if (this.esperaTarefas.get(i).getProprietario().equals(this.preemptionControls.get(indexControlePreemp).scheduledTaskUser()) && this.esperaTarefas.get(i).getIdentificador() == this.preemptionControls.get(indexControlePreemp).scheduledTaskId()) {

                    //Enviar tarefa para execução
                    this.mestre.sendTask(this.esperaTarefas.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa
                    // será executada
                    this.userControls.get(indexStatusUserAlloc).increaseAvailableMachines();
                    this.userControls.get(indexStatusUserAlloc).increaseAvailableProcessingPower(maq.getPoderComputacional());
                    this.userControls.get(indexStatusUserAlloc).increaseEnergyConsumption(maq.getConsumoEnergia());

                    //Atualizar informações de estado do usuário cuja tarefa
                    // teve a execução interrompida
                    this.userControls.get(indexStatusUserPreemp).decreaseAvailableMachines();
                    this.userControls.get(indexStatusUserPreemp).decreaseAvailableProcessingPower(maq.getPoderComputacional());
                    this.userControls.get(indexStatusUserPreemp).decreaseEnergyConsumption(maq.getConsumoEnergia());

                    //Com a preempção feita, os dados necessários para ela
                    // são eliminados
                    this.preemptionControls.remove(indexControlePreemp);
                    break;
                }
            }
        }
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        //super.resultadoAtualizar(mensagem);
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
    //Receber nova tarefa submetida ou tarefa que sofreu preemoção
    public void adicionarTarefa(final Tarefa tarefa) {
        super.adicionarTarefa(tarefa);

        //Atualização da demanda do usuário proprietário da tarefa
        for (int i = 0; i < this.userControls.size(); i++) {
            if (this.userControls.get(i).getUserId().equals(tarefa.getProprietario())) {
                this.userControls.get(i).increaseTaskDemand();
            }
        }

        //Em caso de preempção
        if (tarefa.getLocalProcessamento() != null) {

            //Localizar informações de estado de máquina que executou a
            // tarefa (se houver)
            final var maq = (CS_Processamento) tarefa.getLocalProcessamento();

            //Localizar informações armazenadas sobre a preempção em particular

            int indexControlePreemp = -1;
            for (int j = 0; j < this.preemptionControls.size(); j++) {
                if (this.preemptionControls.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.preemptionControls.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            int indexStatusUserAlloc = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(this.preemptionControls.get(indexControlePreemp).scheduledTaskUser())) {
                    indexStatusUserAlloc = k;
                }
            }

            int indexStatusUserPreemp = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(this.preemptionControls.get(indexControlePreemp).preemptedTaskUser())) {
                    indexStatusUserPreemp = k;
                }
            }

            //Localizar tarefa em espera deseignada para executar
            for (int i = 0; i < this.esperaTarefas.size(); i++) {

                if (this.esperaTarefas.get(i).getProprietario().equals(this.preemptionControls.get(indexControlePreemp).scheduledTaskUser()) && this.esperaTarefas.get(i).getIdentificador() == this.preemptionControls.get(indexControlePreemp).scheduledTaskId()) {

                    //Enviar tarefa para execução
                    this.mestre.sendTask(this.esperaTarefas.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa
                    // será executada
                    this.userControls.get(indexStatusUserAlloc).increaseAvailableMachines();
                    this.userControls.get(indexStatusUserAlloc).increaseAvailableProcessingPower(maq.getPoderComputacional());
                    this.userControls.get(indexStatusUserAlloc).increaseEnergyConsumption(maq.getConsumoEnergia());

                    //Atualizar informações de estado do usuáro cuja tarefa
                    // foi interrompida
                    this.userControls.get(indexStatusUserPreemp).decreaseAvailableMachines();
                    this.userControls.get(indexStatusUserPreemp).decreaseAvailableProcessingPower(maq.getPoderComputacional());
                    this.userControls.get(indexStatusUserPreemp).decreaseEnergyConsumption(maq.getConsumoEnergia());

                    //Com a preempção feita, os dados necessários para ela
                    // são eliminados
                    this.preemptionControls.remove(indexControlePreemp);

                    break;
                }
            }
        }
    }
}
