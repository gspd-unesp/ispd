package ispd.motor.carga;

import ispd.motor.carga.task.TaskSize;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;

/**
 * Represents a load generated randomly, from a collection of intervals.
 */
public class RandomWorkloadGenerator implements WorkloadGenerator {
    private final int taskCount;
    private final int arrivalTime;
    private final TaskSize computation;
    private final TaskSize communication;

    public RandomWorkloadGenerator(
            final int taskCount,
            final int compMinimum, final int compMaximum,
            final int compAverage, final double compProbability,
            final int commMinimum, final int commMaximum,
            final int commAverage, final double commProbability,
            final int arrivalTime) {
        this(taskCount,
                arrivalTime, new TaskSize(
                        compMinimum, compMaximum,
                        compAverage, compProbability),
                new TaskSize(
                        commMinimum, commMaximum,
                        commAverage, commProbability)
        );
    }

    public RandomWorkloadGenerator(
            final int taskCount, final int arrivalTime,
            final TaskSize computation,
            final TaskSize communication) {
        this.taskCount = taskCount;
        this.computation = computation;
        this.communication = communication;
        this.arrivalTime = arrivalTime;
    }

    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return new RandomTaskBuilder()
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

    public Integer getNumeroTarefas() {
        return this.taskCount;
    }

    public double getAverageComputacao() {
        return this.computation.average();
    }

    public double getAverageComunicacao() {
        return this.communication.average();
    }

    public double getProbabilityComputacao() {
        return this.computation.probability();
    }

    public double getProbabilityComunicacao() {
        return this.communication.probability();
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

    public Integer getTimeToArrival() {
        return this.arrivalTime;
    }

    private class RandomTaskBuilder extends TaskBuilder {
        @Override
        public Tarefa makeTaskFor(final CS_Processamento master) {
            return new Tarefa(
                    this.idGenerator.next(),
                    master.getProprietario(),
                    "application1",
                    master,
                    RandomWorkloadGenerator.this.communication.rollTwoStageUniform(this.random),
                    TaskBuilder.FILE_RECEIVE_TIME,
                    RandomWorkloadGenerator.this.computation.rollTwoStageUniform(this.random),
                    this.random.nextExponential(RandomWorkloadGenerator.this.arrivalTime)
            );
        }
    }
}