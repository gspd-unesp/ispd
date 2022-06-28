package ispd.gui.iconico.grade;

public class VirtualMachine {
    private final int processorCount;
    private final double allocatedMemory;
    private final double allocatedDisk;
    private String name;
    private String owner;
    private String vmm;
    private String os;

    public VirtualMachine(final String id,
                          final String owner,
                          final String vmm,
                          final int processorCount,
                          final double allocatedMemory,
                          final double allocatedDisk,
                          final String os) {
        this.name = id;
        this.owner = owner;
        this.vmm = vmm;
        this.processorCount = processorCount;
        this.allocatedMemory = allocatedMemory;
        this.allocatedDisk = allocatedDisk;
        this.os = os;
    }

    public String getProprietario() {
        return this.owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public String getVmm() {
        return this.vmm;
    }

    public void setVmm(final String vmm) {
        this.vmm = vmm;
    }

    public int getPoderComputacional() {
        return this.processorCount;
    }

    public double getAllocatedMemory() {
        return this.allocatedMemory;
    }

    public double getAllocatedDisk() {
        return this.allocatedDisk;
    }

    public String getOs() {
        return this.os;
    }

    public void setOs(final String os) {
        this.os = os;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String nome) {
        this.name = nome;
    }
}