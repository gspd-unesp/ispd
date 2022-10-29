package ispd.motor.carga.workload;

import ispd.escalonador.Escalonador;
import ispd.motor.carga.task.TaskInfo;
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

    public List<Tarefa> processTaskInfo(final TaskInfo taskInfo) {
        this.addUserIfNotPresent(taskInfo);

        final var taskList = this.makeTaskBuilderForType(taskInfo)
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

    private void addUserIfNotPresent(final TaskInfo info) {
        if (!this.isUserPresent(info.user())) {
            this.addDefaultUser(info.user());
        }
    }

    private TraceTaskBuilder makeTaskBuilderForType(final TaskInfo info) {
        if (this.isExternalTraceModel()) {
            return new ExternalModelTaskBuilder(info);
        }

        if ("iSPD".equals(this.traceType)) {
            return new TraceTaskBuilder(info);
        }

        throw new RuntimeException();
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

    private void addDefaultUser(final String userId) {
        this.users.add(userId);
    }

    private boolean isExternalTraceModel() {
        return switch (this.traceType) {
            case "SWF", "GWF" -> true;
            default -> false;
        };
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

    private class ExternalModelTaskBuilder extends TraceTaskBuilder {
        private static final TwoStageUniform SENT_FILE_SIZE =
                new TwoStageUniform(200, 5000, 25000, 0.5);
        private final Distribution random;

        public ExternalModelTaskBuilder(final TaskInfo taskInfo) {
            this(taskInfo, new Distribution(System.currentTimeMillis()));
        }

        private ExternalModelTaskBuilder(
                final TaskInfo taskInfo, final Distribution random) {
            super(taskInfo);
            this.random = random;
        }

        @Override
        public Tarefa makeTaskFor(final CS_Processamento master) {
            final var task = super.makeTaskFor(master);

            if (this.taskInfo.shouldBeCanceled()) {
                task.setLocalProcessamento(master);
                task.cancelar(0);
            }

            return task;
        }

        @Override
        protected double calculateSentFileSize() {
            return ExternalModelTaskBuilder.SENT_FILE_SIZE.generateValue(this.random);
        }

        @Override
        protected double calculateProcessingTime() {
            return super.calculateProcessingTime()
                   * TraceLoadHelper.this.averageComputationalPower();
        }
    }
}