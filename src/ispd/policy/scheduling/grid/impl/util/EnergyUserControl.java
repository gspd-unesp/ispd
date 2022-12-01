package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.metricas.MetricasUsuarios;

import java.util.Collection;

public class EnergyUserControl extends UserControl {
    private final double energyEfficiencyRatioAgainstSystem;
    private double currentEnergyConsumption = 0.0;
    private double energyConsumptionLimit = 0.0;
    private double ownedMachinesEnergyConsumption = 0.0;

    public EnergyUserControl(
            final String userId, final double ownedProcPower,
            final Collection<? extends CS_Processamento> systemMachines) {
        super(userId, ownedProcPower, systemMachines);
        this.energyEfficiencyRatioAgainstSystem =
                this.energyEfficiencyRatioAgainst(systemMachines);
    }

    private double energyEfficiencyRatioAgainst(
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

    public boolean hasLessEnergyConsumptionThan(final EnergyUserControl other) {
        return this.energyConsumptionLimit <= other.energyConsumptionLimit;
    }

    public boolean canUseMachineWithoutExceedingEnergyLimit(final CS_Processamento machine) {
        return this.currentEnergyConsumption + machine.getConsumoEnergia() <= this.energyConsumptionLimit;
    }

    public void calculateEnergyConsumptionLimit(final MetricasUsuarios metrics) {
        final var metricsLimit = metrics.getLimites().get(this.userId);
        this.energyConsumptionLimit =
                this.ownedMachinesEnergyConsumption * metricsLimit / 100;
    }

    public double getOwnedMachinesEnergyConsumption() {
        return this.ownedMachinesEnergyConsumption;
    }

    public void setOwnedMachinesEnergyConsumption(final double consumption) {
        this.ownedMachinesEnergyConsumption = consumption;
    }
}
