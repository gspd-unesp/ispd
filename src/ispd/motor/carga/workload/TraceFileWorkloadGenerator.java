package ispd.motor.carga.workload;

import ispd.escalonador.Escalonador;
import ispd.motor.carga.task.TraceTaskInfo;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

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
        final var helper = new TraceLoadHelper(
                qn, this.traceType, this.taskCount);

        final var tasks =
                this.getTraceTasksFromFile().collect(Collectors.toList());


        final var helper2 = new TraceLoadHelper2(tasks);

        final var task = this.getTraceTasksFromFile()
                .findFirst();

        if (task.isPresent()) {
            TraceFileWorkloadGenerator.updateQueueNetwork(
                    task.get().user(), qn, new ArrayList<>());
        }

        return task // FIXME: Gotta process ALL trace tasks
                .map(helper::processTaskInfo)
                .orElse(null);
    }

    private Stream<TraceTaskInfo> getTraceTasksFromFile() {
        try (final var br = new BufferedReader(
                new FileReader(this.traceFile, StandardCharsets.UTF_8))) {
            return br.lines()
                    .skip(TraceFileWorkloadGenerator.HEADER_LINE_COUNT)
                    .map(TraceTaskInfo::new);
        } catch (final IOException | UncheckedIOException ex) {
            Logger.getLogger(TraceFileWorkloadGenerator.class.getName())
                    .log(Level.SEVERE, null, ex);
            return Stream.empty();
        }
    }

    static void updateQueueNetwork(
            final String user, final RedeDeFilas qn, final List<String> users) {
        if (!(qn.getUsuarios().contains(user) || users.contains(user))) {
            users.add(user);
        }

        qn.getMestres().stream()
                .map(CS_Mestre.class::cast)
                .map(CS_Mestre::getEscalonador)
                .map(Escalonador::getMetricaUsuarios)
                .forEach(metrics -> {
                    final int count = users.size();

                    metrics.addAllUsuarios(
                            users,
                            TraceFileWorkloadGenerator.filledList(0, count),
                            TraceFileWorkloadGenerator.filledList(100, count)
                    );
                });

        qn.getUsuarios().addAll(users);
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

    private class TraceLoadHelper2 {
        public TraceLoadHelper2(final List<TraceTaskInfo> tasks) {
        }
    }
}