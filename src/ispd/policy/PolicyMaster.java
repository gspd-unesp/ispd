package ispd.policy;

import ispd.motor.Simulation;

public interface PolicyMaster {
    Simulation getSimulation();

    void setSimulation(Simulation newSimulation);
}
