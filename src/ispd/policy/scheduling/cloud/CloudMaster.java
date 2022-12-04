package ispd.policy.scheduling.cloud;

import ispd.policy.scheduling.SchedulingMaster;

public interface CloudMaster extends SchedulingMaster {
    void freeScheduler();
}
