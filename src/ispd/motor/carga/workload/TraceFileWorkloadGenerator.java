package ispd.motor.carga.workload;

import ispd.motor.carga.task.TraceTaskInfo;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        
//        final var helper2 = new TraceLoadHelper2()

        return this.getTaskInfoFromFile()
                .findFirst() // FIXME: Gotta process ALL trace tasks
                .map(helper::processTaskInfo)
                .orElse(null);
    }

    private Stream<TraceTaskInfo> getTaskInfoFromFile() {
        try (final var br = new BufferedReader(
                new FileReader(this.traceFile, StandardCharsets.UTF_8))) {
            return TraceFileWorkloadGenerator.getTaskInfoFromFile(br);
        } catch (final IOException | UncheckedIOException ex) {
            Logger.getLogger(TraceFileWorkloadGenerator.class.getName())
                    .log(Level.SEVERE, null, ex);
            return Stream.empty();
        }
    }

    private static Stream<TraceTaskInfo> getTaskInfoFromFile(final BufferedReader br) {
        return br.lines()
                .skip(TraceFileWorkloadGenerator.HEADER_LINE_COUNT)
                .map(TraceTaskInfo::new);
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