package ispd.motor;

import ispd.gui.PickSimulationFaultsDialog;
import ispd.motor.falhas.FIBadDesign;
import ispd.motor.falhas.FIEarly;
import ispd.motor.falhas.FIFullHD;
import ispd.motor.falhas.FIHardware;
import ispd.motor.falhas.FIIncompatibility;
import ispd.motor.falhas.FIInterdependencie;
import ispd.motor.falhas.FILate;
import ispd.motor.falhas.FIOverload;
import ispd.motor.falhas.FIPermanent;
import ispd.motor.falhas.FISoftware;
import ispd.motor.falhas.FITransient;
import ispd.motor.falhas.FIValue;
import ispd.motor.falhas.FState;
import ispd.motor.filas.Client;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.metricas.MetricasGlobais;
import ispd.policy.alocacaoVM.VMM;
import ispd.policy.escalonadorCloud.MestreCloud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public class SimulacaoSequencialCloud extends Simulation {
    private final PriorityQueue<FutureEvent> eventos = new PriorityQueue<>();
    private double time = 0;

    /**
     * @param janela
     * @param redeDeFilas
     * @param tarefas
     * @throws IllegalArgumentException
     */
    public SimulacaoSequencialCloud(
            final ProgressoSimulacao janela,
            final RedeDeFilasCloud redeDeFilas, final List<Tarefa> tarefas) {
        super(janela, redeDeFilas, tarefas);

        if (redeDeFilas == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (redeDeFilas.getMestres() == null || redeDeFilas.getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (redeDeFilas.getLinks() == null || redeDeFilas.getLinks().isEmpty()) {
            janela.println("The model has no Networks.", Color.orange);
        } else if (redeDeFilas.getVMs() == null || redeDeFilas.getVMs().isEmpty())
            janela.println("The model has no virtual machines configured.",
                    Color.orange);
        if (tarefas == null || tarefas.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have " +
                                               "not been configured.");
        }

        janela.print("Creating routing.");
        janela.print(" -> ");

        System.out.println("---------------------------------------");

        for (final var mst : redeDeFilas.getMestres()) {
            final var temp = (VMM) mst;
            final var aux = (MestreCloud) mst;
            aux.setSimulacao(this);
            temp.setSimulation(this);
            System.out.printf("Mestre %s encontrando seus escravos\n",
                    mst.getId());
            mst.determinarCaminhos();
        }

        janela.incProgresso(5);
        janela.println("OK", Color.green);

        final var selecionarFalhas = new PickSimulationFaultsDialog();

        if (selecionarFalhas.isActive()) {
            //-----------Injeção da Falha de Omissão de Hardware --------
            if (selecionarFalhas.cbkOmissaoHardware != null) {
                janela.println("There are injected hardware omission failures" +
                               ".");
                janela.println("Creating Hardware fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                final FIHardware fihardware = new FIHardware();
                fihardware.FIHardware1(janela, redeDeFilas, tarefas);
            } else {
                janela.println("There aren't injected hardware omission " +
                               "failures.");
            }
            //-----------Injeção da  Falha de Omissão de Software --------
            if (selecionarFalhas.cbkOmissaoSoftware != null) {
                janela.println("There are injected software omission failures" +
                               ".");
                janela.println("Creating software fault.");
                janela.println("Software failure created.");
                //ir para ispd.motor.falhas.FISoftware.java
                final FISoftware fisoftdware = new FISoftware();
                fisoftdware.FISfotware1(janela, redeDeFilas, tarefas);
            }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){
            else {
                janela.println("There aren't injected software omission " +
                               "failures.");
            }
            //-----------Injeção da  Falha de Negação de serviço --------
            if (selecionarFalhas.cbxNegacaoService != null) {
                janela.println("There are injected denial of service failures" +
                               ".");
                janela.println("Creating Denial of service fault.");
                janela.println("Development fault.");
            } else {
                janela.println("There aren't injected denial of service " +
                               "failures.");
            }
            //-----------Injeção da  Falha de HD Cheio --------
            if (selecionarFalhas.cbxHDCheio != null) {
                janela.println("There are injected Full HD failures.");
                janela.println("Creating Full HD fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                final FIFullHD HDCheio = new FIFullHD();

            }//if (selecionarFalhas.cbkHDCheio.isSelected()){
            else {
                janela.println("There aren't injected Full HD failures.");
            }

            //-----------Injeção da  Falha de Valores --------
            if (selecionarFalhas.cbxValores != null) {
                janela.println("There are injected Values failures.");
                janela.println("Creating value fault.");
                //ir para ispd.motor.falhas.FValue.java
                final MetricasGlobais global = new MetricasGlobais();
                final FIValue value = new FIValue();
                value.FIValue1(janela, redeDeFilas, global);
            }//if (selecionarFalhas.cbx.isSelected()){
            else {
                janela.println("There aren't injected Value failures.");
            }

            //-----------Injeção da  Falha de Estado --------
            if (selecionarFalhas.cbxEstado != null) {
                janela.println("There are injected State failures.");
                janela.println("Creating state fault.");
                //ir para ispd.motor.falhas.FState.java
                final FState state = new FState();
                state.FIState1(janela, redeDeFilas);
            }//if (selecionarFalhas.cbxEstado.isSelected()){
            else {
                janela.println("There aren't injected State failures.");
            }
            //-----------Injeção da  Falha de Sobrecarga de Tempo --------
            if (selecionarFalhas.cbxSobrecargaTempo != null) {
                janela.println("There are injected time overload failures.");
                janela.println("Creating time overload fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIOverload.java
                final FIOverload overload = new FIOverload();

            }//if (selecionarFalhas.cbxSobrecargaTempo.isSelected()){
            else {
                janela.println("There aren't injected time overload failures.");
            }
            //-----------Injeção da  Falha de Interdependencia --------
            if (selecionarFalhas.cbxInterdependencia != null) {
                janela.println("There are injected interdependencies failures" +
                               ".");
                janela.println("Creating interdependencie fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIInterdependencie.java
                final FIInterdependencie fiInterdependencia =
                        new FIInterdependencie();

            }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){
            else {
                janela.println("There aren't injected interdependencies " +
                               "failures.");
            }
            //-----------Injeção da  Falha de Incompatibilidade --------
            if (selecionarFalhas.cbxIncompatibilidade != null) {
                janela.println("There are injected Incompatibility failures.");
                janela.println("Creating Incompatibility fault.");
                janela.println("Development fault.");//ir para ispd.motor
                // .falhas.FIHardware.java
                final FIIncompatibility fiIncompatibility =
                        new FIIncompatibility();

            }//if (selecionarFalhas.cbxIncompatibilidade.isSelected()){
            else {
                janela.println("There aren't injected Incompatibility " +
                               "failures.");
            }
            //-----------Injeção de Falhas Pemanentes --------   
            if (selecionarFalhas.cbxFPermanentes != null) {
                janela.println("There are injected permanents failures.");
                janela.println("Creating permanents fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIPermanent.java
                final FIPermanent fiPermanent = new FIPermanent();

            }//if (selecionarFalhas.cbxFPermanentes.isSelected()){
            else {
                janela.println("There aren't injected permanents failures.");
            }
            //-----------Injeção da  Falha de Desenho incorreto --------
            if (selecionarFalhas.cbxDesenhoIncorreto != null) {
                janela.println("There are injected bad design failures.");
                janela.println("Creating bad design fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                final FIBadDesign fibadesign = new FIBadDesign();

            }//if (selecionarFalhas.cbxDesenhoIncorreto.isSelected()){
            else {
                janela.println("There aren't injected bad design failures.");
            }
            //-----------Injeção da  Falha Precosse --------
            if (selecionarFalhas.cbxPrecoce != null) {
                janela.println("There are injected early failures.");
                janela.println("Creating early fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIEarly.java
                final FIEarly fiearly = new FIEarly();

            }//if (selecionarFalhas.cbxPrecoce.isSelected()){
            else {
                janela.println("There aren't injected early failures.");
            }
//-----------Injeção da  Falha de Tardia --------   
            if (selecionarFalhas.cbxTardia != null) {
                janela.println("There are injected late failures.");
                janela.println("Creating late fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FILate.java
                final FILate fiTardia = new FILate();

            }//if (selecionarFalhas.cbxTardia.isSelected()){
            else {
                janela.println("There aren't injected late failures.");
            }

            //-----------Injeção da  Falha Transiente --------
            if (selecionarFalhas.cbxTransiente != null) {
                janela.println("There are injected transient failures.");
                janela.println("Creating transient failure.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                final FITransient fitransient = new FITransient();

            }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){
            else {
                janela.println("There aren't injected transient failures.");
            }
        }//if (selecionarFalhas!=null)
        else
            janela.println("There aren't selected faults.");
        if (redeDeFilas.getMaquinasCloud() == null || redeDeFilas.getMaquinasCloud().isEmpty()) {
            janela.println("The model has no phisical machines.", Color.orange);
        } else {
            System.out.println("---------------------------------------");
            for (final CS_MaquinaCloud maq : redeDeFilas.getMaquinasCloud()) {
                //Encontra menor caminho entre o escravo e seu mestre
                maq.setStatus(CS_MaquinaCloud.LIGADO);
                maq.determinarCaminhos();//escravo encontra caminhos para seu
                // mestre
                //System.out.println("Maquina " + maq.getId() + " encontrando
                // seus mestres");
            }
        }
        //fim roteamento
        janela.incProgresso(5);
    }

    @Override
    public void simulate() {
        //inicia os escalonadores
        System.out.println("---------------------------------------");
        this.initCloudSchedulers();
        System.out.println("---------------------------------------");

        this.initCloudAllocators();
        System.out.println("---------------------------------------");
        this.addEventos(this.getJobs());
        System.out.println("---------------------------------------");


        if (this.atualizarEscalonadores()) {
            this.realizarSimulacaoAtualizaTime();
        } else {
            this.realizarSimulacao();
        }

        SimulacaoSequencialCloud.desligarMaquinas(this,
                this.getCloudQueueNetwork());
        this.getWindow().incProgresso(30);
        this.getWindow().println("Simulation completed.", Color.green);
    }

    private void addEventos(final Iterable<? extends Tarefa> tarefas) {
        System.out.println("Tarefas sendo adicionadas na lista de eventos " +
                           "futuros");
        for (final Tarefa tarefa : tarefas) {
            final FutureEvent evt = new FutureEvent(tarefa.getTimeCriacao(),
                    FutureEvent.CHEGADA, tarefa.getOrigem(), tarefa);
            this.eventos.add(evt);
        }
    }

    @Override
    public void addFutureEvent(final FutureEvent ev) {
        this.eventos.offer(ev);
    }

    @Override
    public boolean removeFutureEvent(final int eventType,
                                     final CentroServico eventServer
            , final Client eventClient) {
        //remover evento de saida do cliente do servidor
        for (FutureEvent ev : this.eventos) {
            if (ev.getType() == eventType
                && ev.getServidor().equals(eventServer)
                && ev.getClient().equals(eventClient)) {
                this.eventos.remove(ev);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getTime(final Object origin) {
        return this.time;
    }

    private boolean atualizarEscalonadores() {
        for (final CS_Processamento mst :
                this.getCloudQueueNetwork().getMestres()) {
            final CS_VMM mestre = (CS_VMM) mst;
            if (mestre.getEscalonador().getTempoAtualizar() != null) {
                return true;
            }
        }
        return false;
    }


    private void realizarSimulacao() {
        while (!this.eventos.isEmpty()) {
            //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            final FutureEvent eventoAtual = this.eventos.poll();
            this.time = eventoAtual.getCreationTime();
            switch (eventoAtual.getType()) {
                case FutureEvent.CHEGADA ->
                        eventoAtual.getServidor().chegadaDeCliente(this,
                                (Tarefa) eventoAtual.getClient());
                case FutureEvent.ATENDIMENTO ->
                        eventoAtual.getServidor().atendimento(this,
                                (Tarefa) eventoAtual.getClient());
                case FutureEvent.SAIDA ->
                        eventoAtual.getServidor().saidaDeCliente(this,
                                (Tarefa) eventoAtual.getClient());
                case FutureEvent.ESCALONAR ->
                        eventoAtual.getServidor().requisicao(this, null,
                                FutureEvent.ESCALONAR);
                case FutureEvent.ALOCAR_VMS ->
                        eventoAtual.getServidor().requisicao(this, null,
                                FutureEvent.ALOCAR_VMS);
                default -> eventoAtual.getServidor().requisicao(this,
                        (Mensagem) eventoAtual.getClient(),
                        eventoAtual.getType());
            }
        }
    }

    /**
     * Executa o laço de repetição responsavel por atender todos eventos da
     * simulação, e adiciona o evento para atualizar os escalonadores.
     */
    private void realizarSimulacaoAtualizaTime() {
        final Collection<Object[]> Arrayatualizar = new ArrayList<>();
        for (final CS_Processamento mst : this.getQueueNetwork().getMestres()) {
            final CS_Mestre mestre = (CS_Mestre) mst;
            if (mestre.getEscalonador().getTempoAtualizar() != null) {
                final Object[] item = new Object[3];
                item[0] = mestre;
                item[1] = mestre.getEscalonador().getTempoAtualizar();
                item[2] = mestre.getEscalonador().getTempoAtualizar();
                Arrayatualizar.add(item);
            }
        }
        while (!this.eventos.isEmpty()) {
            //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            for (final Object[] ob : Arrayatualizar) {
                if ((Double) ob[2] < Objects.requireNonNull(this.eventos.peek()).getCreationTime()) {
                    final CS_Mestre mestre = (CS_Mestre) ob[0];
                    for (final CS_Processamento maq :
                            mestre.getEscalonador().getEscravos()) {
                        mestre.atualizar(maq, (Double) ob[2]);
                    }
                    ob[2] = (Double) ob[2] + (Double) ob[1];
                }
            }
            final FutureEvent eventoAtual = this.eventos.poll();
            this.time = Objects.requireNonNull(eventoAtual).getCreationTime();
            switch (eventoAtual.getType()) {
                case FutureEvent.CHEGADA ->
                        eventoAtual.getServidor().chegadaDeCliente(this,
                                (Tarefa) eventoAtual.getClient());
                case FutureEvent.ATENDIMENTO ->
                        eventoAtual.getServidor().atendimento(this,
                                (Tarefa) eventoAtual.getClient());
                case FutureEvent.SAIDA ->
                        eventoAtual.getServidor().saidaDeCliente(this,
                                (Tarefa) eventoAtual.getClient());
                case FutureEvent.ESCALONAR ->
                        eventoAtual.getServidor().requisicao(this, null,
                                FutureEvent.ESCALONAR);
                case FutureEvent.ALOCAR_VMS ->
                        eventoAtual.getServidor().requisicao(this, null,
                                FutureEvent.ALOCAR_VMS);
                default -> eventoAtual.getServidor().requisicao(this,
                        (Mensagem) eventoAtual.getClient(),
                        eventoAtual.getType());
            }
        }
    }

    private static void desligarMaquinas(
            final Simulation simulacao, final RedeDeFilasCloud rdfCloud) {
        for (final CS_MaquinaCloud aux : rdfCloud.getMaquinasCloud()) {
            aux.desligar(simulacao);
        }
    }
}
