package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;

public record PreemptionControl(
        String preemptedTaskUser, int preemptedTaskId,
        String scheduledTaskUser, int scheduledTaskId) {
    public PreemptionControl(final Tarefa preempted, final Tarefa scheduled) {
        this(
                preempted.getProprietario(),
                preempted.getIdentificador(),
                scheduled.getProprietario(),
                scheduled.getIdentificador()
        );
    }
}
