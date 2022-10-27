package ispd.motor.carga;

import ispd.motor.carga.task.TaskSize;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;

abstract class RandomicTaskBuilder extends TaskBuilder {
    protected final Distribution random;
    private final IdGenerator idGenerator;
    private final TaskSize computation;
    private final TaskSize communication;

    protected RandomicTaskBuilder(
            final TaskSize computation, final TaskSize communication) {
        this.idGenerator = new IdGenerator();
        this.random = new Distribution(System.currentTimeMillis());
        this.computation = computation;
        this.communication = communication;
    }

    @Override
    public Tarefa makeTaskFor(final CS_Processamento master) {
        return new Tarefa(
                this.idGenerator.next(),
                this.taskOwner(master),
                this.taskApplication(),
                master,
                this.communication.rollTwoStageUniform(this.random),
                TaskBuilder.FILE_RECEIVE_TIME,
                this.computation.rollTwoStageUniform(this.random),
                this.taskCreationTime()
        );
    }

    public abstract String taskOwner(final CS_Processamento master);

    public abstract String taskApplication();

    public abstract double taskCreationTime();

    private static class IdGenerator {
        private int nextId = 0;

        public int next() {
            final int value = this.nextId;
            this.nextId++;
            return value;
        }
    }
}
