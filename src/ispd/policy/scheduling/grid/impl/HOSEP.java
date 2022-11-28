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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Policy
public class HOSEP extends GridSchedulingPolicy {
    private static final double REFRESH_TIME = 15.0;
    private final List<UserControl> userControls = new ArrayList<>();
    private final List<SlaveControl> slaveControls = new ArrayList<>();
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
            this.userControls.add(uc);
        }

        IntStream.range(0, this.escravos.size())
                .mapToObj(i -> new SlaveControl())
                .forEach(this.slaveControls::add);
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
        final var sortedUserControls = this.userControls.stream()
                .sorted()
                .toList();

        for (final var uc : sortedUserControls) {
            if (this.canScheduleTaskFor(uc)) {
                return;
            }
        }
    }

    private boolean canScheduleTaskFor(final UserControl uc) {
        final Tarefa t = getTarefa(uc);
        if (t == null) return false;

        final int resourceIndex = this.buscarRecurso(uc);
        if (resourceIndex == -1) {
            return false;
        }

        final var machine = this.escravos.get(resourceIndex);
        final var sc = this.slaveControls.get(resourceIndex);

        if (sc.isFree()) {

            this.tarefas.remove(t);
            t.setLocalProcessamento(machine);
            t.setCaminho(this.escalonarRota(machine));
            this.mestre.sendTask(t);

            //Atualização dos dados sobre o usuário
            uc.decreaseTaskDemand();
            uc.increaseAvailableProcessingPower(machine.getPoderComputacional());

            //Controle das máquinas
            sc.setAsBlocked();
            return true;
        }

        //Se é caso de preempção, a tarefa configurada e colocada
        // em espera
        if (sc.isOccupied()) {

            this.tarefas.remove(t);
            t.setLocalProcessamento(machine);
            t.setCaminho(this.escalonarRota(machine));

            //Controle de preempção para enviar a nova tarefa no
            // momento certo
            this.preemptionEntries.add(new PreemptionEntry(
                    sc.getTasksInProcessing().get(0).getProprietario(),
                    sc.getTasksInProcessing().get(0).getIdentificador(),
                    t.getProprietario(),
                    t.getIdentificador()
            ));
            this.tasksToSchedule.add(t);

            //Solicitação de retorno da tarefa em execução e
            // atualização da demanda do usuário
            this.mestre.sendMessage(sc.getTasksInProcessing().get(0), machine
                    , Mensagens.DEVOLVER_COM_PREEMPCAO);
            sc.setAsBlocked();
            uc.decreaseTaskDemand();
            return true;
        }
        return false;
    }

    private Tarefa getTarefa(UserControl uc) {
        int trf = -1;

        if (uc.currentTaskDemand() <= 0) {
            return null;
        }

        //Procura pela menor tarefa nao atendida do usuario.
        for (int j = 0; j < this.tarefas.size(); j++) {
            if (this.tarefas.get(j).getProprietario().equals(uc.getUserId())) {
                //Escolher a tarefa de menor tamanho do usuario
                if (trf == -1 || this.tarefas.get(j).getTamProcessamento() < this.tarefas.get(trf).getTamProcessamento()) {
                    trf = j;
                }
            }
        }

        final int taskIndex = trf;

        if (taskIndex == -1) {
            return null;
        }

        final var t = this.tarefas.get(taskIndex);
        return t;
    }

    private int buscarRecurso(final UserControl cliente) {
        //Índice da máquina escolhida, na lista de máquinas
        int indexSelec = -1;

        for (int i = 0; i < this.escravos.size(); i++) {

            if (this.slaveControls.get(i).isFree()) {
                if (indexSelec == -1 || this.escravos.get(i).getPoderComputacional() > this.escravos.get(indexSelec).getPoderComputacional()) {
                    indexSelec = i;
                }
            }
        }

        if (indexSelec != -1) {
            return indexSelec;
        }

        if (this.userControls.get(this.userControls.size() - 1).currentlyAvailableProcessingPower() > this.userControls.get(this.userControls.size() - 1).getOwnedMachinesProcessingPower() && cliente.currentlyAvailableProcessingPower() < cliente.getOwnedMachinesProcessingPower()) {

            for (int i = 0; i < this.escravos.size(); i++) {

                if (this.slaveControls.get(i).isOccupied()) {
                    if (this.slaveControls.get(i).getTasksInProcessing().get(0).getProprietario().equals(this.userControls.get(this.userControls.size() - 1).getUserId())) {

                        if (indexSelec == -1 || this.escravos.get(i).getPoderComputacional() < this.escravos.get(indexSelec).getPoderComputacional()) {

                            indexSelec = i;

                        }
                    }
                }
            }

            if (indexSelec != -1) {

                final double penalidaUserEsperaPosterior =
                        (cliente.currentlyAvailableProcessingPower() + this.escravos.get(indexSelec).getPoderComputacional() - cliente.getOwnedMachinesProcessingPower()) / cliente.getOwnedMachinesProcessingPower();
                final double penalidaUserEscravoPosterior =
                        (this.userControls.get(this.userControls.size() - 1).currentlyAvailableProcessingPower() - this.escravos.get(indexSelec).getPoderComputacional() - this.userControls.get(this.userControls.size() - 1).getOwnedMachinesProcessingPower()) / this.userControls.get(this.userControls.size() - 1).getOwnedMachinesProcessingPower();

                if (penalidaUserEscravoPosterior >= penalidaUserEsperaPosterior || penalidaUserEscravoPosterior > 0) {
                    return indexSelec;
                } else {
                    return -1;
                }
            }
        }
        return indexSelec;
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
    //Chegada de tarefa concluida
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);

        //Localizar informações sobre máquina que executou a tarefa e usuário
        // proprietário da tarefa
        final var maq = (CS_Processamento) tarefa.getLocalProcessamento();
        final int maqIndex = this.escravos.indexOf(maq);

        if (this.slaveControls.get(maqIndex).isOccupied()) {

            int statusIndex = -1;

            for (int i = 0; i < this.userControls.size(); i++) {
                if (this.userControls.get(i).getUserId().equals(tarefa.getProprietario())) {
                    statusIndex = i;
                }
            }

            this.userControls.get(statusIndex).decreaseAvailableProcessingPower(maq.getPoderComputacional());
            this.slaveControls.get(maqIndex).setAsFree();

        } else if (this.slaveControls.get(maqIndex).isBlocked()) {

            int indexControlePreemp = -1;
            for (int j = 0; j < this.preemptionEntries.size(); j++) {
                if (this.preemptionEntries.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.preemptionEntries.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            int indexStatusUserAlloc = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(this.preemptionEntries.get(indexControlePreemp).scheduledTaskUser())) {
                    indexStatusUserAlloc = k;
                    break;
                }
            }

            int indexStatusUserPreemp = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(this.preemptionEntries.get(indexControlePreemp).preemptedTaskUser())) {
                    indexStatusUserPreemp = k;
                    break;
                }
            }

            //Localizar tarefa em espera designada para executar
            for (int i = 0; i < this.tasksToSchedule.size(); i++) {
                if (this.tasksToSchedule.get(i).getProprietario().equals(this.preemptionEntries.get(indexControlePreemp).scheduledTaskUser()) && this.tasksToSchedule.get(i).getIdentificador() == this.preemptionEntries.get(indexControlePreemp).scheduledTaskId()) {

                    //Enviar tarefa para execução
                    this.mestre.sendTask(this.tasksToSchedule.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa
                    // será executada
                    this.userControls.get(indexStatusUserAlloc).increaseAvailableProcessingPower(maq.getPoderComputacional());

                    //Atualizar informações de estado do usuário cuja tarefa
                    // teve a execução interrompida
                    this.userControls.get(indexStatusUserPreemp).decreaseAvailableProcessingPower(maq.getPoderComputacional());

                    //Com a preempção feita, os dados necessários para ela
                    // são eliminados
                    this.preemptionEntries.remove(indexControlePreemp);

                    break;
                }
            }
        }
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        //super.resultadoAtualizar(mensagem);
        //Localizar máquina que enviou estado atualizado
        final int index = this.escravos.indexOf(mensagem.getOrigem());

        //Atualizar listas de espera e processamento da máquina
        this.slaveControls.get(index).setTasksInProcessing(mensagem.getProcessadorEscravo());

        //Tanto alocação para recurso livre como a preempção levam dois
        // ciclos de atualização para que a máquina possa ser considerada
        // para esacalonamento novamente

        //Primeiro ciclo
        if (this.slaveControls.get(index).isBlocked()) {
            this.slaveControls.get(index).setAsUncertain();
            //Segundo ciclo
        } else if (this.slaveControls.get(index).isUncertain()) {
            //Se não está executando nada
            if (this.slaveControls.get(index).getTasksInProcessing().isEmpty()) {

                this.slaveControls.get(index).setAsFree();
                //Se está executando uma tarefa
            } else if (this.slaveControls.get(index).getTasksInProcessing().size() == 1) {

                this.slaveControls.get(index).setAsOccupied();
                //Se há mais de uma tarefa e a máquina tem mais de um núcleo
            }
        }
    }

    @Override
    //Receber nova tarefa submetida ou tarefa que sofreu preemoção
    public void adicionarTarefa(final Tarefa tarefa) {
        super.adicionarTarefa(tarefa);

        //Atualização da demanda do usuário proprietário da tarefa
        for (final var userControl : this.userControls) {
            if (userControl.getUserId().equals(tarefa.getProprietario())) {
                userControl.increaseTaskDemand();
                break;
            }
        }

        //Em caso de preempção
        if (tarefa.getLocalProcessamento() != null) {

            //Localizar informações de estado de máquina que executou a
            // tarefa (se houver)
            final var maq = (CS_Processamento) tarefa.getLocalProcessamento();

            //Localizar informações armazenadas sobre a preempção em particular

            int indexControlePreemp = -1;
            for (int j = 0; j < this.preemptionEntries.size(); j++) {
                if (this.preemptionEntries.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.preemptionEntries.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            int indexStatusUserAlloc = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(this.preemptionEntries.get(indexControlePreemp).scheduledTaskUser())) {
                    indexStatusUserAlloc = k;
                    break;
                }
            }

            int indexStatusUserPreemp = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(this.preemptionEntries.get(indexControlePreemp).preemptedTaskUser())) {
                    indexStatusUserPreemp = k;
                    break;
                }
            }

            //Localizar tarefa em espera deseignada para executar
            for (int i = 0; i < this.tasksToSchedule.size(); i++) {

                if (this.tasksToSchedule.get(i).getProprietario().equals(this.preemptionEntries.get(indexControlePreemp).scheduledTaskUser()) && this.tasksToSchedule.get(i).getIdentificador() == this.preemptionEntries.get(indexControlePreemp).scheduledTaskId()) {

                    //Enviar tarefa para execução
                    this.mestre.sendTask(this.tasksToSchedule.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa
                    // será executada
                    this.userControls.get(indexStatusUserAlloc).increaseAvailableProcessingPower(maq.getPoderComputacional());

                    //Atualizar informações de estado do usuáro cuja tarefa
                    // foi interrompida
                    this.userControls.get(indexStatusUserPreemp).decreaseAvailableProcessingPower(maq.getPoderComputacional());

                    //Com a preempção feita, os dados necessários para ela
                    // são eliminados
                    this.preemptionEntries.remove(indexControlePreemp);

                    break;
                }
            }
        }
    }
}
