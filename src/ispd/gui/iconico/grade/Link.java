package ispd.gui.iconico.grade;

import ispd.gui.iconico.Vertex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.util.ResourceBundle;
import java.util.Set;

public class Link extends EdgeGridItem {

    /**
     * It represents the <em>dark green</em> color. Further,
     * it is used to draw the link using this color since
     * this link is configured.
     */
    private static final Color DARK_GREEN
            = new Color(0, 130, 0);

    private final Polygon arrowPolygon;

    /**
     * It represents the bandwidth.
     */
    private double bandwidth;

    /**
     * It represents the load factor.
     */
    private double loadFactor;

    /**
     * It represents the latency.
     */
    private double latency;

    /**
     * Constructor of {@link Link} which specifies the source,
     * destination vertices and the local and global
     * identifiers.
     *
     * @param source the source vertex
     * @param destination the destination vertex
     * @param localId the local identifier
     * @param globalId the global identifier
     */
    public Link(final Vertex source,
                final Vertex destination,
                final int localId,
                final int globalId) {
        super(localId, globalId, "link", source,
                destination, true);
        this.arrowPolygon = new Polygon();
    }

    /**
     * Returns the link attributes.
     *
     * @param resourceBundle the resource bundle containing
     *                       the translation messages
     * @return the link attributes
     */
    @Override
    public String getAttributes(
            final ResourceBundle resourceBundle) {
        return resourceBundle.getString("Local ID:") + " " + this.id.getLocalId()
                + "<br>" + resourceBundle.getString("Global ID:") + " " + this.id.getGlobalId()
                + "<br>" + resourceBundle.getString("Label") + ": " + this.id.getName()
                + "<br>" + resourceBundle.getString("X1-coordinate:") + " " + this.getSource().getX()
                + "<br>" + resourceBundle.getString("Y1-coordinate:") + " " + this.getSource().getY()
                + "<br>" + resourceBundle.getString("X2-coordinate:") + " " + this.getDestination().getY()
                + "<br>" + resourceBundle.getString("Y2-coordinate:") + " " + this.getDestination().getX()
                + "<br>" + resourceBundle.getString("Bandwidth") + ": " + this.bandwidth
                + "<br>" + resourceBundle.getString("Latency") + ": " + this.latency
                + "<br>" + resourceBundle.getString("Load Factor") + ": " + this.loadFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Link makeCopy(final int mousePosX,
                         final int mousePosY,
                         final int globalId,
                         final int localId) {
        final var link = new Link(null, null,
                globalId, localId);
        link.bandwidth = this.bandwidth;
        link.latency = this.latency;
        link.loadFactor = this.loadFactor;
        link.checkConfiguration();
        return link;
    }

    /**
     * It draws the link starting from the source vertex
     * and ending at the destination vertex.
     */
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
        final double baseX, baseY;

        xPoints[0] = this.getX();
        yPoints[0] = this.getY();

        // build the line vector
        vecLine[0] = (double) xPoints[0] - this.getSource().getX();
        vecLine[1] = (double) yPoints[0] - this.getSource().getY();

        // build the arrow base vector - normal to the line
        vecLeft[0] = -vecLine[1];
        vecLeft[1] = vecLine[0];

        // setup length parameters
        fLength = Math.sqrt(vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1]);
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

        this.arrowPolygon.reset();
        this.arrowPolygon.addPoint(xPoints[0], yPoints[0]);
        this.arrowPolygon.addPoint(xPoints[1], yPoints[1]);
        this.arrowPolygon.addPoint(xPoints[2], yPoints[2]);

        if (this.isSelected()) {
            g.setColor(Color.BLACK);
        } else if (this.isConfigured()) {
            g.setColor(DARK_GREEN);
        } else {
            g.setColor(Color.RED);
        }

        g.drawLine(this.getSource().getX(), this.getSource().getY(),
                this.getDestination().getX(), this.getDestination().getY());
        g.fillPolygon(this.arrowPolygon);
    }

    /**
     * It throws {@link UnsupportedOperationException}.
     */
    @Override
    public Set<GridItem> getInboundConnections() {
        throw new UnsupportedOperationException();
    }

    /**
     * It throws {@link UnsupportedOperationException}.
     */
    @Override
    public Set<GridItem> getOutboundConnections() {
        throw new UnsupportedOperationException();
    }

    /**
     * It checks if the current link configuration is well
     * configured; if so, then {@link #configured} is set
     * to {@code true}; otherwise, is set to {@code false}.
     */
    private void checkConfiguration() {
        this.configured = this.bandwidth > 0
                && this.latency > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final int x, final int y) {
        return this.arrowPolygon.contains(x, y);
    }

    /* Getters & Setters */

    /**
     * Returns the bandwidth.
     *
     * @return the bandwidth
     */
    public double getBandwidth() {
        return this.bandwidth;
    }


    /**
     * It sets the bandwidth.
     *
     * @param bandwidth the bandwidth to be set
     */
    public void setBandwidth(final double bandwidth) {
        this.bandwidth = bandwidth;
        this.checkConfiguration();
    }

    /**
     * Returns the load factor.
     * @return the load factor
     */
    public double getLoadFactor() {
        return this.loadFactor;
    }

    /**
     * It sets the load factor.
     *
     * @param loadFactor the load factor to be set
     */
    public void setLoadFactor(final double loadFactor) {
        this.loadFactor = loadFactor;
    }

    /**
     * Returns the latency.
     * @return the latency
     */
    public double getLatency() {
        return this.latency;
    }

    /**
     * It sets the latency
     *
     * @param latency the latency to be set
     */
    public void setLatency(final double latency) {
        this.latency = latency;
        checkConfiguration();
    }

    /**
     * Returns the x-coordinate in cartesian coordinates.
     * @return the x-coordinate in cartesian coordinates
     */
    @Override
    public Integer getX() {
        return (((((this.getSource().getX() + this.getDestination().getX()) / 2)
                + this.getDestination().getX()) / 2) + this.getDestination().getX()) / 2;
    }

    /**
     * Returns the y-coordinate in cartesian coordinates.
     * @return the y-coordinate in cartesian coordinates
     */
    @Override
    public Integer getY() {
        return (((((this.getSource().getY() + this.getDestination().getY()) / 2)
                + this.getDestination().getY()) / 2) + this.getDestination().getY()) / 2;
    }

    /* getImage */

    /**
     * Returns {@code null}.
     *
     * @return {@code null}
     */
    @Override
    public Image getImage() {
        return null;
    }
}
