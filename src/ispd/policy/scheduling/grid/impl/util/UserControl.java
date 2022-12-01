package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import jdk.jfr.Percentage;

import java.util.Collection;

public class UserControl {
    protected final String userId;
    protected final double ownedMachinesProcessingPower;
    private final long ownedMachinesCount;
    private int taskDemand = 0;
    private int usedMachineCount = 0;
    private double usedProcessingPower = 0.0;

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
        return (this.usedProcessingPower + delta - this.ownedMachinesProcessingPower)
               / this.ownedMachinesProcessingPower;
    }

    public void stopTaskFrom(final CS_Processamento machine) {
        this.decreaseUsedMachines();
        this.decreaseUsedProcessingPower(machine.getPoderComputacional());
    }

    public void decreaseUsedMachines() {
        this.usedMachineCount--;
    }

    public void decreaseUsedProcessingPower(final double amount) {
        this.usedProcessingPower -= amount;
    }

    public boolean canConcedeProcessingPower(final CS_Processamento machine) {
        return this.excessProcessingPower() >= machine.getPoderComputacional();
    }

    public double excessProcessingPower() {
        return this.ownedMachinesProcessingPower - this.usedProcessingPower;
    }

    public boolean isOwnerOf(final Tarefa task) {
        return this.userId.equals(task.getProprietario());
    }

    public int currentTaskDemand() {
        return this.taskDemand;
    }

    public void startTaskFrom(final CS_Processamento machine) {
        this.increaseUsedMachines();
        this.increaseUsedProcessingPower(machine.getPoderComputacional());
    }

    public void increaseUsedMachines() {
        this.usedMachineCount++;
    }

    public void increaseUsedProcessingPower(final double amount) {
        this.usedProcessingPower += amount;
    }

    public void decreaseTaskDemand() {
        this.taskDemand--;
    }

    public void increaseTaskDemand() {
        this.taskDemand++;
    }

    public int currentlyAvailableMachineCount() {
        return this.usedMachineCount;
    }

    @Percentage
    public double percentageOfProcessingPowerUsed() {
        return this.usedProcessingPower / this.ownedMachinesProcessingPower;
    }

    public double getOwnedMachinesProcessingPower() {
        return this.ownedMachinesProcessingPower;
    }

    public long getOwnedMachinesCount() {
        return this.ownedMachinesCount;
    }

    public boolean hasExcessProcessingPower() {
        return this.excessProcessingPower() >= 0;
    }

    public double currentlyUsedProcessingPower() {
        return this.usedProcessingPower;
    }
}
