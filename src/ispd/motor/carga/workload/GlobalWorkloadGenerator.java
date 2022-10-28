package ispd.motor.carga.workload;

import ispd.motor.carga.task.GlobalTaskBuilder;
import ispd.motor.carga.task.TaskSize;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;

import java.util.List;

/**
 * Represents a load generated randomly, from a collection of intervals.
 */
public class GlobalWorkloadGenerator extends RandomicWorkloadGenerator {
    private final int arrivalTime;

    public GlobalWorkloadGenerator(
            final int taskCount,
            final int compMinimum, final int compMaximum,
            final int compAverage, final double compProbability,
            final int commMinimum, final int commMaximum,
            final int commAverage, final double commProbability,
            final int arrivalTime) {
        this(taskCount, arrivalTime,
                new TaskSize(
                        compMinimum, compMaximum,
                        compAverage, compProbability),
                new TaskSize(
                        commMinimum, commMaximum,
                        commAverage, commProbability)
        );
    }

    public GlobalWorkloadGenerator(
            final int taskCount, final int arrivalTime,
            final TaskSize computation, final TaskSize communication) {
        super(taskCount, computation, communication);
        this.arrivalTime = arrivalTime;
    }

    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return new GlobalTaskBuilder(
                this.arrivalTime, this.computation, this.communication)
                .makeTasksEvenlyDistributedBetweenMasters(qn, this.taskCount);
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
                0, this.arrivalTime, this.taskCount);
    }

    public Integer getTimeToArrival() {
        return this.arrivalTime;
    }
}