package ispd.motor.carga;

import ispd.motor.carga.task.TaskSize;
import ispd.motor.filas.servidores.CS_Processamento;

public class GlobalSequentialTaskBuilder extends RandomicSequentialTaskBuilder {
    private final double taskCreationTime;

    public GlobalSequentialTaskBuilder(
            final double taskCreationTime,
            final TaskSize computation, final TaskSize communication) {
        super(computation, communication);
        this.taskCreationTime = taskCreationTime;
    }

    @Override
    public String taskOwner(final CS_Processamento master) {
        return master.getProprietario();
    }

    @Override
    public String taskApplication() {
        return "application1";
    }

    @Override
    public double taskCreationTime() {
        return this.random.nextExponential(this.taskCreationTime);
    }
}
