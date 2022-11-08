package ispd.policy.mock.managers;

import ispd.policy.mock.PolicyManager;
import ispd.policy.mock.loaders.GridSchedulingPolicyLoader;
import ispd.policy.mock.scheduling.grid.GridSchedulingPolicy;

import java.util.ArrayList;
import java.util.List;

public class GridSchedulingPolicyManager implements PolicyManager {
    private final List<GridSchedulingPolicy> policies = new ArrayList<>();

    @Override
    public void addPolicy(final String name) {
        final var loader = new GridSchedulingPolicyLoader();
        this.policies.add(loader.load(name));
    }
}
