package ispd.motor;

import ispd.motor.filas.Client;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.policy.PolicyMaster;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public class SimulacaoSequencial extends Simulation {
    private final PriorityQueue<FutureEvent> eventos = new PriorityQueue<>();
    private double time = 0;

    /**
     * @param window
     * @param queueNetwork
     * @param jobs
     * @throws IllegalArgumentException
     */
    public SimulacaoSequencial(final ProgressoSimulacao window,
                               final RedeDeFilas queueNetwork,
                               final List<Tarefa> jobs) {
        super(window, queueNetwork, jobs);

        if (queueNetwork == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (queueNetwork.getMestres() == null || queueNetwork.getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (queueNetwork.getLinks() == null || queueNetwork.getLinks().isEmpty()) {
            window.println("The model has no Networks.", Color.orange);
        }
        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have " +
                                               "not been configured.");
        }
        window.print("Creating routing.");
        window.print(" -> ");
        window.print("Creating failuresSS.");

        for (final CS_Processamento mst : queueNetwork.getMestres()) {
            final PolicyMaster temp = (PolicyMaster) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            temp.setSimulation(this);
            //Encontra menor caminho entre o mestre e seus escravos
            mst.determinarCaminhos(); //mestre encontra caminho para seus
            // escravos
        }

        window.incProgresso(5);
        window.println("OK", Color.green);

        if (queueNetwork.getMaquinas() == null || queueNetwork.getMaquinas().isEmpty()) {
            window.println("The model has no processing slaves.", Color.orange);
        } else {
            for (final CS_Maquina maq : queueNetwork.getMaquinas()) {
                //Encontra menor caminho entre o escravo e seu mestre
                maq.determinarCaminhos();//escravo encontra caminhos para seu
                // mestre
            }
        }
        //fim roteamento
        window.incProgresso(5);
    }

    @Override
    public void simulate() {
        //inicia os escalonadores
        this.initSchedulers();
        //adiciona chegada das tarefas na lista de eventos futuros
        this.addEventos(this.getJobs());
        if (this.atualizarEscalonadores()) {
            this.realizarSimulacaoAtualizaTime();
        } else {
            this.realizarSimulacao();
        }
        this.getWindow().incProgresso(30);
        this.getWindow().println("Simulation completed.", Color.green);
        //Centralizando métricas de usuários
    }

    private void addEventos(final Iterable<? extends Tarefa> tarefas) {
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
        for (final CS_Processamento mst : this.getQueueNetwork().getMestres()) {
            final CS_Mestre mestre = (CS_Mestre) mst;
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
                default -> eventoAtual.getServidor().requisicao(this,
                        (Mensagem) eventoAtual.getClient(),
                        eventoAtual.getType());
            }
        }
    }
}
