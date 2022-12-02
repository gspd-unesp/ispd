package ispd.motor.workload.impl.task;

import jdk.jfr.Unsigned;

/**
 * Class to represent the information gathered from a trace file about a
 * single task. See {@link #TraceTaskInfo(String)} for details about how the
 * file contents are interpreted.
 */
public class TraceTaskInfo {
    private final String[] fields;

    /**
     * Create a task information instance from the given {@link String}; the
     * string is split on its quotes ({@code '"'}) and the resulting array of
     * strings is interpreted as 'fields' to be parsed. The relation of the
     * index of a field in the array and what it is interpreted as is as
     * follows:
     * <ol start="0">
     *     <li><i>[IGNORED]</i></li>
     *     <li>Task Id</li>
     *     <li><i>[IGNORED]</i></li>
     *     <li>Creation Time</li>
     *     <li><i>[IGNORED]</i></li>
     *     <li>Status</li>
     *     <li><i>[IGNORED]</i></li>
     *     <li>Computation Size</li>
     *     <li><i>[IGNORED]</i></li>
     *     <li>Communication Size</li>
     *     <li><i>[IGNORED]</i></li>
     *     <li>User Id</li>
     * </ol>
     * Any further fields are also ignored.
     *
     * @param s {@link String} to be split and interpreted as fields
     *          containing task attributes.
     */
    public TraceTaskInfo(final String s) {
        this.fields = s.split("\"");
    }

    /**
     * @return the ID (an integral value) parsed from the task info.
     */
    public int id() {
        return TaskFields.ID.getAsInt(this.fields);
    }

    /**
     * @return the user ID (name) parsed from the task info.
     */
    public String user() {
        return TaskFields.USER.get(this.fields);
    }

    /**
     * @return the computation size (in MFlops) parsed from the task info.
     */
    public double computationSize() {
        return TaskFields.COMPUTATION.getAsDouble(this.fields);
    }

    /**
     * @return the communication size (in MBits) parsed from the task info.
     */
    public double communicationSize() {
        return TaskFields.COMMUNICATION.getAsDouble(this.fields);
    }

    /**
     * @return the creation time (in seconds) parsed from the task info.
     */
    public double creationTime() {
        return TaskFields.CREATION_TIME.getAsDouble(this.fields);
    }

    /**
     * Determines if the task information parsed demands that the task should
     * be cancelled or not. Specifically, a task should be cancelled if its
     * "status" field either contains as a substring {@code "0"} or {@code "5"}.
     *
     * @return {@code true} if this task should be cancelled (according to
     * the task information in the file), and {@code false} otherwise.
     */
    public boolean shouldBeCanceled() {
        return this.status().contains("0") || this.status().contains("5");
    }

    private String status() {
        return TaskFields.STATUS.get(this.fields);
    }

    /**
     * Helper {@code enum} to map field 'meaning' to/from the index that is
     * used to access that value in an array.<br>
     * Also contains utility methods to parse such fields from the array into
     * numeric values.
     */
    private enum TaskFields {
        ID(1),
        CREATION_TIME(3),
        STATUS(5),
        COMPUTATION(7),
        COMMUNICATION(9),
        USER(11),
        ;

        @Unsigned
        private final int index;

        TaskFields(final int index) {
            this.index = index;
        }

        private int getAsInt(final String[] fields) {
            return Integer.parseInt(this.get(fields));
        }

        private String get(final String[] fields) {
            return fields[this.index];
        }

        private double getAsDouble(final String[] fields) {
            return Double.parseDouble(this.get(fields));
        }
    }
}