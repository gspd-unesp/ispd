package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a load generated randomly, from a collection of intervals.
 */
public class CargaRandom extends GerarCarga {
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

    public CargaRandom(
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
    public List<Tarefa> toTarefaList(final RedeDeFilas rdf) {
        final var masters = rdf.getMestres();
        final var masterCount = masters.size();

        final var taskBuilder = new RandomTaskBuilder();
        final var tasks = masters.stream()
                .flatMap(m -> taskBuilder.makeMultipleTasksFrom(m,
                        this.taskCount / masterCount))
                .collect(Collectors.toList());

        final var remainderTasksMaster = masters.get(0);

        final var remainingTasks = taskBuilder
                .makeMultipleTasksFrom(remainderTasksMaster,
                        this.taskCount % masterCount)
                .toList();

        tasks.addAll(remainingTasks);

        return tasks;
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

    @Override
    public int getTipo() {
        return GerarCarga.RANDOM;
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
        public Tarefa makeTaskFrom(final CS_Processamento master) {
            return new Tarefa(
                    this.idGenerator.next(),
                    master.getProprietario(),
                    "application1",
                    master,
                    this.random.twoStageUniform(
                            CargaRandom.this.commMinimum,
                            CargaRandom.this.commAverage,
                            CargaRandom.this.commMaximum,
                            CargaRandom.this.commProbability
                    ),
                    CargaRandom.FILE_RECEIVE_TIME_1KB,
                    this.random.twoStageUniform(
                            CargaRandom.this.compMinimum,
                            CargaRandom.this.compAverage,
                            CargaRandom.this.compMaximum,
                            CargaRandom.this.compProbability
                    ),
                    this.random.nextExponential(CargaRandom.this.arrivalTime)
            );
        }
    }
}