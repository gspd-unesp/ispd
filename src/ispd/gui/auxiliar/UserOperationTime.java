package ispd.gui.auxiliar;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class UserOperationTime implements Comparable<UserOperationTime> {

    private final double time;
    private final boolean startTime;
    private final double nodeUse;
    private final int userId;

    /**
     * Constructor which specifies the time, start time
     * node use and the user id.
     *
     * @param time the time
     * @param startTime the start time
     * @param nodeUse the node use
     * @param userId the user id
     */
    public UserOperationTime(
            final double time,
            final boolean startTime,
            final double nodeUse,
            final int userId) {
        this.time = time;
        this.startTime = startTime;
        this.nodeUse = nodeUse;
        this.userId = userId;
    }

    /**
     * It returns the time.
     *
     * @return the time.
     */
    public double getTime() {
        return this.time;
    }

    /**
     * It returns the start time.
     *
     * @return the start time.
     */
    public boolean isStartTime() {
        return this.startTime;
    }

    /**
     * It returns the node use.
     *
     * @return the node use.
     */
    public double getNodeUse() {
        return this.nodeUse;
    }

    /**
     * It returns the user id.
     *
     * @return the user id
     */
    public int getUserId() {
        return this.userId;
    }

    /**
     * Compares the two specified {@link UserOperationTime} classes.
     * It is used the time set in these classes to compare them. It
     * returns 0 if the {@code this}'s time is equal to the second one,
     * a value less than 0 if {@code this}'s time is numerically less than
     * the second one and a value greater than 0 if {@code this}'s time is
     * numerically greater than the second one.
     *
     * @param o the another {@link UserOperationTime} to be compared.
     *
     * @return 0 if {@code this}'s time is numerically equal than the second one;
     *         a value less than 0 if {@code this}'s time is numerically less than
     *         the second one; and a value greater than 0 if {@code this}'s time is
     *         numerically greater than the second.
     *
     * @see Double#compare(double, double)
     */
    @Override
    public int compareTo(final UserOperationTime o) {
        return Double.compare(this.time, o.time);
    }
}