package ispd.motor.carga.workload;

import ispd.escalonador.Escalonador;
import ispd.motor.carga.task.TraceTaskBuilder;
import ispd.motor.carga.task.TraceTaskInfo;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

public class TraceFileWorkloadGenerator implements WorkloadGenerator {
    private static final int HEADER_LINE_COUNT = 5;
    private final File traceFile;
    private final int taskCount;
    private final String traceType;

    public TraceFileWorkloadGenerator(
            final File traceFile, final int taskCount, final String traceType) {
        this.traceFile = traceFile;
        this.taskCount = taskCount;
        this.traceType = traceType;
    }

    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        final var tasks = this.getTraceTasksFromFile();

        TraceFileWorkloadGenerator.updateQueueNetworkWithTasks(qn, tasks);

        final var taskBuilder = this.makeTaskBuilder(tasks, qn);

        return taskBuilder
                .makeTasksEvenlyDistributedBetweenMasters(qn, this.taskCount);
    }

    private List<TraceTaskInfo> getTraceTasksFromFile() {
        try (final var br = new BufferedReader(
                new FileReader(this.traceFile, StandardCharsets.UTF_8))) {
            return br.lines()
                    .skip(TraceFileWorkloadGenerator.HEADER_LINE_COUNT)
                    .map(TraceTaskInfo::new)
                    .toList();
        } catch (final IOException | UncheckedIOException ex) {
            Logger.getLogger(TraceFileWorkloadGenerator.class.getName())
                    .log(Level.SEVERE, null, ex);
            return new ArrayList();
        }
    }

    private static void updateQueueNetworkWithTasks(
            final RedeDeFilas qn, final Collection<TraceTaskInfo> tasks) {
        TraceFileWorkloadGenerator.updateQueueNetworkWithUsers(qn,
                tasks.stream()
                        .map(TraceTaskInfo::user)
                        .distinct()
                        .filter(Predicate.not(qn.getUsuarios()::contains))
                        .toList()
        );
    }

    private TraceTaskBuilder makeTaskBuilder(
            final List<TraceTaskInfo> tasks, final RedeDeFilas qn) {
        return switch (this.traceType) {
            case "iSPD" -> new TraceTaskBuilder(tasks);
            case "SWF", "GWF" -> new ExternalTraceTaskBuilder(tasks,
                    TraceFileWorkloadGenerator.averageComputationalPower(qn));
            default -> throw new IllegalArgumentException(
                    "Unrecognized trace type '%s'".formatted(this.traceType));
        };
    }

    private static void updateQueueNetworkWithUsers(
            final RedeDeFilas qn, final List<String> users) {
        TraceFileWorkloadGenerator.updateSchedulerUserMetrics(qn, users);
        qn.getUsuarios().addAll(users);
    }

    private static double averageComputationalPower(final RedeDeFilas qn) {
        return qn.getMaquinas().stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .average()
                .orElse(0.0);
    }

    private static void updateSchedulerUserMetrics(
            final RedeDeFilas qn, final List<String> users) {
        final var count = users.size();
        final var compPower = TraceFileWorkloadGenerator.filledList(0, count);
        final var limits = TraceFileWorkloadGenerator.filledList(100, count);

        qn.getMestres().stream()
                .map(CS_Mestre.class::cast)
                .map(CS_Mestre::getEscalonador)
                .map(Escalonador::getMetricaUsuarios)
                .forEach(m -> m.addAllUsuarios(users, compPower, limits));
    }

    private static List<Double> filledList(final double fill, final int count) {
        return DoubleStream.generate(() -> fill)
                .limit(count)
                .boxed()
                .toList();
    }

    @Override
    public WorkloadGeneratorType getType() {
        return WorkloadGeneratorType.TRACE;
    }

    @Override
    public String toString() {
        return this.traceFile.getAbsolutePath();
    }

    public String getTraceType() {
        return this.traceType;
    }

    public File getFile() {
        return this.traceFile;
    }

    public int getNumberTasks() {
        return this.taskCount;
    }
}