package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.grid.impl.util.PreemptionEntry;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserProcessingControl;

import java.util.ArrayList;
import java.util.List;

@Policy
public class M_OSEP extends AbstractOSEP {
    private final List<SlaveControl> slaveControls = new ArrayList<>();
    private final List<Tarefa> tasksInWaiting = new ArrayList<>();
    private final List<PreemptionEntry> preemptionEntries = new ArrayList<>();
    private final List<UserProcessingControl> userControls = new ArrayList<>();
    private Tarefa selectedTask = null;
    private int slaveCounter = 0;

    @Override
    public void iniciar() {
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);

        for (final var user : this.metricaUsuarios.getUsuarios()) {
            this.userControls.add(new UserProcessingControl(user,
                    this.escravos));
        }

        for (final var ignored : this.escravos) {
            this.slaveControls.add(new SlaveControl());
            this.filaEscravo.add(new ArrayList<Tarefa>());
        }
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        final var task = this.escalonarTarefa();
        this.selectedTask = task;

        if (task == null) {
            return;
        }

        final var resource = this.escalonarRecurso();

        if (resource == null) {
            this.tarefas.add(task);
            this.selectedTask = null;
            return;
        }

        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
        //Verifica se não é caso de preempção
        if (!this.slaveControls.get(this.escravos.indexOf(resource)).isPreempted()) {
            this.userControls.get(this.metricaUsuarios.getUsuarios().indexOf(task.getProprietario()))
                    .increaseUsedProcessingPower(resource.getPoderComputacional());
            this.mestre.sendTask(task);
        } else {
            final int resourceIndex = this.escravos.indexOf(resource);
            this.tasksInWaiting.add(task);
            this.preemptionEntries.add(new PreemptionEntry(
                    this.firstTaskIn(resourceIndex).getProprietario(),
                    this.firstTaskIn(resourceIndex).getIdentificador(),
                    task.getProprietario(),
                    task.getIdentificador()
            ));
            this.userControls
                    .get(this.metricaUsuarios.getUsuarios().indexOf(this.firstTaskIn(resourceIndex).getProprietario()))
                    .decreaseUsedProcessingPower(resource.getPoderComputacional());
        }

    }

    @Override
    public Tarefa escalonarTarefa() {
        //Usuários com maior diferença entre uso e posse terão preferência
        double difUsuarioMinimo = -1;
        int indexUsuarioMinimo = -1;
        //Encontrar o usuário que está mais abaixo da sua propriedade
        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            //Verificar se existem tarefas do usuário corrente
            boolean demanda = false;

            for (final Tarefa tarefa : this.tarefas) {
                if (tarefa.getProprietario().equals(this.metricaUsuarios.getUsuarios().get(i))) {
                    demanda = true;
                    break;
                }
            }

            //Caso existam tarefas do usuário corrente e ele esteja com uso
            // menor que sua posse
            if ((this.userControls.get(i).currentlyUsedProcessingPower() < this.userControls.get(i).getOwnedMachinesProcessingPower()) && demanda) {

                if (difUsuarioMinimo == (double) -1) {
                    difUsuarioMinimo =
                            this.userControls.get(i).getOwnedMachinesProcessingPower() - this.userControls.get(i).currentlyUsedProcessingPower();
                    indexUsuarioMinimo = i;
                } else {
                    if (difUsuarioMinimo < this.userControls.get(i).getOwnedMachinesProcessingPower() - this.userControls.get(i).currentlyUsedProcessingPower()) {
                        difUsuarioMinimo =
                                this.userControls.get(i).getOwnedMachinesProcessingPower() - this.userControls.get(i).currentlyUsedProcessingPower();
                        indexUsuarioMinimo = i;
                    }

                }

            }

        }

        if (indexUsuarioMinimo != -1) {
            int indexTarefa = -1;

            for (int i = 0; i < this.tarefas.size(); i++) {
                if (this.tarefas.get(i).getProprietario().equals(this.metricaUsuarios.getUsuarios().get(indexUsuarioMinimo))) {
                    if (indexTarefa == -1) {
                        indexTarefa = i;
                    } else {
                        if (this.tarefas.get(indexTarefa).getTamProcessamento() > this.tarefas.get(i).getTamProcessamento()) {
                            indexTarefa = i;
                        }
                    }
                }
            }

            if (indexTarefa != -1) {
                return this.tarefas.remove(indexTarefa);
            }
        }

        if (this.tarefas.isEmpty()) {
            return null;
        } else {
            return this.tarefas.remove(0);
        }
    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        final var maq = tarefa.getCSLProcessamento();
        final int indexUser =
                this.metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
        this.userControls.get(indexUser)
                .decreaseUsedProcessingPower(maq.getPoderComputacional());
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        super.resultadoAtualizar(mensagem);
        final int index = this.escravos.indexOf(mensagem.getOrigem());
        this.slaveControls.get(index).setTasksInProcessing(mensagem.getProcessadorEscravo());
        this.slaveCounter++;
        if (this.slaveCounter == this.escravos.size()) {
            boolean escalona = false;
            for (int i = 0; i < this.escravos.size(); i++) {
                if (this.slaveControls.get(i).hasTasksInProcessing()
                    && !this.slaveControls.get(i).isPreempted()) {
                    this.slaveControls.get(i).setAsOccupied();
                } else if (!this.slaveControls.get(i).hasTasksInProcessing()
                           && !this.slaveControls.get(i).isPreempted()) {
                    escalona = true;
                    this.slaveControls.get(i).setAsFree();
                } else if (this.slaveControls.get(i).isPreempted()) {
                    this.slaveControls.get(i).setAsBlocked();
                }
            }
            this.slaveCounter = 0;
            if (!this.tarefas.isEmpty() && escalona) {
                this.mestre.executeScheduling();
            }
        }
    }

    @Override
    public void adicionarTarefa(final Tarefa tarefa) {
        super.adicionarTarefa(tarefa);
        final var maq = (CS_Processamento) tarefa.getLocalProcessamento();

        if (tarefa.getLocalProcessamento() == null) {
            this.mestre.executeScheduling();
            return;
        }

        //Em caso de preempção, é procurada a tarefa correspondente para ser
        // enviada ao escravo agora desocupado
        int j;
        int indexControle = -1;
        for (j = 0; j < this.preemptionEntries.size(); j++) {
            if (this.preemptionEntries.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.preemptionEntries.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                indexControle = j;
                break;
            }
        }

        for (int i = 0; i < this.tasksInWaiting.size(); i++) {
            if (this.tasksInWaiting.get(i).getProprietario().equals(this.preemptionEntries.get(indexControle).scheduledTaskUser()) && this.tasksInWaiting.get(i).getIdentificador() == this.preemptionEntries.get(j).scheduledTaskId()) {
                final int indexUser =
                        this.metricaUsuarios.getUsuarios().indexOf(this.preemptionEntries.get(indexControle).scheduledTaskUser());
                this.userControls.get(indexUser).increaseUsedProcessingPower(maq.getPoderComputacional());
                this.mestre.sendTask(this.tasksInWaiting.get(i));
                this.tasksInWaiting.remove(i);
                this.preemptionEntries.remove(j);
                break;
            }
        }

    }

    @Override
    public CS_Processamento escalonarRecurso() {

        //Buscando recurso livre
        CS_Processamento selec = null;

        for (int i = 0; i < this.escravos.size(); i++) {

            if (this.filaEscravo.get(i).isEmpty() && !this.slaveControls.get(i).hasTasksInProcessing() && this.slaveControls.get(i).isFree()) {//Garantir que o escravo está de fato livre e que não há nenhuma tarefa em trânsito para o escravo
                if (selec == null) {
                    selec = this.escravos.get(i);
                } else if (Math.abs(this.escravos.get(i).getPoderComputacional() - this.selectedTask.getTamProcessamento()) < Math.abs(selec.getPoderComputacional() - this.selectedTask.getTamProcessamento())) {//Best Fit
                    selec = this.escravos.get(i);
                }
            }
        }

        if (selec != null) {
            this.slaveControls.get(this.escravos.indexOf(selec)).setAsBlocked();//Inidcar que uma tarefa será enviada e que , portanto , este escravo deve ser bloqueada até a próxima atualização
            return selec;
        }

        String usermax = null;
        double diff = -1;

        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {

            if (this.userControls.get(i).currentlyUsedProcessingPower() > this.userControls.get(i).getOwnedMachinesProcessingPower() && !this.metricaUsuarios.getUsuarios().get(i).equals(this.selectedTask.getProprietario())) {

                if (diff == (double) -1) {

                    usermax = this.metricaUsuarios.getUsuarios().get(i);
                    diff = this.userControls.get(i).currentlyUsedProcessingPower() - this.userControls.get(i).getOwnedMachinesProcessingPower();

                } else {

                    if (this.userControls.get(i).currentlyUsedProcessingPower() - this.userControls.get(i).getOwnedMachinesProcessingPower() > diff) {

                        usermax = this.metricaUsuarios.getUsuarios().get(i);
                        diff = this.userControls.get(i).currentlyUsedProcessingPower() - this.userControls.get(i).getOwnedMachinesProcessingPower();

                    }

                }

            }

        }

        int index = -1;
        if (usermax != null) {

            for (int i = 0; i < this.escravos.size(); i++) {
                if (this.slaveControls.get(i).hasTasksInProcessing() && this.slaveControls.get(i).isOccupied() && this.filaEscravo.get(i).isEmpty() && this.firstTaskIn(i).getProprietario().equals(usermax)) {
                    if (index == -1) {

                        index = i;

                    } else {

                        if (this.escravos.get(i).getPoderComputacional() < this.escravos.get(index).getPoderComputacional()) {

                            index = i;

                        }
                    }
                }
            }
        }

        //Fazer a preempção
        if (index != -1) {
            final CS_Processamento cs_processamento = this.escravos.get(index);
            //Verifica se vale apena fazer preempção
            final int index_selec = this.escravos.indexOf(cs_processamento);
            final Tarefa tar =
                    this.firstTaskIn(index_selec);

            final int indexUserEscravo =
                    this.metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
            final int indexUserEspera =
                    this.metricaUsuarios.getUsuarios().indexOf(this.selectedTask.getProprietario());

            //Penalidade do usuário dono da tarefa em execução, caso a
            // preempção seja feita
            final double penalidaUserEscravoPosterior =
                    (this.userControls.get(indexUserEscravo).currentlyUsedProcessingPower() - cs_processamento.getPoderComputacional() - this.userControls.get(indexUserEscravo).getOwnedMachinesProcessingPower()) / this.userControls.get(indexUserEscravo).getOwnedMachinesProcessingPower();

            //Penalidade do usuário dono da tarefa slecionada para ser posta
            // em execução, caso a preempção seja feita
            final double penalidaUserEsperaPosterior =
                    (this.userControls.get(indexUserEspera).currentlyUsedProcessingPower() + cs_processamento.getPoderComputacional() - this.userControls.get(indexUserEspera).getOwnedMachinesProcessingPower()) / this.userControls.get(indexUserEspera).getOwnedMachinesProcessingPower();

            //Caso o usuário em espera apresente menor penalidade e os donos
            // das tarefas em execução e em espera não sejam a mesma pessoa ,
            // e , ainda, o escravo esteja executando apenas uma tarefa
            if (penalidaUserEscravoPosterior <= penalidaUserEsperaPosterior || (penalidaUserEscravoPosterior > 0 && penalidaUserEsperaPosterior < 0)) {
                final int i = this.escravos.indexOf(cs_processamento);
                this.slaveControls.get(this.escravos.indexOf(cs_processamento)).setAsPreempted();
                this.mestre.sendMessage(this.firstTaskIn(i), cs_processamento
                        , Mensagens.DEVOLVER_COM_PREEMPCAO);
                return cs_processamento;
            }
        }

        return null;
    }

    private Tarefa firstTaskIn(final int i) {
        return this.slaveControls.get(i).firstTaskInProcessing();
    }
}
