package ispd.policy.alocacaoVM;

import ispd.motor.Simulation;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.PolicyCondition;
import ispd.policy.allocation.AllocationMaster;

import java.util.Set;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be used by the schedulers.
 */
public interface VMM extends AllocationMaster {
    void executeAllocation();

    void sendVm(CS_VirtualMac vm);

    Set<PolicyCondition> getAllocationConditions();

    void setAllocationConditions(Set<PolicyCondition> tipo);

    Simulation getSimulation();

    void setSimulation(Simulation simulacao);
}