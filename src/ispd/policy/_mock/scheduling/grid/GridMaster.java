package ispd.policy._mock.scheduling.grid;

import ispd.policy._mock.scheduling.SchedulingMaster;

public interface GridMaster extends SchedulingMaster {
    void connectTo(final GridMaster other);
}
