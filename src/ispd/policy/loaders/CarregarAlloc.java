package ispd.policy.loaders;

import ispd.policy.allocation.vm.VmAllocationPolicy;

public class CarregarAlloc extends ClassPolicyLoader<VmAllocationPolicy> {
    private static final String CLASS_PATH = "ispd.policy.allocation.vm.impl.";

    @Override
    protected String getClassPath() {
        return CarregarAlloc.CLASS_PATH;
    }
}