package ispd.motor.workload.impl.task;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.LinkedList;
import java.util.List;

/**
 * Builds tasks from a {@link List} of {@link TraceTaskInfo}s.<br>
 * The given {@link TraceTaskInfo}s are converted into tasks, one by one, and
 * distributed between the masters given as argument to
 * {@link #makeTaskFor(CS_Processamento)}, in the invoked call order.<br>
 */
public class TraceTaskBuilder extends TaskBuilder {
    private final List<TraceTaskInfo> traceTaskInfos;
    /**
     * Holds the current {@link TraceTaskInfo} being processed in the
     * {@link #makeTaskFor(CS_Processamento)} method. It is initialized with
     * {@code null}.
     */
    protected TraceTaskInfo currTaskInfo = null;

    /**
     * Initialize an instance with the given {@link List} of
     * {@link TraceTaskInfo}s.
     *
     * @param traceTaskInfos list of task information.
     * @throws NullPointerException if given list is {@code null}.
     */
    public TraceTaskBuilder(final List<TraceTaskInfo> traceTaskInfos) {
        this.traceTaskInfos = new LinkedList<>(traceTaskInfos);
    }

    /**
     * Pops a {@link TraceTaskInfo} object from the inner list
     * {@link #traceTaskInfos} and converts it into a task originating from
     * the given {@link CS_Processamento}.
     *
     * @param master {@link CS_Processamento} that will host the task.
     * @return created {@link Tarefa}.
     * @throws IndexOutOfBoundsException if there are no more usable task
     *                                   information instances in the inner
     *                                   list.
     * @apiNote this method can only be called successfully at most {@code n}
     * times, where {@code n} is the size of the {@link List} this instance
     * was initialized with.
     */
    @Override
    public Tarefa makeTaskFor(final CS_Processamento master) {
        this.currTaskInfo = this.traceTaskInfos.remove(0);
        return super.makeTaskFor(master);
    }

    @Override
    protected int makeTaskId() {
        return this.currTaskInfo.id();
    }

    @Override
    protected String makeTaskUser(final CS_Processamento master) {
        return this.currTaskInfo.user();
    }

    @Override
    protected double makeTaskCommunicationSize() {
        return this.currTaskInfo.communicationSize();
    }

    @Override
    protected double makeTaskComputationSize() {
        return this.currTaskInfo.computationSize();
    }

    @Override
    protected double makeTaskCreationTime() {
        return this.currTaskInfo.creationTime();
    }
}