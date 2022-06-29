package ispd.gui.auxiliar;

/**
 * Represents an interval of time during which a machine was executing.
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class ParesOrdenadosUso implements Comparable<ParesOrdenadosUso> {
    // TODO: Rename
    private final Double start;
    private final Double end;

    public ParesOrdenadosUso(final double start, final double end) {
        this.start = start;
        this.end = end;
    }

    public Double getFim() {
        return this.end;
    }

    @Override
    public String toString() {
        return "%s %s".formatted(this.start, this.end);
    }

    @Override
    public int compareTo(final ParesOrdenadosUso o) {
        return this.start.compareTo(o.getInicio());
    }

    public Double getInicio() {
        return this.start;
    }
}