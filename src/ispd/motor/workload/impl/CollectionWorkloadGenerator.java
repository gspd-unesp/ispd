package ispd.motor.workload.impl;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.workload.WorkloadGenerator;
import ispd.motor.workload.WorkloadGeneratorType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a workload from a homogeneous collection of other workloads.
 * Specifically, when used to host a collection of per-node tasks, the method
 * {@link #makeTaskList(RedeDeFilas)} can be used to collect all per-node
 * tasks into a single new workload.
 */
public class CollectionWorkloadGenerator implements WorkloadGenerator {
    private final WorkloadGeneratorType type;
    private final List<WorkloadGenerator> list;

    /**
     * Instantiate a CollectionWorkloadGenerator from a homogeneous {@link
     * List} of other {@link WorkloadGenerator}s.
     *
     * @param type type of {@link WorkloadGenerator}s hosted in the
     *             given list
     * @param list list of {@link WorkloadGenerator}s
     */
    public CollectionWorkloadGenerator(
            final WorkloadGeneratorType type,
            final List<WorkloadGenerator> list) {
        this.type = type;
        this.list = list;
    }

    /**
     * @return the inner workload generator list.
     */
    public List<WorkloadGenerator> getList() {
        return this.list;
    }

    /**
     * Make a task list from the inner list of
     * {@link WorkloadGeneratorType#PER_NODE} workloads, or an <b>empty</b>
     * {@link ArrayList} if the list is of another type of
     * {@link WorkloadGenerator}.
     *
     * @return {@link List} with all tasks in the workloads in {@link #list},
     * or an empty {@code ArrayList} if the type of workloads in the list is not
     * {@link PerNodeWorkloadGenerator}.
     */
    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        if (this.type != WorkloadGeneratorType.PER_NODE) {
            return new ArrayList<>(0);
        }

        return this.list.stream()
                .map(PerNodeWorkloadGenerator.class::cast)
                .flatMap(load -> load.makeTaskList(qn).stream())
                .collect(Collectors.toList());
    }

    /**
     * @return the type of workload in the inner list {@link #list}.
     */
    @Override
    public WorkloadGeneratorType getType() {
        return this.type;
    }

    /**
     * The iconic model format for this workload generator consists of the
     * format for all the models on its {@link #list}, concatenated together
     * by newlines.
     */
    @Override
    public String formatForIconicModel() {
        return this.list.stream()
                .map(WorkloadGenerator::formatForIconicModel)
                .collect(Collectors.joining("\n"));
    }

    /**
     * The string representation for workloads of this class contains the
     * type of the workload generators in the inner list, and a
     * representation for such list.
     */
    @Override
    public String toString() {
        return """
                CollectionWorkloadGenerator{
                    type='%s',
                    list=[
                %s
                    ],
                }""".formatted(
                this.type,
                CollectionWorkloadGenerator.makeStringForList(this.list)
        );
    }

    private static String makeStringForList(final Collection<WorkloadGenerator> list) {
        return list.stream()
                .map(WorkloadGenerator::toString)
                .map(CollectionWorkloadGenerator::adaptStringRepresentation)
                .collect(Collectors.joining());
    }

    private static String adaptStringRepresentation(final String s) {
        return String.format("\t\t%s,\n", s);
    }
}