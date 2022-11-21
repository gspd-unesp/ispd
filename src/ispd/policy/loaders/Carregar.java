package ispd.policy.loaders;

import ispd.policy.scheduling.grid.GridSchedulingPolicy;

public class Carregar extends ClassPolicyLoader<GridSchedulingPolicy> {
    private static final String CLASS_PATH =
            "ispd.policy.scheduling.grid.impl.";

    @Override
    protected String getClassPath() {
        return Carregar.CLASS_PATH;
    }
}
