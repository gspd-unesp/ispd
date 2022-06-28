package ispd.gui.iconico.grade;

import ispd.gui.iconico.Vertex;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class Machine extends Vertex implements GridItem {
    private static final int SOME_OFFSET = 15;
    private static final int CONTAINING_SIZE = 17;
    private static final String NO_SELECTION = "---";
    private final GridItemId id;
    private final Set<GridItem> connectionsIn = new HashSet<>(0);
    private final Set<GridItem> connectionsOut = new HashSet<>(0);
    private String algorithm = Machine.NO_SELECTION;
    private Double computationalPower = 0.0;
    private Integer processorCores = 1;
    private Double loadFactor = 0.0;
    private Boolean isMaster = false;
    private Double ramMemory = 0.0;
    private Double hardDisk = 0.0;
    private boolean isCorrectlyConfigured = false;
    private String owner = "user1";
    private List<GridItem> slaves = new ArrayList<>(0);
    private Double costPerProcessing = 0.0;
    private Double costPerMemory = 0.0;
    private Double costPerDisk = 0.0;
    private String vmmAllocationPolicy = Machine.NO_SELECTION;
    private Double energyConsumption;

    public Machine(final int x,
                   final int y,
                   final int localId,
                   final int globalId,
                   final Double energy) {
        super(x, y);
        this.id = new GridItemId(localId, globalId,
                "mac%d".formatted(globalId));
        this.energyConsumption = energy;
    }

    public Double getEnergyConsumption() {
        return this.energyConsumption;
    }

    public void setEnergyConsumption(final Double energyConsumption) {
        this.energyConsumption = energyConsumption;
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
        return ("%s %d<br>%s %d<br>%s: %s<br>%s %d<br>%s %d<br>%s: %s<br>%s: " +
                "%s%s").formatted(
                translator.getString("Local ID:"), this.id.getLocalId(),
                translator.getString("Global ID:"), this.id.getGlobalId(),
                translator.getString("Label"), this.id.getName(),
                translator.getString("X-coordinate:"), this.getX(),
                translator.getString("Y-coordinate:"), this.getY(),
                translator.getString("Computing power"),
                this.computationalPower,
                translator.getString("Load Factor"), this.loadFactor,
                this.describeRole(translator));
    }

    private String describeRole(final ResourceBundle translator) {
        if (!this.isMaster) {
            return "<br>" + translator.getString("Slave");
        }

        return "<br>%s<br>%s: %s".formatted(
                translator.getString("Master"),
                translator.getString("Scheduling algorithm"), this.algorithm
        );
    }

    public Boolean isMaster() {
        return this.isMaster;
    }

    @Override
    public Machine makeCopy(final int mousePosX, final int mousePosY,
                            final int copyGlobalId,
                            final int copyLocalId) {
        final Machine temp = new Machine(mousePosX, mousePosY, copyGlobalId,
                copyLocalId, this.energyConsumption);
        temp.algorithm = this.algorithm;
        temp.computationalPower = this.computationalPower;
        temp.loadFactor = this.loadFactor;
        temp.isMaster = this.isMaster;
        temp.owner = this.owner;
        temp.validateConfiguration();
        return temp;
    }

    @Override
    public boolean isCorrectlyConfigured() {
        return this.isCorrectlyConfigured;
    }

    private void validateConfiguration() {
        if (this.computationalPower > 0) {
            this.isCorrectlyConfigured =
                    !this.isMaster || (!Machine.NO_SELECTION.equals(this.algorithm) && !
                            Machine.NO_SELECTION.equals(this.vmmAllocationPolicy));
        } else {
            this.isCorrectlyConfigured = false;
        }
    }

    public Double getPoderComputacional() {
        return this.computationalPower;
    }

    public void setComputationalPower(final double computationalPower) {
        this.computationalPower = computationalPower;
        this.validateConfiguration();
    }

    public Double getLoadFactor() {
        return this.loadFactor;
    }

    public void setLoadFactor(final Double loadFactor) {
        this.loadFactor = loadFactor;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
        this.validateConfiguration();
    }

    @Override
    public void draw(final Graphics g) {
        g.drawImage(DesenhoGrade.machineIcon, this.getX() - Machine.SOME_OFFSET,
                this.getY() - Machine.SOME_OFFSET, null);
        if (this.isCorrectlyConfigured) {
            g.drawImage(DesenhoGrade.greenIcon,
                    this.getX() + Machine.SOME_OFFSET,
                    this.getY() + Machine.SOME_OFFSET, null);
        } else {
            g.drawImage(DesenhoGrade.redIcon, this.getX() + Machine.SOME_OFFSET,
                    this.getY() + Machine.SOME_OFFSET, null);
        }

        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(this.id.getGlobalId()), this.getX(),
                this.getY() + 30);
        // Se o icone estiver ativo, desenhamos uma margem nele.
        if (this.isSelected()) {
            g.setColor(Color.RED);
            g.drawRect(this.getX() - 19, this.getY() - 17, 37, 34);
        }
    }

    @Override
    public boolean contains(final int x, final int y) {
        if (x < this.getX() + Machine.CONTAINING_SIZE
            && x > this.getX() - Machine.CONTAINING_SIZE) {
            return y < this.getY() + Machine.CONTAINING_SIZE
                   && y > this.getY() - Machine.CONTAINING_SIZE;
        }
        return false;
    }

    public void setIsMaster(final Boolean isMaster) {
        this.isMaster = isMaster;
        this.validateConfiguration();
    }

    public List<GridItem> getEscravos() {
        return this.slaves;
    }

    public void setSlaves(final List<GridItem> slaves) {
        this.slaves = slaves;
    }

    public List<GridItem> getNosEscalonaveis() {
        final List<GridItem> escalonaveis = new ArrayList<GridItem>();
        final Set internet = new HashSet();
        for (final GridItem link : this.connectionsOut) {
            final GridItem gridItem = (GridItem) ((Link) link).getDestination();
            if (gridItem instanceof Cluster || gridItem instanceof Machine) {
                if (!escalonaveis.contains(gridItem)) {
                    escalonaveis.add(gridItem);
                }
            } else if (gridItem instanceof Internet) {
                internet.add(gridItem);
                this.getIndiretosEscalonaveis(gridItem, escalonaveis, internet);
            }
        }
        escalonaveis.remove(this);
        return escalonaveis;
    }

    private void getIndiretosEscalonaveis(final GridItem gridItem,
                                          final List<GridItem> escalonaveis,
                                          final Set internet) {
        for (final GridItem link : gridItem.getConnectionsOut()) {
            final GridItem item = (GridItem) ((Link) link).getDestination();
            if (item instanceof Cluster || item instanceof Machine) {
                if (!escalonaveis.contains(item)) {
                    escalonaveis.add(item);
                }
            } else if (item instanceof Internet) {
                if (!internet.contains(item)) {
                    internet.add(item);
                    this.getIndiretosEscalonaveis(item, escalonaveis, internet);
                }
            }
        }
    }

    public String getProprietario() {
        return this.owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
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

    public Double getCostPerProcessing() {
        return this.costPerProcessing;
    }

    public void setCostPerProcessing(final Double costPerProcessing) {
        this.costPerProcessing = costPerProcessing;
    }

    public Double getCostPerMemory() {
        return this.costPerMemory;
    }

    public void setCostPerMemory(final Double costPerMemory) {
        this.costPerMemory = costPerMemory;
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
        this.validateConfiguration();
    }

    /* package-private */ Set<GridItem> getNosIndiretosSaida() {
        final Set<GridItem> indiretosSaida = new HashSet<GridItem>();
        for (final GridItem link : this.connectionsOut) {
            final GridItem gridItem = (GridItem) ((Link) link).getDestination();
            if (gridItem instanceof Cluster || gridItem instanceof Machine) {
                indiretosSaida.add(gridItem);
            } else if (gridItem instanceof Internet) {
                indiretosSaida.add(gridItem);
                this.getIndiretosSaida(gridItem, indiretosSaida);
            }
        }
        return indiretosSaida;
    }

    private void getIndiretosSaida(final GridItem internet,
                                   final Set<GridItem> indiretosSaida) {
        for (final GridItem link : internet.getConnectionsOut()) {
            final GridItem item = (GridItem) ((Link) link).getDestination();
            if (item instanceof Cluster || item instanceof Machine) {
                indiretosSaida.add(item);
            } else if (item instanceof Internet) {
                if (!indiretosSaida.contains(item)) {
                    indiretosSaida.add(item);
                    this.getIndiretosSaida(item, indiretosSaida);
                }
            }
        }
    }

    /* package-private */ Set<GridItem> getNosIndiretosEntrada() {
        final Set<GridItem> indiretosEntrada = new HashSet<GridItem>();
        for (final GridItem link : this.connectionsIn) {
            final GridItem gridItem = (GridItem) ((Link) link).getSource();
            if (gridItem instanceof Cluster || gridItem instanceof Machine) {
                indiretosEntrada.add(gridItem);
            } else if (gridItem instanceof Internet) {
                indiretosEntrada.add(gridItem);
                this.getIndiretosEntrada(gridItem, indiretosEntrada);
            }
        }
        return indiretosEntrada;
    }

    private void getIndiretosEntrada(final GridItem internet,
                                     final Set<GridItem> indiretosEntrada) {
        for (final GridItem link : internet.getConnectionsIn()) {
            final GridItem item = (GridItem) ((Link) link).getSource();
            if (item instanceof Cluster || item instanceof Machine) {
                indiretosEntrada.add(item);
            } else if (item instanceof Internet) {
                if (!indiretosEntrada.contains(item)) {
                    indiretosEntrada.add(item);
                    this.getIndiretosSaida(item, indiretosEntrada);
                }
            }
        }
    }
}