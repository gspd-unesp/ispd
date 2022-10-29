package ispd.motor.carga.workload;

import ispd.motor.carga.task.TaskInfo;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        return this.getTaskInfoFromFile()
                .map(new TraceLoadHelper(qn, this.traceType, this.taskCount)::processTaskInfo)
                .orElse(null);
    }

    private Optional<TaskInfo> getTaskInfoFromFile() {
        try (final var br = new BufferedReader(
                new FileReader(this.traceFile, StandardCharsets.UTF_8))) {
            return Optional.of(TraceFileWorkloadGenerator.getTaskInfoFromFile(br));
        } catch (final IOException | UncheckedIOException ex) {
            Logger.getLogger(TraceFileWorkloadGenerator.class.getName())
                    .log(Level.SEVERE, null, ex);
            return Optional.empty();
        }
    }

    private static TaskInfo getTaskInfoFromFile(final BufferedReader br) {
        return br.lines()
                .skip(TraceFileWorkloadGenerator.HEADER_LINE_COUNT)
                .findFirst()
                .map(TaskInfo::new)
                .orElseThrow();
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

    public Integer getNumberTasks() {
        return this.taskCount;
    }
}