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
 * Represents a load generated randomly, from a collection of intervals.
 */
public class GlobalWorkloadGenerator extends RandomicWorkloadGenerator {
    private final int taskCreationTime;

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

    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return this.makeTasksEvenlyDistributedBetweenMasters(qn,
                this.taskCount);
    }

    @Override
    public WorkloadGeneratorType getType() {
        return WorkloadGeneratorType.RANDOM;
    }

    @Override
    public String toString() {
        return String.format("%f %f %f %f\n%f %f %f %f\n%d %d %d",
                this.computation.minimum(),
                this.computation.intervalSplit(),
                this.computation.maximum(),
                this.computation.firstIntervalProbability(),
                this.communication.minimum(),
                this.communication.maximum(),
                this.communication.intervalSplit(),
                this.communication.firstIntervalProbability(),
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
    protected double makeTaskCreationTime() {
        return this.random.nextExponential(this.taskCreationTime);
    }
}