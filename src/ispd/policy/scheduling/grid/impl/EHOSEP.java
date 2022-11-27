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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Policy
public class EHOSEP extends GridSchedulingPolicy {
    private final List<UserControl> userControls = new ArrayList<>();
    private final List<SlaveControl> slaveControls = new ArrayList<>();
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

        for (int i = 0; i < this.userControls.size(); i++) {
            final var cliente = this.userControls.get(i);

            //Buscar tarefa para execucao
            final int indexTarefa = this.buscarTarefa(cliente);
            if (indexTarefa != -1) {

                //Buscar máquina para executar a tarefa definida
                final int indexEscravo = this.buscarRecurso(cliente,
                        this.tarefas.get(indexTarefa));
                if (indexEscravo != -1) {

                    //Se não é caso de preempção, a tarefa é configurada e
                    // enviada
                    if (this.slaveControls.get(indexEscravo).isFree()) {

                        final Tarefa tar = this.tarefas.remove(indexTarefa);
                        tar.setLocalProcessamento(this.escravos.get(indexEscravo));
                        tar.setCaminho(this.escalonarRota(this.escravos.get(indexEscravo)));
                        this.mestre.sendTask(tar);

                        //Atualização dos dados sobre o usuário
                        cliente.decreaseTaskDemand();
                        cliente.increaseAvailableMachines();
                        cliente.increaseAvailableProcessingPower(this.escravos.get(indexEscravo).getPoderComputacional());
                        cliente.increaseEnergyConsumption(this.escravos.get(indexEscravo).getConsumoEnergia());

                        //Controle das máquinas
                        this.slaveControls.get(indexEscravo).setAsBlocked();
                        return;
                    }

                    //Se é caso de preempção, a tarefa configurada e colocada
                    // em espera
                    if (this.slaveControls.get(indexEscravo).isOccupied()) {

                        final Tarefa tar = this.tarefas.remove(indexTarefa);
                        tar.setLocalProcessamento(this.escravos.get(indexEscravo));
                        tar.setCaminho(this.escalonarRota(this.escravos.get(indexEscravo)));

                        //Controle de preempção para enviar a nova tarefa no
                        // momento certo
                        this.preemptionControls.add(new PreemptionControl(
                                this.slaveControls.get(indexEscravo).getTasksInProcessing().get(0).getProprietario(),
                                this.slaveControls.get(indexEscravo).getTasksInProcessing().get(0).getIdentificador(),
                                tar.getProprietario(),
                                tar.getIdentificador()
                        ));
                        this.esperaTarefas.add(tar);

                        //Solicitação de retorno da tarefa em execução e
                        // atualização da demanda do usuário
                        this.mestre.sendMessage(this.slaveControls.get(indexEscravo).getTasksInProcessing().get(0), this.escravos.get(indexEscravo), Mensagens.DEVOLVER_COM_PREEMPCAO);
                        this.slaveControls.get(indexEscravo).setAsBlocked();
                        cliente.decreaseTaskDemand();
                        return;
                    }
                }
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
        return 15.0;
    }

    private int buscarTarefa(final UserControl usuario) {
        //Indice da tarefa na lista de tarefas
        int trf = -1;
        //Se o usuario tem demanda nao atendida e seu consumo nao chegou ao
        // limite
        if (usuario.currentTaskDemand() > 0 && usuario.currentEnergyConsumption() < usuario.getEnergyConsumptionLimit()) {
            //Procura pela menor tarefa nao atendida do usuario.
            for (int j = 0; j < this.tarefas.size(); j++) {
                if (this.tarefas.get(j).getProprietario().equals(usuario.getUserId())) {
                    if (trf == -1) {
                        trf = j;
                    } else if (this.tarefas.get(j).getTamProcessamento() < this.tarefas.get(trf).getTamProcessamento()) {//Escolher a tarefa de menor tamanho do usuario
                        trf = j;
                    }
                }
            }
        }
        return trf;
    }

    private int buscarRecurso(final UserControl cliente,
                              final Tarefa TarAloc) {

        /*++++++++++++++++++Buscando recurso livres++++++++++++++++++*/

        //Índice da máquina escolhida, na lista de máquinas
        int indexSelec = -1;
        //Consumo da máquina escolhida e da máquina comparada com a escolhida
        // previamente em passagem anterior do laço
        double consumoSelec = 0.0;
        double consumoMaqTestada;

        for (int i = 0; i < this.escravos.size(); i++) {

            //Verificar o limite de consumo e garantir que o escravo está de
            // fato livre e que não há nenhuma tarefa em trânsito para o
            // escravo. É escolhido o recurso que consumir menos energia pra
            // executar a tarefa alocada.
            if (this.slaveControls.get(i).isFree() && (this.escravos.get(i).getConsumoEnergia() + cliente.currentEnergyConsumption()) <= cliente.getEnergyConsumptionLimit()) {

                if (indexSelec == -1) {

                    indexSelec = i;
                    //Tempo para processar
                    consumoSelec =
                            TarAloc.getTamProcessamento() / this.escravos.get(i).getPoderComputacional();
                    //Consumo em Joule para processar
                    consumoSelec =
                            consumoSelec * this.escravos.get(i).getConsumoEnergia();

                } else {
                    //Tempo para processar
                    consumoMaqTestada =
                            TarAloc.getTamProcessamento() / this.escravos.get(i).getPoderComputacional();
                    //Consumo em Joule para processar
                    consumoMaqTestada =
                            consumoMaqTestada * this.escravos.get(i).getConsumoEnergia();

                    if (consumoSelec > consumoMaqTestada) {

                        indexSelec = i;
                        consumoSelec = consumoMaqTestada;
                    } else if (consumoSelec == consumoMaqTestada) {

                        if (this.escravos.get(i).getPoderComputacional() > this.escravos.get(indexSelec).getPoderComputacional()) {

                            indexSelec = i;
                            consumoSelec = consumoMaqTestada;
                        }
                    }
                }
            }
        }

        if (indexSelec != -1) {
            return indexSelec;
        }

        /*+++++++++++++++++Busca por usuário para preempção+++++++++++++++++*/

        //Se o usuário com maior valor de DifPoder não tem excesso de poder
        // computacional, não há usuário que possa sofrer preempção. Além
        // disso, não ocorrerá preempção para atender usuário que tem excesso.
        if (this.userControls.get(this.userControls.size() - 1).currentlyAvailableProcessingPower() <= this.userControls.get(this.userControls.size() - 1).getOwnedMachinesProcessingPower() || cliente.currentlyAvailableProcessingPower() >= cliente.getOwnedMachinesProcessingPower()) {
            return -1;
        }


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

        if (indexUserPreemp == -1) {
            return -1;
        }

        //Buscar recurso para preempção
        double desperdicioTestado;
        double desperdicioSelec = 0.0;

        for (int j = 0; j < this.escravos.size(); j++) {
            //Procurar recurso ocupado com tarefa do usuário que perderá máquina
            if (this.slaveControls.get(j).isOccupied() && (this.escravos.get(j).getConsumoEnergia() + cliente.currentEnergyConsumption()) <= cliente.getEnergyConsumptionLimit()) {

                final var tarPreemp =
                        this.slaveControls.get(j).getTasksInProcessing().get(0);

                if (tarPreemp.getProprietario().equals(this.userControls.get(indexUserPreemp).getUserId())) {

                    if (indexSelec == -1) {
                        //Se há checkpointing de tarefas
                        if (tarPreemp.getCheckPoint() > 0.0) {
                            //((tempo atual - tempo em que a execução da
                            // tarefa começou no recurso)*poder
                            // computacional)%bloco de checkpointing
                            desperdicioSelec =
                                    ((this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * this.escravos.get(j).getPoderComputacional()) % tarPreemp.getCheckPoint();
                        } else {
                            //Se não há chekcpointin de tarefas, o
                            // desperdício é o tempo total executado para a
                            // tarefa na máquina corrente no laço
                            desperdicioSelec =
                                    (this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * this.escravos.get(j).getPoderComputacional();
                        }
                        indexSelec = j;
                    } else {

                        if (tarPreemp.getCheckPoint() > 0.0) {

                            desperdicioTestado =
                                    ((this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * this.escravos.get(j).getPoderComputacional()) % tarPreemp.getCheckPoint();
                        } else {
                            desperdicioTestado =
                                    (this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * this.escravos.get(j).getPoderComputacional();
                        }
                        //É escolhida a máquina de menor desperdício
                        if (desperdicioTestado < desperdicioSelec) {

                            desperdicioSelec = desperdicioTestado;
                            indexSelec = j;
                        }
                        //Se o desperdício é igual, é escolhida a máquina com
                        // menor poder computacional
                        else if (desperdicioTestado == desperdicioSelec && this.escravos.get(j).getPoderComputacional() < this.escravos.get(indexSelec).getPoderComputacional()) {
                            indexSelec = j;
                        }
                    }
                }
            }
        }

        if (indexUserPreemp != -1 && indexSelec != -1) {
            if ((this.userControls.get(indexUserPreemp).currentlyAvailableProcessingPower() - this.escravos.get(indexSelec).getPoderComputacional()) < this.userControls.get(indexUserPreemp).getOwnedMachinesProcessingPower()) {
                if (this.userControls.get(indexUserPreemp).getEnergyConsumptionLimit() <= cliente.getEnergyConsumptionLimit()) {
                    indexSelec = -1;
                }
            }
        }

        return indexSelec;
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
        final int maqIndex = this.escravos.indexOf(maq);

        if (this.slaveControls.get(maqIndex).isOccupied()) {

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

            this.slaveControls.get(maqIndex).setAsFree();
        } else if (this.slaveControls.get(maqIndex).isBlocked()) {

            int indexControlePreemp = -1;
            for (int j = 0; j < this.preemptionControls.size(); j++) {
                if (this.preemptionControls.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.preemptionControls.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            int indexStatusUserAlloc = -1;
            for (int k = 0; k < this.userControls.size(); k++) {
                if (this.userControls.get(k).getUserId().equals(this.preemptionControls.get(indexControlePreemp).allocatedTaskUser())) {
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
                if (this.esperaTarefas.get(i).getProprietario().equals(this.preemptionControls.get(indexControlePreemp).allocatedTaskUser()) && this.esperaTarefas.get(i).getIdentificador() == this.preemptionControls.get(indexControlePreemp).allocatedTaskId()) {

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
        final int index = this.escravos.indexOf(mensagem.getOrigem());

        //Atualizar listas de espera e processamento da máquina
        this.slaveControls.get(index).setTasksInProcessing((ArrayList<Tarefa>) mensagem.getProcessadorEscravo());
        this.slaveControls.get(index).setTasksOnHold(mensagem.getFilaEscravo());

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
                if (this.userControls.get(k).getUserId().equals(this.preemptionControls.get(indexControlePreemp).allocatedTaskUser())) {
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

                if (this.esperaTarefas.get(i).getProprietario().equals(this.preemptionControls.get(indexControlePreemp).allocatedTaskUser()) && this.esperaTarefas.get(i).getIdentificador() == this.preemptionControls.get(indexControlePreemp).allocatedTaskId()) {

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
