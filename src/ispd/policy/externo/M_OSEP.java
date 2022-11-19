package ispd.policy.externo;

import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyConditions;
import ispd.policy.escalonador.Escalonador;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class M_OSEP extends Escalonador {
    private final List<ControleEscravos> controleEscravos;
    private final List<Tarefa> esperaTarefas;
    private final List<ControlePreempcao> controlePreempcao;
    private final List<List> processadorEscravos;
    private Tarefa tarefaSelec = null;
    private List<StatusUser> status = null;
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
        this.mestre.setTipoEscalonamento(PolicyConditions.ALL);//Escalonamento quando
        // chegam tarefas e quando tarefas são concluídas
        this.status = new ArrayList<>();

        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            //Objetos de controle de uso e cota para cada um dos usuários
            this.status.add(new StatusUser(this.metricaUsuarios.getUsuarios().get(i), this.metricaUsuarios.getPoderComputacional(this.metricaUsuarios.getUsuarios().get(i))));
        }

        for (int i = 0; i < this.escravos.size(); i++) {//Contadores para
            // lidar com a dinamicidade dos dados
            this.controleEscravos.add(new ControleEscravos());
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

            if (this.filaEscravo.get(i).isEmpty() && this.processadorEscravos.get(i).isEmpty() && this.controleEscravos.get(i).Livre()) {//Garantir que o escravo está de fato livre e que não há nenhuma tarefa em trânsito para o escravo
                if (selec == null) {

                    selec = this.escravos.get(i);

                } else if (Math.abs(this.escravos.get(i).getPoderComputacional() - this.tarefaSelec.getTamProcessamento()) < Math.abs(selec.getPoderComputacional() - this.tarefaSelec.getTamProcessamento())) {//Best Fit

                    selec = this.escravos.get(i);

                }

            }

        }

        if (selec != null) {

            this.controleEscravos.get(this.escravos.indexOf(selec)).SetBloqueado();//Inidcar que uma tarefa será enviada e que , portanto , este escravo deve ser bloqueada até a próxima atualização

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
                if (this.processadorEscravos.get(i).size() == 1 && this.controleEscravos.get(i).Ocupado() && this.filaEscravo.get(i).isEmpty() && ((Tarefa) this.processadorEscravos.get(i).get(0)).getProprietario().equals(usermax)) {
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
                    (this.status.get(indexUserEscravo).GetUso() - selec.getPoderComputacional() - this.status.get(indexUserEscravo).GetCota()) / this.status.get(indexUserEscravo).Cota;

            //Penalidade do usuário dono da tarefa slecionada para ser posta
            // em execução, caso a preempção seja feita
            final double penalidaUserEsperaPosterior =
                    (this.status.get(indexUserEspera).GetUso() + selec.getPoderComputacional() - this.status.get(indexUserEspera).GetCota()) / this.status.get(indexUserEspera).Cota;

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
                this.controleEscravos.get(this.escravos.indexOf(selec)).setPreemp();
                this.mestre.enviarMensagem((Tarefa) this.processadorEscravos.get(index_selec).get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
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
                if (!this.controleEscravos.get(this.escravos.indexOf(rec)).Preemp()) {
//                    numEscravosLivres--;
                    this.status.get(this.metricaUsuarios.getUsuarios().indexOf(trf.getProprietario())).AtualizaUso(rec.getPoderComputacional(), 1);
                    //controleEscravos.get(escravos.indexOf(rec))
                    // .SetBloqueado();
                    this.mestre.enviarTarefa(trf);
                } else {
                    final int index_rec = this.escravos.indexOf(rec);
                    this.esperaTarefas.add(trf);
                    this.controlePreempcao.add(new ControlePreempcao(((Tarefa) this.processadorEscravos.get(index_rec).get(0)).getProprietario(), ((Tarefa) this.processadorEscravos.get(index_rec).get(0)).getIdentificador(), trf.getProprietario(), trf.getIdentificador()));
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
                    this.mestre.enviarTarefa(this.esperaTarefas.get(i));
                    final int index =
                            this.escravos.indexOf(this.esperaTarefas.get(i).getLocalProcessamento());
                    //controleEscravos.get(index).SetBloqueado();
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

        //System.out.println("Tarefa " + tarefa.getIdentificador() + " do
        // user " + tarefa.getProprietario() + " concluida " + mestre
        // .getSimulacao().getTime() + " O usuário perdeu " + maq
        // .getPoderComputacional() + " MFLOPS");
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
                if (this.processadorEscravos.get(i).size() == 1 && !this.controleEscravos.get(i).Preemp()) {
                    this.controleEscravos.get(i).SetOcupado();
                } else if (this.processadorEscravos.get(i).isEmpty() && !this.controleEscravos.get(i).Preemp()) {
                    escalona = true;
                    this.controleEscravos.get(i).SetLivre();
                } else if (this.controleEscravos.get(i).Preemp()) {
                    this.controleEscravos.get(i).SetBloqueado();
                }
            }
            this.contadorEscravos = 0;
            if (!this.tarefas.isEmpty() && escalona) {
                this.mestre.executarEscalonamento();
            }
        }


    }

    private static class ControleEscravos {

        private int contador;

        private ControleEscravos() {
            this.contador = 0;
        }

        public boolean Ocupado() {
            return this.contador == 1;
        }

        public boolean Livre() {
            return this.contador == 0;
        }

        public boolean Bloqueado() {
            return this.contador == 2;
        }

        public boolean Preemp() {
            return this.contador == 3;
        }

        public void SetOcupado() {
            this.contador = 1;
        }

        public void SetLivre() {
            this.contador = 0;
        }

        public void SetBloqueado() {
            this.contador = 2;
        }

        public void setPreemp() {
            this.contador = 3;
        }
    }

    private static class ControlePreempcao {

        private final String usuarioPreemp;
        private final String usuarioAlloc;
        private final int preempID;
        private final int allocID;

        private ControlePreempcao(final String user1, final int pID,
                                  final String user2, final int aID) {
            this.usuarioPreemp = user1;
            this.preempID = pID;
            this.usuarioAlloc = user2;
            this.allocID = aID;
        }

        public String getUsuarioPreemp() {
            return this.usuarioPreemp;
        }

        public int getPreempID() {
            return this.preempID;
        }

        public String getUsuarioAlloc() {
            return this.usuarioAlloc;
        }

        public int getAllocID() {
            return this.allocID;
        }
    }

    private class StatusUser {

        private final Double Cota;
        private Double PoderEmUso;
        private int numCota;
        private int numUso;

        private StatusUser(final String usuario, final Double poder) {
            this.PoderEmUso = 0.0;
            this.Cota = poder;
            this.numCota = 0;
            this.numUso = 0;

            for (final CS_Processamento escravo : M_OSEP.this.escravos) {
                if (escravo.getProprietario().equals(usuario)) {
                    this.numCota++;
                }
            }


        }

        public void AtualizaUso(final Double poder, final int opc) {
            if (opc == 1) {
                this.PoderEmUso = this.PoderEmUso + poder;
                this.numUso++;
            } else {
                this.PoderEmUso = this.PoderEmUso - poder;
                this.numUso--;
            }
        }

        public Double GetCota() {
            return this.Cota;
        }

        public Double GetUso() {
            return this.PoderEmUso;
        }

        public int GetNumCota() {
            return this.numCota;
        }

        public int GetNumUso() {
            return this.numUso;
        }
    }
}