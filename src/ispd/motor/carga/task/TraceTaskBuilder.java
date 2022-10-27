package ispd.motor.carga.task;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

public class TraceTaskBuilder extends TaskBuilder {
    protected final TaskInfo taskInfo;

    public TraceTaskBuilder(final TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    @Override
    public Tarefa makeTaskFor(final CS_Processamento master) {
        return new Tarefa(
                this.taskInfo.id(),
                this.taskInfo.user(),
                "application1",
                master,
                this.calculateSentFileSize(),
                TaskBuilder.FILE_RECEIVE_TIME,
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
}
