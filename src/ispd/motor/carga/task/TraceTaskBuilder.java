package ispd.motor.carga.task;

import ispd.motor.carga.workload.WorkloadGenerator;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TraceTaskBuilder {
    protected final TraceTaskInfo traceTaskInfo;

    public TraceTaskBuilder(final TraceTaskInfo traceTaskInfo) {
        this.traceTaskInfo = traceTaskInfo;
    }

    public Tarefa makeTaskFor(final CS_Processamento master) {
        return new Tarefa(
                this.traceTaskInfo.id(),
                this.traceTaskInfo.user(),
                "application1",
                master,
                this.calculateSentFileSize(),
                WorkloadGenerator.FILE_RECEIVE_TIME,
                this.calculateProcessingTime(),
                this.traceTaskInfo.creationTime()
        );
    }

    protected double calculateSentFileSize() {
        return this.traceTaskInfo.sentFileSize();
    }

    protected double calculateProcessingTime() {
        return this.traceTaskInfo.processingTime();
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
