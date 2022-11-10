package ispd.gui.iconico.grade;

import ispd.alocacaoVM.VMM;

import java.awt.Image;
import java.util.ResourceBundle;

public class Cluster extends VertexGridItem {

    /**
     * It represents the bandwidth.
     */
    private double bandwidth;

    /**
     * It represents the latency.
     */
    private double latency;

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
     * It represents the computational power.
     */
    private double computationalPower;

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
     * It represents the amount of slaves.
     */
    private int slaveCount;

    /**
     * It represents if this cluster acts as master, that is,
     * if this variable is {@code true}, then this cluster is
     * a master; otherwise {@code false}.
     */
    private boolean master;

    /**
     * It contains the scheduling algorithm used.
     */
    private String schedulingAlgorithm;

    /**
     * It represents the owner.
     */
    private String owner;

    /**
     * It contains the {@link VMM} allocation policy.
     */
    private String vmmAllocationPolicy;

    /**
     * Constructor of {@link Cluster} which specifies the
     * x-coordinate and y-coordinate (in cartesian coordinates),
     * the local, global identifiers and the energy consumption.
     *
     * @param x                 the x-coordinate in cartesian coordinates
     * @param y                 the y-coordinate in cartesian coordinates
     * @param localId           the local identifier
     * @param globalId          the global identifier
     * @param energyConsumption the energy consumption
     */
    public Cluster(final Integer x,
                   final Integer y,
                   final int localId,
                   final int globalId,
                   final double energyConsumption) {
        super(localId, globalId, "cluster", x, y);
        this.schedulingAlgorithm = "---";
        this.owner = "user1";
        this.coreCount = 1;
        this.master = true;
        this.vmmAllocationPolicy = "---";
        this.energyConsumption = energyConsumption;
    }

    /**
     * Return the cluster attributes.
     *
     * @param translator the resource bundle containing
     *                   the translation messages
     * @return the cluster attributes
     */
    @Override
    public String makeDescription(
            final ResourceBundle translator) {
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
                        this.schedulingAlgorithm
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cluster makeCopy(final int mousePosX,
                            final int mousePosY,
                            final int globalId,
                            final int localId) {
        final var cluster = new Cluster(mousePosX, mousePosY,
                globalId, localId, this.energyConsumption);
        cluster.schedulingAlgorithm = this.schedulingAlgorithm;
        cluster.computationalPower = this.computationalPower;
        cluster.master = this.master;
        cluster.owner = this.owner;
        cluster.bandwidth = this.bandwidth;
        cluster.latency = this.latency;
        cluster.slaveCount = this.slaveCount;
        cluster.coreCount = this.coreCount;
        cluster.ram = this.ram;
        cluster.hardDisk = this.hardDisk;
        cluster.checkConfiguration();
        return cluster;
    }

    /**
     * It checks if the current cluster configuration is well
     * configured; if so, then {@link #configured} is set to
     * {@code true}; otherwise, is set to {@code false}.
     */
    private void checkConfiguration() {
        if (this.bandwidth <= 0 || this.latency <= 0 ||
                this.computationalPower <= 0 || this.slaveCount <= 0) {
            this.configured = false;
            return;
        }

        this.configured = !(this.master &&
                "---".equals(this.schedulingAlgorithm));
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
     * It sets the bandwidth
     *
     * @param bandwidth the bandwidth to be set
     */
    public void setBandwidth(final double bandwidth) {
        this.bandwidth = bandwidth;
        this.checkConfiguration();
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
     * @param latency the latency to be set
     */
    public void setLatency(final double latency) {
        this.latency = latency;
        this.checkConfiguration();
    }

    /**
     * Returns the amount of RAM memory in <em>gigabytes (GB)</em>.
     *
     * @return the amount of RAM memory in <em>gigabytes (GB)</em>
     */
    public double getRam() {
        return this.ram;
    }

    /**
     * It sets the amount of RAM memory in <em>gigabytes (GB)</em>.
     *
     * @param ram the amount of RAM memory to be set
     */
    public void setRam(final double ram) {
        this.ram = ram;
    }

    /**
     * Returns the amount of hard disk in <em>gigabytes (GB)</em>.
     *
     * @return the amount of hard disk in <em>gigabytes (GB)</em>
     */
    public double getHardDisk() {
        return this.hardDisk;
    }

    /**
     * It sets the amount of hard disk in <em>gigabytes (GB)</em>.
     *
     * @param hardDisk the amount of hard disk to be set to
     */
    public void setHardDisk(final double hardDisk) {
        this.hardDisk = hardDisk;
    }

    /**
     * Returns the computational power.
     *
     * @return the computational power.
     */
    public double getComputationalPower() {
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
     * Returns the energy consumption.
     *
     * @return the energy consumption
     */
    public double getEnergyConsumption() {
        return this.energyConsumption;
    }

    /**
     * Set the energy consumption.
     *
     * @param energyConsumption the energy consumption to
     *                          be set
     */
    public void setEnergyConsumption(
            final double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    /**
     * Returns the cost per processing.
     *
     * @return the cost per processing
     */
    public double getCostPerProcessing() {
        return this.costPerProcessing;
    }

    /**
     * It sets the cost per processing
     *
     * @param costPerProcessing the cost per processing to
     *                          to be set
     */
    public void setCostPerProcessing(
            final double costPerProcessing) {
        this.costPerProcessing = costPerProcessing;
    }

    /**
     * Returns the cost per memory.
     *
     * @return the cost per memory
     */
    public double getCostPerMemory() {
        return this.costPerMemory;
    }

    /**
     * It sets the cost per memory.
     *
     * @param costPerMemory the cost per memory to be set
     */
    public void setCostPerMemory(
            final double costPerMemory) {
        this.costPerMemory = costPerMemory;
    }

    /**
     * Returns the cost per disk.
     *
     * @return the cost per disk
     */
    public double getCostPerDisk() {
        return this.costPerDisk;
    }

    /**
     * It sets the cost per disk.
     *
     * @param costPerDisk the cost per disk
     */
    public void setCostPerDisk(final double costPerDisk) {
        this.costPerDisk = costPerDisk;
    }

    /**
     * Returns the amount of cores in the chip.
     *
     * @return the amount of cores in the chip
     */
    public int getCoreCount() {
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
     * Returns the amount of slaves.
     *
     * @return the amount of slaves
     */
    public int getSlaveCount() {
        return this.slaveCount;

    }

    /**
     * It sets the amount of slaves.
     *
     * @param slaveCount the amount of slaves to be set
     */
    public void setSlaveCount(final Integer slaveCount) {
        this.slaveCount = slaveCount;
        this.checkConfiguration();
    }

    /**
     * Returns {@code true} since this cluster is master.
     * Otherwise, {@code false} is returned.
     *
     * @return {@code true} since this cluster is master;
     *         otherwise, {@code false} is returned.
     */
    public boolean isMaster() {
        return this.master;
    }

    /**
     * It sets this cluster as master or not.
     *
     * @param master {@code true} to set this cluster as
     *               master, otherwise {@code false}.
     */
    public void setMaster(final Boolean master) {
        this.master = master;
        this.checkConfiguration();
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
     * It sets the scheduling algorithm
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
     * Returns the owner.
     *
     * @return the owner
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * It sets the owner
     *
     * @param owner the owner to be set
     */
    public void setOwner(final String owner) {
        this.owner = owner;
    }

    /**
     * Returns the {@link VMM} allocation policy.
     *
     * @return the {@link VMM} allocation policy
     */
    public String getVmmAllocationPolicy() {
        return vmmAllocationPolicy;
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
    }

    /* getImage */

    /**
     * Returns the cluster image.
     *
     * @return the cluster image
     */
    @Override
    public Image getImage() {
        return DesenhoGrade.clusterIcon;
    }

    /* toString */

    /**
     * Returns the string representation of the
     * {@link Cluster}.
     *
     * @return the string representation of the
     *         {@link Cluster}
     */
    @Override
    public String toString() {
        return "id: " + this.id.getGlobalId() + " " + this.id.getName();
    }
}
