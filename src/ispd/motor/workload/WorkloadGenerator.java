package ispd.motor.workload;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;

import java.util.List;

/**
 * Represents a workload generator from some data source.<br>
 * Workloads are represented as a {@link List} of {@link Tarefa}s.
 */
public interface WorkloadGenerator {
    /**
     * Create a {@link Tarefa} list as currently configured, distributed
     * between the masters in the given {@link RedeDeFilas}.
     *
     * @param qn {@link RedeDeFilas} with masters that will host the
     *           {@link Tarefa}s.
     * @return {@link List} of {@link Tarefa}s generated.
     */
    List<Tarefa> makeTaskList(RedeDeFilas qn);

    /**
     * @return the generator type of this instance.
     * @see WorkloadGeneratorType
     */
    WorkloadGeneratorType getType();

    /**
     * @return human-readable string representation of how the generator is
     * currently configured.
     */
    @Override
    String toString();

    /**
     * Outputs the current workload generator configuration, formatted
     * appropriately for inclusion in an iconic model file. For a more
     * <i>human-readable</i> representation, use {@link #toString()}.<br>
     * Odd formatting choices can be attributed to backwards compatibility
     * with how the
     * {@link ispd.arquivo.interpretador.internal.iconic.InterpretadorIconico
     * iconic model interpreter} parses workload configurations from files.
     *
     * @return iconic-model fitting string representation of how the
     * generator is currently configured.
     * @see ispd.arquivo.interpretador.internal.iconic.InterpretadorIconico
     * Iconic model interpreter
     */
    String formatForIconicModel();
}