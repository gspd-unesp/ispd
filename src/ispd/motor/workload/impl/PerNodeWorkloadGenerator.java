package ispd.motor.workload.impl;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.random.Distribution;
import ispd.motor.random.TwoStageUniform;
import ispd.motor.workload.WorkloadGeneratorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Generates a workload for a single master node in a {@link RedeDeFilas}.<br>
 * Some particularities:
 * <ul>
 *     <li>This class generates computation and communication sizes with
 *     instances of {@link TwoStageUniform}, but used as <b>single</b> stage
 *     uniforms.</li>
 *     <li>This class generates task creation times from an exponential
 *     distribution with {@code beta = 5}, which may be adapted if the task's
 *     owner has id {@link #USER_NO_DELAY}. See
 *     {@link #makeTaskCreationTime()} for details.</li>
 * </ul>
 *
 * @see TwoStageUniform
 * @see RandomicWorkloadGenerator
 * @see Distribution#nextExponential(double)
 */
public class PerNodeWorkloadGenerator extends RandomicWorkloadGenerator {
    private static final String USER_NO_DELAY = "NoDelay";
    private static final int ON_NO_DELAY = 120;
    private final String application;
    private final String owner;
    private final String schedulerId;

    /**
     * Create a per-node workload generator with the given parameters.
     *
     * @param application      workload's originating application
     * @param owner            id (name) of the user who owns the workload
     * @param schedulerId      id of the machine which the workload will be
     *                         directed to
     * @param taskCount        total number of tasks
     * @param maxComputation   computation distribution maximum
     * @param minComputation   computation distribution minimum
     * @param maxCommunication communication distribution maximum
     * @param minCommunication communication distribution minimum
     * @param idSupplier       supplier of ids for the generated tasks. Must
     *                         supply at least {@code taskCount} ids
     */
    public PerNodeWorkloadGenerator(
            final String application, final String owner,
            final String schedulerId, final int taskCount,
            final double maxComputation, final double minComputation,
            final double maxCommunication, final double minCommunication,
            final Supplier<Integer> idSupplier) {
        this(
                application, owner, schedulerId, taskCount,
                new TwoStageUniform(minComputation, maxComputation),
                new TwoStageUniform(minCommunication, maxCommunication),
                idSupplier
        );
    }

    /**
     * Create a per-node workload generator with the given parameters.
     *
     * @param application   workload's originating application
     * @param owner         id (name) of the user who owns the workload
     * @param schedulerId   id of the machine which the workload will be
     *                      directed to
     * @param taskCount     total number of tasks
     * @param computation   {@link TwoStageUniform} for the computation size
     * @param communication {@link TwoStageUniform} for the communication size
     * @param idSupplier    supplier of ids for the generated tasks. Must
     *                      supply at least {@code taskCount} ids
     */
    public PerNodeWorkloadGenerator(
            final String application, final String owner,
            final String schedulerId, final int taskCount,
            final TwoStageUniform computation,
            final TwoStageUniform communication,
            final Supplier<Integer> idSupplier) {
        super(
                taskCount, computation, communication,
                idSupplier, new Distribution(System.currentTimeMillis())
        );
        this.application = application;
        this.owner = owner;
        this.schedulerId = schedulerId;
    }

    /**
     * Build a {@link Vector} with data from this instance, in the following
     * order:
     * <ol>
     *     <li>{@link #application}</li>
     *     <li>{@link #owner}</li>
     *     <li>{@link #schedulerId}</li>
     *     <li>{@link #taskCount}</li>
     *     <li>{@link #computation}'s interval maximum</li>
     *     <li>{@link #computation}'s interval minimum</li>
     *     <li>{@link #communication}'s interval maximum</li>
     *     <li>{@link #communication}'s interval minimum</li>
     * </ol>
     *
     * @return {@link Vector} with this instance's data.
     * @see TwoStageUniform
     */
    public Vector<String> toVector() {
        final var temp = new Vector<String>(8);
        temp.add(0, this.application);
        temp.add(1, this.owner);
        temp.add(2, this.schedulerId);
        temp.add(3, String.valueOf(this.taskCount));
        temp.add(4, String.valueOf(this.computation.maximum()));
        temp.add(5, String.valueOf(this.computation.minimum()));
        temp.add(6, String.valueOf(this.communication.maximum()));
        temp.add(7, String.valueOf(this.communication.minimum()));
        return temp;
    }

    /**
     * Generate tasks as configured, all targeted to the first master found
     * with id matching the one in {@link #schedulerId}. If no appropriate
     * master is found, this method returns an empty {@link ArrayList}.
     *
     * @param qn {@link RedeDeFilas} with the master that will host the
     *           {@link Tarefa}s.
     * @return {@link List} of {@link Tarefa}s, configured for the correct
     * master, or an empty {@link ArrayList}, if the {@link RedeDeFilas} has
     * no master with the correct id.
     */
    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return qn.getMestres().stream()
                .filter(this::hasCorrectId)
                .findFirst()
                .map(this::makeTaskListOriginatingAt)
                .orElseGet(ArrayList::new);
    }

    private boolean hasCorrectId(final CentroServico m) {
        return m.getId().equals(this.schedulerId);
    }

    /**
     * Make a task list of appropriate size, with all tasks originating at
     * the given {@link CS_Processamento}.
     *
     * @param origin that hosts all generated tasks.
     * @return {@link List} with generated {@link Tarefa}s.
     */
    private List<Tarefa> makeTaskListOriginatingAt(final CS_Processamento origin) {
        return IntStream.range(0, this.taskCount)
                .mapToObj(i -> this.makeTaskFor(origin))
                .collect(Collectors.toList());
    }

    /**
     * @return {@link WorkloadGeneratorType#PER_NODE}
     */
    @Override
    public WorkloadGeneratorType getType() {
        return WorkloadGeneratorType.PER_NODE;
    }

    /**
     * The string representation for this workload generator includes is task
     * count, application, owner, scheduler id, and distributions for
     * generating computation and communication sizes.
     *
     * @return representation of the configuration for this generator.
     */
    @Override
    public String toString() {
        return """
                PerNodeWorkloadGenerator{
                    taskCount=%d,
                    application='%s',
                    owner='%s',
                    scheduler='%s',
                    computation=%s,
                    communication=%s,
                }""".formatted(
                this.taskCount,
                this.application,
                this.owner,
                this.schedulerId,
                this.computation,
                this.communication
        );
    }

    /**
     * @return id of the scheduler (i.e., master) that will host the tasks
     * generated by this
     */
    public String getEscalonador() {
        return this.schedulerId;
    }

    /**
     * @return that which will be set as the tasks' originating application
     */
    public String getAplicacao() {
        return this.application;
    }

    /**
     * @return the user's id who is going to be set as the tasks' owner
     */
    public String getProprietario() {
        return this.owner;
    }

    @Override
    protected String makeTaskUser(final CS_Processamento master) {
        return this.owner;
    }

    @Override
    protected String makeTaskApplication() {
        return this.application;
    }

    /**
     * The task creation time generation involves an exponential
     * distribution, with {@code beta = 5}, and possibly an added delay
     * depending on the task owner's id.
     *
     * @return task creation time (in seconds), to be used to make a new task.
     * @see #calculateExtraDelay()
     */
    @Override
    protected double makeTaskCreationTime() {
        return this.random.nextExponential(5) + this.calculateExtraDelay();
    }

    /**
     * @return no extra delay (in seconds) for most users, but an extra
     * {@link #ON_NO_DELAY} if the task owner's id is {@link #USER_NO_DELAY}.
     */
    private int calculateExtraDelay() {
        return PerNodeWorkloadGenerator.USER_NO_DELAY.equals(this.owner) ?
                PerNodeWorkloadGenerator.ON_NO_DELAY : 0;
    }
}