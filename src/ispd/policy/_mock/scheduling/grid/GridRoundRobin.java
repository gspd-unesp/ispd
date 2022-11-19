package ispd.policy._mock.scheduling.grid;

public class GridRoundRobin implements GridSchedulingPolicy {
    private GridMaster master;

    @Override
    public void setMaster(final GridMaster master) {
        this.master = master;
        this.master.executePolicy(); // From Master
        this.master.scheduleTask(5); // From SchedulingMaster
        this.master.connectTo(this.master); // From GridMaster
    }
}
