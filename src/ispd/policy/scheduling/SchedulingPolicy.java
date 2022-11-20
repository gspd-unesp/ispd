package ispd.policy.scheduling;

import ispd.motor.filas.Tarefa;
import ispd.policy.Policy;

public abstract class SchedulingPolicy <T extends SchedulingMaster> implements Policy<T> {
    public abstract Tarefa escalonarTarefa();
}
