package ispd.motor.carga.workload;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;

import java.io.File;
import java.util.List;

public class TraceFileWorkloadGenerator implements WorkloadGenerator {
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
        return new TraceLoadHelper(qn, this.traceType, this.taskCount)
                .toTaskList(this.traceFile.getAbsolutePath());
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