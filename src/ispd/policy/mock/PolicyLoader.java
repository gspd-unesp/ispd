package ispd.policy.mock;

public interface PolicyLoader<M extends Master> {
    Policy<M> load(final String policyName);
}