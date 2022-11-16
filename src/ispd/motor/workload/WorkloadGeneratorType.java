package ispd.motor.workload;

/**
 * Enumeration of all implemented types of {@link WorkloadGenerator}s.
 */
public enum WorkloadGeneratorType {
    /**
     * Generator of tasks with randomly generated sizes, configured for specific
     * master nodes in the system.
     */
    PER_NODE,
    /**
     * Generator of tasks with randomly generated sizes, and distributed evenly
     * among all master nodes in the system.
     */
    RANDOM,
    /**
     * Generator of tasks from a trace file, and distributed evenly among all
     * master nodes in the system.
     */
    TRACE,
}