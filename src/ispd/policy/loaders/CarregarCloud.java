package ispd.policy.loaders;

import ispd.policy.scheduling.cloud.CloudSchedulingPolicy;

public class CarregarCloud extends PolicyLoader<CloudSchedulingPolicy> {
    private static final String CLASS_PATH =
            "ispd.policy.scheduling.cloud.impl.";

    @Override
    protected String getClassPath() {
        return CarregarCloud.CLASS_PATH;
    }
}
