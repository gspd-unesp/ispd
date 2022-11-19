package ispd.policy.externo;

import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.escalonador.Escalonador;
import ispd.policy.escalonador.Mestre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class OSEP extends Escalonador {
    private final List<ControleEscravos> controleEscravos = new ArrayList<>();
    private final List<Tarefa> esperaTarefas = new ArrayList<>();
    private final List<ControlePreempcao> controlePreempcao = new ArrayList<>();
    private final List<List> processadorEscravos = new ArrayList<>();
    private Tarefa tarefaSelec = null;
    private HashMap<String, StatusUser> status = null;
    private int contadorEscravos = 0;

    public OSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        this.mestre.setTipoEscalonamento(Mestre.AMBOS);//Escalonamento quando
        // chegam tarefas e quando tarefas são concluídas
        this.status = new HashMap<>();

        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            //Objetos de controle de uso e cota para cada um dos usuários
            this.status.put(this.metricaUsuarios.getUsuarios().get(i),
                    new StatusUser(this.metricaUsuarios.getUsuarios().get(i),
                            i,
                            this.metricaUsuarios.getPoderComputacional(this.metricaUsuarios.getUsuarios().get(i))));
        }

        for (final CS_Processamento escravo : this.escravos) {//Contadores para
            // lidar com a dinamicidade dos dados
            this.controleEscravos.add(new ControleEscravos(escravo.getId()));
            this.filaEscravo.add(new ArrayList<Tarefa>());
            this.processadorEscravos.add(new ArrayList<Tarefa>());
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        //Usuários com maior diferença entre uso e posse terão preferência
        int difUsuarioMinimo = -1;
        int indexUsuarioMinimo = -1;
        String user;
        //Encontrar o usuário que está mais abaixo da sua propriedade 
        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            user = this.metricaUsuarios.getUsuarios().get(i);

            //Caso existam tarefas do usuário corrente e ele esteja com uso
            // menor que sua posse
            if ((this.status.get(user).getServedNum() < this.status.get(user).getOwnerShare()) && this.status.get(user).getDemanda() > 0) {

                if (difUsuarioMinimo == -1) {
                    difUsuarioMinimo =
                            this.status.get(user).getOwnerShare() - this.status.get(user).getServedNum();
                    indexUsuarioMinimo = i;
                } else {
                    if (difUsuarioMinimo < this.status.get(user).getOwnerShare() - this.status.get(user).getServedNum()) {
                        difUsuarioMinimo =
                                this.status.get(user).getOwnerShare() - this.status.get(user).getServedNum();
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
    public CS_Processamento escalonarRecurso() {
        String user;
        //Buscando recurso livre
        CS_Processamento selec = null;

        for (int i = 0; i < this.escravos.size(); i++) {

            if ("Livre".equals(this.controleEscravos.get(i).getStatus())) {
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
        int diff = -1;

        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            user = this.metricaUsuarios.getUsuarios().get(i);
            if (this.status.get(user).getServedNum() > this.status.get(user).getOwnerShare() && !user.equals(this.tarefaSelec.getProprietario())) {

                if (diff == -1) {

                    usermax = this.metricaUsuarios.getUsuarios().get(i);
                    diff = this.status.get(user).getServedNum() - this.status.get(user).getOwnerShare();

                } else {

                    if (this.status.get(user).getServedNum() - this.status.get(user).getOwnerShare() > diff) {

                        usermax = user;
                        diff = this.status.get(user).getServedNum() - this.status.get(user).getOwnerShare();

                    }

                }

            }

        }

        int index = -1;
        if (usermax != null) {

            for (int i = 0; i < this.escravos.size(); i++) {
                if ("Ocupado".equals(this.controleEscravos.get(i).getStatus()) && ((Tarefa) this.processadorEscravos.get(i).get(0)).getProprietario().equals(usermax)) {
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
            this.mestre.enviarMensagem((Tarefa) this.processadorEscravos.get(index_selec).get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
            return selec;
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
        if (trf != null) {
            this.tarefaSelec = trf;
            final StatusUser estado = this.status.get(trf.getProprietario());
            final CS_Processamento rec = this.escalonarRecurso();
            if (rec != null) {
                trf.setLocalProcessamento(rec);
                trf.setCaminho(this.escalonarRota(rec));
                //Verifica se não é caso de preempção
                if ("Livre".equals(this.controleEscravos.get(this.escravos.indexOf(rec)).getStatus())) {

                    estado.rmDemanda();
                    estado.addServedNum();

                    this.controleEscravos.get(this.escravos.indexOf(rec)).setBloqueado();
                    this.mestre.enviarTarefa(trf);

                } else {

                    if ("Ocupado".equals(this.controleEscravos.get(this.escravos.indexOf(rec)).getStatus())) {
                        final int index_rec = this.escravos.indexOf(rec);
                        this.esperaTarefas.add(trf);
                        this.controlePreempcao.add(new ControlePreempcao(((Tarefa) this.processadorEscravos.get(index_rec).get(0)).getProprietario(), ((Tarefa) this.processadorEscravos.get(index_rec).get(0)).getIdentificador(), trf.getProprietario(), trf.getIdentificador()));
                        this.controleEscravos.get(this.escravos.indexOf(rec)).setBloqueado();
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
                //mestre.enviarTarefa(trf);

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
        final StatusUser estadoUser = this.status.get(tarefa.getProprietario());
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

                    this.mestre.enviarTarefa(this.esperaTarefas.get(i));

                    this.status.get(this.controlePreempcao.get(indexControle).getUsuarioAlloc()).addServedNum();

                    this.status.get(this.controlePreempcao.get(indexControle).getUsuarioPreemp()).addDemanda();
                    this.status.get(this.controlePreempcao.get(indexControle).getUsuarioPreemp()).rmServedNum();

                    this.controleEscravos.get(this.escravos.indexOf(maq)).setBloqueado();

                    this.esperaTarefas.remove(i);
                    this.controlePreempcao.remove(j);
                    break;
                }
            }

        } else {
            //System.out.println("Tarefa " + tarefa.getIdentificador() + " do
            // user " + tarefa.getProprietario() + " chegou " + mestre
            // .getSimulacao().getTime());
            this.mestre.executarEscalonamento();
            estadoUser.addDemanda();
        }
    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        final CS_Processamento maq =
                (CS_Processamento) tarefa.getLocalProcessamento();
        final StatusUser estado = this.status.get(tarefa.getProprietario());

        estado.rmServedNum();
        final int index = this.escravos.indexOf(maq);
        this.controleEscravos.get(index).setLivre();
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
        if (this.contadorEscravos == this.escravos.size()) {
            boolean escalona = false;
            for (int i = 0; i < this.escravos.size(); i++) {
                if ("Bloqueado".equals(this.controleEscravos.get(i).getStatus())) {
                    this.controleEscravos.get(i).setIncerto();
                }
                if ("Incerto".equals(this.controleEscravos.get(i).getStatus())) {
                    if (this.processadorEscravos.get(i).isEmpty()) {
                        this.controleEscravos.get(i).setLivre();
                        escalona = true;
                    }
                    if (this.processadorEscravos.size() == 1) {
                        this.controleEscravos.get(i).setOcupado();
                    }
                    if (this.processadorEscravos.size() > 1) {
                        System.out.println("Houve Fila");
                    }
                }
            }
            this.contadorEscravos = 0;
            if (!this.tarefas.isEmpty() && escalona) {
                this.mestre.executarEscalonamento();
            }
        }
    }

    private static class ControleEscravos {

        private final String ID;//Id da máquina escravo
        private String status;//Estado da máquina

        private ControleEscravos(final String ID) {
            this.status = "Livre";
            this.ID = ID;
        }

        public String getID() {
            return this.ID;
        }

        private String getStatus() {
            return this.status;
        }

        private void setOcupado() {
            this.status = "Ocupado";
        }

        private void setLivre() {
            this.status = "Livre";
        }

        private void setBloqueado() {
            this.status = "Bloqueado";
        }

        private void setIncerto() {
            this.status = "Incerto";
        }
    }

    private static class ControlePreempcao {

        private final String usuarioPreemp;
        private final String usuarioAlloc;
        private final int preempID;//ID da tarefa que sofreu preempção
        private final int allocID;//ID da tarefa alocada

        private ControlePreempcao(final String user1, final int pID,
                                  final String user2, final int aID) {
            this.usuarioPreemp = user1;
            this.preempID = pID;
            this.usuarioAlloc = user2;
            this.allocID = aID;
        }

        private String getUsuarioPreemp() {
            return this.usuarioPreemp;
        }

        private int getPreempID() {
            return this.preempID;
        }

        private String getUsuarioAlloc() {
            return this.usuarioAlloc;
        }

        private int getAllocID() {
            return this.allocID;
        }
    }

    private class StatusUser {

        private final String user;//Nome do usuario;
        private final int indexUser;//Índice do usuário;
        private final double perfShare;//Desempenho total das máquinas do
        private final double servedPower;//Consumo de energia total que
        private int demanda;//Número de tarefas na fila
        private int indexTarefaMax;//Índice da maior tarefa na fila
        private int indexTarefaMin;//Índice da menor tarefa na fila
        private int ownerShare;//Número de máquinas do usuario
        // usuário
        private double powerShare;//Consumo de energia total das máquinas do
        // usuário
        private int servedNum;//Número de máquinas que atendem ao usuário
        private double servedPerf;//Desempenho total que atende ao usuário
        // atende ao usuario

        private StatusUser(final String user, final int indexUser,
                           final double perfShare) {
            this.user = user;
            this.indexUser = indexUser;
            this.demanda = 0;
            this.indexTarefaMax = -1;
            this.indexTarefaMin = -1;
            this.ownerShare = 0;
            this.perfShare = perfShare;
            this.powerShare = 0.0;
            this.servedNum = 0;
            this.servedPerf = 0.0;
            this.servedPower = 0.0;

            int i;
            int j = 0;
            for (i = 0; i < OSEP.this.escravos.size(); i++) {
                if (OSEP.this.escravos.get(i).getProprietario().equals(user)) {
                    j++;
                    //this.eficienciaMedia += escravos.get(i)
                    // .getPoderComputacional()/escravos.get(i)
                    // .getConsumoEnergia();
                }
            }
            this.ownerShare = j;
            //this.eficienciaMedia = this.eficienciaMedia/j;


        }

        private void addDemanda() {
            this.demanda++;
        }

        private void rmDemanda() {
            this.demanda--;
        }

        public void setTarefaMinima(final int index) {
            this.indexTarefaMin = index;
        }

        public void setTarefaMaxima(final int index) {
            this.indexTarefaMax = index;
        }

        public void addShare() {
            this.ownerShare++;
        }

        public void addPowerShare(final Double power) {
            this.powerShare += power;
        }

        private void addServedNum() {
            this.servedNum++;
        }

        private void rmServedNum() {
            this.servedNum--;
        }

        public void addServedPerf(final Double perf) {
            this.servedPerf += perf;
        }

        public void rmServedPerf(final Double perf) {
            this.servedPerf -= perf;
        }

        public void addServedPower(final Double power) {
            this.servedPerf += power;
        }

        public void rmServedPower(final Double power) {
            this.servedPerf -= power;
        }

        public String getUser() {
            return this.user;
        }

        public int getIndexUser() {
            return this.indexUser;
        }

        private int getDemanda() {
            return this.demanda;
        }

        public int getIndexTarefaMax() {
            return this.indexTarefaMax;
        }

        public int getIndexTarefaMin() {
            return this.indexTarefaMin;
        }

        private int getOwnerShare() {
            return this.ownerShare;
        }

        public double getPerfShare() {
            return this.perfShare;
        }

        public double getPowerShare() {
            return this.powerShare;
        }

        private int getServedNum() {
            return this.servedNum;
        }

        public double getServedPerf() {
            return this.servedPerf;
        }

        public double getServedPower() {
            return this.servedPower;
        }
    }
}
