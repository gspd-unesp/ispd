package ispd.motor.carga;

/**
 * Enumeration of all possible types of {@link WorkloadGenerator}s.
 */
public enum WorkloadGeneratorType {
    /**
     * Generate tasks with randomly generated sizes, configured for specific
     * master nodes in the system.
     */
    PER_NODE,
    /**
     * Generate tasks with randomly generated sizes, and distributed evenly
     * for all master nodes in the system.
     */
    RANDOM,
    /**
     * Generate tasks from a trace file, and distributed evenly for all
     * master nodes in the system..
     */
    TRACE,
}