package ispd.policy.allocation;

import ispd.policy.PolicyCondition;
import ispd.policy.PolicyMaster;

import java.util.Set;

public interface AllocationMaster extends PolicyMaster {
    void executeAllocation();

    Set<PolicyCondition> getAllocationConditions();

    void setAllocationConditions(Set<PolicyCondition> tipo);
}
