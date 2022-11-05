package ispd.motor.workload.impl.task;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.LinkedList;
import java.util.List;

/**
 * Builds tasks from a {@link List} of {@link TraceTaskInfo}s.
 */
public class TraceTaskBuilder extends TaskBuilder {
    protected final List<TraceTaskInfo> traceTasks;
    protected TraceTaskInfo currTraceTask = null;

    public TraceTaskBuilder(final List<TraceTaskInfo> traceTasks) {
        this.traceTasks = new LinkedList<>(traceTasks);
    }

    @Override
    public Tarefa makeTaskFor(final CS_Processamento master) {
        this.currTraceTask = this.traceTasks.remove(0);
        return super.makeTaskFor(master);
    }

    @Override
    protected int makeTaskId() {
        return this.currTraceTask.id();
    }

    @Override
    protected String makeTaskUser(final CS_Processamento master) {
        return this.currTraceTask.user();
    }

    @Override
    protected String makeTaskApplication() {
        return "application1";
    }

    @Override
    protected double makeTaskCommunicationSize() {
        return this.currTraceTask.sentFileSize();
    }

    protected double makeTaskComputationSize() {
        return this.currTraceTask.processingTime();
    }

    @Override
    protected double makeTaskCreationTime() {
        return this.currTraceTask.creationTime();
    }
}