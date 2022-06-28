package ispd.gui.iconico.grade;

import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ResourceBundle;
import java.util.Set;

public class Link extends Edge implements GridItem {
    private static final Color DARK_GREEN = new Color(0, 130, 0);
    private final GridItemId id;
    private final Polygon arrow = new Polygon();
    private boolean isSelected = true;
    private double bandwidth = 0.0;
    private double loadFactor = 0.0;
    private double latency = 0.0;
    private boolean isCorrectlyConfigured = false;

    public Link(final Vertex origin,
                final Vertex destination,
                final int localId,
                final int globalId) {
        super(origin, destination);
        this.id = new GridItemId(localId, globalId,
                "link%d".formatted(globalId));
    }

    @Override
    public void draw(final Graphics g) {
        final double arrowWidth = 11.0f;
        final double theta = 0.423f;
        final int[] xPoints = new int[3];
        final int[] yPoints = new int[3];
        final double[] vecLine = new double[2];
        final double[] vecLeft = new double[2];
        final double fLength;
        final double th;
        final double ta;
        final double baseX;
        final double baseY;

        xPoints[0] = this.getX();
        yPoints[0] = this.getY();

        // build the line vector
        vecLine[0] = (double) xPoints[0] - this.getSource().getX();
        vecLine[1] = (double) yPoints[0] - this.getSource().getY();

        // build the arrow base vector - normal to the line
        vecLeft[0] = -vecLine[1];
        vecLeft[1] = vecLine[0];

        // setup length parameters
        fLength =
                Math.sqrt(vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1]);
        th = arrowWidth / (2.0f * fLength);
        ta = arrowWidth / (2.0f * (Math.tan(theta) / 2.0f) * fLength);

        // find the base of the arrow
        baseX = ((double) xPoints[0] - ta * vecLine[0]);
        baseY = ((double) yPoints[0] - ta * vecLine[1]);

        // build the points on the sides of the arrow
        xPoints[1] = (int) (baseX + th * vecLeft[0]);
        yPoints[1] = (int) (baseY + th * vecLeft[1]);
        xPoints[2] = (int) (baseX - th * vecLeft[0]);
        yPoints[2] = (int) (baseY - th * vecLeft[1]);

        this.arrow.reset();
        this.arrow.addPoint(xPoints[0], yPoints[0]);
        this.arrow.addPoint(xPoints[1], yPoints[1]);
        this.arrow.addPoint(xPoints[2], yPoints[2]);

        if (this.isSelected) {
            g.setColor(Color.BLACK);
        } else if (this.isCorrectlyConfigured) {
            g.setColor(Link.DARK_GREEN);
        } else {
            g.setColor(Color.RED);
        }
        g.drawLine(this.getSource().getX(), this.getSource().getY(),
                this.getDestination().getX(), this.getDestination().getY());
        g.fillPolygon(this.arrow);
    }

    @Override
    public boolean contains(final int x, final int y) {
        return this.arrow.contains(x, y);
    }

    @Override
    public boolean isSelected() {
        return this.isSelected;
    }

    @Override
    public void setSelected(final boolean selected) {
        this.isSelected = selected;
    }

    @Override
    public Integer getX() {
        return Link.biasedMidPoint(
                this.getSource().getX(),
                this.getDestination().getX()
        );
    }

    private static int biasedMidPoint(final int p1, final int p2) {
        return (p1 + 7 * p2) / 8;
    }

    @Override
    public Integer getY() {
        return Link.biasedMidPoint(
                this.getSource().getY(),
                this.getDestination().getY()
        );
    }

    @Override
    public GridItemId getId() {
        return this.id;
    }

    @Override
    public Set<GridItem> getConnectionsIn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Set<GridItem> getConnectionsOut() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String makeDescription(final ResourceBundle translator) {
        return ("%s %d<br>%s %d<br>%s: %s<br>%s %d<br>%s %d<br>%s %d<br>%s " +
                "%d<br>%s: %s<br>%s: %s<br>%s: %s").formatted(
                translator.getString("Local ID:"), this.id.getLocalId(),
                translator.getString("Global ID:"), this.id.getGlobalId(),
                translator.getString("Label"), this.id.getName(),
                translator.getString("X1-coordinate:"), this.getSource().getX(),
                translator.getString("Y1-coordinate:"), this.getSource().getY(),
                translator.getString("X2-coordinate:"),
                this.getDestination().getY(),
                translator.getString("Y2-coordinate:"),
                this.getDestination().getX(),
                translator.getString("Bandwidth"), this.bandwidth,
                translator.getString("Latency"), this.latency,
                translator.getString("Load Factor"), this.loadFactor
        );
    }


    @Override
    public Link makeCopy(final int mousePosX,
                         final int mousePosY,
                         final int copyGlobalId,
                         final int copyLocalId) {
        final Link temp = new Link(null, null, copyGlobalId, copyLocalId);
        temp.bandwidth = this.bandwidth;
        temp.latency = this.latency;
        temp.loadFactor = this.loadFactor;
        temp.validateConfiguration();
        return temp;
    }


    @Override
    public boolean isCorrectlyConfigured() {
        return this.isCorrectlyConfigured;
    }

    private void validateConfiguration() {
        this.isCorrectlyConfigured = this.bandwidth > 0 && this.latency > 0;
    }

    public double getBandwidth() {
        return this.bandwidth;
    }

    public void setBandwidth(final double bandwidth) {
        this.bandwidth = bandwidth;
        this.validateConfiguration();
    }

    public double getLoadFactor() {
        return this.loadFactor;
    }

    public void setLoadFactor(final double loadFactor) {
        this.loadFactor = loadFactor;
    }

    public double getLatency() {
        return this.latency;
    }

    public void setLatency(final double latency) {
        this.latency = latency;
        this.validateConfiguration();
    }
}