package ispd.motor.workload.impl.task;

public class TraceTaskInfo {
    private final String[] fields;

    public TraceTaskInfo(final String s) {
        this(s.split("\""));
    }

    private TraceTaskInfo(final String[] fields) {
        this.fields = fields.clone();
    }

    public int id() {
        return Integer.parseInt(this.fields[1]);
    }

    public String user() {
        return this.fields[11];
    }

    public double processingTime() {
        return this.fieldAsDouble(7);
    }

    private double fieldAsDouble(final int index) {
        return Double.parseDouble(this.fields[index]);
    }

    public double sentFileSize() {
        return this.fieldAsDouble(9);
    }

    public double creationTime() {
        return this.fieldAsDouble(3);
    }

    public boolean shouldBeCanceled() {
        return this.status().contains("0") || this.status().contains("5");
    }

    private String status() {
        return this.fields[5];
    }
}