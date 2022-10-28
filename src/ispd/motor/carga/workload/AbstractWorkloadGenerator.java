package ispd.motor.carga.workload;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

public abstract class AbstractWorkloadGenerator implements WorkloadGenerator {
    public Tarefa makeTaskFor(final CS_Processamento master) {
        return new Tarefa(
                this.makeTaskId(),
                this.makeTaskUser(master),
                this.makeTaskApplication(),
                master,
                this.makeTaskCommunicationSize(),
                WorkloadGenerator.FILE_RECEIVE_TIME,
                this.makeTaskComputationSize(),
                this.makeTaskCreationTime()
        );
    }

    protected abstract int makeTaskId();

    protected abstract String makeTaskUser(CS_Processamento master);

    protected abstract String makeTaskApplication();

    protected abstract double makeTaskCommunicationSize();

    protected abstract double makeTaskComputationSize();

    protected abstract double makeTaskCreationTime();
}
