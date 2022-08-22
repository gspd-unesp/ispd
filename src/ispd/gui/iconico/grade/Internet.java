package ispd.gui.iconico.grade;

import java.awt.Image;
import java.util.ResourceBundle;

public class Internet extends VertexGridItem {

    /**
     * It represents the bandwidth.
     */
    private double bandwidth;

    /**
     * It represents the latency.
     */
    private double latency;

    /**
     * It represents the load factor.
     */
    private double loadFactor;

    /**
     * Constructor of {@link Internet} which specifies the
     * x-coordinate and y-coordinate (in cartesian coordinates),
     * the local and global identifiers.
     *
     * @param x        the x-coordinate in cartesian coordinates
     * @param y        the y-coordinate in cartesian coordinates
     * @param localId  the local identifier
     * @param globalId the global identifier
     */
    public Internet(final int x,
                    final int y,
                    final int localId,
                    final int globalId) {
        super(localId, globalId, "net", x, y);
    }

    /**
     * Return the internet attributes.
     *
     * @param translator the resource bundle containing
     *                       the translation messages
     * @return the internet attributes
     */
    @Override
    public String makeDescription(
            final ResourceBundle translator) {
        return ("%s %d<br>%s %d<br>%s: %s<br>%s %d<br>%s %d<br>%s: %s<br>%s:" +
                " %s<br>%s: %s").formatted(
                translator.getString("Local ID:"), this.id.getLocalId(),
                translator.getString("Global ID:"), this.id.getGlobalId(),
                translator.getString("Label"), this.id.getName(),
                translator.getString("X-coordinate:"), this.getX(),
                translator.getString("Y-coordinate:"), this.getY(),
                translator.getString("Bandwidth"), this.bandwidth,
                translator.getString("Latency"), this.latency,
                translator.getString("Load Factor"), this.loadFactor
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Internet makeCopy(final int mousePosX,
                             final int mousePosY,
                             final int globalId,
                             final int localId) {
        final var internet = new Internet(mousePosX,
                mousePosY, globalId, localId);
        internet.bandwidth = this.bandwidth;
        internet.loadFactor = this.loadFactor;
        internet.latency = this.latency;
        internet.checkConfiguration();
        return internet;
    }


    /**
     * It checks if the current internet configuration is
     * well configured; if so, then {@link #configured} is
     * set to {@code true}; otherwise, is set to {@code false}.
     */
    private void checkConfiguration() {
        this.configured = this.bandwidth > 0 && this.latency > 0;
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
     *
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
     *
     * @return the latency
     */
    public double getLatency() {
        return this.latency;
    }

    /**
     * It sets the latency.
     *
     * @param latency the latency to be set to
     */
    public void setLatency(final double latency) {
        this.latency = latency;
        this.checkConfiguration();
    }

    /* getImage */

    /**
     * Returns the internet image.
     *
     * @return the internet image
     */
    @Override
    public Image getImage() {
        return DesenhoGrade.internetIcon;
    }
}
