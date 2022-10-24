package ispd.motor.carga;

import ispd.escalonador.Escalonador;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.metricas.MetricasUsuarios;

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
                .makeTasksDistributedBetweenMasters(
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

    private TaskBuilder makeTaskBuilderForType(final TaskInfo info) {
        if (this.isExternalTraceModel()) {
            return new ExternalModelTaskBuilder(info);
        }

        if ("iSPD".equals(this.traceType)) {
            return new IspdTaskBuilder(info);
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

    public abstract static class TraceTaskBuilder extends TaskBuilder {
        protected final TaskInfo taskInfo;

        protected TraceTaskBuilder(final TaskInfo taskInfo) {
            this.taskInfo = taskInfo;
        }

        @Override
        public Tarefa makeTaskFor(final CS_Processamento master) {
            return new Tarefa(
                    this.taskInfo.id(),
                    this.taskInfo.user(),
                    "application1",
                    master,
                    this.calculateSentFileSize(),
                    TaskBuilder.FILE_RECEIVE_TIME,
                    this.calculateProcessingTime(),
                    this.taskInfo.creationTime()
            );
        }

        protected abstract double calculateSentFileSize();

        protected double calculateProcessingTime() {
            return this.taskInfo.processingTime();
        }
    }

    static class TaskInfo {
        private final String[] fields;

        private TaskInfo(final String s) {
            this(s.split("\""));
        }

        private TaskInfo(final String[] fields) {
            this.fields = fields;
        }

        public int id() {
            return Integer.parseInt(this.fields[1]);
        }

        public String user() {
            return this.fields[11];
        }

        public double processingTime() {
            return this.fieldAsDouble(7);
        }

        private double fieldAsDouble(final int index) {
            return Double.parseDouble(this.fields[index]);
        }

        public double sentFileSize() {
            return this.fieldAsDouble(9);
        }

        public double creationTime() {
            return this.fieldAsDouble(3);
        }

        private boolean shouldBeCanceled() {
            return this.status().contains("0") || this.status().contains("5");
        }

        private String status() {
            return this.fields[5];
        }
    }

    private class IspdTaskBuilder extends TraceTaskBuilder {
        public IspdTaskBuilder(final TaskInfo taskInfo) {
            super(taskInfo);
        }

        @Override
        protected double calculateSentFileSize() {
            return this.taskInfo.sentFileSize();
        }
    }

    private class ExternalModelTaskBuilder extends TraceTaskBuilder {
        public ExternalModelTaskBuilder(final TaskInfo taskInfo) {
            super(taskInfo);
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
            return this.random.twoStageUniform(200, 5000, 25000, 0.5);
        }

        @Override
        protected double calculateProcessingTime() {
            return super.calculateProcessingTime()
                   * TraceLoadHelper.this.averageComputationalPower();
        }
    }
}