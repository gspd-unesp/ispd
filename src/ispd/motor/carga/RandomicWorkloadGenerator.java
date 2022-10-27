package ispd.motor.carga;

import ispd.motor.carga.task.TaskSize;

public abstract class RandomicWorkloadGenerator implements WorkloadGenerator {
    protected final int taskCount;
    protected final TaskSize computation;
    protected final TaskSize communication;

    protected RandomicWorkloadGenerator(
            final int taskCount,
            final TaskSize computation, final TaskSize communication) {
        this.computation = computation;
        this.communication = communication;
        this.taskCount = taskCount;
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

    public int getNumeroTarefas() {
        return this.taskCount;
    }
}