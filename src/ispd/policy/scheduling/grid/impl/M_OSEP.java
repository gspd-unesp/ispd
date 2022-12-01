package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.PreemptionEntry;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserProcessingControl;

import java.util.ArrayList;
import java.util.List;

@Policy
public class M_OSEP extends GridSchedulingPolicy {
    private final List<SlaveControl> controleEscravos = new ArrayList<>();
    private final List<Tarefa> esperaTarefas = new ArrayList<>();
    private final List<PreemptionEntry> controlePreempcao = new ArrayList<>();
    private final List<List> processadorEscravos = new ArrayList<>();
    private final List<UserProcessingControl> status = new ArrayList<>();
    private Tarefa tarefaSelec = null;
    private int contadorEscravos = 0;

    public M_OSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);

        for (final var user : this.metricaUsuarios.getUsuarios()) {
            final var comp = this.metricaUsuarios.getPoderComputacional(user);
            this.status.add(new UserProcessingControl(user, this.escravos));
        }

        for (int i = 0; i < this.escravos.size(); i++) {
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
        final var task = this.escalonarTarefa();
        this.tarefaSelec = task;

        if (task != null) {
            final var resource = this.escalonarRecurso();
            if (resource != null) {
                task.setLocalProcessamento(resource);
                task.setCaminho(this.escalonarRota(resource));
                //Verifica se não é caso de preempção
                if (!this.controleEscravos.get(this.escravos.indexOf(resource)).isPreempted()) {
                    this.status.get(this.metricaUsuarios.getUsuarios().indexOf(task.getProprietario()))
                            .increaseUsedProcessingPower(resource.getPoderComputacional());
                    this.mestre.sendTask(task);
                } else {
                    final int resourceIndex = this.escravos.indexOf(resource);
                    this.esperaTarefas.add(task);
                    this.controlePreempcao.add(new PreemptionEntry(
                            ((Tarefa) this.processadorEscravos.get(resourceIndex).get(0)).getProprietario(),
                            ((Tarefa) this.processadorEscravos.get(resourceIndex).get(0)).getIdentificador(),
                            task.getProprietario(),
                            task.getIdentificador()
                    ));
                    this.status.get(this.metricaUsuarios.getUsuarios().indexOf(((Tarefa) this.processadorEscravos.get(resourceIndex).get(0)).getProprietario()))
                            .decreaseUsedProcessingPower(resource.getPoderComputacional());
                }
            } else {
                this.tarefas.add(task);
                this.tarefaSelec = null;
            }
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
            if ((this.status.get(i).currentlyUsedProcessingPower() < this.status.get(i).getOwnedMachinesProcessingPower()) && demanda) {

                if (difUsuarioMinimo == (double) -1) {
                    difUsuarioMinimo =
                            this.status.get(i).getOwnedMachinesProcessingPower() - this.status.get(i).currentlyUsedProcessingPower();
                    indexUsuarioMinimo = i;
                } else {
                    if (difUsuarioMinimo < this.status.get(i).getOwnedMachinesProcessingPower() - this.status.get(i).currentlyUsedProcessingPower()) {
                        difUsuarioMinimo =
                                this.status.get(i).getOwnedMachinesProcessingPower() - this.status.get(i).currentlyUsedProcessingPower();
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

        if (!this.tarefas.isEmpty()) {
            return this.tarefas.remove(0);
        } else {
            return null;
        }

    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        final var maq = (CS_Processamento) tarefa.getLocalProcessamento();
        final int indexUser =
                this.metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
        this.status.get(indexUser)
                .decreaseUsedProcessingPower(maq.getPoderComputacional());
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
                if (this.processadorEscravos.get(i).size() == 1 && !this.controleEscravos.get(i).isPreempted()) {
                    this.controleEscravos.get(i).setAsOccupied();
                } else if (this.processadorEscravos.get(i).isEmpty() && !this.controleEscravos.get(i).isPreempted()) {
                    escalona = true;
                    this.controleEscravos.get(i).setAsFree();
                } else if (this.controleEscravos.get(i).isPreempted()) {
                    this.controleEscravos.get(i).setAsBlocked();
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
        final var maq = (CS_Processamento) tarefa.getLocalProcessamento();
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
                if (this.esperaTarefas.get(i).getProprietario().equals(this.controlePreempcao.get(indexControle).scheduledTaskUser()) && this.esperaTarefas.get(i).getIdentificador() == this.controlePreempcao.get(j).scheduledTaskId()) {
                    final int indexUser =
                            this.metricaUsuarios.getUsuarios().indexOf(this.controlePreempcao.get(indexControle).scheduledTaskUser());
                    this.status.get(indexUser).increaseUsedProcessingPower(maq.getPoderComputacional());
                    this.mestre.sendTask(this.esperaTarefas.get(i));
                    this.esperaTarefas.remove(i);
                    this.controlePreempcao.remove(j);
                    break;
                }
            }

        } else {
            this.mestre.executeScheduling();
        }
    }

    @Override
    public CS_Processamento escalonarRecurso() {

        //Buscando recurso livre
        CS_Processamento selec = null;

        for (int i = 0; i < this.escravos.size(); i++) {

            if (this.filaEscravo.get(i).isEmpty() && this.processadorEscravos.get(i).isEmpty() && this.controleEscravos.get(i).isFree()) {//Garantir que o escravo está de fato livre e que não há nenhuma tarefa em trânsito para o escravo
                if (selec == null) {

                    selec = this.escravos.get(i);

                } else if (Math.abs(this.escravos.get(i).getPoderComputacional() - this.tarefaSelec.getTamProcessamento()) < Math.abs(selec.getPoderComputacional() - this.tarefaSelec.getTamProcessamento())) {//Best Fit

                    selec = this.escravos.get(i);

                }

            }

        }

        if (selec != null) {

            this.controleEscravos.get(this.escravos.indexOf(selec)).setAsBlocked();//Inidcar que uma tarefa será enviada e que , portanto , este escravo deve ser bloqueada até a próxima atualização

            return selec;

        }

        String usermax = null;
        double diff = -1;

        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {

            if (this.status.get(i).currentlyUsedProcessingPower() > this.status.get(i).getOwnedMachinesProcessingPower() && !this.metricaUsuarios.getUsuarios().get(i).equals(this.tarefaSelec.getProprietario())) {

                if (diff == (double) -1) {

                    usermax = this.metricaUsuarios.getUsuarios().get(i);
                    diff = this.status.get(i).currentlyUsedProcessingPower() - this.status.get(i).getOwnedMachinesProcessingPower();

                } else {

                    if (this.status.get(i).currentlyUsedProcessingPower() - this.status.get(i).getOwnedMachinesProcessingPower() > diff) {

                        usermax = this.metricaUsuarios.getUsuarios().get(i);
                        diff = this.status.get(i).currentlyUsedProcessingPower() - this.status.get(i).getOwnedMachinesProcessingPower();

                    }

                }

            }

        }

        int index = -1;
        if (usermax != null) {

            for (int i = 0; i < this.escravos.size(); i++) {
                if (this.processadorEscravos.get(i).size() == 1 && this.controleEscravos.get(i).isOccupied() && this.filaEscravo.get(i).isEmpty() && ((Tarefa) this.processadorEscravos.get(i).get(0)).getProprietario().equals(usermax)) {
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
            selec = this.escravos.get(index);
            //Verifica se vale apena fazer preempção
            int index_selec = this.escravos.indexOf(selec);
            final Tarefa tar =
                    (Tarefa) this.processadorEscravos.get(index_selec).get(0);

            final int indexUserEscravo =
                    this.metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
            final int indexUserEspera =
                    this.metricaUsuarios.getUsuarios().indexOf(this.tarefaSelec.getProprietario());

            //Penalidade do usuário dono da tarefa em execução, caso a
            // preempção seja feita
            final double penalidaUserEscravoPosterior =
                    (this.status.get(indexUserEscravo).currentlyUsedProcessingPower() - selec.getPoderComputacional() - this.status.get(indexUserEscravo).getOwnedMachinesProcessingPower()) / this.status.get(indexUserEscravo).getOwnedMachinesProcessingPower();

            //Penalidade do usuário dono da tarefa slecionada para ser posta
            // em execução, caso a preempção seja feita
            final double penalidaUserEsperaPosterior =
                    (this.status.get(indexUserEspera).currentlyUsedProcessingPower() + selec.getPoderComputacional() - this.status.get(indexUserEspera).getOwnedMachinesProcessingPower()) / this.status.get(indexUserEspera).getOwnedMachinesProcessingPower();

            //Caso o usuário em espera apresente menor penalidade e os donos
            // das tarefas em execução e em espera não sejam a mesma pessoa ,
            // e , ainda, o escravo esteja executando apenas uma tarefa
            if (penalidaUserEscravoPosterior <= penalidaUserEsperaPosterior || (penalidaUserEscravoPosterior > 0 && penalidaUserEsperaPosterior < 0)) {
                index_selec = this.escravos.indexOf(selec);
                this.controleEscravos.get(this.escravos.indexOf(selec)).setAsPreempted();
                this.mestre.sendMessage((Tarefa) this.processadorEscravos.get(index_selec).get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
                return selec;
            }
        }
        return null;
    }

    @Override
    public Double getTempoAtualizar() {
        return 15.0;
    }

}