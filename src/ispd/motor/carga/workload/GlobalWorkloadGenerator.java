package ispd.motor.carga.workload;

import ispd.motor.carga.task.TaskSize;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a load generated randomly, from a collection of intervals.
 */
public class GlobalWorkloadGenerator extends RandomicWorkloadGenerator {
    private final int taskCreationTime;

    public GlobalWorkloadGenerator(
            final int taskCount,
            final int compMinimum, final int compMaximum,
            final int compAverage, final double compProbability,
            final int commMinimum, final int commMaximum,
            final int commAverage, final double commProbability,
            final int taskCreationTime) {
        this(taskCount, taskCreationTime,
                new TaskSize(
                        compMinimum, compMaximum,
                        compAverage, compProbability),
                new TaskSize(
                        commMinimum, commMaximum,
                        commAverage, commProbability)
        );
    }

    public GlobalWorkloadGenerator(
            final int taskCount, final int taskCreationTime,
            final TaskSize computation, final TaskSize communication) {
        super(taskCount, computation, communication);
        this.taskCreationTime = taskCreationTime;
    }

    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return this.makeTasksEvenlyDistributedBetweenMasters(qn
        );
    }

    private List<Tarefa> makeTasksEvenlyDistributedBetweenMasters(final RedeDeFilas qn) {
        final var masters = qn.getMestres();

        return IntStream.range(0, this.taskCount)
                .map(i -> i % masters.size())
                .mapToObj(masters::get)
                .map(this::makeTaskFor)
                .collect(Collectors.toList());
    }

    @Override
    public WorkloadGeneratorType getType() {
        return WorkloadGeneratorType.RANDOM;
    }

    @Override
    public String toString() {
        return String.format("%f %f %f %f\n%f %f %f %f\n%d %d %d",
                this.computation.minimum(), this.computation.average(),
                this.computation.maximum(), this.computation.probability(),
                this.communication.minimum(), this.communication.maximum(),
                this.communication.average(), this.communication.probability(),
                0, this.taskCreationTime, this.taskCount);
    }

    public Integer getTimeToArrival() {
        return this.taskCreationTime;
    }

    @Override
    protected String makeTaskUser(final CS_Processamento master) {
        return master.getProprietario();
    }

    @Override
    protected String makeTaskApplication() {
        return "application1";
    }

    @Override
    protected double makeTaskCreationTime() {
        return this.random.nextExponential(this.taskCreationTime);
    }
}