package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a workload on a per-node basis.
 */
public class CargaForNode extends GerarCarga {
    private static final double FILE_RECEIVE_TIME = 0.0009765625;
    private final String application;
    private final int taskCount;
    private final Double minComputation;
    private final Double maxComputation;
    private final Double minCommunication;
    private final Double maxCommunication;
    private final String owner;
    private final String scheduler;
    private int nextAvailableTaskId = 0;

    public CargaForNode(
            final String application, final String owner,
            final String scheduler, final int taskCount,
            final double maxComputation, final double minComputation,
            final double maxCommunication, final double minCommunication) {
        this.application = application;
        this.owner = owner;
        this.scheduler = scheduler;
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
     * {@link #application}, {@link #owner}, {@link #scheduler}, {@link #taskCount},
     * and minimums and maximums for computation and communication.
     * The order of the attributes after {@link #owner} is the same as the one used in this class'
     * {@link #toString()} and {@link #newGerarCarga(String)} methods.
     *
     * @return {@link Vector} with this instance's data.
     */
    public Vector toVector() {
        final Vector temp = new Vector<Integer>(8);
        temp.add(0, this.application);
        temp.add(1, this.owner);
        temp.add(2, this.scheduler);
        temp.add(3, this.taskCount);
        temp.add(4, this.maxComputation);
        temp.add(5, this.minComputation);
        temp.add(6, this.maxCommunication);
        temp.add(7, this.minCommunication);
        return temp;
    }

    @Override
    public List<Tarefa> toTarefaList(final RedeDeFilas rdf) {
        return this.findMasterWithCorrectScheduler(rdf)
                .map(this::makeTaskListFromMaster)
                .orElseGet(ArrayList::new);
    }

    private Optional<CS_Processamento> findMasterWithCorrectScheduler(final RedeDeFilas rdf) {
        return rdf.getMestres().stream()
                .filter(this::hasCorrectScheduler)
                .findFirst();
    }

    private List<Tarefa> makeTaskListFromMaster(final CS_Processamento master) {
        final var random = new Distribution(System.currentTimeMillis());

        return IntStream.range(0, this.taskCount)
                .mapToObj(i -> this.makeTaskWith(master, random, calculateDelay()))
                .collect(Collectors.toList());
    }

    private boolean hasCorrectScheduler(final CS_Processamento m) {
        return m.getId().equals(this.scheduler);
    }

    private Tarefa makeTaskWith(
            final CS_Processamento master, final Distribution random, final int delay) {

        return new Tarefa(
                this.nextTaskId(),
                this.owner,
                this.application,
                master,
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
                random.nextExponential(5) + delay
        );
    }

    private int calculateDelay() {
        return "NoDelay".equals(this.owner) ? 120 : 0;
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

    public Integer getNumeroTarefas() {
        return this.taskCount;
    }

    @Override
    public String toString() {
        return String.format("%s %d %f %f %f %f",
                this.scheduler, this.taskCount,
                this.maxComputation, this.minComputation,
                this.maxCommunication, this.minCommunication
        );
    }

    @Override
    public int getTipo() {
        return GerarCarga.FORNODE;
    }

    void setInicioIdentificadorTarefa(final int taskIdentifierStart) {
        this.nextAvailableTaskId = taskIdentifierStart;
    }

    public String getEscalonador() {
        return this.scheduler;
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