package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
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
    private final String application;
    private final String owner;
    private final String schedulerId;
    private final int taskCount;
    private final double maxComputation;
    private final double minComputation;
    private final double maxCommunication;
    private final double minCommunication;
    private int nextAvailableTaskId = 0;

    /**
     * Create a per-node workload with the given parameters
     *
     * @param application      application of the workload
     * @param owner            name of the user responsible for the workload
     * @param schedulerId      id of the machine responsible for scheduling this workload's tasks
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
     * Create an instance from the given {@link String}.
     * The string is expected to have the same format as one produced by this class' {@link #toString()} method.
     *
     * @param s the {@link String} to be parsed into an instance of this class.
     * @return the constructed instance.
     * @throws ArrayIndexOutOfBoundsException if the string does not have enough 'fields' present
     * @throws NumberFormatException          if fields 2-6 are not formatted correctly.
     */
    /* package-private */
    static GerarCarga newGerarCarga(final String s) {
        final var values = s.split(" ");
        final String scheduler = values[0];
        final int taskCount = Integer.parseInt(values[1]);
        final double maxComputation = Double.parseDouble(values[2]);
        final double minComputation = Double.parseDouble(values[3]);
        final double maxCommunication = Double.parseDouble(values[4]);
        final double minCommunication = Double.parseDouble(values[5]);

        return new CargaForNode(
                "application0",
                "user1",
                scheduler,
                taskCount,
                maxComputation,
                minComputation,
                maxCommunication,
                minCommunication
        );
    }

    /**
     * Build a {@link Vector} with data from this instance:
     * {@link #application}, {@link #owner}, {@link #schedulerId}, {@link #taskCount},
     * and minimums and maximums for computation and communication.
     * The order of the attributes after {@link #owner} is the same as the one used in this class'
     * {@link #toString()} and {@link #newGerarCarga(String)} methods.
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
        return rdf.getMestres().stream()
                .filter(this::hasCorrectId)
                .findFirst()
                .map(this::makeTaskListOriginatingAt)
                .orElseGet(ArrayList::new);
    }

    private boolean hasCorrectId(final CS_Processamento m) {
        return m.getId().equals(this.schedulerId);
    }

    private List<Tarefa> makeTaskListOriginatingAt(final CentroServico origin) {
        final var random = new Distribution(System.currentTimeMillis());

        return IntStream.range(0, this.taskCount)
                .mapToObj(i -> this.makeTaskOriginatingAt(origin, random))
                .collect(Collectors.toList());
    }

    private Tarefa makeTaskOriginatingAt(
            final CentroServico origin, final Distribution random) {

        return new Tarefa(
                this.nextTaskId(),
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

    private int nextTaskId() {
        final var value = this.nextAvailableTaskId;
        this.nextAvailableTaskId++;
        return value;
    }

    private static double fromTwoStageUniform(
            final Distribution random, final Double min, final Double max) {
        return random.twoStageUniform(min, min + (max - min) / 2, max, 1);
    }

    private int calculateDelay() {
        return "NoDelay".equals(this.owner) ? 120 : 0;
    }

    public Integer getNumeroTarefas() {
        return this.taskCount;
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

    void setInicioIdentificadorTarefa(final int newNextId) {
        this.nextAvailableTaskId = newNextId;
    }

    public String getEscalonador() {
        return this.schedulerId;
    }

    public String getAplicacao() {
        return this.application;
    }

    public Double getMaxComputacao() {
        return this.maxComputation;
    }

    public Double getMaxComunicacao() {
        return this.maxCommunication;
    }

    public Double getMinComputacao() {
        return this.minComputation;
    }

    public Double getMinComunicacao() {
        return this.minCommunication;
    }

    public String getProprietario() {
        return this.owner;
    }
}