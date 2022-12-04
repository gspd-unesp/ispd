package ispd.policy.loaders;

import ispd.policy.scheduling.cloud.CloudSchedulingPolicy;

public class CloudSchedulingPolicyLoader extends
        GenericPolicyLoader<CloudSchedulingPolicy> {
    private static final String CLASS_PATH =
            "ispd.policy.scheduling.cloud.impl.";

    @Override
    protected String getClassPath() {
        return CloudSchedulingPolicyLoader.CLASS_PATH;
    }
}
