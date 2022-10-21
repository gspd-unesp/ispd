package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;

/**
 * Represents a load generated randomly, from a collection of intervals.
 */
public class RandomWorkloadGenerator implements WorkloadGenerator {
    private static final double FILE_RECEIVE_TIME_1KB = 0.0009765625;
    private final int taskCount;
    private final int compMinimum;
    private final int compMaximum;
    private final int compAverage;
    private final double compProbability;
    private final int commMinimum;
    private final int commMaximum;
    private final int commAverage;
    private final double commProbability;
    private final int arrivalTime;

    public RandomWorkloadGenerator(
            final int taskCount,
            final int compMinimum, final int compMaximum,
            final int compAverage, final double compProbability,
            final int commMinimum, final int commMaximum,
            final int commAverage, final double commProbability,
            final int arrivalTime) {
        this.taskCount = taskCount;
        this.compMinimum = compMinimum;
        this.compMaximum = compMaximum;
        this.compAverage = compAverage;
        this.compProbability = compProbability;
        this.commMinimum = commMinimum;
        this.commMaximum = commMaximum;
        this.commAverage = commAverage;
        this.commProbability = commProbability;
        this.arrivalTime = arrivalTime;
    }

    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return new RandomTaskBuilder()
                .makeTasksDistributedBetweenMasters(qn, this.taskCount);
    }

    @Override
    public WorkloadGeneratorType getType() {
        return WorkloadGeneratorType.RANDOM;
    }

    @Override
    public String toString() {
        return String.format("%d %d %d %f\n%d %d %d %f\n%d %d %d",
                this.compMinimum, this.compAverage,
                this.compMaximum, this.compProbability,
                this.commMinimum, this.commMaximum,
                this.commAverage, this.commProbability,
                0, this.arrivalTime, this.taskCount);
    }

    public Integer getNumeroTarefas() {
        return this.taskCount;
    }

    public Integer getAverageComputacao() {
        return this.compAverage;
    }

    public Integer getAverageComunicacao() {
        return this.commAverage;
    }

    public Double getProbabilityComputacao() {
        return this.compProbability;
    }

    public Double getProbabilityComunicacao() {
        return this.commProbability;
    }

    public Integer getMaxComputacao() {
        return this.compMaximum;
    }

    public Integer getMaxComunicacao() {
        return this.commMaximum;
    }

    public Integer getMinComputacao() {
        return this.compMinimum;
    }

    public Integer getMinComunicacao() {
        return this.commMinimum;
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
                    this.random.twoStageUniform(
                            RandomWorkloadGenerator.this.commMinimum,
                            RandomWorkloadGenerator.this.commAverage,
                            RandomWorkloadGenerator.this.commMaximum,
                            RandomWorkloadGenerator.this.commProbability
                    ),
                    RandomWorkloadGenerator.FILE_RECEIVE_TIME_1KB,
                    this.random.twoStageUniform(
                            RandomWorkloadGenerator.this.compMinimum,
                            RandomWorkloadGenerator.this.compAverage,
                            RandomWorkloadGenerator.this.compMaximum,
                            RandomWorkloadGenerator.this.compProbability
                    ),
                    this.random.nextExponential(RandomWorkloadGenerator.this.arrivalTime)
            );
        }
    }
}