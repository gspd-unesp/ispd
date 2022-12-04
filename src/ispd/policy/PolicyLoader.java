package ispd.policy;

public interface PolicyLoader <T extends Policy<?>> {
    T loadPolicy(String policyName);
}
