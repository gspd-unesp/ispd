package ispd.motor.workload.impl.task;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;
import ispd.motor.random.TwoStageUniform;

import java.util.List;

public class ExternalTraceTaskBuilder extends TraceTaskBuilder {
    private static final TwoStageUniform SENT_FILE_SIZE =
            new TwoStageUniform(200, 5000, 25000, 0.5);
    private final Distribution random;
    private final double averageComputationPower;

    public ExternalTraceTaskBuilder(
            final List<TraceTaskInfo> traceTasks, final RedeDeFilas qn) {
        this(traceTasks, qn, new Distribution(System.currentTimeMillis()));
    }

    private ExternalTraceTaskBuilder(
            final List<TraceTaskInfo> traceTasks,
            final RedeDeFilas qn, final Distribution random) {
        super(traceTasks);
        this.random = random;
        this.averageComputationPower = qn.averageComputationalPower();
    }

    @Override
    public Tarefa makeTaskFor(final CS_Processamento master) {
        final var task = super.makeTaskFor(master);

        if (this.currTaskInfo.shouldBeCanceled()) {
            task.setLocalProcessamento(master);
            task.cancelar(0);
        }

        return task;
    }

    @Override
    protected double makeTaskCommunicationSize() {
        return ExternalTraceTaskBuilder.SENT_FILE_SIZE.generateValue(this.random);
    }

    @Override
    protected double makeTaskComputationSize() {
        return super.makeTaskComputationSize() * this.averageComputationPower;
    }
}