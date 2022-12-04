package ispd.motor;

import ispd.gui.PickSimulationFaultsDialog;
import ispd.motor.falhas.FIBadDesign;
import ispd.motor.falhas.FIDenialService;
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
import ispd.policy.PolicyMaster;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class SimulacaoSequencialCloud extends Simulation {
    private final PriorityQueue<FutureEvent> eventos = new PriorityQueue<>();
    private double time = 0;

    /**
     * @param window
     * @param cloudQueueNetwork
     * @param jobs
     * @throws IllegalArgumentException
     */
    public SimulacaoSequencialCloud(
            final ProgressoSimulacao window,
            final RedeDeFilasCloud cloudQueueNetwork,
            final List<Tarefa> jobs) {
        super(window, cloudQueueNetwork, jobs);

        if (cloudQueueNetwork == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (cloudQueueNetwork.getMestres() == null || cloudQueueNetwork.getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (cloudQueueNetwork.getLinks() == null || cloudQueueNetwork.getLinks().isEmpty()) {
            window.println("The model has no Networks.", Color.orange);
        } else if (cloudQueueNetwork.getVMs() == null || cloudQueueNetwork.getVMs().isEmpty())
            window.println(
                    "The model has no virtual machines configured.",
                    Color.orange
            );
        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException(
                    "One or more  workloads have not been configured.");
        }

        window.print("Creating routing.");
        window.print(" -> ");

        System.out.println("---------------------------------------");
        for (final CS_Processamento mst : cloudQueueNetwork.getMestres()) {
            // TODO: WTF?
            final PolicyMaster temp = (PolicyMaster) mst;
            final PolicyMaster aux = (PolicyMaster) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            aux.setSimulation(this);
            temp.setSimulation(this);
            //Encontra menor caminho entre o mestre e seus escravos
            System.out.printf("Mestre %s encontrando seus escravos\n",
                    mst.getId());
            mst.determinarCaminhos(); //mestre encontra caminho para seus
            // escravos
        }

        window.incProgresso(5);
        window.println("OK", Color.green);

        //--------------- Injeção de falhas
        //By Camila
        /*Injetando as falhas:
        verifica qual checkbox foi clicado quando escolheu a falha e executa*/
        //Injetar falhar de Omissão de Hardware: desligar uma máquina física
        final var selecionarFalhas = new PickSimulationFaultsDialog();


        if (selecionarFalhas.isActive()) {
            //-----------Injeção da Falha de Omissão de Hardware --------
            if (selecionarFalhas.cbkOmissaoHardware != null) {
                window.println("There are injected hardware omission failures" +
                               ".");
                window.println("Creating Hardware fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                final FIHardware fihardware = new FIHardware();
                fihardware.FIHardware1(window, cloudQueueNetwork, jobs);
            } else {
                window.println("There aren't injected hardware omission " +
                               "failures.");
            }
            //-----------Injeção da  Falha de Omissão de Software --------
            if (selecionarFalhas.cbkOmissaoSoftware != null) {
                window.println("There are injected software omission failures" +
                               ".");
                window.println("Creating software fault.");
                window.println("Software failure created.");
                //ir para ispd.motor.falhas.FISoftware.java
                final FISoftware fisoftdware = new FISoftware();
                fisoftdware.FISfotware1(window, cloudQueueNetwork, jobs);
            }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){
            else {
                window.println("There aren't injected software omission " +
                               "failures.");
            }
            //-----------Injeção da  Falha de Negação de serviço --------
            if (selecionarFalhas.cbxNegacaoService != null) {
                window.println("There are injected denial of service failures" +
                               ".");
                window.println("Creating Denial of service fault.");
                window.println("Development fault.");
                //ir para ispd.motor.falhas.FIDenialService.java
                final FIDenialService negacaoServico = new FIDenialService();

            }//if (selecionarFalhas.cbxNegacaoService.isSelected()){
            else {
                window.println("There aren't injected denial of service " +
                               "failures.");
            }
            //-----------Injeção da  Falha de HD Cheio --------
            if (selecionarFalhas.cbxHDCheio != null) {
                window.println("There are injected Full HD failures.");
                window.println("Creating Full HD fault.");
                window.println("Development fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                final FIFullHD HDCheio = new FIFullHD();

            }//if (selecionarFalhas.cbkHDCheio.isSelected()){
            else {
                window.println("There aren't injected Full HD failures.");
            }

            //-----------Injeção da  Falha de Valores --------
            if (selecionarFalhas.cbxValores != null) {
                window.println("There are injected Values failures.");
                window.println("Creating value fault.");
                //ir para ispd.motor.falhas.FValue.java
                final MetricasGlobais global = new MetricasGlobais();
                final FIValue value = new FIValue();
                value.FIValue1(window, cloudQueueNetwork, global);
            }//if (selecionarFalhas.cbx.isSelected()){
            else {
                window.println("There aren't injected Value failures.");
            }

            //-----------Injeção da  Falha de Estado --------
            if (selecionarFalhas.cbxEstado != null) {
                window.println("There are injected State failures.");
                window.println("Creating state fault.");
                //ir para ispd.motor.falhas.FState.java
                final FState state = new FState();
                state.FIState1(window, cloudQueueNetwork);
            }//if (selecionarFalhas.cbxEstado.isSelected()){
            else {
                window.println("There aren't injected State failures.");
            }
            //-----------Injeção da  Falha de Sobrecarga de Tempo --------
            if (selecionarFalhas.cbxSobrecargaTempo != null) {
                window.println("There are injected time overload failures.");
                window.println("Creating time overload fault.");
                window.println("Development fault.");
                //ir para ispd.motor.falhas.FIOverload.java
                final FIOverload overload = new FIOverload();

            }//if (selecionarFalhas.cbxSobrecargaTempo.isSelected()){
            else {
                window.println("There aren't injected time overload failures.");
            }
            //-----------Injeção da  Falha de Interdependencia --------
            if (selecionarFalhas.cbxInterdependencia != null) {
                window.println("There are injected interdependencies failures" +
                               ".");
                window.println("Creating interdependencie fault.");
                window.println("Development fault.");
                //ir para ispd.motor.falhas.FIInterdependencie.java
                final FIInterdependencie fiInterdependencia =
                        new FIInterdependencie();

            }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){
            else {
                window.println("There aren't injected interdependencies " +
                               "failures.");
            }
            //-----------Injeção da  Falha de Incompatibilidade --------
            if (selecionarFalhas.cbxIncompatibilidade != null) {
                window.println("There are injected Incompatibility failures.");
                window.println("Creating Incompatibility fault.");
                window.println("Development fault.");//ir para ispd.motor
                // .falhas.FIHardware.java
                final FIIncompatibility fiIncompatibility =
                        new FIIncompatibility();

            }//if (selecionarFalhas.cbxIncompatibilidade.isSelected()){
            else {
                window.println("There aren't injected Incompatibility " +
                               "failures.");
            }
            //-----------Injeção de Falhas Pemanentes --------   
            if (selecionarFalhas.cbxFPermanentes != null) {
                window.println("There are injected permanents failures.");
                window.println("Creating permanents fault.");
                window.println("Development fault.");
                //ir para ispd.motor.falhas.FIPermanent.java
                final FIPermanent fiPermanent = new FIPermanent();

            }//if (selecionarFalhas.cbxFPermanentes.isSelected()){
            else {
                window.println("There aren't injected permanents failures.");
            }
            //-----------Injeção da  Falha de Desenho incorreto --------
            if (selecionarFalhas.cbxDesenhoIncorreto != null) {
                window.println("There are injected bad design failures.");
                window.println("Creating bad design fault.");
                window.println("Development fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                final FIBadDesign fibadesign = new FIBadDesign();

            }//if (selecionarFalhas.cbxDesenhoIncorreto.isSelected()){
            else {
                window.println("There aren't injected bad design failures.");
            }
            //-----------Injeção da  Falha Precosse --------
            if (selecionarFalhas.cbxPrecoce != null) {
                window.println("There are injected early failures.");
                window.println("Creating early fault.");
                window.println("Development fault.");
                //ir para ispd.motor.falhas.FIEarly.java
                final FIEarly fiearly = new FIEarly();

            }//if (selecionarFalhas.cbxPrecoce.isSelected()){
            else {
                window.println("There aren't injected early failures.");
            }
//-----------Injeção da  Falha de Tardia --------   
            if (selecionarFalhas.cbxTardia != null) {
                window.println("There are injected late failures.");
                window.println("Creating late fault.");
                window.println("Development fault.");
                //ir para ispd.motor.falhas.FILate.java
                final FILate fiTardia = new FILate();

            }//if (selecionarFalhas.cbxTardia.isSelected()){
            else {
                window.println("There aren't injected late failures.");
            }

            //-----------Injeção da  Falha Transiente --------
            if (selecionarFalhas.cbxTransiente != null) {
                window.println("There are injected transient failures.");
                window.println("Creating transient failure.");
                window.println("Development fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                final FITransient fitransient = new FITransient();

            }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){
            else {
                window.println("There aren't injected transient failures.");
            }
        }//if (selecionarFalhas!=null)
        else
            window.println("There aren't selected faults.");
        if (cloudQueueNetwork.getMaquinasCloud() == null || cloudQueueNetwork.getMaquinasCloud().isEmpty()) {
            window.println("The model has no phisical machines.", Color.orange);
        } else {
            System.out.println("---------------------------------------");
            for (final CS_MaquinaCloud maq :
                    cloudQueueNetwork.getMaquinasCloud()) {
                //Encontra menor caminho entre o escravo e seu mestre
                maq.setStatus(CS_MaquinaCloud.LIGADO);
                maq.determinarCaminhos();//escravo encontra caminhos para seu
                // mestre
                //System.out.println("Maquina " + maq.getId() + " encontrando
                // seus mestres");
            }
        }
        //fim roteamento
        window.incProgresso(5);
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

        this.desligarMaquinas(this, this.getCloudQueueNetwork());
        this.getWindow().incProgresso(30);
        this.getWindow().println("Simulation completed.", Color.green);
    }

    public void addEventos(final List<Tarefa> tarefas) {
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
    public boolean removeFutureEvent(
            final int eventType,
            final CentroServico eventServer,
            final Client eventClient) {
        //remover evento de saida do cliente do servidor
        final var interator = this.eventos.iterator();
        while (interator.hasNext()) {
            final FutureEvent ev = interator.next();
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
            this.processTopEvent();
        }
    }

    /**
     * Executa o laço de repetição responsavel por atender todos eventos da
     * simulação, e adiciona o evento para atualizar os escalonadores.
     */
    private void realizarSimulacaoAtualizaTime() {
        final var updateArray = this.makeUpdateArray();

        while (!this.eventos.isEmpty()) {
            //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            for (final Object[] ob : updateArray) {
                final var event = this.eventos.peek();
                Objects.requireNonNull(event);
                if ((Double) ob[2] < event.getCreationTime()) {
                    final CS_Mestre mestre = (CS_Mestre) ob[0];
                    for (final CS_Processamento maq :
                            mestre.getEscalonador().getEscravos()) {
                        mestre.atualizar(maq, (Double) ob[2]);
                    }
                    ob[2] = (Double) ob[2] + (Double) ob[1];
                }
            }
            this.processTopEvent();
        }
    }

    private void processTopEvent() {
        final FutureEvent eventoAtual = this.eventos.poll();
        Objects.requireNonNull(eventoAtual);
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

    private List<Object[]> makeUpdateArray() {
        return this.getQueueNetwork().getMestres().stream()
                .map(CS_Mestre.class::cast)
                .filter(m -> m.getEscalonador().getTempoAtualizar() != null)
                .map(m -> new Object[] {
                        m,
                        m.getEscalonador().getTempoAtualizar(),
                        m.getEscalonador().getTempoAtualizar()
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void desligarMaquinas(
            final Simulation simulation, final RedeDeFilasCloud qn) {
        for (final CS_MaquinaCloud aux : qn.getMaquinasCloud()) {
            aux.desligar(simulation);
        }
    }
}
