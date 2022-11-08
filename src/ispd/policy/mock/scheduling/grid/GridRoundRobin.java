package ispd.policy.mock.scheduling.grid;

public class GridRoundRobin implements GridSchedulingPolicy{
    private GridMaster master;

    @Override
    public void setMaster(GridMaster master) {
        this.master = master;
        this.master.scheduleTask(5);
        this.master.connectTo(this.master);
    }
}
