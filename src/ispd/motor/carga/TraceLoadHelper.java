package ispd.motor.carga;

import ispd.escalonador.Escalonador;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class TraceLoadHelper {
    private static final int USER_FIELD_INDEX = 11;
    private static final int HEADER_LINE_COUNT = 5;
    private static final double RECEIVING_FILE_SIZE = 0.0009765625;
    private final List<Tarefa> tasks = new ArrayList<>(0);
    private final List<String> users = new ArrayList<>(0);
    private final List<Double> computationalPowers = new ArrayList<>(0);
    private final List<Double> profiles = new ArrayList<>(0);
    private final int taskCount;
    private final String traceType;
    private final RedeDeFilas queueNetwork;

    TraceLoadHelper(
            final RedeDeFilas qn,
            final String traceType,
            final int taskCount) {
        this.queueNetwork = qn;
        this.traceType = traceType;
        this.taskCount = taskCount;
    }

    List<Tarefa> toTaskList(final String path) {
        try (final var br = new BufferedReader(
                new FileReader(path, StandardCharsets.UTF_8))) {

            final var attrs = br.lines()
                    .skip(TraceLoadHelper.HEADER_LINE_COUNT)
                    .findFirst()
                    .orElseThrow()
                    .split("\"");

            this.addUserIfNotPresent(attrs);

            final var taskList = this.makeTaskBuilderForType(attrs)
                    .makeTasksDistributedBetweenMasters(
                            this.queueNetwork, this.taskCount);

            this.tasks.addAll(taskList);

            this.queueNetwork.getMestres().stream()
                    .map(CS_Mestre.class::cast)
                    .map(CS_Mestre::getEscalonador)
                    .map(Escalonador::getMetricaUsuarios)
                    .forEach((metrics) -> metrics.addAllUsuarios(
                            this.users,
                            this.computationalPowers,
                            this.profiles
                    ));

            this.queueNetwork.getUsuarios().addAll(this.users);

            return this.tasks;

        } catch (final IOException ex) {
            Logger.getLogger(TraceLoadHelper.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private void addUserIfNotPresent(final String[] attrs) {
        final var userId = attrs[TraceLoadHelper.USER_FIELD_INDEX];
        if (!this.queueNetwork.getUsuarios().contains(userId) && !this.users.contains(userId)) {
            this.addDefaultUser(userId);
        }
    }

    private TaskBuilder makeTaskBuilderForType(final String[] attrs) {
        if (Set.of("SWF", "GWF").contains(this.traceType)) {
            return new SwfGwfTaskBuilder(attrs);
        }

        if ("iSPD".equals(this.traceType)) {
            return new IspdTaskBuilder(attrs);
        }

        throw new RuntimeException();
    }

    private void addDefaultUser(final String userId) {
        this.users.add(userId);
        this.profiles.add(100.0);
        this.computationalPowers.add(0.0);
    }

    private double averageComputationalPower() {
        return this.queueNetwork.getMaquinas().stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .average()
                .orElse(0.0);
    }

    public abstract static class TraceTaskBuilder extends TaskBuilder {
        protected final String[] attrs;

        protected TraceTaskBuilder(final String[] attrs) {
            this.attrs = attrs;
        }

        @Override
        public Tarefa makeTaskFor(final CS_Processamento master) {
            return new Tarefa(
                    Integer.parseInt(this.attrs[1]),
                    this.attrs[TraceLoadHelper.USER_FIELD_INDEX],
                    "application1",
                    master,
                    this.calculateSentFileSize(),
                    TraceLoadHelper.RECEIVING_FILE_SIZE,
                    this.calculateProcessingTime(),
                    Double.parseDouble(this.attrs[3])
            );
        }

        protected abstract double calculateSentFileSize();

        protected double calculateProcessingTime() {
            return Double.parseDouble(this.attrs[7]);
        }
    }

    private class IspdTaskBuilder extends TraceTaskBuilder {
        public IspdTaskBuilder(final String[] attrs) {
            super(attrs);
        }

        @Override
        protected double calculateSentFileSize() {
            return Double.parseDouble(this.attrs[9]);
        }
    }

    private class SwfGwfTaskBuilder extends TraceTaskBuilder {
        public SwfGwfTaskBuilder(final String[] attrs) {
            super(attrs);
        }

        @Override
        public Tarefa makeTaskFor(final CS_Processamento master) {
            final var task = super.makeTaskFor(master);

            if (this.attrs[5].contains("0") || this.attrs[5].contains("5")) {
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