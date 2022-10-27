package ispd.motor.carga;

import ispd.motor.carga.task.TaskSize;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;

public abstract class RandomicWorkloadGenerator implements WorkloadGenerator {
    protected final int taskCount;
    protected final TaskSize computation;
    protected final TaskSize communication;

    protected RandomicWorkloadGenerator(
            final int taskCount,
            final TaskSize computation, final TaskSize communication) {
        this.computation = computation;
        this.communication = communication;
        this.taskCount = taskCount;
    }

    public double getAverageComputacao() {
        return this.computation.average();
    }

    public double getAverageComunicacao() {
        return this.communication.average();
    }

    public double getProbabilityComputacao() {
        return this.computation.probability();
    }

    public double getProbabilityComunicacao() {
        return this.communication.probability();
    }

    public double getMaxComputacao() {
        return this.computation.maximum();
    }

    public double getMaxComunicacao() {
        return this.communication.maximum();
    }

    public double getMinComputacao() {
        return this.computation.minimum();
    }

    public double getMinComunicacao() {
        return this.communication.minimum();
    }

    public int getNumeroTarefas() {
        return this.taskCount;
    }

    protected abstract class RandomicSequentialTaskBuilder extends TaskBuilder {
        protected final Distribution random;
        private final IdGenerator idGenerator;

        protected RandomicSequentialTaskBuilder() {
            this(System.currentTimeMillis());
        }

        private RandomicSequentialTaskBuilder(final long seed) {
            this.idGenerator = new IdGenerator();
            this.random = new Distribution(seed);
        }

        @Override
        public Tarefa makeTaskFor(final CS_Processamento master) {
            return new Tarefa(
                    this.idGenerator.next(),
                    this.taskOwner(master),
                    this.taskApplication(),
                    master,
                    RandomicWorkloadGenerator.this.communication.rollTwoStageUniform(this.random),
                    TaskBuilder.FILE_RECEIVE_TIME,
                    RandomicWorkloadGenerator.this.computation.rollTwoStageUniform(this.random),
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
}
