package ispd.motor.carga.workload;

import ispd.escalonador.Escalonador;
import ispd.motor.carga.task.TaskInfo;
import ispd.motor.carga.task.TraceTaskBuilder;
import ispd.motor.random.TwoStageUniform;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.metricas.MetricasUsuarios;
import ispd.motor.random.Distribution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

class TraceLoadHelper {
    private static final int HEADER_LINE_COUNT = 5;
    private final List<Tarefa> tasks = new ArrayList<>();
    private final List<String> users = new ArrayList<>();
    private final List<Double> computationalPowers = new ArrayList<>();
    private final List<Double> profiles = new ArrayList<>();
    private final int taskCount;
    private final String traceType;
    private final RedeDeFilas queueNetwork;

    TraceLoadHelper(
            final RedeDeFilas qn, final String traceType, final int taskCount) {
        this.queueNetwork = qn;
        this.traceType = traceType;
        this.taskCount = taskCount;
    }

    List<Tarefa> toTaskList(final String path) {
        return TraceLoadHelper
                .getTaskInfoFromFile(path)
                .map(this::processTaskInfo)
                .orElse(null);
    }

    private static Optional<TaskInfo> getTaskInfoFromFile(final String filePath) {
        try (final var br = new BufferedReader(
                new FileReader(filePath, StandardCharsets.UTF_8))) {
            return Optional.of(TraceLoadHelper.getTaskInfoFromFile(br));
        } catch (final IOException ex) {
            Logger.getLogger(TraceLoadHelper.class.getName())
                    .log(Level.SEVERE, null, ex);
            return Optional.empty();
        }
    }

    private List<Tarefa> processTaskInfo(final TaskInfo taskInfo) {
        this.addUserIfNotPresent(taskInfo);

        final var taskList = this.makeTaskBuilderForType(taskInfo)
                .makeTasksEvenlyDistributedBetweenMasters(
                        this.queueNetwork, this.taskCount);

        this.tasks.addAll(taskList);

        this.queueNetwork.getMestres().stream()
                .map(CS_Mestre.class::cast)
                .map(CS_Mestre::getEscalonador)
                .map(Escalonador::getMetricaUsuarios)
                .forEach(this::updateUserMetrics);

        this.queueNetwork.getUsuarios().addAll(this.users);

        return this.tasks;
    }

    private static TaskInfo getTaskInfoFromFile(final BufferedReader br) {
        return br.lines()
                .skip(TraceLoadHelper.HEADER_LINE_COUNT)
                .findFirst()
                .map(TaskInfo::new)
                .orElseThrow();
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

    private void updateUserMetrics(final MetricasUsuarios metrics) {
        metrics.addAllUsuarios(
                this.users,
                this.computationalPowers,
                this.profiles
        );
    }

    private boolean isUserPresent(final String userId) {
        return this.queueNetwork.getUsuarios().contains(userId) || this.users.contains(userId);
    }

    private void addDefaultUser(final String userId) {
        this.users.add(userId);
        this.profiles.add(100.0);
        this.computationalPowers.add(0.0);
    }

    private boolean isExternalTraceModel() {
        return switch (this.traceType) {
            case "SWF", "GWF" -> true;
            default -> false;
        };
    }

    private double averageComputationalPower() {
        return this.queueNetwork.getMaquinas().stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .average()
                .orElse(0.0);
    }

    private class ExternalModelTaskBuilder extends TraceTaskBuilder {
        private static final TwoStageUniform SENT_FILE_SIZE = new TwoStageUniform(
                200, 5000, 25000, 0.5);
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