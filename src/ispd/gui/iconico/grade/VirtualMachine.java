package ispd.gui.iconico.grade;

public class VirtualMachine {

    /**
     * It represents the amount of cores in the chip.
     */
    private int coreCount;

    /**
     * It represents the allocated memory in
     * <em>gigabytes (GB)</em>.
     */
    private double allocatedMemory;

    /**
     * It represents the allocated disk in
     * <em>gigabytes (GB)</em>.
     */
    private double allocatedDisk;

    /**
     * It represents its name.
     */
    private String name;

    /**
     * It represents its owner.
     */
    private String owner;

    /**
     * It represents the running operating system.s
     */
    private String operatingSystem;

    private String VMM;

    /**
     * Constructor of {@link VirtualMachine} which specifies
     * the id, owner, VMM, core count, allocated memory,
     * allocated disk and the running operating system.
     *
     * @param id              the id
     * @param owner           the owner
     * @param VMM             the VMM
     * @param coreCount       the core count in the chip
     * @param allocatedMemory the allocated memory in
     *                        <em>gigabytes (GB)</em>.
     * @param allocatedDisk   the allocated disk in
     *                        <em>gigabytes (GB)</em>.
     * @param operatingSystem the running operating system
     */
    public VirtualMachine(final String id,
                          final String owner,
                          final String VMM,
                          final int coreCount,
                          final double allocatedMemory,
                          final double allocatedDisk,
                          final String operatingSystem) {
        this.name = id;
        this.owner = owner;
        this.VMM = VMM;
        this.coreCount = coreCount;
        this.allocatedMemory = allocatedMemory;
        this.allocatedDisk = allocatedDisk;
        this.operatingSystem = operatingSystem;
    }

    /**
     * Returns the core count.
     *
     * @return the core count
     */
    public int getCoreCount() {
        return this.coreCount;
    }

    /**
     * It sets the core count.
     *
     * @param coreCount the core count to be set
     */
    public void setCoreCount(final int coreCount) {
        this.coreCount = coreCount;
    }

    /**
     * Returns the allocated memory.
     *
     * @return the allocated memory
     */
    public double getAllocatedMemory() {
        return this.allocatedMemory;
    }

    /**
     * It sets the allocated memory.
     *
     * @param allocatedMemory the allocated memory
     */
    public void setAllocatedMemory(
            final double allocatedMemory) {
        this.allocatedMemory = allocatedMemory;
    }

    /**
     * Returns the allocated disk.
     *
     * @return the allocated disk
     */
    public double getAllocatedDisk() {
        return this.allocatedDisk;
    }

    /**
     * It sets the allocated disk.
     *
     * @param allocatedDisk the allocated disk
     */
    public void setAllocatedDisk(
            final double allocatedDisk) {
        this.allocatedDisk = allocatedDisk;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * It sets the name
     *
     * @param name the name to be set
     */
    public void setName(final String name) {
        this.name = name;
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
     * Returns the running operating system.
     *
     * @return the running operating system
     */
    public String getOperatingSystem() {
        return this.operatingSystem;
    }

    /**
     * It sets the running operating system
     *
     * @param operatingSystem the running operating system
     *                        to be set
     */
    public void setOperatingSystem(
            final String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    /**
     * Returns the VMM.
     *
     * @return the VMM
     */
    public String getVMM() {
        return this.VMM;
    }

    /**
     * It sets the VMM.
     *
     * @param VMM the VMM to be set
     */
    public void setVMM(String VMM) {
        this.VMM = VMM;
    }
}
