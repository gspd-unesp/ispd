package ispd.motor.carga.task;

import ispd.motor.carga.workload.WorkloadGenerator;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TraceTaskBuilder {
    protected final TaskInfo taskInfo;

    public TraceTaskBuilder(final TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public Tarefa makeTaskFor(final CS_Processamento master) {
        return new Tarefa(
                this.taskInfo.id(),
                this.taskInfo.user(),
                "application1",
                master,
                this.calculateSentFileSize(),
                WorkloadGenerator.FILE_RECEIVE_TIME,
                this.calculateProcessingTime(),
                this.taskInfo.creationTime()
        );
    }

    protected double calculateSentFileSize() {
        return this.taskInfo.sentFileSize();
    }

    protected double calculateProcessingTime() {
        return this.taskInfo.processingTime();
    }

    public List<Tarefa> makeTasksEvenlyDistributedBetweenMasters(
            final RedeDeFilas qn, final int taskCount) {
        final var masters = qn.getMestres();

        return IntStream.range(0, taskCount)
                .map(i -> i % masters.size())
                .mapToObj(masters::get)
                .map(this::makeTaskFor)
                .collect(Collectors.toList());
    }
}
