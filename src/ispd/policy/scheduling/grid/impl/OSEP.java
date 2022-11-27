package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.PreemptionControl;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Policy
public class OSEP extends GridSchedulingPolicy {
    private final List<SlaveControl> controleEscravos = new ArrayList<>();
    private final List<Tarefa> esperaTarefas = new ArrayList<>();
    private final List<PreemptionControl> controlePreempcao =
            new ArrayList<>();
    private final List<List> processadorEscravos = new ArrayList<>();
    private Tarefa tarefaSelec = null;
    private HashMap<String, UserControl> status = null;
    private int contadorEscravos = 0;

    public OSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);
        //Escalonamento quando
        // chegam tarefas e quando tarefas são concluídas
        this.status = new HashMap<>();

        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            //Objetos de controle de uso e cota para cada um dos usuários
            String user = this.metricaUsuarios.getUsuarios().get(i);
            double perfShare =
                    this.metricaUsuarios.getPoderComputacional(this.metricaUsuarios.getUsuarios().get(i));
            java.util.Collection<? extends CS_Processamento> slaves =
                    OSEP.this.escravos;
            this.status.put(this.metricaUsuarios.getUsuarios().get(i),
                    new UserControl(user, perfShare, slaves));
        }

        for (final CS_Processamento escravo : this.escravos) {//Contadores para
            // lidar com a dinamicidade dos dados
            String ID = escravo.getId();
            this.controleEscravos.add(new SlaveControl());
            this.filaEscravo.add(new ArrayList<Tarefa>());
            this.processadorEscravos.add(new ArrayList<Tarefa>());
        }
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        final Tarefa trf = this.escalonarTarefa();
        if (trf != null) {
            this.tarefaSelec = trf;
            final UserControl estado = this.status.get(trf.getProprietario());
            final CS_Processamento rec = this.escalonarRecurso();
            if (rec != null) {
                trf.setLocalProcessamento(rec);
                trf.setCaminho(this.escalonarRota(rec));
                //Verifica se não é caso de preempção
                if (this.controleEscravos.get(this.escravos.indexOf(rec)).isFree()) {

                    estado.decreaseTaskDemand();
                    estado.increaseAvailableMachines();

                    this.controleEscravos.get(this.escravos.indexOf(rec)).setAsBlocked();
                    this.mestre.sendTask(trf);

                } else {

                    if (this.controleEscravos.get(this.escravos.indexOf(rec)).isOccupied()) {
                        final int index_rec = this.escravos.indexOf(rec);
                        this.esperaTarefas.add(trf);
                        this.controlePreempcao.add(new PreemptionControl(((Tarefa) this.processadorEscravos.get(index_rec).get(0)).getProprietario(), ((Tarefa) this.processadorEscravos.get(index_rec).get(0)).getIdentificador(), trf.getProprietario(), trf.getIdentificador()));
                        this.controleEscravos.get(this.escravos.indexOf(rec)).setAsBlocked();
                    }
                }

                for (int i = 0; i < this.escravos.size(); i++) {
                    if (this.processadorEscravos.get(i).size() > 1) {
                        System.out.printf("Escravo %s executando %d\n",
                                this.escravos.get(i).getId(),
                                this.processadorEscravos.get(i).size());
                        System.out.println("PROBLEMA1");
                    }
                    if (!this.filaEscravo.get(i).isEmpty()) {
                        System.out.println("Tem Fila");
                    }
                }
            } else {
                this.tarefas.add(trf);
                this.tarefaSelec = null;
            }
        }

    }

    @Override
    public Tarefa escalonarTarefa() {
        //Usuários com maior diferença entre uso e posse terão preferência
        long difUsuarioMinimo = -1;
        int indexUsuarioMinimo = -1;
        String user;
        //Encontrar o usuário que está mais abaixo da sua propriedade
        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            user = this.metricaUsuarios.getUsuarios().get(i);

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
                    if (indexTarefa == -1) {
                        indexTarefa = i;
                        break;
                    }
                }
            }

            if (indexTarefa != -1) {
                return this.tarefas.remove(indexTarefa);
            }

        }

        if (!this.tarefas.isEmpty()) {
            return this.tarefas.remove(0);
        } else {
            return null;
        }

    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        final CS_Processamento maq =
                (CS_Processamento) tarefa.getLocalProcessamento();
        final UserControl estado = this.status.get(tarefa.getProprietario());

        estado.decreaseAvailableMachines();
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
                    if (this.processadorEscravos.size() > 1) {
                        System.out.println("Houve Fila");
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
        final CS_Processamento maq =
                (CS_Processamento) tarefa.getLocalProcessamento();
        final UserControl estadoUser =
                this.status.get(tarefa.getProprietario());
        //Em caso de preempção, é procurada a tarefa correspondente para ser
        // enviada ao escravo agora desocupado
        if (tarefa.getLocalProcessamento() != null) {

            int j;
            int indexControle = -1;
            for (j = 0; j < this.controlePreempcao.size(); j++) {
                if (this.controlePreempcao.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.controlePreempcao.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                    indexControle = j;
                    break;
                }
            }

            for (int i = 0; i < this.esperaTarefas.size(); i++) {
                if (this.esperaTarefas.get(i).getProprietario().equals(this.controlePreempcao.get(indexControle).allocatedTaskUser()) && this.esperaTarefas.get(i).getIdentificador() == this.controlePreempcao.get(j).allocatedTaskId()) {

                    this.mestre.sendTask(this.esperaTarefas.get(i));

                    this.status.get(this.controlePreempcao.get(indexControle).allocatedTaskUser()).increaseAvailableMachines();

                    this.status.get(this.controlePreempcao.get(indexControle).preemptedTaskUser()).increaseTaskDemand();
                    this.status.get(this.controlePreempcao.get(indexControle).preemptedTaskUser()).decreaseAvailableMachines();

                    this.controleEscravos.get(this.escravos.indexOf(maq)).setAsBlocked();

                    this.esperaTarefas.remove(i);
                    this.controlePreempcao.remove(j);
                    break;
                }
            }

        } else {
            this.mestre.executeScheduling();
            estadoUser.increaseTaskDemand();
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
                if (selec == null) {

                    selec = this.escravos.get(i);
                    break;

                }

            }

        }

        if (selec != null) {

            //controleEscravos.get(escravos.indexOf(selec)).setBloqueado();
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
            selec = this.escravos.get(index);
            final int index_selec = this.escravos.indexOf(selec);
            //controleEscravos.get(escravos.indexOf(selec)).setBloqueado();
            this.mestre.sendMessage((Tarefa) this.processadorEscravos.get(index_selec).get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
            return selec;
        }
        return null;
    }

    @Override
    public Double getTempoAtualizar() {
        return 15.0;
    }

}
