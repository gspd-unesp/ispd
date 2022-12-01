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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Policy
public class OSEP extends AbstractOSEP {
    private final List<SlaveControl> controleEscravos = new ArrayList<>();
    private final List<Tarefa> esperaTarefas = new ArrayList<>();
    private final List<PreemptionEntry> controlePreempcao = new ArrayList<>();
    private final Map<String, UserProcessingControl> status = new HashMap<>();
    private Tarefa tarefaSelec = null;
    private int contadorEscravos = 0;

    @Override
    public void iniciar() {
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);

        for (final var user : this.metricaUsuarios.getUsuarios()) {
            this.status.put(user, new UserProcessingControl(user,
                    this.escravos));
        }

        for (final var ignored : this.escravos) {
            this.controleEscravos.add(new SlaveControl());
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

        if (task == null) {
            return;
        }

        this.tarefaSelec = task;
        final var userStatus = this.status.get(task.getProprietario());
        final var resource = this.escalonarRecurso();

        if (resource == null) {
            this.tarefas.add(task);
            this.tarefaSelec = null;
            return;
        }

        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));

        //Verifica se não é caso de preempção
        if (this.controleEscravos.get(this.escravos.indexOf(resource)).isFree()) {
            userStatus.decreaseTaskDemand();
            userStatus.increaseUsedMachines();
            this.controleEscravos.get(this.escravos.indexOf(resource)).setAsBlocked();
            this.mestre.sendTask(task);
        } else if (this.controleEscravos.get(this.escravos.indexOf(resource)).isOccupied()) {
            final int resourceIndex = this.escravos.indexOf(resource);
            this.esperaTarefas.add(task);
            this.controlePreempcao.add(new PreemptionEntry(
                    this.firstTaskIn(resourceIndex).getProprietario(),
                    this.firstTaskIn(resourceIndex).getIdentificador(),
                    task.getProprietario(),
                    task.getIdentificador())
            );
            this.controleEscravos
                    .get(this.escravos.indexOf(resource))
                    .setAsBlocked();
        }
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        String user;
        //Buscando recurso livre
        CS_Processamento selec = null;

        for (int i = 0; i < this.escravos.size(); i++) {

            if (this.controleEscravos.get(i).isFree()) {
                //Garantir que o escravo está de fato livre e que não há
                // nenhuma tarefa em trânsito para o escravo
                selec = this.escravos.get(i);
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
            if (this.status.get(user).currentlyUsedMachineCount() > this.status.get(user).getOwnedMachinesCount() && !user.equals(this.tarefaSelec.getProprietario())) {

                if (diff == -1) {
                    usermax = this.metricaUsuarios.getUsuarios().get(i);
                    diff = this.status.get(user).currentlyUsedMachineCount() - this.status.get(user).getOwnedMachinesCount();
                } else {
                    if (this.status.get(user).currentlyUsedMachineCount() - this.status.get(user).getOwnedMachinesCount() > diff) {
                        usermax = user;
                        diff = this.status.get(user).currentlyUsedMachineCount() - this.status.get(user).getOwnedMachinesCount();
                    }
                }
            }
        }

        int index = -1;
        if (usermax != null) {
            for (int i = 0; i < this.escravos.size(); i++) {
                if (this.controleEscravos.get(i).isOccupied() && this.firstTaskIn(i).getProprietario().equals(usermax)) {
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

    private Tarefa firstTaskIn(final int index) {
        return this.controleEscravos.get(index).firstTaskInProcessing();
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
        return this.status.values().stream()
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
        final var uc = this.status.get(tarefa.getProprietario());

        uc.decreaseUsedMachines();
        final int index = this.escravos.indexOf(maq);
        this.controleEscravos.get(index).setAsFree();
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        super.resultadoAtualizar(mensagem);
        final int index = this.escravos.indexOf(mensagem.getOrigem());
        this.controleEscravos.get(index)
                .setTasksInProcessing(mensagem.getProcessadorEscravo());
        this.contadorEscravos++;
        if (this.contadorEscravos == this.escravos.size()) {
            boolean escalona = false;
            for (int i = 0; i < this.escravos.size(); i++) {
                if (this.controleEscravos.get(i).isBlocked()) {
                    this.controleEscravos.get(i).setAsUncertain();
                }
                if (this.controleEscravos.get(i).isUncertain()) {
                    if (this.controleEscravos.get(i).hasTasksInProcessing()) {
                        this.controleEscravos.get(i).setAsOccupied();
                    } else {
                        this.controleEscravos.get(i).setAsFree();
                        escalona = true;
                    }
                }
            }
            this.contadorEscravos = 0;
            if (!this.tarefas.isEmpty() && escalona) {
                this.mestre.executeScheduling();
            }
        }
    }

    @Override
    public void adicionarTarefa(final Tarefa tarefa) {
        super.adicionarTarefa(tarefa);
        final CS_Processamento maq = tarefa.getCSLProcessamento();
        final var estadoUser = this.status.get(tarefa.getProprietario());

        if (tarefa.getLocalProcessamento() == null) {
            this.mestre.executeScheduling();
            estadoUser.increaseTaskDemand();
            return;
        }

        //Em caso de preempção, é procurada a tarefa correspondente para ser
        // enviada ao escravo agora desocupado
        int j;
        int indexControle = -1;
        for (j = 0; j < this.controlePreempcao.size(); j++) {
            if (this.controlePreempcao.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.controlePreempcao.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                indexControle = j;
                break;
            }
        }

        for (int i = 0; i < this.esperaTarefas.size(); i++) {
            if (this.esperaTarefas.get(i).getProprietario().equals(this.controlePreempcao.get(indexControle).scheduledTaskUser()) && this.esperaTarefas.get(i).getIdentificador() == this.controlePreempcao.get(j).scheduledTaskId()) {

                this.mestre.sendTask(this.esperaTarefas.get(i));

                this.status.get(this.controlePreempcao.get(indexControle).scheduledTaskUser()).increaseUsedMachines();

                this.status.get(this.controlePreempcao.get(indexControle).preemptedTaskUser()).increaseTaskDemand();
                this.status.get(this.controlePreempcao.get(indexControle).preemptedTaskUser()).decreaseUsedMachines();

                this.controleEscravos.get(this.escravos.indexOf(maq)).setAsBlocked();

                this.esperaTarefas.remove(i);
                this.controlePreempcao.remove(j);
                break;
            }
        }

    }

    private Optional<Tarefa> firstAvailableTask() {
        return this.tarefas.stream()
                .findFirst()
                .map(this::popTaskFromQueue);
    }

    private int getSelectedIndex() {
        long maximalExcess = -1;
        int selectedIndex = -1;

        //Encontrar o usuário que está mais abaixo da sua propriedade
        final var users = this.metricaUsuarios.getUsuarios();
        for (int i = 0; i < users.size(); i++) {
            final var user = this.status.get(users.get(i));

            //Caso existam tarefas do usuário corrente e ele esteja com uso
            // menor que sua posse
            if (this.isUserEligibleForTask(user)) {
                if (maximalExcess == -1 || user.excessMachines() > maximalExcess) {
                    maximalExcess = user.excessMachines();
                    selectedIndex = i;
                }
            }
        }
        return selectedIndex;
    }
}
