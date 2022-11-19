package ispd.policy._mock.managers;

import ispd.policy._mock.PolicyManager;
import ispd.policy._mock.loaders.GridSchedulingPolicyLoader;
import ispd.policy._mock.scheduling.grid.GridSchedulingPolicy;

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
