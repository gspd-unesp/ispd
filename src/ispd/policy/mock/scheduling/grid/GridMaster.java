package ispd.policy.mock.scheduling.grid;

import ispd.policy.mock.scheduling.SchedulingMaster;

public interface GridMaster extends SchedulingMaster {
    void connectTo(final GridMaster other);
}
