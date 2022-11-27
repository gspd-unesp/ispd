package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.M_OSEP_StatusUser;
import ispd.policy.scheduling.grid.impl.util.PreemptionControl;
import ispd.policy.scheduling.grid.impl.util.SlaveStatusControl;

import java.util.ArrayList;
import java.util.List;

@Policy
public class M_OSEP extends GridSchedulingPolicy {
    private final List<SlaveStatusControl> controleEscravos;
    private final List<Tarefa> esperaTarefas;
    private final List<PreemptionControl> controlePreempcao;
    private final List<List> processadorEscravos;
    private Tarefa tarefaSelec = null;
    private List<M_OSEP_StatusUser> status = null;
    private int contadorEscravos = 0;

    public M_OSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.controleEscravos = new ArrayList<>();
        this.esperaTarefas = new ArrayList<>();
        this.controlePreempcao = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
        this.processadorEscravos = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);//Escalonamento quando
        // chegam tarefas e quando tarefas são concluídas
        this.status = new ArrayList<>();

        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            //Objetos de controle de uso e cota para cada um dos usuários
            this.status.add(new M_OSEP_StatusUser(this.metricaUsuarios.getUsuarios().get(i), this.metricaUsuarios.getPoderComputacional(this.metricaUsuarios.getUsuarios().get(i)), M_OSEP.this.escravos));
        }

        for (int i = 0; i < this.escravos.size(); i++) {//Contadores para
            // lidar com a dinamicidade dos dados
            this.controleEscravos.add(new SlaveStatusControl());
            this.filaEscravo.add(new ArrayList<Tarefa>());
            this.processadorEscravos.add(new ArrayList<Tarefa>());
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
            if ((this.status.get(i).GetUso() < this.status.get(i).GetCota()) && demanda) {

                if (difUsuarioMinimo == (double) -1) {
                    difUsuarioMinimo =
                            this.status.get(i).GetCota() - this.status.get(i).GetUso();
                    indexUsuarioMinimo = i;
                } else {
                    if (difUsuarioMinimo < this.status.get(i).GetCota() - this.status.get(i).GetUso()) {
                        difUsuarioMinimo =
                                this.status.get(i).GetCota() - this.status.get(i).GetUso();
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

            if (this.status.get(i).GetUso() > this.status.get(i).GetCota() && !this.metricaUsuarios.getUsuarios().get(i).equals(this.tarefaSelec.getProprietario())) {

                if (diff == (double) -1) {

                    usermax = this.metricaUsuarios.getUsuarios().get(i);
                    diff = this.status.get(i).GetUso() - this.status.get(i).GetCota();

                } else {

                    if (this.status.get(i).GetUso() - this.status.get(i).GetCota() > diff) {

                        usermax = this.metricaUsuarios.getUsuarios().get(i);
                        diff = this.status.get(i).GetUso() - this.status.get(i).GetCota();

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
                    (this.status.get(indexUserEscravo).GetUso() - selec.getPoderComputacional() - this.status.get(indexUserEscravo).GetCota()) / this.status.get(indexUserEscravo).GetCota();

            //Penalidade do usuário dono da tarefa slecionada para ser posta
            // em execução, caso a preempção seja feita
            final double penalidaUserEsperaPosterior =
                    (this.status.get(indexUserEspera).GetUso() + selec.getPoderComputacional() - this.status.get(indexUserEspera).GetCota()) / this.status.get(indexUserEspera).GetCota();

            //Caso o usuário em espera apresente menor penalidade e os donos
            // das tarefas em execução e em espera não sejam a mesma pessoa ,
            // e , ainda, o escravo esteja executando apenas uma tarefa
            if (penalidaUserEscravoPosterior <= penalidaUserEsperaPosterior || (penalidaUserEscravoPosterior > 0 && penalidaUserEsperaPosterior < 0)) {

                //System.out.println("Preempção: Tarefa " + ((Tarefa) selec
                // .getInformacaoDinamicaProcessador().get(0))
                // .getIdentificador() + " do user " + ((Tarefa) selec
                // .getInformacaoDinamicaProcessador().get(0))
                // .getProprietario() + " <=> " + tarefaSelec
                // .getIdentificador() + " do user " + tarefaSelec
                // .getProprietario());
                index_selec = this.escravos.indexOf(selec);
                this.controleEscravos.get(this.escravos.indexOf(selec)).setAsPreempted();
                this.mestre.sendMessage((Tarefa) this.processadorEscravos.get(index_selec).get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
                return selec;
            }
        }
        return null;
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        final Tarefa trf = this.escalonarTarefa();
        this.tarefaSelec = trf;
        if (trf != null) {
            final CS_Processamento rec = this.escalonarRecurso();
            if (rec != null) {
                trf.setLocalProcessamento(rec);
                trf.setCaminho(this.escalonarRota(rec));
                //Verifica se não é caso de preempção
                if (!this.controleEscravos.get(this.escravos.indexOf(rec)).isPreempted()) {
//                    numEscravosLivres--;
                    this.status.get(this.metricaUsuarios.getUsuarios().indexOf(trf.getProprietario())).AtualizaUso(rec.getPoderComputacional(), 1);
                    //controleEscravos.get(escravos.indexOf(rec))
                    // .SetBloqueado();
                    this.mestre.sendTask(trf);
                } else {
                    final int index_rec = this.escravos.indexOf(rec);
                    this.esperaTarefas.add(trf);
                    String user1 =
                            ((Tarefa) this.processadorEscravos.get(index_rec).get(0)).getProprietario();
                    int pID = ((Tarefa) this.processadorEscravos.get(index_rec).get(0)).getIdentificador();
                    String user2 = trf.getProprietario();
                    int aID = trf.getIdentificador();
                    this.controlePreempcao.add(new PreemptionControl(user1, pID, user2, aID));
                    final int indexUser =
                            this.metricaUsuarios.getUsuarios().indexOf(((Tarefa) this.processadorEscravos.get(index_rec).get(0)).getProprietario());
                    this.status.get(indexUser).AtualizaUso(rec.getPoderComputacional(), 0);
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
    public void adicionarTarefa(final Tarefa tarefa) {
        super.adicionarTarefa(tarefa);
        final CS_Processamento maq =
                (CS_Processamento) tarefa.getLocalProcessamento();
        final int indexUser;
        //Em caso de preempção, é procurada a tarefa correspondente para ser
        // enviada ao escravo agora desocupado
        if (tarefa.getLocalProcessamento() != null) {

            int j;
            int indexControle = -1;
            for (j = 0; j < this.controlePreempcao.size(); j++) {
                if (this.controlePreempcao.get(j).getPreempID() == tarefa.getIdentificador() && this.controlePreempcao.get(j).getUsuarioPreemp().equals(tarefa.getProprietario())) {
                    indexControle = j;
                    break;
                }
            }

            for (int i = 0; i < this.esperaTarefas.size(); i++) {
                if (this.esperaTarefas.get(i).getProprietario().equals(this.controlePreempcao.get(indexControle).getUsuarioAlloc()) && this.esperaTarefas.get(i).getIdentificador() == this.controlePreempcao.get(j).getAllocID()) {
                    indexUser =
                            this.metricaUsuarios.getUsuarios().indexOf(this.controlePreempcao.get(indexControle).getUsuarioAlloc());
                    this.status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 1);
                    this.mestre.sendTask(this.esperaTarefas.get(i));
                    final int index =
                            this.escravos.indexOf(this.esperaTarefas.get(i).getLocalProcessamento());
                    //controleEscravos.get(index).SetBloqueado();
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
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        final CS_Processamento maq =
                (CS_Processamento) tarefa.getLocalProcessamento();
        final int indexUser =
                this.metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
        this.status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 0);
    }

    @Override
    public Double getTempoAtualizar() {
        return 15.0;
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        super.resultadoAtualizar(mensagem);
        final int index = this.escravos.indexOf(mensagem.getOrigem());
        this.processadorEscravos.set(index, mensagem.getProcessadorEscravo());
        this.contadorEscravos++;
        for (int i = 0; i < this.escravos.size(); i++) {
            if (this.processadorEscravos.get(i).size() > 1) {
                System.out.printf("Escravo %s executando %d\n",
                        this.escravos.get(i).getId(),
                        this.processadorEscravos.get(i).size());
                System.out.println("PROBLEMA!");
            }
            if (!this.filaEscravo.get(i).isEmpty()) {
                System.out.println("Tem Fila");
            }
        }
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

}