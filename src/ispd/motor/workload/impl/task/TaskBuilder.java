package ispd.motor.workload.impl.task;

import ispd.motor.workload.WorkloadGenerator;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class to host utility methods for {@link WorkloadGenerator}s that create
 * their own tasks. In particular, {@link #makeTaskFor(CS_Processamento)} is
 * a "template" that most generators will follow when creating a task.
 */
public abstract class TaskBuilder {
    /**
     * Time to receive a task file, in seconds.
     */
    private static final double FILE_RECEIVE_TIME = 0.0009765625;

    /**
     * Create a {@link Tarefa} originating at the given
     * {@link CS_Processamento} instance.
     *
     * @param master {@link CS_Processamento} that will host the task.
     * @return a generated {@link Tarefa}.
     */
    public Tarefa makeTaskFor(final CS_Processamento master) {
        return new Tarefa(
                this.makeTaskId(),
                this.makeTaskUser(master),
                this.makeTaskApplication(),
                master,
                this.makeTaskCommunicationSize(),
                TaskBuilder.FILE_RECEIVE_TIME,
                this.makeTaskComputationSize(),
                this.makeTaskCreationTime()
        );
    }

    public List<Tarefa> makeTasksEvenlyDistributedBetweenMasters(final RedeDeFilas qn, final int taskCount) {
        final var masters = qn.getMestres();

        return IntStream.range(0, taskCount)
                .map(i -> i % masters.size())
                .mapToObj(masters::get)
                .map(this::makeTaskFor)
                .collect(Collectors.toList());
    }

    /**
     * Create a suitable id for a new task.
     *
     * @return an integral value representing a task id.
     */
    protected abstract int makeTaskId();

    /**
     * Select a suitable user for a new task. Such generation may involve the
     * given {@link CS_Processamento}, or not.
     *
     * @param master {@link CS_Processamento} that may host information about
     *               which user the task will be linked with.
     * @return a user id for the new task.
     */
    protected abstract String makeTaskUser(CS_Processamento master);

    /**
     * @return the application which a new task will be associated with.
     */
    protected abstract String makeTaskApplication();

    /**
     * @return the task communication size (in MFlops), usually random.
     */
    protected abstract double makeTaskCommunicationSize();

    /**
     * @return the task computation size (in Mbits), usually random.
     */
    protected abstract double makeTaskComputationSize();

    /**
     * @return the task creation time, usually random.
     */
    protected abstract double makeTaskCreationTime();
}