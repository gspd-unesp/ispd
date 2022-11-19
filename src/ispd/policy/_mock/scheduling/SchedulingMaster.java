package ispd.policy._mock.scheduling;

import ispd.policy._mock.Master;

public interface SchedulingMaster extends Master {
    void scheduleTask(int taskId);
}
