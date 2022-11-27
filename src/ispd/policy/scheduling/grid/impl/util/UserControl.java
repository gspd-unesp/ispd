package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.Collection;
import java.util.Comparator;

public class UserControl implements Comparable<UserControl> {
    private final long ownedMachinesCount;
    private final String userId;
    private final double ownedMachinesProcessingPower;
    private int taskDemand = 0;
    private double ownedMachinesEnergyConsumption = 0.0;
    private int availableMachineCount = 0;
    private double availableProcessingPower = 0.0;
    private double currentEnergyConsumption = 0.0;
    private double energyConsumptionLimit = 0.0;
    private double energyEfficiencyRatioAgainstSystem = 0.0;

    public UserControl(
            final String userId, final double ownedProcPower,
            final Collection<? extends CS_Processamento> machines) {
        this.userId = userId;
        // TODO: Warn if oPP is zero (ZDE in comparison)
        this.ownedMachinesProcessingPower = ownedProcPower;
        this.ownedMachinesCount = machines.stream()
                .filter(this::isOwnedByUser)
                .count();
    }

    private boolean isOwnedByUser(final CS_Processamento machine) {
        return machine.getProprietario().equals(this.userId);
    }

    public void calculateEnergyEfficiencyAgainst(
            final double sysProcPower, final double sysEnergyConsumption) {
        final var sysEnergyEfficiency = sysProcPower / sysEnergyConsumption;
        this.energyEfficiencyRatioAgainstSystem =
                sysEnergyEfficiency / this.energyEfficiency();
    }

    private double energyEfficiency() {
        return this.ownedMachinesProcessingPower / this.ownedMachinesEnergyConsumption;
    }

    public void increaseTaskDemand() {
        this.taskDemand++;
    }

    public void decreaseTaskDemand() {
        this.taskDemand--;
    }

    public void increaseAvailableMachines() {
        this.availableMachineCount++;
    }

    public void decreaseAvailableMachines() {
        this.availableMachineCount--;
    }

    public void increaseAvailableProcessingPower(final double amount) {
        this.availableProcessingPower += amount;
    }

    public void decreaseAvailableProcessingPower(final double amount) {
        this.availableProcessingPower -= amount;
    }

    public void increaseEnergyConsumption(final double amount) {
        this.currentEnergyConsumption += amount;
    }

    public void decreaseEnergyConsumption(final double amount) {
        this.currentEnergyConsumption -= amount;
    }

    public String getUserId() {
        return this.userId;
    }

    public int currentTaskDemand() {
        return this.taskDemand;
    }

    public double getEnergyConsumptionLimit() {
        return this.energyConsumptionLimit;
    }

    public void setEnergyConsumptionLimit(final double limit) {
        this.energyConsumptionLimit = limit;
    }

    public int currentlyAvailableMachineCount() {
        return this.availableMachineCount;
    }

    public double currentEnergyConsumption() {
        return this.currentEnergyConsumption;
    }

    public double calculatedEnergyEfficiencyRatio() {
        return this.energyEfficiencyRatioAgainstSystem;
    }

    @Override
    public int compareTo(final UserControl o) {
        // TODO: Document that comparison uses non-final fields
        // TODO: Document ordering inconsistent with equals
        return Comparator
                .comparingDouble(UserControl::ratioOfProcessingPowerInUse)
                .thenComparingDouble(UserControl::getOwnedMachinesProcessingPower)
                .thenComparingDouble(UserControl::getOwnedMachinesEnergyConsumption)
                .reversed()
                .compare(this, o);
    }

    private double ratioOfProcessingPowerInUse() {
        return (this.availableProcessingPower - this.ownedMachinesProcessingPower)
               / this.ownedMachinesProcessingPower;
    }

    public double getOwnedMachinesProcessingPower() {
        return this.ownedMachinesProcessingPower;
    }

    public double getOwnedMachinesEnergyConsumption() {
        return this.ownedMachinesEnergyConsumption;
    }

    public void setOwnedMachinesEnergyConsumption(final double consumption) {
        this.ownedMachinesEnergyConsumption = consumption;
    }

    public double currentlyAvailableProcessingPower() {
        return this.availableProcessingPower;
    }

    public long getOwnedMachinesCount() {
        return this.ownedMachinesCount;
    }
}
