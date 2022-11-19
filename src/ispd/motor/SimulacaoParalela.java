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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class SimulacaoParalela extends Simulation {
    private final int numThreads;
    private final List<CentroServico> recursos;
    private final Map<CentroServico, PriorityBlockingQueue<FutureEvent>> threadFilaEventos;
    private final HashMap<CentroServico, ThreadTrabalhador> threadTrabalhador;
    private ExecutorService threadPool;

    /**
     * @param window
     * @param queueNetwork
     * @param jobs
     * @param numThreads
     * @throws IllegalArgumentException
     */
    public SimulacaoParalela(
            final ProgressoSimulacao window,
            final RedeDeFilas queueNetwork,
            final List<Tarefa> jobs,
            final int numThreads) {
        super(window, queueNetwork, jobs);
        this.threadPool = Executors.newFixedThreadPool(numThreads);
        //Cria lista com todos os recursos da grade
        this.recursos = new ArrayList<>();
        this.recursos.addAll(queueNetwork.getMaquinas());
        this.recursos.addAll(queueNetwork.getLinks());
        this.recursos.addAll(queueNetwork.getInternets());
        //Cria um trabalhador e uma fila de evento para cada recurso
        this.threadFilaEventos = new HashMap<>();
        this.threadTrabalhador = new HashMap<>();
        for (final CentroServico rec : queueNetwork.getMestres()) {
            this.threadFilaEventos.put(rec, new PriorityBlockingQueue<>());
            if (((CS_Mestre) rec).getEscalonador().getTempoAtualizar() != null) {
                this.threadTrabalhador.put(
                        rec, new ThreadTrabalhadorDinamico(rec, this));
            } else {
                this.threadTrabalhador.put(
                        rec, new ThreadTrabalhador(rec, this));
            }
        }

        for (final CentroServico rec : this.recursos) {
            this.threadFilaEventos.put(rec, new PriorityBlockingQueue<>());
            this.threadTrabalhador.put(rec, new ThreadTrabalhador(rec, this));
        }

        this.recursos.addAll(queueNetwork.getMestres());
        this.numThreads = numThreads;
        if (this.getQueueNetwork() == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (this.getQueueNetwork().getMestres() == null || this.getQueueNetwork().getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (this.getQueueNetwork().getLinks() == null || this.getQueueNetwork().getLinks().isEmpty()) {
            window.println("The model has no Networks.", Color.orange);
        }
        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException(
                    "One or more  workloads have not been configured.");
        }
    }

    @Override
    public void simulate() {
        System.out.println("Iniciando: " + this.numThreads + " threads");
        this.threadPool = Executors.newFixedThreadPool(this.numThreads);
        this.initSchedulers();
        //Adiciona tarefas iniciais
        for (final CentroServico mestre : this.getQueueNetwork().getMestres()) {
            this.threadPool.execute(new tarefasIniciais(mestre));
        }
        this.threadPool.shutdown();
        while (!this.threadPool.isTerminated()) {
        }
        System.out.println("Iniciando: " + this.numThreads + " threads");
        this.threadPool = Executors.newFixedThreadPool(this.numThreads);

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        //Realizar a simulação
        boolean fim = false;
        while (!fim) {
            fim = true;
            for (final CentroServico rec : this.recursos) {
                if (!this.threadFilaEventos.get(rec).isEmpty() && !this.threadTrabalhador.get(rec).executando) {
                    this.threadTrabalhador.get(rec).executando = true;
                    this.threadPool.execute(this.threadTrabalhador.get(rec));
                    fim = false;
                } else if (!this.threadFilaEventos.get(rec).isEmpty()) {
                    fim = false;
                }
            }
        }
        this.threadPool.shutdown();
        while (!this.threadPool.isTerminated()) {
        }
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        this.getWindow().incProgresso(30);
        this.getWindow().println("Simulation completed.", Color.green);
    }

    @Override
    public void addFutureEvent(final FutureEvent ev) {
        if (ev.getType() == FutureEvent.CHEGADA) {
            this.threadFilaEventos.get(ev.getServidor()).offer(ev);
        } else {
            this.threadFilaEventos.get(ev.getServidor()).offer(ev);
        }
    }

    @Override
    public boolean removeFutureEvent(
            final int eventType,
            final CentroServico eventServer,
            final Client eventClient) {
        //remover evento de saida do cliente do servidor
        for (final var ev : this.threadFilaEventos.get(eventServer)) {
            if (ev.getType() == eventType
                && ev.getServidor().equals(eventServer)
                && ev.getClient().equals(eventClient)) {
                this.threadFilaEventos.get(eventServer).remove(ev);
                return true;
            }
        }
        return false;
    }

    @Override
    public void createRouting() {
        for (final CS_Processamento mst : this.getQueueNetwork().getMestres()) {
            final PolicyMaster temp = (PolicyMaster) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            temp.setSimulation(this);
            //Encontra menor caminho entre o mestre e seus escravos
            this.threadPool.execute(new determinarCaminho(mst));
        }
        if (this.getQueueNetwork().getMaquinas() == null || this.getQueueNetwork().getMaquinas().isEmpty()) {
            this.getWindow().println("The model has no processing slaves.",
                    Color.orange);
        } else {
            for (final CS_Maquina maq : this.getQueueNetwork().getMaquinas()) {
                //Encontra menor caminho entre o escravo e seu mestre
                this.threadPool.execute(new determinarCaminho(maq));
            }

        }
        this.threadPool.shutdown();
        while (!this.threadPool.isTerminated()) {
        }
    }

    @Override
    public double getTime(final Object origin) {
        if (origin != null) {
            return this.threadTrabalhador.get(origin).getRelogioLocal();
        } else {
            double val = 0;
            for (final CentroServico rec : this.recursos) {
                if (this.threadTrabalhador.get(rec).getRelogioLocal() > val) {
                    val = this.threadTrabalhador.get(rec).getRelogioLocal();
                }
            }
            return val;
        }
    }

    private class ThreadTrabalhador implements Runnable {
        private final CentroServico recurso;
        private final Simulation simulacao;
        private double relogioLocal = 0.0;
        private boolean executando = false;

        private ThreadTrabalhador(final CentroServico rec,
                                  final Simulation sim) {
            this.recurso = rec;
            this.simulacao = sim;
        }

        public double getRelogioLocal() {
            return this.relogioLocal;
        }

        protected void setRelogioLocal(final double relogio) {
            this.relogioLocal = relogio;
        }

        public Simulation getSimulacao() {
            return this.simulacao;
        }

        protected void setExecutando(final boolean executando) {
            this.executando = executando;
        }

        public CentroServico getRecurso() {
            return this.recurso;
        }

        @Override
        public void run() {
            //bloqueia este trabalhador
            synchronized (this) {
                while (!SimulacaoParalela.this.threadFilaEventos.get(this.recurso).isEmpty()) {
                    //Verificando ocorencia de erro
                    final FutureEvent eventoAtual =
                            SimulacaoParalela.this.threadFilaEventos.get(this.recurso).poll();
                    if (eventoAtual.getCreationTime() > this.relogioLocal) {
                        this.relogioLocal = eventoAtual.getCreationTime();
                    }
                    switch (eventoAtual.getType()) {
                        case FutureEvent.CHEGADA:
                            eventoAtual.getServidor().chegadaDeCliente(this.simulacao, (Tarefa) eventoAtual.getClient());
                            break;
                        case FutureEvent.ATENDIMENTO:
                            eventoAtual.getServidor().atendimento(this.simulacao, (Tarefa) eventoAtual.getClient());
                            break;
                        case FutureEvent.SAIDA:
                            eventoAtual.getServidor().saidaDeCliente(this.simulacao, (Tarefa) eventoAtual.getClient());
                            break;
                        case FutureEvent.ESCALONAR:
                            eventoAtual.getServidor().requisicao(this.simulacao, null, FutureEvent.ESCALONAR);
                            break;
                        default:
                            eventoAtual.getServidor().requisicao(this.simulacao, (Mensagem) eventoAtual.getClient(), eventoAtual.getType());
                            break;
                    }
                }
                this.executando = false;
            }
        }
    }

    private class ThreadTrabalhadorDinamico extends ThreadTrabalhador {
        private Object[] item;

        private ThreadTrabalhadorDinamico(
                final CentroServico rec, final Simulation sim) {
            super(rec, sim);
            if (rec instanceof CS_Mestre mestre) {
                if (mestre.getEscalonador().getTempoAtualizar() != null) {
                    this.item = new Object[3];
                    this.item[0] = mestre;
                    this.item[1] = mestre.getEscalonador().getTempoAtualizar();
                    this.item[2] = mestre.getEscalonador().getTempoAtualizar();
                }
            }
        }

        @Override
        public void run() {
            //bloqueia este trabalhador
            synchronized (this) {
                while (!SimulacaoParalela.this.threadFilaEventos.get(this.getRecurso()).isEmpty()) {
                    if ((Double) this.item[2] < SimulacaoParalela.this.threadFilaEventos.get(this.getRecurso()).peek().getCreationTime()) {
                        final CS_Mestre mestre = (CS_Mestre) this.item[0];
                        for (final CS_Processamento maq :
                                mestre.getEscalonador().getEscravos()) {
                            mestre.atualizar(maq, (Double) this.item[2]);
                        }
                        this.item[2] =
                                (Double) this.item[2] + (Double) this.item[1];
                    }
                    final FutureEvent eventoAtual =
                            SimulacaoParalela.this.threadFilaEventos.get(this.getRecurso()).poll();
                    if (eventoAtual.getCreationTime() > this.getRelogioLocal()) {
                        this.setRelogioLocal(eventoAtual.getCreationTime());
                    }
                    switch (eventoAtual.getType()) {
                        case FutureEvent.CHEGADA:
                            eventoAtual.getServidor().chegadaDeCliente(this.getSimulacao(), (Tarefa) eventoAtual.getClient());
                            break;
                        case FutureEvent.ATENDIMENTO:
                            eventoAtual.getServidor().atendimento(this.getSimulacao(), (Tarefa) eventoAtual.getClient());
                            break;
                        case FutureEvent.SAIDA:
                            eventoAtual.getServidor().saidaDeCliente(this.getSimulacao(), (Tarefa) eventoAtual.getClient());
                            break;
                        case FutureEvent.ESCALONAR:
                            eventoAtual.getServidor().requisicao(this.getSimulacao(), null, FutureEvent.ESCALONAR);
                            break;
                        default:
                            eventoAtual.getServidor().requisicao(this.getSimulacao(), (Mensagem) eventoAtual.getClient(), eventoAtual.getType());
                            break;
                    }
                }
                this.setExecutando(false);
            }
        }
    }

    private static class determinarCaminho implements Runnable {

        private final CS_Processamento mst;

        private determinarCaminho(final CS_Processamento mst) {
            this.mst = mst;
        }

        @Override
        public void run() {
            this.mst.determinarCaminhos();
        }
    }

    private class tarefasIniciais implements Runnable {

        private final CentroServico mestre;

        private tarefasIniciais(final CentroServico mestre) {
            this.mestre = mestre;
        }

        @Override
        public void run() {
            synchronized (SimulacaoParalela.this.threadFilaEventos.get(this.mestre)) {
                System.out.println("Nome: " + Thread.currentThread().getName() + " Vou criar tarefas do " + this.mestre.getId());
                for (final Tarefa tarefa : SimulacaoParalela.this.getJobs()) {
                    if (tarefa.getOrigem() == this.mestre) {
                        //criar evento...
                        final FutureEvent evt =
                                new FutureEvent(tarefa.getTimeCriacao(),
                                        FutureEvent.CHEGADA,
                                        tarefa.getOrigem(), tarefa);
                        SimulacaoParalela.this.threadFilaEventos.get(this.mestre).add(evt);
                    }
                }
                System.out.println("Nome: " + Thread.currentThread().getName() + " foram criadas " + SimulacaoParalela.this.threadFilaEventos.get(this.mestre).size());
            }
        }
    }
}
