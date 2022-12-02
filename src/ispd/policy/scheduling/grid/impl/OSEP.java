package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.scheduling.grid.impl.util.PreemptionEntry;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserProcessingControl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Policy
public class OSEP extends AbstractOSEP<UserProcessingControl> {
    private final List<Tarefa> tasksInWaiting = new ArrayList<>();
    private final List<PreemptionEntry> preemptionEntries = new ArrayList<>();
    private Tarefa selectedTask = null;
    private int slaveCount = 0;

    @Override
    public void escalonar() {
        final var task = this.escalonarTarefa();

        if (task == null) {
            return;
        }

        this.selectedTask = task;
        final var userStatus = this.userControls.get(task.getProprietario());
        final var resource = this.escalonarRecurso();

        if (resource == null) {
            this.tarefas.add(task);
            this.selectedTask = null;
            return;
        }

        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));

        //Verifica se não é caso de preempção
        final var sc = this.slaveToControl(resource);
        if (sc.isFree()) {
            userStatus.decreaseTaskDemand();
            userStatus.increaseUsedMachines();
            sc.setAsBlocked();
            this.mestre.sendTask(task);
        } else if (sc.isOccupied()) {
            final int resourceIndex = this.escravos.indexOf(resource);
            this.tasksInWaiting.add(task);

            this.preemptionEntries.add(new PreemptionEntry(
                    this.firstTaskIn(resourceIndex).getProprietario(),
                    this.firstTaskIn(resourceIndex).getIdentificador(),
                    task.getProprietario(),
                    task.getIdentificador())
            );

            sc.setAsBlocked();
        }
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        String user;
        //Buscando recurso livre
        CS_Processamento selec = null;

        for (int i = 0; i < this.escravos.size(); i++) {
            final var slave = this.escravos.get(i);

            if (this.slaveToControl(slave).isFree()) {
                //Garantir que o escravo está de fato livre e que não há
                // nenhuma tarefa em trânsito para o escravo
                selec = slave;
                break;
            }
        }

        if (selec != null) {
            // Inidcar que uma tarefa será enviada e que , portanto , este
            // escravo deve ser bloqueada até a próxima atualização
            return selec;
        }

        String usermax = null;
        long diff = -1;

        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            user = this.metricaUsuarios.getUsuarios().get(i);
            if (this.userControls.get(user).currentlyUsedMachineCount() > this.userControls.get(user).getOwnedMachinesCount() && !user.equals(this.selectedTask.getProprietario())) {

                if (diff == -1) {
                    usermax = this.metricaUsuarios.getUsuarios().get(i);
                    diff = this.userControls.get(user).currentlyUsedMachineCount() - this.userControls.get(user).getOwnedMachinesCount();
                } else {
                    if (this.userControls.get(user).currentlyUsedMachineCount() - this.userControls.get(user).getOwnedMachinesCount() > diff) {
                        usermax = user;
                        diff = this.userControls.get(user).currentlyUsedMachineCount() - this.userControls.get(user).getOwnedMachinesCount();
                    }
                }
            }
        }

        int index = -1;
        if (usermax != null) {
            for (int i = 0; i < this.escravos.size(); i++) {
                final var s = this.escravos.get(i);
                if (this.slaveControls.get(s).isOccupied() && this.firstTaskIn(i).getProprietario().equals(usermax)) {
                    index = i;
                    break;
                }
            }
        }

        //Fazer a preempção
        if (index != -1) {
            final CS_Processamento cs_processamento = this.escravos.get(index);
            final int index_selec = this.escravos.indexOf(cs_processamento);
            this.mestre.sendMessage(this.firstTaskIn(index_selec),
                    cs_processamento, Mensagens.DEVOLVER_COM_PREEMPCAO);
            return cs_processamento;
        }

        return null;
    }

    private SlaveControl slaveToControl(final CS_Processamento resource) {
        return this.slaveControls.get(resource);
    }

    private Tarefa firstTaskIn(final int index) {
        return this.slaveControls.get(this.escravos.get(index)).firstTaskInProcessing();
    }

    @Override
    public Tarefa escalonarTarefa() {
        return this.getBestUserForSomeTask()
                .flatMap(this::findAnyTaskOf)
                .map(this::popTaskFromQueue)
                .or(this::firstAvailableTask)
                .orElse(null);
    }

    private Optional<UserProcessingControl> getBestUserForSomeTask() {
        return this.userControls.values().stream()
                .filter(this::isUserEligibleForTask)
                .max(Comparator
                        .comparingLong(UserProcessingControl::excessMachines));
    }

    private boolean isUserEligibleForTask(final UserProcessingControl user) {
        return user.hasExcessMachines() && user.isEligibleForTask();
    }

    private Optional<Tarefa> findAnyTaskOf(final UserProcessingControl uc) {
        return this.tarefas.stream()
                .filter(uc::isOwnerOf)
                .findAny();
    }

    private Tarefa popTaskFromQueue(final Tarefa task) {
        this.tarefas.remove(task);
        return task;
    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        final var maq = tarefa.getCSLProcessamento();
        final var uc = this.userControls.get(tarefa.getProprietario());

        uc.decreaseUsedMachines();
        this.slaveToControl(maq).setAsFree();
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        super.resultadoAtualizar(mensagem);

        this.slaveControls.get((CS_Processamento) mensagem.getOrigem())
                .setTasksInProcessing(mensagem.getProcessadorEscravo());

        this.slaveCount++;

        if (this.slaveCount != this.escravos.size()) {
            return;
        }

        this.slaveCount = 0;

        boolean shouldSchedule = false;
        for (final CS_Processamento s : this.escravos) {
            final var sc = this.slaveControls.get(s);
            if (sc.isBlocked()) {
                sc.setAsUncertain();
            }
            if (sc.isUncertain()) {
                if (sc.hasTasksInProcessing()) {
                    sc.setAsOccupied();
                } else {
                    sc.setAsFree();
                    shouldSchedule = true;
                }
            }
        }

        if (!this.tarefas.isEmpty() && shouldSchedule) {
            this.mestre.executeScheduling();
        }
    }

    @Override
    public void adicionarTarefa(final Tarefa tarefa) {
        super.adicionarTarefa(tarefa);
        final CS_Processamento maq = tarefa.getCSLProcessamento();
        final var estadoUser = this.userControls.get(tarefa.getProprietario());

        if (tarefa.getLocalProcessamento() == null) {
            this.mestre.executeScheduling();
            estadoUser.increaseTaskDemand();
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

                this.mestre.sendTask(this.tasksInWaiting.get(i));

                this.userControls.get(this.preemptionEntries.get(indexControle).scheduledTaskUser()).increaseUsedMachines();

                this.userControls.get(this.preemptionEntries.get(indexControle).preemptedTaskUser()).increaseTaskDemand();
                this.userControls.get(this.preemptionEntries.get(indexControle).preemptedTaskUser()).decreaseUsedMachines();

                this.slaveToControl(maq).setAsBlocked();

                this.tasksInWaiting.remove(i);
                this.preemptionEntries.remove(j);
                break;
            }
        }
    }

    private Optional<Tarefa> firstAvailableTask() {
        return this.tarefas.stream()
                .findFirst()
                .map(this::popTaskFromQueue);
    }
}
