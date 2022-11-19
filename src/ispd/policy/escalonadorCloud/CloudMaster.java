package ispd.policy.escalonadorCloud;

import ispd.policy.scheduling.SchedulingMaster;

public interface CloudMaster extends SchedulingMaster {
    void freeScheduler();
}
