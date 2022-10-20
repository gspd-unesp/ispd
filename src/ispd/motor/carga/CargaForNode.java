package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.random.Distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a workload on a per-node basis.
 */
public class CargaForNode extends GerarCarga {
    private static final double FILE_RECEIVE_TIME = 0.0009765625;
    private static final int ON_NO_DELAY = 120;
    private final String application;
    private final String owner;
    private final String schedulerId;
    private final int taskCount;
    private final double maxComputation;
    private final double minComputation;
    private final double maxCommunication;
    private final double minCommunication;

    /**
     * Create a per-node workload with the given parameters
     *
     * @param application      application of the workload
     * @param owner            name of the user responsible for the workload
     * @param schedulerId      id of the machine responsible for scheduling
     *                         this workload's tasks
     * @param taskCount        total number of tasks
     * @param maxComputation   computation maximum
     * @param minComputation   computation minimum
     * @param maxCommunication communication maximum
     * @param minCommunication communication minimum
     */
    public CargaForNode(
            final String application, final String owner,
            final String schedulerId, final int taskCount,
            final double maxComputation, final double minComputation,
            final double maxCommunication, final double minCommunication) {
        this.application = application;
        this.owner = owner;
        this.schedulerId = schedulerId;
        this.taskCount = taskCount;
        this.maxComputation = maxComputation;
        this.minComputation = minComputation;
        this.maxCommunication = maxCommunication;
        this.minCommunication = minCommunication;
    }

    /**
     * Build a {@link Vector} with data from this instance:
     * {@link #application}, {@link #owner},
     * {@link #schedulerId}, {@link #taskCount},
     * and minimums and maximums for computation and communication.
     * The order of the attributes after {@link #owner} is the same as the
     * one used in this class' {@link #toString()} method.
     *
     * @return {@link Vector} with this instance's data.
     */
    public Vector<String> toVector() {
        final var temp = new Vector<String>(8);
        temp.add(0, this.application);
        temp.add(1, this.owner);
        temp.add(2, this.schedulerId);
        temp.add(3, String.valueOf(this.taskCount));
        temp.add(4, String.valueOf(this.maxComputation));
        temp.add(5, String.valueOf(this.minComputation));
        temp.add(6, String.valueOf(this.maxCommunication));
        temp.add(7, String.valueOf(this.minCommunication));
        return temp;
    }

    @Override
    public List<Tarefa> toTarefaList(final RedeDeFilas rdf) {
        return this.toTaskList(rdf, new SequentialIntegerGenerator());
    }

    @Override
    public String toString() {
        return String.format("%s %d %f %f %f %f",
                this.schedulerId, this.taskCount,
                this.maxComputation, this.minComputation,
                this.maxCommunication, this.minCommunication
        );
    }

    @Override
    public int getTipo() {
        return GerarCarga.FORNODE;
    }

    public List<Tarefa> toTaskList(
            final RedeDeFilas qn, final Generator<Integer> idGenerator) {
        return qn.getMestres().stream()
                .filter(this::hasCorrectId)
                .findFirst()
                .map(proc -> this.makeTaskListOriginatingAt(proc, idGenerator))
                .orElseGet(ArrayList::new);
    }

    private boolean hasCorrectId(final CentroServico m) {
        return m.getId().equals(this.schedulerId);
    }

    private List<Tarefa> makeTaskListOriginatingAt(
            final CentroServico origin, final Generator<Integer> idGenerator) {
        final var random = new Distribution(System.currentTimeMillis());

        return IntStream.range(0, this.taskCount)
                .mapToObj(i -> this.makeTaskOriginatingAt(
                        origin, random, idGenerator))
                .collect(Collectors.toList());
    }

    private Tarefa makeTaskOriginatingAt(
            final CentroServico origin, final Distribution random,
            final Generator<Integer> idGenerator) {

        return new Tarefa(
                idGenerator.next(),
                this.owner,
                this.application,
                origin,
                CargaForNode.fromTwoStageUniform(
                        random,
                        this.minCommunication,
                        this.maxCommunication
                ),
                CargaForNode.FILE_RECEIVE_TIME,
                CargaForNode.fromTwoStageUniform(
                        random,
                        this.minComputation,
                        this.maxComputation
                ),
                random.nextExponential(5) + this.calculateDelay()
        );
    }

    private static double fromTwoStageUniform(
            final Distribution random, final double min, final double max) {
        return random.twoStageUniform(min, min + (max - min) / 2, max, 1);
    }

    private int calculateDelay() {
        return "NoDelay".equals(this.owner) ? CargaForNode.ON_NO_DELAY : 0;
    }

    public int getNumeroTarefas() {
        return this.taskCount;
    }

    public String getEscalonador() {
        return this.schedulerId;
    }

    public String getAplicacao() {
        return this.application;
    }

    public double getMaxComputacao() {
        return this.maxComputation;
    }

    public double getMaxComunicacao() {
        return this.maxCommunication;
    }

    public double getMinComputacao() {
        return this.minComputation;
    }

    public double getMinComunicacao() {
        return this.minCommunication;
    }

    public String getProprietario() {
        return this.owner;
    }
}