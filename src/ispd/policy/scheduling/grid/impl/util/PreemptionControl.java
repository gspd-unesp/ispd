package ispd.policy.scheduling.grid.impl.util;

public record PreemptionControl(
        String preemptedTaskUser, int preemptedTaskId,
        String allocatedTaskUser, int allocatedTaskId) {
}
