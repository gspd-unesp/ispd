package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;

import java.io.File;
import java.util.List;

public class TraceFileWorkloadGenerator implements WorkloadGenerator {
    private final File file;
    private final int taskCount;
    private final String type;

    public TraceFileWorkloadGenerator(
            final File file, final int taskCount, final String type) {
        this.file = file;
        this.taskCount = taskCount;
        this.type = type;
    }

    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        return new TraceLoadHelper(qn, this.type, this.taskCount)
                .toTaskList(this.file.getAbsolutePath());
    }

    @Override
    public WorkloadGeneratorType getType() {
        return WorkloadGeneratorType.TRACE;
    }

    @Override
    public String toString() {
        return this.file.getAbsolutePath();
    }

    public String getTraceType() {
        return this.type;
    }

    public File getFile() {
        return this.file;
    }

    public Integer getNumberTasks() {
        return this.taskCount;
    }
}