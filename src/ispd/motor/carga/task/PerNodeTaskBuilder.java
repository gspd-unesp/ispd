package ispd.motor.carga.task;

import ispd.motor.filas.servidores.CS_Processamento;

public class PerNodeTaskBuilder extends RandomicTaskBuilder {
    private static final int ON_NO_DELAY = 120;
    private final String owner;
    private final String application;

    public PerNodeTaskBuilder(
            final String owner, final String application,
            final TaskSize computation, final TaskSize communication) {
        super(computation, communication);
        this.owner = owner;
        this.application = application;
    }

    @Override
    public String taskOwner(final CS_Processamento master) {
        return this.owner;
    }

    @Override
    public String taskApplication() {
        return this.application;
    }

    @Override
    public double taskCreationTime() {
        return this.random.nextExponential(5) + this.calculateDelay();
    }

    private int calculateDelay() {
        return "NoDelay".equals(this.owner) ?
                PerNodeTaskBuilder.ON_NO_DELAY : 0;
    }
}
