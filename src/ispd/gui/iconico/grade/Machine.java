package ispd.gui.iconico.grade;

import ispd.alocacaoVM.VMM;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class Machine extends VertexGridItem {

    /**
     * It represents the computational power.
     */
    private double computationalPower;

    /**
     * It represents the laod factor.
     */
    private double loadFactor;

    /**
     * It represents the amount of memory RAM in
     * <em>gigabytes (GB)</em>.
     */
    private double ram;

    /**
     * It represents the amount of hard disk in
     * <em>gigabytes (GB)</em>.
     */
    private double hardDisk;

    /**
     * It represents the energy consumption.
     */
    private double energyConsumption;

    /**
     * It represents the cost per processing.
     */
    private double costPerProcessing;

    /**
     * It represents the cost per memory.
     */
    private double costPerMemory;

    /**
     * It represents the cost per disk.
     */
    private double costPerDisk;

    /**
     * It represents the amount of cores in the chip.
     */
    private int coreCount;

    /**
     * It represents if this machine acts as master, that is,
     * if this variable is {@code true}, then this machine
     * is a master; otherwise {@code false}.
     */
    private boolean master;

    /**
     * It represents the owner.
     */
    private String owner;

    /**
     * It contains the scheduling algorithm used.
     */
    private String schedulingAlgorithm;

    /**
     * It contains the {@link VMM} allocation policy.
     */
    private String vmmAllocationPolicy;

    /**
     * It contains a list of this machine slaves.
     */
    private List<GridItem> slaves;

    /**
     * Constructor of {@link Machine} which specifies the
     * x-coordinate and y-coordinate (in cartesian coordinates),
     * the local, global identifiers and the energy consumption.
     *
     * @param x                 the x-coordinate in cartesian coordinates
     * @param y                 the y-coordinate in cartesian coordinates
     * @param localId           the local identifier
     * @param globalId          the global identifier
     * @param energyConsumption the energy consumption
     */
    public Machine(final int x,
                   final int y,
                   final int localId,
                   final int globalId,
                   final Double energyConsumption) {
        super(localId, globalId, "mac", x, y);
        this.owner = "user1";
        this.coreCount = 1;
        this.schedulingAlgorithm = "---";
        this.vmmAllocationPolicy = "---";
        this.energyConsumption = energyConsumption;
        this.slaves = new ArrayList<>();
    }

    /**
     * Returns the machine attributes.
     *
     * @param translator the resource bundle containing
     *                   the translation messages
     * @return the machine attributes
     */
    @Override
    public String makeDescription(
            final ResourceBundle translator) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Machine makeCopy(final int mousePosX,
                            final int mousePosY,
                            final int globalId,
                            final int localId) {
        final var machine = new Machine(mousePosX, mousePosY,
                globalId, localId, this.energyConsumption);

        machine.schedulingAlgorithm = this.schedulingAlgorithm;
        // machine.vmmAllocationPolicy = this.vmmAllocationPolicy;
        machine.computationalPower = this.computationalPower;
        machine.loadFactor = this.loadFactor;
        machine.master = this.master;
        machine.owner = this.owner;
        machine.coreCount = this.coreCount;
        machine.ram = this.ram;
        machine.hardDisk = this.hardDisk;
        machine.checkConfiguration();

        return machine;
    }

    /**
     * It checks if the current machine configuration is well
     * configured; if so, then {@link #configured} is set to
     * {@code true}; otherwise, is set to {@code false}.
     */
    private void checkConfiguration() {
        if (this.computationalPower <= 0) {
            this.configured = false;
            return;
        }

        this.configured = !(this.master &&
                ("---".equals(this.schedulingAlgorithm) ||
                        "---".equals(this.vmmAllocationPolicy)));
    }

    /**
     * It returns a set of <strong>connected</strong> nodes
     * starting from this {@link Machine} and ending in other
     * grid items. Further, {@code null} will never be returned,
     * that is, an empty set is at least returned.
     *
     * @return a set of connected outbound nodes.
     */
    protected Set<GridItem> connectedOutboundNodes() {
        final var outboundConnectedNodes
                = new HashSet<GridItem>();
        this.calculateConnectedOutboundNodes(this,
                outboundConnectedNodes);
        return outboundConnectedNodes;
    }

    /**
     * It calculates the <strong>connected</strong> outbound
     * nodes starting from the given <em>grid item</em>. Further,
     * those nodes found in the process are added into the
     * given outbound connected nodes set.
     *
     * @param gridItem               the grid item
     * @param outboundConnectedNodes the outbound connected
     *                               nodes
     * @implNote Pre-conditions:
     *         1. It is supposed that the given grid item is
     *         <strong>not null</strong>. Otherwise, a
     *         {@link NullPointerException} will be thrown.
     *         2. It is supposed that the given outbound
     *         connected nodes set is <strong>not null</strong>.
     *         Otherwise, a {@link NullPointerException} will be thrown.
     */
    private void calculateConnectedOutboundNodes(
            final GridItem gridItem,
            final Set<GridItem> outboundConnectedNodes) {

        for (final var gridItemLink :
                gridItem.getOutboundConnections()) {
            final var outboundLink = (Link) gridItemLink;
            final var destinationItem =
                    (GridItem) outboundLink.getDestination();

            /* If the destination item is a Cluster or */
            /* a Machine, then just add them */
            if (destinationItem instanceof Cluster ||
                    destinationItem instanceof Machine) {
                outboundConnectedNodes.add(destinationItem);
            }
            /* If the destination item is an Internet, then */
            /* add as well its outbound connections */
            else if (destinationItem instanceof Internet) {
                outboundConnectedNodes.add(destinationItem);

                /* Calculate the outbound connection nodes */
                /* starting from the Internet node */
                calculateConnectedOutboundNodes(destinationItem,
                        outboundConnectedNodes);
            }
        }
    }

    /**
     * It returns a set of <strong>connected</strong> nodes
     * ending at this {@link Machine} and starting from mother
     * grid items. Further, {@code null} will never be returned,
     * that is, an empty set is at least returned.
     *
     * @return a set of connected outbound nodes.
     */
    protected Set<GridItem> connectedInboundNodes() {
        final var inboundConnectedNodes
                = new HashSet<GridItem>();
        this.calculateConnectedInboundNodes(this,
                inboundConnectedNodes);
        return inboundConnectedNodes;
    }

    /**
     * It calculates the <strong>connected</strong> inbound
     * nodes ending at the given <em>grid item</em>. Further,
     * those nodes found in the process are added into the
     * inbound connected nodes set.
     *
     * @param gridItem              the grid item
     * @param inboundConnectedNodes the inbound connected
     *                              nodes
     * @implNote Pre-conditions:
     *         1. It is supposed that the given grid item is
     *         <strong>not null</strong>. Otherwise, a
     *         {@link NullPointerException} will be thrown.
     *         2. It is supposed that the given inbound connected
     *         nodes set is <strong>not null</strong>.
     *         Otherwise, {@link NullPointerException} will be thrown.
     */
    private void calculateConnectedInboundNodes(
            final GridItem gridItem,
            final Set<GridItem> inboundConnectedNodes) {
        for (final var gridItemLink :
                gridItem.getInboundConnections()) {
            final var inboundLink = (Link) gridItemLink;
            final var sourceItem = (GridItem) inboundLink.getSource();

            /* If the source item is a Cluster or a Machine, */
            /* then just add them */
            if (sourceItem instanceof Cluster ||
                    sourceItem instanceof Machine) {
                inboundConnectedNodes.add(sourceItem);
            }
            /* If the source item is an Internet, then add */
            /* as well its inbound connections */
            else if (sourceItem instanceof Internet) {
                inboundConnectedNodes.add(sourceItem);

                /* Calculate the inbound connection nodes */
                /* ending at this Internet node */
                calculateConnectedInboundNodes(sourceItem,
                        inboundConnectedNodes);
            }
        }
    }

    /**
     * It returns a list of <strong>connected</strong>
     * schedulable nodes. Further, {@code null} will never
     * be returned, that is, an empty list is at least returned.
     *
     * @return a list of connected schedulable nodes
     */
    public List<GridItem> connectedSchedulableNodes() {
        final var schedulableItems
                = new ArrayList<GridItem>();
        this.calculateConnectedSchedulableNodes(this,
                schedulableItems);

        /* Remove this grid item from the schedulable items */
        schedulableItems.remove(this);
        return schedulableItems;
    }

    /**
     * It calculates the <strong>connected</strong> schedulable
     * nodes. Further, those nodes found in the process are
     * added into the schedulable items list.
     *
     * @param gridItem         the grid item
     * @param schedulableItems the schedulable items
     * @implNote Pre-conditions:
     *         1. It is supposed that the given grid item is
     *         <strong>not null</strong>. Otherwise, a
     *         {@link NullPointerException} will be thrown.
     *         2. It is supposed that the given schedulable items
     *         list is <strong>not null</strong>. Otherwise,
     *         {@link NullPointerException} will be thrown.
     */
    private void calculateConnectedSchedulableNodes(
            final GridItem gridItem,
            final List<GridItem> schedulableItems) {
        for (final var gridItemLink : gridItem.getOutboundConnections()) {
            final var outboundLink = (Link) gridItemLink;
            final var destinationItem = (GridItem) outboundLink
                    .getDestination();

            /* If the destination item is a Cluster or is */
            /* a Machine, then just add them */
            if (destinationItem instanceof Cluster ||
                    destinationItem instanceof Machine) {
                /* Prevent duplicates */
                if (!schedulableItems.contains(destinationItem))
                    schedulableItems.add(destinationItem);
            }
            /* if the destination item is an Internet, then add */
            /* as well its schedulable nodes */
            else if (destinationItem instanceof Internet) {
                calculateConnectedSchedulableNodes(destinationItem,
                        schedulableItems);
            }
        }
    }

    /* Getters & Setters */

    /**
     * Returns the computational power.
     *
     * @return the computational power
     */
    public Double getComputationalPower() {
        return this.computationalPower;
    }

    /**
     * It sets the computational power
     *
     * @param computationalPower the computational power to
     *                           be set
     */
    public void setComputationalPower(
            final double computationalPower) {
        this.computationalPower = computationalPower;
        this.checkConfiguration();
    }


    /**
     * Returns the load factor.
     *
     * @return the load factor
     */
    public Double getLoadFactor() {
        return this.loadFactor;
    }

    /**
     * It sets the load factor
     *
     * @param loadFactor the load factor to be set
     */
    public void setLoadFactor(final Double loadFactor) {
        this.loadFactor = loadFactor;
    }

    /**
     * Returns the amount of memory RAM in <em>gigabytes (GB)</em>.
     *
     * @return the amount of memory RAM in <em>gigabytes (GB)</em>
     */
    public Double getRam() {
        return this.ram;
    }

    /**
     * It sets the amount of memory RAM in <em>gigabytes (GB)</em>.
     *
     * @param ram the amount of memory RAM to be set
     */
    public void setRam(final Double ram) {
        this.ram = ram;
    }

    /**
     * Returns the amount of hard disk in <em>gigabytes (GB)</em>.
     *
     * @return the amount of hard disk in <em>gigabytes (GB)</em>
     */
    public Double getHardDisk() {
        return this.hardDisk;
    }

    /**
     * It sets the amount of hard disk in <em>gigabytes (GB)</em>.
     *
     * @param hardDisk the amount of hard disk to be set
     */
    public void setHardDisk(final Double hardDisk) {
        this.hardDisk = hardDisk;
    }

    /**
     * Returns the energy consumption.
     *
     * @return the energy consumption
     */
    public Double getEnergyConsumption() {
        return this.energyConsumption;
    }

    /**
     * It sets the energy consumption
     *
     * @param energyConsumption the energy consumption to
     *                          be set
     */
    public void setEnergyConsumption(
            final Double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    /**
     * Returns the cost per processing.
     *
     * @return the cost per processing
     */
    public Double getCostPerProcessing() {
        return this.costPerProcessing;
    }

    /**
     * It sets the cost per processing
     *
     * @param costPerProcessing the cost per processing
     *                          to be set
     */
    public void setCostPerProcessing(
            final Double costPerProcessing) {
        this.costPerProcessing = costPerProcessing;
    }

    /**
     * Returns the cost per memory.
     *
     * @return the cost per memory
     */
    public Double getCostPerMemory() {
        return this.costPerMemory;
    }

    /**
     * It sets the cost per memory.
     *
     * @param costPerMemory the cost per memory to be set
     */
    public void setCostPerMemory(
            final Double costPerMemory) {
        this.costPerMemory = costPerMemory;
    }

    /**
     * Returns the cost per disk.
     *
     * @return the cost per disk to be set
     */
    public Double getCostPerDisk() {
        return this.costPerDisk;
    }

    /**
     * It sets the cost per disk.
     *
     * @param costPerDisk the cost per disk to be set
     */
    public void setCostPerDisk(
            final Double costPerDisk) {
        this.costPerDisk = costPerDisk;
    }

    /**
     * Returns the amount of cores in the chip.
     *
     * @return the amount of cores in the chip
     */
    public Integer getCoreCount() {
        return this.coreCount;
    }

    /**
     * It sets the amount of cores in the chip.
     *
     * @param coreCount the amount of cores in the chip
     *                  to be set
     */
    public void setCoreCount(final Integer coreCount) {
        this.coreCount = coreCount;
    }


    /**
     * Returns {@code true} since this machine is master.
     * Otherwise, {@code false} is returned.
     *
     * @return {@code true} since this machine is master;
     *         otherwise, {@code false} is returned.
     */
    public Boolean isMaster() {
        return this.master;
    }

    /**
     * It sets this machine as master or not.
     *
     * @param master {@code true} to set this machine as
     *               master, otherwise {@code false}.
     */
    public void setMaster(final Boolean master) {
        this.master = master;
        this.checkConfiguration();
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * It sets the owner.
     *
     * @param owner the owner to be set
     */
    public void setOwner(final String owner) {
        this.owner = owner;
    }

    /**
     * Returns the scheduling algorithm.
     *
     * @return the scheduling algorithm
     */
    public String getSchedulingAlgorithm() {
        return this.schedulingAlgorithm;
    }

    /**
     * It sets the scheduling algorithm.
     *
     * @param schedulingAlgorithm the scheduling algorithm
     *                            to be set
     */
    public void setSchedulingAlgorithm(
            final String schedulingAlgorithm) {
        this.schedulingAlgorithm = schedulingAlgorithm;
        this.checkConfiguration();
    }

    /**
     * Returns the {@link VMM} allocation policy.
     *
     * @return the {@link VMM} allocation policy.
     */
    public String getVmmAllocationPolicy() {
        return this.vmmAllocationPolicy;
    }

    /**
     * It sets the {@link VMM} allocation policy.
     *
     * @param vmmAllocationPolicy the {@link VMM} allocation
     *                            policy to be set
     */
    public void setVmmAllocationPolicy(
            final String vmmAllocationPolicy) {
        this.vmmAllocationPolicy = vmmAllocationPolicy;
        this.checkConfiguration();
    }

    /**
     * Returns the list of slaves.
     *
     * @return the list of slaves
     */
    public List<GridItem> getSlaves() {
        return this.slaves;
    }

    /**
     * It sets the list of slaves.
     *
     * @param slaves the slave list to be set
     */
    public void setSlaves(final List<GridItem> slaves) {
        this.slaves = slaves;
    }

    /* getImage */

    /**
     * Returns the machine icon image.
     *
     * @return the machine icon image
     */
    @Override
    public Image getImage() {
        return DesenhoGrade.machineIcon;
    }

    /* toString */

    /**
     * Returns the string representation of {@link Machine}.
     *
     * @return the string representation of {@link Machine}
     */
    @Override
    public String toString() {
        return "id: " + this.id.getGlobalId()
                + " " + this.id.getName();
    }

    /**
     * It describes this machine's role relative if it is a
     * master.
     *
     * @param translator the translator containing the
     *                   translated messages
     * @return the described machine role
     */
    private String describeRole(
            final ResourceBundle translator) {
        if (!this.master) {
            return "<br>" + translator.getString("Slave");
        }

        return "<br>%s<br>%s: %s".formatted(
                translator.getString("Master"),
                translator.getString("Scheduling algorithm"),
                this.schedulingAlgorithm
        );
    }
}
