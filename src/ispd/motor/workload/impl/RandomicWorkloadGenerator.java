package ispd.motor.workload.impl;

import ispd.motor.random.Distribution;
import ispd.motor.random.TwoStageUniform;
import ispd.motor.workload.WorkloadGenerator;
import ispd.motor.workload.impl.task.TaskBuilder;

import java.util.function.Supplier;

/* package-private */
abstract class RandomicWorkloadGenerator extends TaskBuilder implements WorkloadGenerator {
    protected final int taskCount;
    protected final TwoStageUniform computation;
    protected final TwoStageUniform communication;
    protected final Distribution random;

    private final Supplier<Integer> idSupplier;

    /* package-private */ RandomicWorkloadGenerator(
            final int taskCount,
            final TwoStageUniform computation,
            final TwoStageUniform communication,
            final Supplier<Integer> idSupplier,
            final Distribution random) {
        this.computation = computation;
        this.communication = communication;
        this.taskCount = taskCount;
        this.idSupplier = idSupplier;
        this.random = random;
    }

    @Override
    protected int makeTaskId() {
        return this.idSupplier.get();
    }

    @Override
    protected double makeTaskCommunicationSize() {
        return this.communication.generateValue(this.random);
    }

    @Override
    protected double makeTaskComputationSize() {
        return this.computation.generateValue(this.random);
    }

    public double getAverageComputacao() {
        return this.computation.intervalSplit();
    }

    public double getAverageComunicacao() {
        return this.communication.intervalSplit();
    }

    public double getProbabilityComputacao() {
        return this.computation.firstIntervalProbability();
    }

    public double getProbabilityComunicacao() {
        return this.communication.firstIntervalProbability();
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

    public int getNumeroTarefas() {
        return this.taskCount;
    }
}