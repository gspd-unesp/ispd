package ispd.gui.auxiliar;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
class UserOperationTime implements Comparable<UserOperationTime> {
    // TODO: maybe this should be a record
    private final Double time;
    private final Boolean isStartTime;
    private final Double nodeUse;
    private final Integer userId;

    UserOperationTime(
            final double time,
            final boolean isStartTime,
            final Double nodeUse,
            final Integer userId) {
        this.time = time;
        this.isStartTime = isStartTime;
        this.nodeUse = nodeUse;
        this.userId = userId;
    }

    Integer getUserId() {
        return this.userId;
    }

    Boolean isStartTime() {
        return this.isStartTime;
    }

    Double getNodeUse() {
        return this.nodeUse;
    }

    @Override
    public int compareTo(final UserOperationTime o) {
        return this.time.compareTo(o.getTime());
    }

    Double getTime() {
        return this.time;
    }
}