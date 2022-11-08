package ispd.policy.mock;

public interface Policy<T extends Master> {
    void setMaster(final T master);
}