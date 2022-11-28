package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;

public record PreemptionEntry(
        String preemptedTaskUser, int preemptedTaskId,
        String scheduledTaskUser, int scheduledTaskId) {
    public PreemptionEntry(final Tarefa preempted, final Tarefa scheduled) {
        this(
                preempted.getProprietario(),
                preempted.getIdentificador(),
                scheduled.getProprietario(),
                scheduled.getIdentificador()
        );
    }

    public boolean hasPreemptedTask(final Tarefa task) {
        return this.preemptedTaskUser.equals(task.getProprietario()) &&
               this.preemptedTaskId == task.getIdentificador();
    }

    public boolean hasScheduledTask(final Tarefa task) {
        return this.scheduledTaskUser.equals(task.getProprietario()) &&
               this.scheduledTaskId == task.getIdentificador();
    }
}
