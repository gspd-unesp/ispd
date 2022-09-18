package ispd.gui.auxiliar;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class UserOperationTime implements Comparable<UserOperationTime> {
    // TODO: maybe this should be a record
    private final Double time;
    private final Boolean isStartTime;
    private final Double nodeUse;
    private final Integer userId;

    public UserOperationTime(
            final double time,
            final boolean isStartTime,
            final Double nodeUse,
            final Integer userId) {
        this.time = time;
        this.isStartTime = isStartTime;
        this.nodeUse = nodeUse;
        this.userId = userId;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public Boolean isStartTime() {
        return this.isStartTime;
    }

    public Double getNodeUse() {
        return this.nodeUse;
    }

    @Override
    public int compareTo(final UserOperationTime o) {
        return this.time.compareTo(o.getTime());
    }

    public Double getTime() {
        return this.time;
    }
}