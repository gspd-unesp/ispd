package ispd.policy.mock;

public interface PolicyLoader <P extends Policy<?>> {
    P load(final String policyName);
}
