package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.metricas.MetricasUsuarios;

import java.util.Collection;
import java.util.Comparator;

public class UserControl implements Comparable<UserControl> {
    private final long ownedMachinesCount;
    private final String userId;
    private final double ownedMachinesProcessingPower;
    private final double energyEfficiencyRatioAgainstSystem;
    private int taskDemand = 0;
    private double ownedMachinesEnergyConsumption = 0.0;
    private int availableMachineCount = 0;
    private double availableProcessingPower = 0.0;
    private double currentEnergyConsumption = 0.0;
    private double energyConsumptionLimit = 0.0;

    public UserControl(
            final String userId, final double ownedProcPower,
            final Collection<? extends CS_Processamento> systemMachines) {
        this.userId = userId;
        // TODO: Warn if oPP is zero (ZDE in comparison)
        this.ownedMachinesProcessingPower = ownedProcPower;
        this.ownedMachinesCount = systemMachines.stream()
                .filter(this::isOwnedByUser)
                .count();
        this.energyEfficiencyRatioAgainstSystem =
                this.calculateEnergyEfficiencyRatioAgainst(systemMachines);
    }

    public boolean isOwnerOf(final Tarefa task) {
        return this.userId.equals(task.getProprietario());
    }

    public boolean isEligibleForTask() {
        return !this.hasNoTaskDemand() && !this.hasExceededEnergyLimit();
    }

    public boolean hasExceededEnergyLimit() {
        return this.currentEnergyConsumption >= this.energyConsumptionLimit;
    }

    public boolean hasNoTaskDemand() {
        return this.currentTaskDemand() == 0;
    }

    private boolean isOwnedByUser(final CS_Processamento machine) {
        return machine.getProprietario().equals(this.userId);
    }

    private double calculateEnergyEfficiencyRatioAgainst(
            final Collection<? extends CS_Processamento> machines) {
        final var sysCompPower = machines.stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .sum();

        final var sysEnergyCons = machines.stream()
                .mapToDouble(CS_Processamento::getConsumoEnergia)
                .sum();

        final var sysEnergyEfficiency = sysCompPower / sysEnergyCons;
        return sysEnergyEfficiency / this.energyEfficiency();
    }

    private double energyEfficiency() {
        return this.ownedMachinesProcessingPower / this.ownedMachinesEnergyConsumption;
    }

    public void gotTaskFrom(final CS_Processamento resource) {
        this.decreaseTaskDemand();
        this.increaseAvailableMachines();
        this.increaseAvailableProcessingPower(resource.getPoderComputacional());
        this.increaseEnergyConsumption(resource.getConsumoEnergia());
    }

    public void decreaseTaskDemand() {
        this.taskDemand--;
    }

    public void increaseAvailableMachines() {
        this.availableMachineCount++;
    }

    public void increaseAvailableProcessingPower(final double amount) {
        this.availableProcessingPower += amount;
    }

    public void increaseEnergyConsumption(final double amount) {
        this.currentEnergyConsumption += amount;
    }

    public void increaseTaskDemand() {
        this.taskDemand++;
    }

    public void decreaseAvailableMachines() {
        this.availableMachineCount--;
    }

    public void decreaseAvailableProcessingPower(final double amount) {
        this.availableProcessingPower -= amount;
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

    public void calculateEnergyConsumptionLimit(final MetricasUsuarios metrics) {
        final var metricsLimit = metrics.getLimites().get(this.userId);
        this.energyConsumptionLimit =
                this.ownedMachinesEnergyConsumption * metricsLimit / 100;
    }
}
