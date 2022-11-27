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

            final int resourceIndex = this.findResourceBestSuitedFor(
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

    private int findResourceBestSuitedFor(
            final UserControl uc, final double taskProcessingSize) {
        int indexSelec = this.something(uc, taskProcessingSize);

        if (indexSelec != -1) {
            return indexSelec;
        }

        //Se o usuário com maior valor de DifPoder não tem excesso de poder
        // computacional, não há usuário que possa sofrer preempção. Além
        // disso, não ocorrerá preempção para atender usuário que tem excesso.
        if (this.lastUserControl().canBePreempted() || uc.getOwnedMachinesProcessingPower() <= uc.currentlyAvailableProcessingPower()) {
            return -1;
        }


        final int indexUserPreemp = this.somethingElse();

        if (indexUserPreemp == -1) {
            return -1;
        }

        //Buscar recurso para preempção
        double desperdicioTestado;
        double desperdicioSelec = 0.0;

        for (int j = 0; j < this.escravos.size(); j++) {
            //Procurar recurso ocupado com tarefa do usuário que perderá máquina
            final var machine = this.escravos.get(j);
            final var sc = this.slaveControls.get(machine);

            if (sc.isOccupied() && uc.canUseMachinePower(machine)) {

                final var tarPreemp =
                        sc.firstTaskInProcessing();

                if (this.userControls.get(indexUserPreemp).isOwnerOf(tarPreemp)) {

                    if (indexSelec == -1) {
                        //Se há checkpointing de tarefas
                        if (tarPreemp.getCheckPoint() > 0.0) {
                            //((tempo atual - tempo em que a execução da
                            // tarefa começou no recurso)*poder
                            // computacional)%bloco de checkpointing
                            desperdicioSelec =
                                    ((this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * machine.getPoderComputacional()) % tarPreemp.getCheckPoint();
                        } else {
                            //Se não há chekcpointin de tarefas, o
                            // desperdício é o tempo total executado para a
                            // tarefa na máquina corrente no laço
                            desperdicioSelec =
                                    (this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * machine.getPoderComputacional();
                        }
                        indexSelec = j;
                    } else {

                        if (tarPreemp.getCheckPoint() > 0.0) {

                            desperdicioTestado =
                                    ((this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * machine.getPoderComputacional()) % tarPreemp.getCheckPoint();
                        } else {
                            desperdicioTestado =
                                    (this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * machine.getPoderComputacional();
                        }
                        //É escolhida a máquina de menor desperdício
                        if (desperdicioTestado < desperdicioSelec) {

                            desperdicioSelec = desperdicioTestado;
                            indexSelec = j;
                        }
                        //Se o desperdício é igual, é escolhida a máquina com
                        // menor poder computacional
                        else if (desperdicioTestado == desperdicioSelec && machine.getPoderComputacional() < this.escravos.get(indexSelec).getPoderComputacional()) {
                            indexSelec = j;
                        }
                    }
                }
            }
        }

        if (indexUserPreemp != -1 && indexSelec != -1) {
            if ((this.userControls.get(indexUserPreemp).currentlyAvailableProcessingPower() - this.escravos.get(indexSelec).getPoderComputacional()) < this.userControls.get(indexUserPreemp).getOwnedMachinesProcessingPower()) {
                if (this.userControls.get(indexUserPreemp).getEnergyConsumptionLimit() <= uc.getEnergyConsumptionLimit()) {
                    indexSelec = -1;
                }
            }
        }

        return indexSelec;
    }

    private int somethingElse() {
        //Métricas e índice do usuário que possivelmente perderá recurso
        //Começando pelo usuário de maior excesso
        double consumoPonderadoSelec = 0.0;
        int indexUserPreemp = -1;
        for (int i = this.userControls.size() - 1; i >= 0; i--) {
            //Apenas usuários que tem excesso de poder computacional podem
            // sofrer preempção
            if (this.userControls.get(i).currentlyAvailableProcessingPower() > this.userControls.get(i).getOwnedMachinesProcessingPower()) {
                //Se ainda não foi escolhido
                if (indexUserPreemp == -1) {
                    indexUserPreemp = i;
                    //Sofre preempção o usuário com maior métrica calculada
                    consumoPonderadoSelec =
                            (this.userControls.get(i).currentEnergyConsumption()) * this.userControls.get(i).calculatedEnergyEfficiencyRatio();
                } else {

                    final double consumoPonderadoCorrente =
                            (this.userControls.get(i).currentEnergyConsumption()) * this.userControls.get(i).calculatedEnergyEfficiencyRatio();
                    if (consumoPonderadoCorrente > consumoPonderadoSelec) {

                        indexUserPreemp = i;
                        consumoPonderadoSelec = consumoPonderadoCorrente;
                    } else if (consumoPonderadoCorrente == consumoPonderadoSelec) {

                        if ((this.userControls.get(i).currentlyAvailableProcessingPower() - this.userControls.get(i).getOwnedMachinesProcessingPower()) > (this.userControls.get(indexUserPreemp).currentlyAvailableProcessingPower() - this.userControls.get(indexUserPreemp).getOwnedMachinesProcessingPower())) {
                            indexUserPreemp = i;
                            consumoPonderadoSelec = consumoPonderadoCorrente;
                        }
                    }
                }
            }
        }
        return indexUserPreemp;
    }

    private UserControl lastUserControl() {
        return this.userControls.get(this.userControls.size() - 1);
    }

    private int something(
            final UserControl uc, final double taskProcessingSize) {
        final var x = this.escravos.stream()
                .filter(this::isSuitedForTask)
                .filter(uc::canUseMachinePower)
                .max(EHOSEP.consumptionForTaskSizeComparator(taskProcessingSize));

        int indexSelec = -1;
        double consumoSelec = 0.0;

        for (int i = 0; i < this.escravos.size(); i++) {

            //Verificar o limite de consumo e garantir que o escravo está de
            // fato livre e que não há nenhuma tarefa em trânsito para o
            // escravo. É escolhido o recurso que consumir menos energia pra
            // executar a tarefa alocada.
            final var machine = this.escravos.get(i);

            if (!(this.isSuitedForTask(machine) && uc.canUseMachinePower(machine))) {
                continue;
            }

            if (indexSelec == -1) {
                indexSelec = i;
                consumoSelec =
                        EHOSEP.machineConsumptionForTask(
                                machine,
                                taskProcessingSize
                        );
            } else {
                final var consumoMaqTestada =
                        EHOSEP.machineConsumptionForTask(
                                machine,
                                taskProcessingSize
                        );

                if (consumoMaqTestada < consumoSelec) {
                    indexSelec = i;
                    consumoSelec = consumoMaqTestada;
                } else if (consumoMaqTestada == consumoSelec) {
                    if (machine.getPoderComputacional() > this.escravos.get(indexSelec).getPoderComputacional()) {
                        indexSelec = i;
                        consumoSelec = consumoMaqTestada;
                    }
                }
            }
        }

        return indexSelec;
    }

    private static Comparator<CS_Processamento> consumptionForTaskSizeComparator(final double taskProcessingSize) {
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

    private boolean isSuitedForTask(final CS_Processamento machine) {
        return this.slaveControls.get(machine).isFree();
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
