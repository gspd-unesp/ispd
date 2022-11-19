package ispd.policy._mock;

public interface Policy <T extends Master> {
    void setMaster(final T master);
}
