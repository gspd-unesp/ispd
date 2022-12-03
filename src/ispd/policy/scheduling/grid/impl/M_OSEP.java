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

@Policy
public class M_OSEP extends AbstractOSEP<UserProcessingControl> {
    private final List<Tarefa> tasksInWaiting = new ArrayList<>();
    private final List<PreemptionEntry> preemptionEntries = new ArrayList<>();
    private Tarefa selectedTask = null;
    private int slaveCounter = 0;

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
        final var sc = this.slaveControls.get(resource);

        if (sc.isPreempted()) {
            this.tasksInWaiting.add(task);

            this.preemptionEntries.add(new PreemptionEntry(sc.firstTaskInProcessing(), task));

            this.userControls.get(sc.firstTaskInProcessing().getProprietario())
                    .decreaseUsedProcessingPower(resource.getPoderComputacional());

        } else {
            final var userId = task.getProprietario();
            this.userControls.get(userId)
                    .increaseUsedProcessingPower(resource.getPoderComputacional());
            this.mestre.sendTask(task);
        }

    }

    @Override
    public Tarefa escalonarTarefa() {
        //Usuários com maior diferença entre uso e posse terão preferência
        double difUsuarioMinimo = -1;
        int indexUsuarioMinimo = -1;
        //Encontrar o usuário que está mais abaixo da sua propriedade
        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            final var userId = this.metricaUsuarios.getUsuarios().get(i);

            //Verificar se existem tarefas do usuário corrente
            boolean demanda = false;

            for (final Tarefa tarefa : this.tarefas) {
                if (tarefa.getProprietario().equals(userId)) {
                    demanda = true;
                    break;
                }
            }

            //Caso existam tarefas do usuário corrente e ele esteja com uso
            // menor que sua posse
            final var uc = this.userControls.get(userId);

            if ((uc.currentlyUsedProcessingPower() < uc.getOwnedMachinesProcessingPower()) && demanda) {

                if (difUsuarioMinimo == (double) -1) {
                    difUsuarioMinimo =
                            uc.getOwnedMachinesProcessingPower() - uc.currentlyUsedProcessingPower();
                    indexUsuarioMinimo = i;
                } else {
                    if (difUsuarioMinimo < uc.getOwnedMachinesProcessingPower() - uc.currentlyUsedProcessingPower()) {
                        difUsuarioMinimo =
                                uc.getOwnedMachinesProcessingPower() - uc.currentlyUsedProcessingPower();
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
        this.userControls.get(tarefa.getProprietario())
                .decreaseUsedProcessingPower(maq.getPoderComputacional());
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        super.resultadoAtualizar(mensagem);

        this.slaveControls.get((CS_Processamento) mensagem.getOrigem())
                .setTasksInProcessing(mensagem.getProcessadorEscravo());

        this.slaveCounter++;
        if (this.slaveCounter != this.escravos.size()) {
            return;
        }
        this.slaveCounter = 0;


        // TODO: Separate updateAndShouldSchedule function
        final var result = this.slaveControls.values().stream()
                .map(M_OSEP::updateAndShouldSchedule)
                .toList();

        final var shouldSchedule = result.stream()
                .anyMatch(Boolean::booleanValue);

        if (!this.tarefas.isEmpty() && shouldSchedule) {
            this.mestre.executeScheduling();
        }
    }

    private static boolean updateAndShouldSchedule(final SlaveControl sc) {
        if (sc.isPreempted()) {
            sc.setAsBlocked();
            return false;
        }

        if (sc.hasTasksInProcessing()) {
            sc.setAsOccupied();
            return false;
        }

        if (!sc.hasTasksInProcessing()) {
            sc.setAsFree();
            return true;
        }

        return false;
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
            final var stu =
                    this.preemptionEntries.get(indexControle).scheduledTaskUser();

            if (this.tasksInWaiting.get(i).getProprietario().equals(stu) && this.tasksInWaiting.get(i).getIdentificador() == this.preemptionEntries.get(j).scheduledTaskId()) {
                this.userControls.get(stu).increaseUsedProcessingPower(maq.getPoderComputacional());
                this.mestre.sendTask(this.tasksInWaiting.get(i));
                this.tasksInWaiting.remove(i);
                this.preemptionEntries.remove(j);
                break;
            }
        }

    }

    @Override
    public CS_Processamento escalonarRecurso() {
        final var selec = this.escravos.stream()
                .filter(this::isMachineAvailable)
                .min(Comparator.comparingDouble(
                        s -> this.fitForSelectedTask(s, this.selectedTask)))
                .orElse(null);

        if (selec != null) {
            this.slaveControls.get(selec).setAsBlocked();
            return selec;
        }

        return this.preemptedMachine();
    }

    private CS_Processamento preemptedMachine() {
        final var bestUser = this.userControls.values().stream()
                .filter(uc2 -> uc2.isOwnerOf(this.selectedTask))
                .filter(UserProcessingControl::hasExcessProcessingPower)
                .max(Comparator.comparingDouble(UserProcessingControl::excessProcessingPower));

        if (bestUser.isEmpty()) {
            return null;
        }

        final var machine = this.escravos.stream()
                .filter(this::isMachineOccupied)
                .filter(m -> bestUser.get().isOwnerOf(this.taskToPreemptIn(m)))
                .min(Comparator.comparingDouble(CS_Processamento::getPoderComputacional))
                .orElse(null);

        if (machine == null)
            return null;

        //Fazer a preempção
        //Verifica se vale apena fazer preempção
        final Tarefa tar =
                this.taskToPreemptIn(machine);

        //Penalidade do usuário dono da tarefa em execução, caso a
        // preempção seja feita
        final var uc1 = this.userControls.get(tar.getProprietario());
        final double penalidaUserEscravoPosterior =
                uc1.penaltyWithProcessing(-machine.getPoderComputacional());

        //Penalidade do usuário dono da tarefa slecionada para ser posta
        // em execução, caso a preempção seja feita
        final var uc =
                this.userControls.get(this.selectedTask.getProprietario());
        final double penalidaUserEsperaPosterior =
                uc.penaltyWithProcessing(machine.getPoderComputacional());

        //Caso o usuário em espera apresente menor penalidade e os donos
        // das tarefas em execução e em espera não sejam a mesma pessoa ,
        // e , ainda, o escravo esteja executando apenas uma tarefa
        if (penalidaUserEscravoPosterior <= penalidaUserEsperaPosterior || (penalidaUserEscravoPosterior > 0 && penalidaUserEsperaPosterior < 0)) {
            this.slaveControls.get(machine).setAsPreempted();
            this.mestre.sendMessage(
                    tar,
                    machine,
                    Mensagens.DEVOLVER_COM_PREEMPCAO
            );
            return machine;
        }

        return null;
    }

    private boolean isMachineOccupied(final CS_Processamento machine) {
        final var sc = this.slaveControls.get(machine);
        return sc.hasTasksInProcessing() && sc.isOccupied();
    }

    private Tarefa taskToPreemptIn(final CS_Processamento machine) {
        return this.slaveControls.get(machine).firstTaskInProcessing();
    }

    private boolean isMachineAvailable(final CS_Processamento slave) {
        final var sc = this.slaveControls.get(slave);
        return !sc.hasTasksInProcessing() && sc.isFree();
    }

    private double fitForSelectedTask(
            final CS_Processamento s, final Tarefa task) {
        return Math.abs(s.getPoderComputacional() - task.getTamProcessamento());
    }
}
