package ispd.policy.loaders;

import ispd.policy.scheduling.grid.GridSchedulingPolicy;

public class GridSchedulingPolicyLoader
        extends GenericPolicyLoader<GridSchedulingPolicy> {
    private static final String CLASS_PATH =
            "ispd.policy.scheduling.grid.impl.";

    @Override
    protected String getClassPath() {
        return GridSchedulingPolicyLoader.CLASS_PATH;
    }
}
