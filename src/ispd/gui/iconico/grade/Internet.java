package ispd.gui.iconico.grade;

import ispd.gui.iconico.Vertex;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class Internet extends Vertex implements GridItem {
    private static final int IMAGE_SIZE = 15;
    private static final int ICON_SIZE = 17;
    private final GridItemId id;
    private final Set<GridItem> connectionsIn = new HashSet<>(0);
    private final Set<GridItem> connectionsOut = new HashSet<>(0);
    private double bandwidth = 0.0;
    private double loadFactor = 0.0;
    private double latency = 0.0;
    private boolean isConfigured = false;

    public Internet(final int x,
                    final int y,
                    final int idLocal,
                    final int idGlobal) {
        super(x, y);
        this.id = new GridItemId(idLocal, idGlobal,
                "net%d".formatted(idGlobal));
    }

    @Override
    public void draw(final Graphics g) {
        g.drawImage(DesenhoGrade.internetIcon,
                this.getX() - Internet.IMAGE_SIZE,
                this.getY() - Internet.IMAGE_SIZE, null);

        if (this.isConfigured) {
            g.drawImage(DesenhoGrade.greenIcon,
                    this.getX() + Internet.IMAGE_SIZE,
                    this.getY() + Internet.IMAGE_SIZE, null);
        } else {
            g.drawImage(DesenhoGrade.redIcon, this.getX() + Internet.IMAGE_SIZE,
                    this.getY() + Internet.IMAGE_SIZE, null);
        }

        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(this.id.getGlobalId()), this.getX(),
                this.getY() + 30);
        // Se o icone estiver ativo, desenhamos uma margem nele.
        if (this.isSelected()) {
            g.setColor(Color.RED);
            g.drawRect(this.getX() - 19, this.getY() - Internet.ICON_SIZE, 37
                    , 34);
        }
    }

    @Override
    public boolean contains(final int x, final int y) {
        if (x < this.getX() + Internet.ICON_SIZE
            && x > this.getX() - Internet.ICON_SIZE) {
            return y < this.getY() + Internet.ICON_SIZE
                   && y > this.getY() - Internet.ICON_SIZE;
        }
        return false;
    }

    @Override
    public GridItemId getId() {
        return this.id;
    }

    @Override
    public Set<GridItem> getConnectionsIn() {
        return this.connectionsIn;
    }

    @Override
    public Set<GridItem> getConnectionsOut() {
        return this.connectionsOut;
    }

    @Override
    public String makeDescription(final ResourceBundle translator) {
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

    @Override
    public Internet makeCopy(final int mousePosX, final int mousePosY,
                             final int copyGlobalId, final int copyLocalId) {
        final var other = new Internet(mousePosX, mousePosY,
                copyGlobalId, copyLocalId);
        other.bandwidth = this.bandwidth;
        other.loadFactor = this.loadFactor;
        other.latency = this.latency;
        other.validateConfiguration();
        return other;
    }

    @Override
    public boolean isCorrectlyConfigured() {
        return this.isConfigured;
    }

    private void validateConfiguration() {
        this.isConfigured = this.bandwidth > 0 && this.latency > 0;
    }

    public double getBandwidth() {
        return this.bandwidth;
    }

    public void setBandwidth(final double bandwidth) {
        this.bandwidth = bandwidth;
        this.validateConfiguration();
    }

    public double getLatency() {
        return this.latency;
    }

    public void setLatency(final double latency) {
        this.latency = latency;
        this.validateConfiguration();
    }

    public double getLoadFactor() {
        return this.loadFactor;
    }

    public void setLoadFactor(final double loadFactor) {
        this.loadFactor = loadFactor;
    }
}