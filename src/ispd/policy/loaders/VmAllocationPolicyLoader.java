package ispd.policy.loaders;

import ispd.policy.allocation.vm.VmAllocationPolicy;

public class VmAllocationPolicyLoader
        extends GenericPolicyLoader<VmAllocationPolicy> {
    private static final String CLASS_PATH = "ispd.policy.allocation.vm.impl.";

    @Override
    protected String getClassPath() {
        return VmAllocationPolicyLoader.CLASS_PATH;
    }
}