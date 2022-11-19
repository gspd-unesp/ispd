package ispd.policy._mock.loaders;

import ispd.policy._mock.PolicyLoader;
import ispd.policy._mock.scheduling.grid.GridRoundRobin;
import ispd.policy._mock.scheduling.grid.GridSchedulingPolicy;

public class GridSchedulingPolicyLoader implements PolicyLoader<GridSchedulingPolicy> {
    @Override
    public GridSchedulingPolicy load(final String policyName) {
        return new GridRoundRobin();
    }
}