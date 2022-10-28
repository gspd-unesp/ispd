package ispd.motor.carga.workload;

import ispd.motor.carga.task.TaskSize;
import ispd.motor.random.Distribution;

public abstract class RandomicWorkloadGenerator extends AbstractWorkloadGenerator {
    protected final int taskCount;
    protected final TaskSize computation;
    protected final TaskSize communication;
    protected final Distribution random;

    private int nextAvailableId = 0;

    protected RandomicWorkloadGenerator(
            final int taskCount,
            final TaskSize computation, final TaskSize communication) {
        this.computation = computation;
        this.communication = communication;
        this.taskCount = taskCount;
        this.random = new Distribution(System.currentTimeMillis());
    }

    @Override
    protected int makeTaskId() {
        final int id = this.nextAvailableId;
        this.nextAvailableId++;
        return id;
    }

    @Override
    protected double makeTaskCommunicationSize() {
        return this.communication.rollTwoStageUniform(this.random);
    }

    @Override
    protected double makeTaskComputationSize() {
        return this.computation.rollTwoStageUniform(this.random);
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