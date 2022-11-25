package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.grid.GridMaster;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.EHOSEP_StatusUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Policy
public class EHOSEP extends GridSchedulingPolicy {
    private final ArrayList<EHOSEP_StatusUser> status;
    private final List<ControleEscravos> controleEscravos;
    private final List<Tarefa> esperaTarefas;
    private final List<ControlePreempcao> controlePreempcao;

    public EHOSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.controleEscravos = new ArrayList<>();
        this.esperaTarefas = new ArrayList<>();
        this.controlePreempcao = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
        final List<List> processadorEscravos = new ArrayList<>();
        this.status = new ArrayList();
    }

    @Override
    public void iniciar() {
        //Escalonamento quando chegam tarefas e quando tarefas são concluídas
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);

        //Calcular poder computacional total do sistema, e consumo total do
        // mesmo.
        double poderTotal = 0.0;
        Double consumoTotal = 0.0;
        for (int i = 0; i < this.escravos.size(); i++) {
            poderTotal += this.escravos.get(i).getPoderComputacional();
            consumoTotal += this.escravos.get(i).getConsumoEnergia();
        }
        //System.out.println(consumoTotal);
        //System.out.println(poderTotal);

        //Objetos de controle de uso e cota para cada um dos usuários
        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            //Calcular o poder computacional da porção de cada usuário
            double poderComp = 0.0;
            Double consumoPorcao = 0.0;
            for (int j = 0; j < this.escravos.size(); j++) {
                //Se o nó corrente não é mestre e pertence ao usuário corrente
                if (!(this.escravos.get(j) instanceof GridMaster) && this.escravos.get(j).getProprietario().equals(this.metricaUsuarios.getUsuarios().get(i))) {
                    //Calcular o poder total da porcao do usuário corrente
                    poderComp += this.escravos.get(j).getPoderComputacional();
                    //Calcular o consumo total da porcao do usuário correntes
                    consumoPorcao += this.escravos.get(j).getConsumoEnergia();
                }
            }
            //Adiciona dados do usuário corrente à lista 
            this.status.add(new EHOSEP_StatusUser(this.metricaUsuarios.getUsuarios().get(i), i
                    , poderComp));
            //Inserir consumo da porção nos dados do usuário
            this.status.get(i).setPowerShare(consumoPorcao);
            //Calcular a relação entre a eficiência da porção e a eficiência
            // do sistema
            this.status.get(i).calculaRelacaoEficienciaEficienciaSisPor(poderTotal, consumoTotal);
            //Calcular o consumo máximo de energia de cada usuario
            final Double limite =
                    this.status.get(i).getPowerShare() * ((this.metricaUsuarios.getLimites().get(this.status.get(i).getNome()) / 100));
            this.status.get(i).setLimite(limite);
        }

        //Controle dos nós, com cópias das filas de cada um e da tarefa que
        // executa em cada um
        for (int i = 0; i < this.escravos.size(); i++) {
            this.controleEscravos.add(new ControleEscravos(this.escravos.get(i).getId()
                    , i, new ArrayList<>(), new ArrayList<>()));
        }
    }

    //Metodo necessario para implementar interface. Não é usado.
    @Override
    public Tarefa escalonarTarefa() {
        throw new UnsupportedOperationException("Not supported yet."); //To
        // change body of generated methods, choose Tools | Templates.
    }

    //Metodo necessario para implementar interface. Não é usado.
    @Override
    public CS_Processamento escalonarRecurso() {
        throw new UnsupportedOperationException("Not supported yet."); //To
        // change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {

        int indexTarefa;
        int indexEscravo;
        EHOSEP_StatusUser cliente;

        //Ordenar os usuários em ordem crescente de Poder Remanescente
        Collections.sort(this.status);


        for (int i = 0; i < this.status.size(); i++) {
            cliente = this.status.get(i);

            //Buscar tarefa para execucao
            indexTarefa = this.buscarTarefa(cliente);
            if (indexTarefa != -1) {

                //Buscar máquina para executar a tarefa definida
                indexEscravo = this.buscarRecurso(cliente,
                        this.tarefas.get(indexTarefa));
                if (indexEscravo != -1) {

                    //Se não é caso de preempção, a tarefa é configurada e
                    // enviada
                    if ("Livre".equals(this.controleEscravos.get(indexEscravo).getStatus())) {

                        final Tarefa tar = this.tarefas.remove(indexTarefa);
                        tar.setLocalProcessamento(this.escravos.get(indexEscravo));
                        tar.setCaminho(this.escalonarRota(this.escravos.get(indexEscravo)));
                        this.mestre.sendTask(tar);

                        //Atualização dos dados sobre o usuário
                        cliente.rmDemanda();
                        cliente.addServedNum();
                        cliente.addServedPerf(this.escravos.get(indexEscravo).getPoderComputacional());
                        cliente.addServedPower(this.escravos.get(indexEscravo).getConsumoEnergia());

                        //Controle das máquinas
                        this.controleEscravos.get(indexEscravo).setBloqueado();
                        return;
                    }

                    //Se é caso de preempção, a tarefa configurada e colocada
                    // em espera
                    if ("Ocupado".equals(this.controleEscravos.get(indexEscravo).getStatus())) {

                        final Tarefa tar = this.tarefas.remove(indexTarefa);
                        tar.setLocalProcessamento(this.escravos.get(indexEscravo));
                        tar.setCaminho(this.escalonarRota(this.escravos.get(indexEscravo)));

                        //Controle de preempção para enviar a nova tarefa no
                        // momento certo
                        final String userPreemp =
                                this.controleEscravos.get(indexEscravo).GetProcessador().get(0).getProprietario();
                        final int idTarefaPreemp =
                                this.controleEscravos.get(indexEscravo).GetProcessador().get(0).getIdentificador();
                        this.controlePreempcao.add(new ControlePreempcao(userPreemp, idTarefaPreemp, tar.getProprietario(), tar.getIdentificador()));
                        this.esperaTarefas.add(tar);

                        //Solicitação de retorno da tarefa em execução e
                        // atualização da demanda do usuário
                        this.mestre.sendMessage(this.controleEscravos.get(indexEscravo).GetProcessador().get(0), this.escravos.get(indexEscravo), Mensagens.DEVOLVER_COM_PREEMPCAO);
                        this.controleEscravos.get(indexEscravo).setBloqueado();
                        cliente.rmDemanda();
                        return;
                    }
                }
            }
        }
    }

    private int buscarTarefa(final EHOSEP_StatusUser usuario) {

        //Indice da tarefa na lista de tarefas
        int trf = -1;
        //Se o usuario tem demanda nao atendida e seu consumo nao chegou ao
        // limite
        if (usuario.getDemanda() > 0 && usuario.getServedPower() < usuario.getLimite()) {
            //Procura pela menor tarefa nao atendida do usuario.
            for (int j = 0; j < this.tarefas.size(); j++) {
                if (this.tarefas.get(j).getProprietario().equals(usuario.getNome())) {
                    if (trf == -1) {
                        trf = j;
                    } else if (this.tarefas.get(j).getTamProcessamento() < this.tarefas.get(trf).getTamProcessamento()) {//Escolher a tarefa de menor tamanho do usuario
                        trf = j;
                    }
                }
            }
        }
        return trf;
    }

    private int buscarRecurso(final EHOSEP_StatusUser cliente, final Tarefa TarAloc) {

        /*++++++++++++++++++Buscando recurso livres++++++++++++++++++*/

        //Índice da máquina escolhida, na lista de máquinas
        int indexSelec = -1;
        //Consumo da máquina escolhida e da máquina comparada com a escolhida
        // previamente em passagem anterior do laço
        double consumoSelec = 0.0;
        double consumoMaqTestada;

        for (int i = 0; i < this.escravos.size(); i++) {

            //Verificar o limite de consumo e garantir que o escravo está de
            // fato livre e que não há nenhuma tarefa em trânsito para o
            // escravo. É escolhido o recurso que consumir menos energia pra
            // executar a tarefa alocada.
            if ("Livre".equals(this.controleEscravos.get(i).getStatus()) && (this.escravos.get(i).getConsumoEnergia() + cliente.getServedPower()) <= cliente.getLimite()) {

                if (indexSelec == -1) {

                    indexSelec = i;
                    //Tempo para processar
                    consumoSelec =
                            TarAloc.getTamProcessamento() / this.escravos.get(i).getPoderComputacional();
                    //Consumo em Joule para processar
                    consumoSelec =
                            consumoSelec * this.escravos.get(i).getConsumoEnergia();

                } else {
                    //Tempo para processar
                    consumoMaqTestada =
                            TarAloc.getTamProcessamento() / this.escravos.get(i).getPoderComputacional();
                    //Consumo em Joule para processar
                    consumoMaqTestada =
                            consumoMaqTestada * this.escravos.get(i).getConsumoEnergia();

                    if (consumoSelec > consumoMaqTestada) {

                        indexSelec = i;
                        consumoSelec = consumoMaqTestada;
                    } else if (consumoSelec == consumoMaqTestada) {

                        if (this.escravos.get(i).getPoderComputacional() > this.escravos.get(indexSelec).getPoderComputacional()) {

                            indexSelec = i;
                            consumoSelec = consumoMaqTestada;
                        }
                    }
                }
            }
        }

        if (indexSelec != -1) {
            return indexSelec;
        }

        /*+++++++++++++++++Busca por usuário para preempção+++++++++++++++++*/

        //Se o usuário com maior valor de DifPoder não tem excesso de poder
        // computacional, não há usuário que possa sofrer preempção. Além
        // disso, não ocorrerá preempção para atender usuário que tem excesso.
        if (this.status.get(this.status.size() - 1).getServedPerf() <= this.status.get(this.status.size() - 1).getPerfShare() || cliente.getServedPerf() >= cliente.getPerfShare()) {
            return -1;
        }


        //Métricas e índice do usuário que possivelmente perderá recurso
        double consumoPonderadoSelec = 0.0;
        int indexUserPreemp = -1;
        //Começando pelo usuário de maior excesso
        for (int i = this.status.size() - 1; i >= 0; i--) {
            //Apenas usuários que tem excesso de poder computacional podem
            // sofrer preempção
            if (this.status.get(i).getServedPerf() > this.status.get(i).getPerfShare()) {
                //Se ainda não foi escolhido
                if (indexUserPreemp == -1) {
                    indexUserPreemp = i;
                    //Sofre preempção o usuário com maior métrica calculada
                    consumoPonderadoSelec =
                            (this.status.get(i).getServedPower()) * this.status.get(i).getRelacaoEficienciSisPor();
                } else {

                    final double consumoPonderadoCorrente =
                            (this.status.get(i).getServedPower()) * this.status.get(i).getRelacaoEficienciSisPor();
                    if (consumoPonderadoCorrente > consumoPonderadoSelec) {

                        indexUserPreemp = i;
                        consumoPonderadoSelec = consumoPonderadoCorrente;
                    } else if (consumoPonderadoCorrente == consumoPonderadoSelec) {

                        if ((this.status.get(i).getServedPerf() - this.status.get(i).getPerfShare()) > (this.status.get(indexUserPreemp).getServedPerf() - this.status.get(indexUserPreemp).getPerfShare())) {
                            indexUserPreemp = i;
                            consumoPonderadoSelec = consumoPonderadoCorrente;
                        }
                    }
                }
            }
        }

        if (indexUserPreemp == -1) {
            return -1;
        }

        //Buscar recurso para preempção
        double desperdicioTestado;
        double desperdicioSelec = 0.0;

        for (int j = 0; j < this.escravos.size(); j++) {
            //Procurar recurso ocupado com tarefa do usuário que perderá máquina
            if ("Ocupado".equals(this.controleEscravos.get(j).getStatus()) && (this.escravos.get(j).getConsumoEnergia() + cliente.getServedPower()) <= cliente.getLimite()) {

                final Tarefa tarPreemp =
                        this.controleEscravos.get(j).GetProcessador().get(0);
                if (tarPreemp.getProprietario().equals(this.status.get(indexUserPreemp).getNome())) {

                    if (indexSelec == -1) {
                        //Se há checkpointing de tarefas
                        if (tarPreemp.getCheckPoint() > 0.0) {
                            //((tempo atual - tempo em que a execução da
                            // tarefa começou no recurso)*poder
                            // computacional)%bloco de checkpointing
                            desperdicioSelec =
                                    ((this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * this.escravos.get(j).getPoderComputacional()) % tarPreemp.getCheckPoint();
                        } else {
                            //Se não há chekcpointin de tarefas, o
                            // desperdício é o tempo total executado para a
                            // tarefa na máquina corrente no laço
                            desperdicioSelec =
                                    (this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * this.escravos.get(j).getPoderComputacional();
                        }
                        indexSelec = j;
                    } else {

                        if (tarPreemp.getCheckPoint() > 0.0) {

                            desperdicioTestado =
                                    ((this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * this.escravos.get(j).getPoderComputacional()) % tarPreemp.getCheckPoint();
                        } else {
                            desperdicioTestado =
                                    (this.mestre.getSimulation().getTime(this) - tarPreemp.getTempoInicial().get(tarPreemp.getTempoInicial().size() - 1)) * this.escravos.get(j).getPoderComputacional();
                        }
                        //É escolhida a máquina de menor desperdício
                        if (desperdicioTestado < desperdicioSelec) {

                            desperdicioSelec = desperdicioTestado;
                            indexSelec = j;
                        }
                        //Se o desperdício é igual, é escolhida a máquina com
                        // menor poder computacional
                        else if (desperdicioTestado == desperdicioSelec && this.escravos.get(j).getPoderComputacional() < this.escravos.get(indexSelec).getPoderComputacional()) {
                            indexSelec = j;
                        }
                    }
                }
            }
        }

        if (indexUserPreemp != -1 && indexSelec != -1) {
            if ((this.status.get(indexUserPreemp).getServedPerf() - this.escravos.get(indexSelec).getPoderComputacional()) < this.status.get(indexUserPreemp).getPerfShare()) {
                if (this.status.get(indexUserPreemp).getLimite() <= cliente.getLimite()) {
                    indexSelec = -1;
                }
            }
        }

        return indexSelec;
    }

    @Override
    //Receber nova tarefa submetida ou tarefa que sofreu preemoção
    public void adicionarTarefa(final Tarefa tarefa) {

        //Method herdado, obrigatório executar para obter métricas ao final
        // da slimuação
        super.adicionarTarefa(tarefa);

        //Atualização da demanda do usuário proprietário da tarefa
        for (int i = 0; i < this.status.size(); i++) {
            if (this.status.get(i).getNome().equals(tarefa.getProprietario())) {
                this.status.get(i).addDemanda();
            }
        }

        //Em caso de preempção
        if (tarefa.getLocalProcessamento() != null) {

            //Localizar informações de estado de máquina que executou a
            // tarefa (se houver)
            final CS_Processamento maq =
                    (CS_Processamento) tarefa.getLocalProcessamento();

            //Localizar informações armazenadas sobre a preempção em particular

            int indexControlePreemp = -1;
            int indexStatusUserAlloc = -1;
            int indexStatusUserPreemp = -1;

            for (int j = 0; j < this.controlePreempcao.size(); j++) {
                if (this.controlePreempcao.get(j).getPreempID() == tarefa.getIdentificador() && this.controlePreempcao.get(j).getUsuarioPreemp().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            for (int k = 0; k < this.status.size(); k++) {
                if (this.status.get(k).getNome().equals(this.controlePreempcao.get(indexControlePreemp).getUsuarioAlloc())) {
                    indexStatusUserAlloc = k;
                }
            }

            for (int k = 0; k < this.status.size(); k++) {
                if (this.status.get(k).getNome().equals(this.controlePreempcao.get(indexControlePreemp).getUsuarioPreemp())) {
                    indexStatusUserPreemp = k;
                }
            }

            //Localizar tarefa em espera deseignada para executar
            for (int i = 0; i < this.esperaTarefas.size(); i++) {

                if (this.esperaTarefas.get(i).getProprietario().equals(this.controlePreempcao.get(indexControlePreemp).getUsuarioAlloc()) && this.esperaTarefas.get(i).getIdentificador() == this.controlePreempcao.get(indexControlePreemp).getAllocID()) {

                    //Enviar tarefa para execução
                    this.mestre.sendTask(this.esperaTarefas.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa
                    // será executada
                    this.status.get(indexStatusUserAlloc).addServedNum();
                    this.status.get(indexStatusUserAlloc).addServedPerf(maq.getPoderComputacional());
                    this.status.get(indexStatusUserAlloc).addServedPower(maq.getConsumoEnergia());

                    //Atualizar informações de estado do usuáro cuja tarefa
                    // foi interrompida
                    this.status.get(indexStatusUserPreemp).rmServedNum();
                    this.status.get(indexStatusUserPreemp).rmServedPerf(maq.getPoderComputacional());
                    this.status.get(indexStatusUserPreemp).rmServedPower(maq.getConsumoEnergia());

                    //Com a preempção feita, os dados necessários para ela
                    // são eliminados
                    this.controlePreempcao.remove(indexControlePreemp);
                    //Encerrar laço
                    break;
                }
            }
        }
    }

    @Override
    //Chegada de tarefa concluida
    public void addTarefaConcluida(final Tarefa tarefa) {
        //Method herdado, obrigatório executar para obter métricas ao final
        // da simulação
        super.addTarefaConcluida(tarefa);

        //Localizar informações sobre máquina que executou a tarefa e usuário
        // proprietário da tarefa
        final CS_Processamento maq =
                (CS_Processamento) tarefa.getLocalProcessamento();
        final int maqIndex = this.escravos.indexOf(maq);

        if ("Ocupado".equals(this.controleEscravos.get(maqIndex).getStatus())) {

            int statusIndex = -1;

            for (int i = 0; i < this.status.size(); i++) {
                if (this.status.get(i).getNome().equals(tarefa.getProprietario())) {
                    statusIndex = i;
                }
            }

            //Atualização das informações de estado do proprietario da tarefa
            // terminada.
            this.status.get(statusIndex).rmServedNum();
            this.status.get(statusIndex).rmServedPerf(maq.getPoderComputacional());
            this.status.get(statusIndex).rmServedPower(maq.getConsumoEnergia());

            this.controleEscravos.get(maqIndex).setLivre();
        } else if ("Bloqueado".equals(
                this.controleEscravos.get(maqIndex).getStatus())) {

            int indexControlePreemp = -1;
            int indexStatusUserAlloc = -1;
            int indexStatusUserPreemp = -1;

            for (int j = 0; j < this.controlePreempcao.size(); j++) {
                if (this.controlePreempcao.get(j).getPreempID() == tarefa.getIdentificador() && this.controlePreempcao.get(j).getUsuarioPreemp().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            for (int k = 0; k < this.status.size(); k++) {
                if (this.status.get(k).getNome().equals(this.controlePreempcao.get(indexControlePreemp).getUsuarioAlloc())) {
                    indexStatusUserAlloc = k;
                    break;
                }
            }

            for (int k = 0; k < this.status.size(); k++) {
                if (this.status.get(k).getNome().equals(this.controlePreempcao.get(indexControlePreemp).getUsuarioPreemp())) {
                    indexStatusUserPreemp = k;
                    break;
                }
            }

            //Localizar tarefa em espera designada para executar
            for (int i = 0; i < this.esperaTarefas.size(); i++) {
                if (this.esperaTarefas.get(i).getProprietario().equals(this.controlePreempcao.get(indexControlePreemp).getUsuarioAlloc()) && this.esperaTarefas.get(i).getIdentificador() == this.controlePreempcao.get(indexControlePreemp).getAllocID()) {

                    //Enviar tarefa para execução
                    this.mestre.sendTask(this.esperaTarefas.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa
                    // será executada
                    this.status.get(indexStatusUserAlloc).addServedNum();
                    this.status.get(indexStatusUserAlloc).addServedPerf(maq.getPoderComputacional());
                    this.status.get(indexStatusUserAlloc).addServedPower(maq.getConsumoEnergia());

                    //Atualizar informações de estado do usuário cuja tarefa
                    // teve a execução interrompida
                    this.status.get(indexStatusUserPreemp).rmServedNum();
                    this.status.get(indexStatusUserPreemp).rmServedPerf(maq.getPoderComputacional());
                    this.status.get(indexStatusUserPreemp).rmServedPower(maq.getConsumoEnergia());

                    //Com a preempção feita, os dados necessários para ela
                    // são eliminados
                    this.controlePreempcao.remove(indexControlePreemp);
                    //Encerrar laço
                    break;
                }
            }
        }
    }

    //Definir o intervalo de tempo, em segundos, em que as máquinas enviarão
    // dados de atualização para o escalonador
    @Override
    public Double getTempoAtualizar() {
        return 15.0;
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        //super.resultadoAtualizar(mensagem);
        //Localizar máquina que enviou estado atualizado
        final int index = this.escravos.indexOf(mensagem.getOrigem());

        //Atualizar listas de espera e processamento da máquina
        this.controleEscravos.get(index).setProcessador((ArrayList<Tarefa>) mensagem.getProcessadorEscravo());
        this.controleEscravos.get(index).setFila((ArrayList<Tarefa>) mensagem.getFilaEscravo());

        //Tanto alocação para recurso livre como a preempção levam dois
        // ciclos de atualização para que a máquina possa ser considerada
        // para esacalonamento novamente

        //Primeiro ciclo
        if ("Bloqueado".equals(this.controleEscravos.get(index).getStatus())) {
            this.controleEscravos.get(index).setIncerto();
            //Segundo ciclo
        } else if ("Incerto".equals(this.controleEscravos.get(index).getStatus())) {
            //Se não está executando nada
            if (this.controleEscravos.get(index).GetProcessador().isEmpty()) {

                this.controleEscravos.get(index).setLivre();
                //Se está executando uma tarefa
            } else if (this.controleEscravos.get(index).GetProcessador().size() == 1) {

                this.controleEscravos.get(index).setOcupado();
                //Se há mais de uma tarefa e a máquina tem mais de um núcleo
            } else if (this.controleEscravos.get(index).GetProcessador().size() > 1) {

                System.out.println("Houve Paralelismo");
            }
        }
        //Se há fila de tarefa na máquina
        if (!this.controleEscravos.get(index).GetFila().isEmpty()) {
            System.out.println("Houve Fila");
        }
    }

    //Classe para arnazenar o estado das máquinas no sistema
    private static class ControleEscravos {

        private final String ID;//Id da máquina escravo
        private final int index;//Índice na lista de escravos
        private String status;//Estado da máquina
        private ArrayList<Tarefa> fila;
        private ArrayList<Tarefa> processador;

        public ControleEscravos(final String Ident, final int ind,
                                final ArrayList<Tarefa> F,
                                final ArrayList<Tarefa> P) {
            this.status = "Livre";
            this.ID = Ident;
            this.index = ind;
            this.fila = F;
            this.processador = P;
        }

        public String getID() {
            return this.ID;
        }

        public int GetIndex() {
            return this.index;
        }

        public List<Tarefa> GetFila() {
            return this.fila;
        }

        public ArrayList<Tarefa> GetProcessador() {
            return this.processador;
        }

        public String getStatus() {
            return this.status;
        }

        public void setFila(final ArrayList<Tarefa> F) {
            this.fila = F;
        }

        public void setProcessador(final ArrayList<Tarefa> P) {
            this.processador = P;
        }

        public void setOcupado() {
            this.status = "Ocupado";
        }

        public void setLivre() {
            this.status = "Livre";
        }

        public void setBloqueado() {
            this.status = "Bloqueado";
        }

        public void setIncerto() {
            this.status = "Incerto";
        }
    }

    //Classe para armazenar dados sobre as preempções que ainda não terminaram
    public static class ControlePreempcao {

        private final String usuarioPreemp;
        private final String usuarioAlloc;
        private final int preempID;//ID da tarefa que sofreu preempção
        private final int allocID;//ID da tarefa alocada

        public ControlePreempcao(final String userP, final int pID,
                                 final String userA, final int aID) {
            this.usuarioPreemp = userP;
            this.preempID = pID;
            this.usuarioAlloc = userA;
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
}
