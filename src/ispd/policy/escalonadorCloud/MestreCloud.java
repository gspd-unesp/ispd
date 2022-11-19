package ispd.policy.escalonadorCloud;

import ispd.policy.scheduling.SchedulingMaster;

public interface MestreCloud extends SchedulingMaster {
    void freeScheduler();
}
