package ispd.motor.workload.impl;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;
import ispd.motor.random.TwoStageUniform;
import ispd.motor.workload.WorkloadGeneratorType;
import ispd.utils.SequentialIntegerSupplier;

import java.util.List;

/**
 * Generates a workload with randomly-decided sizes from a collection of
 * uniform intervals, and evenly distributes the tasks of such workload
 * across all masters in a {@link RedeDeFilas}.<br>
 * Some particularities:
 * <ul>
 *     <li>This class generates task ids sequentially, starting from {@code 0
 *     }.</li>
 *     <li>This class generates task creation times from an exponential
 *     distribution.</li>
 * </ul>
 *
 * @see RandomicWorkloadGenerator
 * @see SequentialIntegerSupplier
 * @see Distribution#nextExponential(double)
 */
public class GlobalWorkloadGenerator extends RandomicWorkloadGenerator {
    private final int taskCreationTime;

    /**
     * Instantiate a generator with the given properties and interval bounds
     * .<br>
     * Parameters relating to the generation of the computation and
     * communication size of the task are used in a two-stage uniform
     * distribution.
     *
     * @param taskCount        number of tasks to generate
     * @param compMin          computation distribution minimum
     * @param compMax          computation distribution maximum
     * @param compAvg          computation distribution interval split
     * @param compProb         computation distribution first interval
     *                         probability
     * @param commMin          communication distribution minimum
     * @param commMax          communication distribution maximum
     * @param commAvg          communication distribution interval split
     * @param commProb         communication distribution first interval
     *                         probability
     * @param taskCreationTime task creation time ({@code beta} for an
     *                         exponential distribution)
     * @see TwoStageUniform
     * @see Distribution#nextExponential(double)
     */
    public GlobalWorkloadGenerator(
            final int taskCount,
            final int compMin, final int compMax,
            final int compAvg, final double compProb,
            final int commMin, final int commMax,
            final int commAvg, final double commProb,
            final int taskCreationTime) {
        this(
                taskCount, taskCreationTime,
                new TwoStageUniform(compMin, compAvg, compMax, compProb),
                new TwoStageUniform(commMin, commAvg, commMax, commProb)
        );
    }

    /**
     * Instantiate a generator with the given properties, and that uses the
     * given {@link TwoStageUniform}s to generate task computation and
     * communication sizes, respectively.
     *
     * @param taskCount        number of tasks to generate
     * @param taskCreationTime task creation time ({@code beta} for an
     *                         exponential distribution)
     * @param computation      {@link TwoStageUniform} that will generate
     *                         task computation size
     * @param communication    {@link TwoStageUniform} that will generate
     *                         task computation size
     * @see TwoStageUniform
     * @see Distribution#nextExponential(double)
     */
    public GlobalWorkloadGenerator(
            final int taskCount, final int taskCreationTime,
            final TwoStageUniform computation,
            final TwoStageUniform communication) {
        super(
                taskCount, computation, communication,
                new SequentialIntegerSupplier(),
                new Distribution(System.currentTimeMillis())
        );
        this.taskCreationTime = taskCreationTime;
    }

    /**
     * Generates tasks as configured, and distributes then evenly among the
     * masters in the given {@link RedeDeFilas}.
     *
     * @param qn {@link RedeDeFilas} that will host the {@link Tarefa}s.
     * @return generated workload (task list).
     * @see ispd.motor.workload.impl.task.TaskBuilder#makeTasksDistributedAmongMasters(RedeDeFilas, int)
     */
    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return this.makeTasksDistributedAmongMasters(qn, this.taskCount);
    }

    /**
     * @return {@link WorkloadGeneratorType#RANDOM}.
     */
    @Override
    public WorkloadGeneratorType getType() {
        return WorkloadGeneratorType.RANDOM;
    }

    /**
     * The string representation for this workload generator includes its
     * task count, task creation time parameter, and distributions for
     * generating computation and communication sizes.
     *
     * @return representation of the configuration for this generator.
     */
    @Override
    public String toString() {
        return """
                GlobalWorkloadGenerator(
                    taskCount=%d,
                    taskCreationTime=%d,
                    computation=%s,
                    communication=%s,
                )""".formatted(
                this.taskCount,
                this.taskCreationTime,
                this.computation,
                this.communication
        );
    }

    /**
     * @return task creation time configured for this instance.
     */
    public Integer getTimeToArrival() {
        return this.taskCreationTime;
    }

    /**
     * The task user decided by this workload generator is simply the owner
     * of the {@link CS_Processamento} that will host the task.
     *
     * @param master {@link CS_Processamento} that hosts information about
     *               which user the task will be linked with.
     * @return user id of the owner of the given master, to be used to
     * generate a new task.
     * @see CS_Processamento#getProprietario()
     */
    @Override
    protected String makeTaskUser(final CS_Processamento master) {
        return master.getProprietario();
    }

    /**
     * The task creation time generation involves an exponential
     * distribution, with {@code beta} equaling the task creation time
     * configured for this instance.
     *
     * @return task creation time (in seconds), to be used to make a new task.
     */
    @Override
    protected double makeTaskCreationTime() {
        return this.random.nextExponential(this.taskCreationTime);
    }
}