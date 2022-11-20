package ispd.policy.scheduling;

import ispd.motor.filas.Tarefa;
import ispd.policy.Policy;

public interface SchedulingPolicy <T extends SchedulingMaster> extends Policy<T> {
    Tarefa escalonarTarefa();
}
