package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.Collection;

public class UserEnergyControl extends UserProcessingControl {
    private final double energyEfficiencyRatioAgainstSystem;
    private final double energyConsumptionLimit;
    private final double ownedMachinesEnergyConsumption;
    private double currentEnergyConsumption = 0.0;

    public UserEnergyControl(
            final String userId,
            final Collection<? extends CS_Processamento> systemMachines,
            final double energyConsPercentage) {
        super(userId, systemMachines);

        this.ownedMachinesEnergyConsumption = this
                .ownedNonMasterMachinesIn(systemMachines)
                .mapToDouble(CS_Processamento::getConsumoEnergia)
                .sum();

        this.energyConsumptionLimit =
                this.calculateEnergyConsumptionLimit(energyConsPercentage);

        this.energyEfficiencyRatioAgainstSystem =
                this.calculateEnergyEfficiencyRatioAgainst(systemMachines);
    }

    private double calculateEnergyConsumptionLimit(final double energyConsPercentage) {
        return this.ownedMachinesEnergyConsumption * energyConsPercentage / 100;
    }

    private double calculateEnergyEfficiencyRatioAgainst(
            final Collection<? extends CS_Processamento> machines) {
        final var sysComputationPower = machines.stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .sum();

        final var sysEnergyConsumption = machines.stream()
                .mapToDouble(CS_Processamento::getConsumoEnergia)
                .sum();

        final var sysEnergyEff = sysComputationPower / sysEnergyConsumption;
        return sysEnergyEff / this.energyEfficiency();
    }

    private double energyEfficiency() {
        return this.getOwnedMachinesProcessingPower() / this.ownedMachinesEnergyConsumption;
    }

    @Override
    public boolean isEligibleForTask() {
        return super.isEligibleForTask()
               && !this.hasExceededEnergyLimit();
    }

    @Override
    public void stopTaskFrom(final CS_Processamento machine) {
        super.stopTaskFrom(machine);
        this.decreaseEnergyConsumption(machine.getConsumoEnergia());
    }

    @Override
    public void startTaskFrom(final CS_Processamento machine) {
        super.startTaskFrom(machine);
        this.increaseEnergyConsumption(machine.getConsumoEnergia());
    }

    private void increaseEnergyConsumption(final double amount) {
        this.currentEnergyConsumption += amount;
    }

    private void decreaseEnergyConsumption(final double amount) {
        this.currentEnergyConsumption -= amount;
    }

    private boolean hasExceededEnergyLimit() {
        return this.currentEnergyConsumption >= this.energyConsumptionLimit;
    }

    public double currentConsumptionWeightedByEfficiency() {
        return this.currentEnergyConsumption * this.energyEfficiencyRatioAgainstSystem;
    }

    public boolean hasLessEnergyConsumptionThan(final UserEnergyControl other) {
        return this.energyConsumptionLimit <= other.energyConsumptionLimit;
    }

    public boolean canUseMachineWithoutExceedingEnergyLimit(final CS_Processamento machine) {
        return this.currentEnergyConsumption + machine.getConsumoEnergia() <= this.energyConsumptionLimit;
    }

    public double getOwnedMachinesEnergyConsumption() {
        return this.ownedMachinesEnergyConsumption;
    }
}
