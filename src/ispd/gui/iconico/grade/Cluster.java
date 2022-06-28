package ispd.gui.iconico.grade;

import ispd.gui.iconico.Vertex;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class Cluster extends Vertex implements GridItem {
    private static final int SOME_OFFSET = 15;
    private static final String NO_SELECTION = "---";
    private static final int RANGE_WIDTH = 17;
    private final GridItemId id;
    private final Set<GridItem> connectionsIn = new HashSet<>(0);
    private final Set<GridItem> connectionsOut = new HashSet<>(0);
    private Double bandwidth = 0.0;
    private Double latency = 0.0;
    private String algorithm = Cluster.NO_SELECTION;
    private Double computationalPower = 0.0;
    private Integer processorCores = 1;
    private Integer slaveCount = 0;
    private Boolean isMaster = true;
    private Double ramMemory = 0.0;
    private Double hardDisk = 0.0;
    private boolean isConfigured = false;
    private String owner = "user1";
    private Double costPerProcessing = 0.0;
    private Double costPerMemory = 0.0;
    private Double costPerDisk = 0.0;
    private String vmmAllocationPolicy = Cluster.NO_SELECTION;
    private Double energyConsumption;

    public Cluster(
            final Integer x, final Integer y,
            final int idLocal,
            final int idGlobal,
            final Double energy) {
        super(x, y);
        this.id = new GridItemId(
                idLocal, idGlobal,
                "cluster%d".formatted(idGlobal)
        );
        this.energyConsumption = energy;
    }

    public Double getEnergyConsumption() {
        return this.energyConsumption;
    }

    public void setEnergyConsumption(final Double energy) {
        this.energyConsumption = energy;
    }

    @Override
    public String toString() {
        return "id: %d %s".formatted(this.id.getGlobalId(), this.id.getName());
    }

    @Override
    public void draw(final Graphics g) {
        g.drawImage(DesenhoGrade.clusterIcon,
                this.getX() - Cluster.SOME_OFFSET,
                this.getY() - Cluster.SOME_OFFSET, null);
        final var image = this.isConfigured
                ? DesenhoGrade.greenIcon
                : DesenhoGrade.redIcon;
        g.drawImage(image, this.getX() + Cluster.SOME_OFFSET,
                this.getY() + Cluster.SOME_OFFSET, null);

        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(this.id.getGlobalId()), this.getX(),
                this.getY() + 30);

        // Se o icone estiver ativo, desenhamos uma margem nele.
        if (this.isSelected()) {
            g.setColor(Color.RED);
            g.drawRect(this.getX() - 19, this.getY() - Cluster.RANGE_WIDTH,
                    37, 34);
        }
    }

    @Override
    public boolean contains(final int x, final int y) {
        if (x < this.getX() + Cluster.RANGE_WIDTH && x > this.getX() - Cluster.RANGE_WIDTH) {
            return y < this.getY() + Cluster.RANGE_WIDTH && y > this.getY() - Cluster.RANGE_WIDTH;
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
        return ("%s %d<br>%s %d<br>%s: %s<br>%s %d<br>%s %d<br>%s: %d<br>%s: " +
                "%s<br>%s: %s<br>%s: %s<br>%s: %s")
                .formatted(
                        translator.getString("Local ID:"),
                        this.id.getLocalId(),
                        translator.getString("Global ID:"),
                        this.id.getGlobalId(),
                        translator.getString("Label"),
                        this.id.getName(),
                        translator.getString("X-coordinate:"),
                        this.getX(),
                        translator.getString("Y-coordinate:"),
                        this.getY(),
                        translator.getString("Number of slaves"),
                        this.slaveCount,
                        translator.getString("Computing power"),
                        this.computationalPower,
                        translator.getString("Bandwidth"),
                        this.bandwidth,
                        translator.getString("Latency"),
                        this.latency,
                        translator.getString("Scheduling algorithm"),
                        this.algorithm
                );
    }

    /**
     * @param mousePosX the value of X position
     * @param mousePosY the value of Y position
     * @param copyGlobalId      the value of idGlobal
     * @param copyLocalId       the value of idLocal
     */
    @Override
    public Cluster makeCopy(final int mousePosX, final int mousePosY,
                            final int copyGlobalId, final int copyLocalId) {
        final var other = new Cluster(
                mousePosX, mousePosY,
                copyGlobalId, copyLocalId,
                this.energyConsumption
        );

        other.algorithm = this.algorithm;
        other.computationalPower = this.computationalPower;
        other.isMaster = this.isMaster;
        other.owner = this.owner;
        other.bandwidth = this.bandwidth;
        other.latency = this.latency;
        other.slaveCount = this.slaveCount;
        other.validateConfiguration();

        return other;
    }

    @Override
    public boolean isCorrectlyConfigured() {
        return this.isConfigured;
    }

    private void validateConfiguration() {
        this.isConfigured = this.shouldBeConfigured();
    }

    private boolean shouldBeConfigured() {
        for (final var attr : new double[] { this.bandwidth, this.latency,
                this.computationalPower, this.slaveCount }) {
            if (attr <= 0) {
                return false;
            }
        }

        return !(this.isMaster && this.algorithm.equals(Cluster.NO_SELECTION));
    }

    public void setComputationalPower(final Double computationalPower) {
        this.computationalPower = computationalPower;
        this.validateConfiguration();
    }

    public Boolean isMaster() {
        return this.isMaster;
    }

    public void setIsMaster(final Boolean isMaster) {
        this.isMaster = isMaster;
        this.validateConfiguration();
    }

    public Integer getProcessorCores() {
        return this.processorCores;
    }

    public void setProcessorCores(final Integer processorCores) {
        this.processorCores = processorCores;
    }

    public Double getRamMemory() {
        return this.ramMemory;
    }

    public void setRamMemory(final Double ramMemory) {
        this.ramMemory = ramMemory;
    }

    public Double getHardDisk() {
        return this.hardDisk;
    }

    public void setHardDisk(final Double hardDisk) {
        this.hardDisk = hardDisk;
    }

    public Integer getSlaveCount() {
        return this.slaveCount;
    }

    public void setSlaveCount(final Integer slaveCount) {
        this.slaveCount = slaveCount;
        this.validateConfiguration();
    }

    public Double getPoderComputacional() {
        return this.computationalPower;
    }

    public Double getCostPerProcessing() {
        return this.costPerProcessing;
    }

    public void setCostPerProcessing(final Double costPerProcessing) {
        this.costPerProcessing = costPerProcessing;
    }

    public Double getBandwidth() {
        return this.bandwidth;
    }

    public void setBandwidth(final Double bandwidth) {
        this.bandwidth = bandwidth;
        this.validateConfiguration();
    }

    public Double getCostPerMemory() {
        return this.costPerMemory;
    }

    public void setCostPerMemory(final Double costPerMemory) {
        this.costPerMemory = costPerMemory;
    }

    public Double getLatency() {
        return this.latency;
    }

    public void setLatency(final Double latency) {
        this.latency = latency;
        this.validateConfiguration();
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
        this.validateConfiguration();
    }

    public Double getCostPerDisk() {
        return this.costPerDisk;
    }

    public void setCostPerDisk(final Double costPerDisk) {
        this.costPerDisk = costPerDisk;
    }

    public String getVmmAllocationPolicy() {
        return this.vmmAllocationPolicy;
    }

    public void setVmmAllocationPolicy(final String vmmAllocationPolicy) {
        this.vmmAllocationPolicy = vmmAllocationPolicy;
    }

    public String getProprietario() {
        return this.owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }
}