package ispd.motor;

import ispd.motor.filas.Client;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.metricas.Metricas;
import ispd.policy.PolicyMaster;

import java.awt.Color;
import java.util.List;

/**
 * @author denison
 */
public abstract class Simulation {
    private final List<Tarefa> jobs;
    private final ProgressoSimulacao window;
    private RedeDeFilas queueNetwork = null;
    private RedeDeFilasCloud cloudQueueNetwork = null;

    protected Simulation(
            final ProgressoSimulacao window,
            final RedeDeFilas queueNetwork,
            final List<Tarefa> jobs) {
        this.jobs = jobs;
        this.queueNetwork = queueNetwork;
        this.window = window;
    }

    protected Simulation(
            final ProgressoSimulacao window,
            final RedeDeFilasCloud cloudQueueNetwork,
            final List<Tarefa> jobs) {
        this.jobs = jobs;
        this.cloudQueueNetwork = cloudQueueNetwork;
        this.window = window;
    }

    public ProgressoSimulacao getWindow() {
        return this.window;
    }

    RedeDeFilasCloud getCloudQueueNetwork() {
        return this.cloudQueueNetwork;
    }

    public RedeDeFilas getQueueNetwork() {
        return this.queueNetwork;
    }

    List<Tarefa> getJobs() {
        return this.jobs;
    }

    public abstract void simulate();

    public abstract void addFutureEvent(FutureEvent ev);

    public abstract boolean removeFutureEvent(
            int eventType, CentroServico eventServer, Client eventClient);

    public void addJob(final Tarefa job) {
        this.jobs.add(job);
    }

    void initSchedulers() {
        for (final CS_Processamento master : this.queueNetwork.getMestres()) {
            ((CS_Mestre) master).getEscalonador().iniciar();
        }
    }

    void initCloudAllocators() {
        for (final CS_Processamento genericMaster :
                this.cloudQueueNetwork.getMestres()) {
            final CS_VMM master = (CS_VMM) genericMaster;

            System.out.printf("VMM %s iniciando o alocador %s%n",
                    genericMaster.getId(), master.getAlocadorVM().toString());

            master.getAlocadorVM().iniciar();
        }
    }

    void initCloudSchedulers() {
        for (final CS_Processamento genericMaster :
                this.cloudQueueNetwork.getMestres()) {
            final CS_VMM master = (CS_VMM) genericMaster;

            System.out.printf("VMM %s iniciando escalonador %s%n",
                    genericMaster.getId(), master.getEscalonador().toString());

            master.getEscalonador().iniciar();
            master.instanciarCaminhosVMs();
        }
    }

    public void createRouting() {
        for (final CS_Processamento master : this.queueNetwork.getMestres()) {
            final PolicyMaster temp = (PolicyMaster) master;

            // Give access to the master of the queue of future events.
            temp.setSimulation(this);

            // Find the shortest path between the master and its slaves.
            master.determinarCaminhos();
        }
        if (this.queueNetwork.getMaquinas() == null || this.queueNetwork.getMaquinas().isEmpty()) {
            this.window.println("The model has no processing slaves.",
                    Color.orange);
        } else {
            // Find the shortest path between each slave and the master.
            for (final CS_Maquina machine : this.queueNetwork.getMaquinas()) {
                machine.determinarCaminhos();
            }
        }
    }

    public Metricas getMetrics() {
        final Metricas metric = new Metricas(this.queueNetwork,
                this.getTime(null), this.jobs);

        this.window.print("Getting Results.");
        this.window.print(" -> ");

        this.window.incProgresso(5);

        this.window.println("OK", Color.green);
        this.window.print("Falha injetada");
        this.window.println("OK", Color.red);

        return metric;
    }

    public abstract double getTime(Object origin);

    public Metricas getCloudMetrics() {
        this.window.print("Getting Results.");
        this.window.print(" -> ");

        final Metricas metric = new Metricas(this.cloudQueueNetwork,
                this.getTime(null), this.jobs);

        this.window.incProgresso(5);
        this.window.println("OK", Color.green);

        return metric;
    }
}
