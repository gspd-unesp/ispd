package ispd.motor.carga.impl;

import ispd.motor.carga.WorkloadGeneratorType;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.TwoStageUniform;

import java.util.List;

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
                new TwoStageUniform(
                        compMinimum, compAverage, compMaximum,
                        compProbability),
                new TwoStageUniform(
                        commMinimum, commAverage, commMaximum,
                        commProbability)
        );
    }

    public GlobalWorkloadGenerator(
            final int taskCount, final int taskCreationTime,
            final TwoStageUniform computation,
            final TwoStageUniform communication) {
        super(taskCount, computation, communication);
        this.taskCreationTime = taskCreationTime;
    }

    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return this.makeTasksEvenlyDistributedBetweenMasters(qn, this.taskCount);
    }

    @Override
    public WorkloadGeneratorType getType() {
        return WorkloadGeneratorType.RANDOM;
    }

    @Override
    public String toString() {
        return String.format("%f %f %f %f\n%f %f %f %f\n%d %d %d",
                this.computation.minimum(), this.computation.intervalSplit(),
                this.computation.maximum(),
                this.computation.firstIntervalProbability(),
                this.communication.minimum(), this.communication.maximum(),
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
    protected String makeTaskApplication() {
        return "application1";
    }

    @Override
    protected double makeTaskCreationTime() {
        return this.random.nextExponential(this.taskCreationTime);
    }
}