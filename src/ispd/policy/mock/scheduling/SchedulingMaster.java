package ispd.policy.mock.scheduling;

import ispd.policy.mock.Master;

public interface SchedulingMaster extends Master {
    void scheduleTask(int taskId);
}
