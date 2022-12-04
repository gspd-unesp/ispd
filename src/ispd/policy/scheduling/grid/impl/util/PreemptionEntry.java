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

    public boolean willPreemptTask(final Tarefa task) {
        return this.preemptedTaskId == task.getIdentificador()
               && this.preemptedTaskUser.equals(task.getProprietario());
    }

    public boolean willScheduleTask(final Tarefa task) {
        return this.scheduledTaskId == task.getIdentificador()
               && this.scheduledTaskUser.equals(task.getProprietario());
    }
}
