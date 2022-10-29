package ispd.motor.carga.workload;

import ispd.escalonador.Escalonador;
import ispd.motor.carga.task.TraceTaskInfo;
import ispd.motor.carga.task.TraceTaskBuilder;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.metricas.MetricasUsuarios;
import ispd.motor.random.Distribution;
import ispd.motor.random.TwoStageUniform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

class TraceLoadHelper {
    private final List<String> users = new ArrayList<>();
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
        this.addUserIfNotPresent(traceTaskInfo.user());

        final var taskList = this.makeTaskBuilderForType(traceTaskInfo)
                .makeTasksEvenlyDistributedBetweenMasters(
                        this.queueNetwork, this.taskCount);

        this.queueNetwork.getMestres().stream()
                .map(CS_Mestre.class::cast)
                .map(CS_Mestre::getEscalonador)
                .map(Escalonador::getMetricaUsuarios)
                .forEach(this::updateSchedulerUserMetrics);

        this.queueNetwork.getUsuarios().addAll(this.users);

        return taskList;
    }

    private void addUserIfNotPresent(final String user) {
        if (!this.isUserPresent(user)) {
            this.users.add(user);
        }
    }

    private TraceTaskBuilder makeTaskBuilderForType(final TraceTaskInfo info) {
        return switch (this.traceType) {
            case "iSPD" -> new TraceTaskBuilder(info);
            case "SWF", "GWF" -> new ExternalTraceTaskBuilder(info);
            default -> throw new IllegalArgumentException(
                    "Unrecognized trace type '%s'".formatted(this.traceType));
        };
    }

    private void updateSchedulerUserMetrics(final MetricasUsuarios metrics) {
        final int count = this.users.size();

        metrics.addAllUsuarios(
                this.users,
                TraceLoadHelper.filledList(0, count),
                TraceLoadHelper.filledList(100, count)
        );
    }

    private boolean isUserPresent(final String userId) {
        return this.queueNetwork.getUsuarios().contains(userId) || this.users.contains(userId);
    }

    private static List<Double> filledList(final double fill, final int count) {
        return DoubleStream.generate(() -> fill)
                .limit(count)
                .boxed()
                .toList();
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