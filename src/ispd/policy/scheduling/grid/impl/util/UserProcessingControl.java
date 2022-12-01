package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.scheduling.grid.GridMaster;
import jdk.jfr.Percentage;
import jdk.jfr.Unsigned;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class UserProcessingControl {
    private final String userId;
    @Unsigned
    private final long ownedMachinesCount;
    @Unsigned
    private final double ownedMachinesProcessingPower;
    @Unsigned
    private int taskDemand = 0;
    @Unsigned
    private int usedMachineCount = 0;
    @Unsigned
    private double usedProcessingPower = 0.0;

    public UserProcessingControl(
            final String userId,
            final Collection<? extends CS_Processamento> systemMachines) {
        this.userId = userId;

        this.ownedMachinesProcessingPower = this
                .ownedNonMasterMachinesIn(systemMachines)
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .sum();

        this.ownedMachinesCount = systemMachines.stream()
                .filter(this::hasMachine)
                .toList().size();
    }

    protected Stream<? extends CS_Processamento> ownedNonMasterMachinesIn(
            final Collection<? extends CS_Processamento> systemMachines) {
        return systemMachines.stream()
                .filter(this::hasMachine)
                .filter(Predicate.not(GridMaster.class::isInstance));
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

    public int currentlyUsedMachineCount() {
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

    public boolean hasExcessMachines() {
        return this.excessMachines() > 0;
    }

    public long excessMachines() {
        return this.ownedMachinesCount - this.usedMachineCount;
    }
}
