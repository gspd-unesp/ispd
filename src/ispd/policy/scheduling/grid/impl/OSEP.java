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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Policy
public class OSEP extends AbstractOSEP {
    private final List<SlaveControl> controleEscravos = new ArrayList<>();
    private final List<Tarefa> esperaTarefas = new ArrayList<>();
    private final List<PreemptionEntry> controlePreempcao =
            new ArrayList<>();
    private final List<List<Tarefa>> processadorEscravos = new ArrayList<>();
    private final Map<String, UserProcessingControl> status = new HashMap<>();
    private Tarefa tarefaSelec = null;
    private int contadorEscravos = 0;

    public OSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

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
            this.processadorEscravos.add(new ArrayList<>());
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
                    ((Tarefa) this.processadorEscravos.get(resourceIndex).get(0)).getProprietario(),
                    ((Tarefa) this.processadorEscravos.get(resourceIndex).get(0)).getIdentificador(),
                    task.getProprietario(),
                    task.getIdentificador())
            );
            this.controleEscravos
                    .get(this.escravos.indexOf(resource))
                    .setAsBlocked();
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        //Usuários com maior diferença entre uso e posse terão preferência
        long difUsuarioMinimo = -1;
        int indexUsuarioMinimo = -1;

        //Encontrar o usuário que está mais abaixo da sua propriedade
        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            final var user = this.metricaUsuarios.getUsuarios().get(i);

            //Caso existam tarefas do usuário corrente e ele esteja com uso
            // menor que sua posse
            if ((this.status.get(user).currentlyAvailableMachineCount() < this.status.get(user).getOwnedMachinesCount()) && this.status.get(user).currentTaskDemand() > 0) {
                if (difUsuarioMinimo == -1) {
                    difUsuarioMinimo =
                            this.status.get(user).getOwnedMachinesCount() - this.status.get(user).currentlyAvailableMachineCount();
                    indexUsuarioMinimo = i;
                } else {
                    if (difUsuarioMinimo < this.status.get(user).getOwnedMachinesCount() - this.status.get(user).currentlyAvailableMachineCount()) {
                        difUsuarioMinimo =
                                this.status.get(user).getOwnedMachinesCount() - this.status.get(user).currentlyAvailableMachineCount();
                        indexUsuarioMinimo = i;
                    }
                }
            }
        }

        if (indexUsuarioMinimo != -1) {
            int indexTarefa = -1;

            for (int i = 0; i < this.tarefas.size(); i++) {
                if (this.tarefas.get(i).getProprietario().equals(this.metricaUsuarios.getUsuarios().get(indexUsuarioMinimo))) {
                    indexTarefa = i;
                    break;
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
        final var uc = this.status.get(tarefa.getProprietario());

        uc.decreaseUsedMachines();
        final int index = this.escravos.indexOf(maq);
        this.controleEscravos.get(index).setAsFree();
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        super.resultadoAtualizar(mensagem);
        final int index = this.escravos.indexOf(mensagem.getOrigem());
        this.processadorEscravos.set(index, mensagem.getProcessadorEscravo());
        this.contadorEscravos++;
        if (this.contadorEscravos == this.escravos.size()) {
            boolean escalona = false;
            for (int i = 0; i < this.escravos.size(); i++) {
                if (this.controleEscravos.get(i).isBlocked()) {
                    this.controleEscravos.get(i).setAsUncertain();
                }
                if (this.controleEscravos.get(i).isUncertain()) {
                    if (this.processadorEscravos.get(i).isEmpty()) {
                        this.controleEscravos.get(i).setAsFree();
                        escalona = true;
                    }
                    if (this.processadorEscravos.size() == 1) {
                        this.controleEscravos.get(i).setAsOccupied();
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
            if (this.status.get(user).currentlyAvailableMachineCount() > this.status.get(user).getOwnedMachinesCount() && !user.equals(this.tarefaSelec.getProprietario())) {

                if (diff == -1) {
                    usermax = this.metricaUsuarios.getUsuarios().get(i);
                    diff = this.status.get(user).currentlyAvailableMachineCount() - this.status.get(user).getOwnedMachinesCount();
                } else {
                    if (this.status.get(user).currentlyAvailableMachineCount() - this.status.get(user).getOwnedMachinesCount() > diff) {
                        usermax = user;
                        diff = this.status.get(user).currentlyAvailableMachineCount() - this.status.get(user).getOwnedMachinesCount();
                    }
                }
            }
        }

        int index = -1;
        if (usermax != null) {
            for (int i = 0; i < this.escravos.size(); i++) {
                if (this.controleEscravos.get(i).isOccupied() && ((Tarefa) this.processadorEscravos.get(i).get(0)).getProprietario().equals(usermax)) {
                    index = i;
                    break;
                }
            }
        }

        //Fazer a preempção
        if (index != -1) {
            final CS_Processamento cs_processamento = this.escravos.get(index);
            final int index_selec = this.escravos.indexOf(cs_processamento);
            this.mestre.sendMessage((Tarefa) this.processadorEscravos.get(index_selec).get(0), cs_processamento, Mensagens.DEVOLVER_COM_PREEMPCAO);
            return cs_processamento;
        }

        return null;
    }

    @Override
    public Double getTempoAtualizar() {
        return 15.0;
    }
}
