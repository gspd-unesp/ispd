package ispd.policy.mock.loaders;

import ispd.policy.mock.PolicyLoader;
import ispd.policy.mock.scheduling.grid.GridRoundRobin;
import ispd.policy.mock.scheduling.grid.GridSchedulingPolicy;

public class GridSchedulingPolicyLoader implements PolicyLoader<GridSchedulingPolicy> {
    @Override
    public GridSchedulingPolicy load(final String policyName) {
        return new GridRoundRobin();
    }
}