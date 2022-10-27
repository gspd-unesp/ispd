package ispd.motor.carga;

import ispd.motor.carga.task.TaskSize;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Represents a workload on a per-node basis.
 */
public class PerNodeWorkloadGenerator implements WorkloadGenerator {
    private final String application;
    private final String owner;
    private final String schedulerId;
    private final int taskCount;
    private final TaskSize computation;
    private final TaskSize communication;

    /**
     * Create a per-node workload generator with the given parameters
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
    public PerNodeWorkloadGenerator(
            final String application, final String owner,
            final String schedulerId, final int taskCount,
            final double maxComputation, final double minComputation,
            final double maxCommunication, final double minCommunication) {
        this(application, owner, schedulerId, taskCount,
                new TaskSize(maxComputation, minComputation),
                new TaskSize(maxCommunication, minCommunication)
        );
    }

    /**
     * Create a per-node workload generator with the given parameters
     *
     * @param application   application of the workload
     * @param owner         name of the user responsible for the workload
     * @param schedulerId   id of the machine responsible for scheduling
     *                      this workload's tasks
     * @param taskCount     total number of tasks
     * @param computation   {@link TaskSize} about the computation size
     * @param communication {@link TaskSize} about the communication size
     */
    public PerNodeWorkloadGenerator(
            final String application, final String owner,
            final String schedulerId, final int taskCount,
            final TaskSize computation, final TaskSize communication) {
        this.application = application;
        this.owner = owner;
        this.schedulerId = schedulerId;
        this.taskCount = taskCount;
        this.computation = computation;
        this.communication = communication;
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
        temp.add(4, String.valueOf(this.computation.minimum()));
        temp.add(5, String.valueOf(this.computation.maximum()));
        temp.add(6, String.valueOf(this.communication.minimum()));
        temp.add(7, String.valueOf(this.communication.maximum()));
        return temp;
    }

    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return qn.getMestres().stream()
                .filter(this::hasCorrectId)
                .findFirst()
                .map(this::makeTaskListOriginatingAt)
                .orElseGet(ArrayList::new);
    }

    @Override
    public WorkloadGeneratorType getType() {
        return WorkloadGeneratorType.PER_NODE;
    }

    private boolean hasCorrectId(final CentroServico m) {
        return m.getId().equals(this.schedulerId);
    }

    private List<Tarefa> makeTaskListOriginatingAt(final CS_Processamento origin) {
        return new PerNodeTaskBuilder()
                .makeMultipleTasksFor(origin, this.taskCount)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("%s %d %f %f %f %f",
                this.schedulerId, this.taskCount,
                this.computation.maximum(), this.computation.minimum(),
                this.communication.maximum(), this.communication.minimum()
        );
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
        return this.computation.maximum();
    }

    public double getMaxComunicacao() {
        return this.communication.maximum();
    }

    public double getMinComputacao() {
        return this.computation.minimum();
    }

    public double getMinComunicacao() {
        return this.communication.minimum();
    }

    public String getProprietario() {
        return this.owner;
    }

    private class PerNodeTaskBuilder extends TaskBuilder {
        private static final int ON_NO_DELAY = 120;

        @Override
        public Tarefa makeTaskFor(final CS_Processamento master) {
            return new Tarefa(
                    this.idGenerator.next(),
                    PerNodeWorkloadGenerator.this.owner,
                    PerNodeWorkloadGenerator.this.application,
                    master,
                    PerNodeWorkloadGenerator.this.communication.rollTwoStageUniform(this.random),
                    TaskBuilder.FILE_RECEIVE_TIME,
                    PerNodeWorkloadGenerator.this.computation.rollTwoStageUniform(this.random),
                    this.random.nextExponential(5) + this.calculateDelay()
            );
        }

        private int calculateDelay() {
            return "NoDelay".equals(PerNodeWorkloadGenerator.this.owner) ?
                    PerNodeTaskBuilder.ON_NO_DELAY : 0;
        }
    }
}