package ispd.policy._mock;

public interface PolicyLoader <P extends Policy<?>> {
    P load(final String policyName);
}
