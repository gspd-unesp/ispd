package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

import java.util.Collection;
import java.util.Comparator;

public class UserControl implements Comparable<UserControl> {
    protected final String userId;
    protected final long ownedMachinesCount;
    protected final double ownedMachinesProcessingPower;
    protected double energyEfficiencyRatioAgainstSystem;
    protected double ownedMachinesEnergyConsumption = 0.0;
    protected int taskDemand = 0;
    protected int availableMachineCount = 0;
    protected double availableProcessingPower = 0.0;

    public UserControl(
            final String userId, final double ownedProcPower,
            final Collection<? extends CS_Processamento> systemMachines) {
        this.userId = userId;
        // TODO: Warn if oPP is zero (ZDE in comparison)
        this.ownedMachinesProcessingPower = ownedProcPower;
        this.ownedMachinesCount = systemMachines.stream()
                .filter(this::hasMachine)
                .count();
    }

    private boolean hasMachine(final CS_Processamento machine) {
        return machine.getProprietario().equals(this.userId);
    }

    public boolean isEligibleForTask() {
        return this.taskDemand > 0;
    }

    public double penaltyWithProcessing(final double delta) {
        return (this.availableProcessingPower + delta - this.ownedMachinesProcessingPower)
               / this.ownedMachinesProcessingPower;
    }

    public void stopTaskFrom(final CS_Processamento machine) {
        this.decreaseAvailableMachines();
        this.decreaseAvailableProcessingPower(machine.getPoderComputacional());
    }

    public void decreaseAvailableMachines() {
        this.availableMachineCount--;
    }

    public void decreaseAvailableProcessingPower(final double amount) {
        this.availableProcessingPower -= amount;
    }

    public boolean canConcedeProcessingPower(final CS_Processamento machine) {
        return this.availableProcessingPower - machine.getPoderComputacional() >= this.ownedMachinesProcessingPower;
    }

    public double excessProcessingPower() {
        return this.availableProcessingPower - this.ownedMachinesProcessingPower;
    }

    public boolean isOwnerOf(final Tarefa task) {
        return this.userId.equals(task.getProprietario());
    }

    public int currentTaskDemand() {
        return this.taskDemand;
    }

    public void startTaskFrom(final CS_Processamento machine) {
        this.increaseAvailableMachines();
        this.increaseAvailableProcessingPower(machine.getPoderComputacional());
    }

    public void increaseAvailableMachines() {
        this.availableMachineCount++;
    }

    public void increaseAvailableProcessingPower(final double amount) {
        this.availableProcessingPower += amount;
    }

    public void decreaseTaskDemand() {
        this.taskDemand--;
    }

    public void increaseTaskDemand() {
        this.taskDemand++;
    }

    public int currentlyAvailableMachineCount() {
        return this.availableMachineCount;
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

    private double getOwnedMachinesEnergyConsumption() {
        return this.ownedMachinesEnergyConsumption;
    }

    public void setOwnedMachinesEnergyConsumption(final double consumption) {
        this.ownedMachinesEnergyConsumption = consumption;
    }

    public long getOwnedMachinesCount() {
        return this.ownedMachinesCount;
    }

    public boolean hasExcessProcessingPower() {
        return this.ownedMachinesProcessingPower <= this.availableProcessingPower;
    }

    public double currentlyAvailableProcessingPower() {
        return this.availableProcessingPower;
    }
}
