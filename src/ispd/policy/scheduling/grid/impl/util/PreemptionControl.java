package ispd.policy.scheduling.grid.impl.util;

public class PreemptionControl {
    private final String preemptedTaskUser;
    private final int preemptedTaskId;
    private final String allocatedTaskUser;
    private final int allocatedTaskId;

    public PreemptionControl(
            final String preemptedTaskUser,
            final int preemptedTaskId,
            final String allocatedTaskUser,
            final int allocatedTaskId) {
        this.preemptedTaskUser = preemptedTaskUser;
        this.preemptedTaskId = preemptedTaskId;
        this.allocatedTaskUser = allocatedTaskUser;
        this.allocatedTaskId = allocatedTaskId;
    }

    public String getUsuarioPreemp() {
        return this.preemptedTaskUser;
    }

    public int getPreempID() {
        return this.preemptedTaskId;
    }

    public String getUsuarioAlloc() {
        return this.allocatedTaskUser;
    }

    public int getAllocID() {
        return this.allocatedTaskId;
    }
}
