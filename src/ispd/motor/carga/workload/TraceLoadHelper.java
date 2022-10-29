package ispd.motor.carga.workload;

import ispd.motor.carga.task.TraceTaskBuilder;
import ispd.motor.carga.task.TraceTaskInfo;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;
import ispd.motor.random.TwoStageUniform;

import java.util.List;

class TraceLoadHelper {
    private final int taskCount;
    private final String traceType;
    private final RedeDeFilas queueNetwork;

    TraceLoadHelper(
            final RedeDeFilas qn, final String traceType, final int taskCount) {
        this.queueNetwork = qn;
        this.traceType = traceType;
        this.taskCount = taskCount;
    }

    public List<Tarefa> processTaskInfo(final TraceTaskInfo traceTaskInfo) {
        return this.makeTaskBuilderForType(traceTaskInfo)
                .makeTasksEvenlyDistributedBetweenMasters(
                        this.queueNetwork, this.taskCount);
    }

    private TraceTaskBuilder makeTaskBuilderForType(final TraceTaskInfo info) {
        return switch (this.traceType) {
            case "iSPD" -> new TraceTaskBuilder(info);
            case "SWF", "GWF" -> new ExternalTraceTaskBuilder(info);
            default -> throw new IllegalArgumentException(
                    "Unrecognized trace type '%s'".formatted(this.traceType));
        };
    }

    private double averageComputationalPower() {
        return this.queueNetwork.getMaquinas().stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .average()
                .orElse(0.0);
    }

    private class ExternalTraceTaskBuilder extends TraceTaskBuilder {
        private static final TwoStageUniform SENT_FILE_SIZE =
                new TwoStageUniform(200, 5000, 25000, 0.5);
        private final Distribution random;

        public ExternalTraceTaskBuilder(final TraceTaskInfo traceTaskInfo) {
            this(traceTaskInfo, new Distribution(System.currentTimeMillis()));
        }

        private ExternalTraceTaskBuilder(
                final TraceTaskInfo traceTaskInfo, final Distribution random) {
            super(traceTaskInfo);
            this.random = random;
        }

        @Override
        public Tarefa makeTaskFor(final CS_Processamento master) {
            final var task = super.makeTaskFor(master);

            if (this.traceTaskInfo.shouldBeCanceled()) {
                task.setLocalProcessamento(master);
                task.cancelar(0);
            }

            return task;
        }

        @Override
        protected double calculateSentFileSize() {
            return ExternalTraceTaskBuilder.SENT_FILE_SIZE.generateValue(this.random);
        }

        @Override
        protected double calculateProcessingTime() {
            return super.calculateProcessingTime()
                   * TraceLoadHelper.this.averageComputationalPower();
        }
    }
}