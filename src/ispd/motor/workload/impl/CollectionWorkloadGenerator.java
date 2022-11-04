package ispd.motor.workload.impl;

import ispd.motor.workload.WorkloadGenerator;
import ispd.motor.workload.WorkloadGeneratorType;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a workload from a homogeneous collection of other workloads.
 * Specifically, when used to host a collection of per-node tasks, the method
 * {@link #makeTaskList(RedeDeFilas)} can be used to collect all per-node
 * tasks into a single, flat list.
 */
public class CollectionWorkloadGenerator implements WorkloadGenerator {
    private final WorkloadGeneratorType type;
    private final List<WorkloadGenerator> workloadList;

    /**
     * Instantiate a CollectionWorkloadGenerator from a homogeneous {@code List} of other
     * {@link WorkloadGenerator}s.
     *
     * @param workloadList list of {@link WorkloadGenerator}s
     * @param type     type of {@link WorkloadGenerator}s hosted in the given list
     */
    public CollectionWorkloadGenerator(final List<WorkloadGenerator> workloadList, final WorkloadGeneratorType type) {
        this.type = type;
        this.workloadList = workloadList;
    }

    public List<WorkloadGenerator> getList() {
        return this.workloadList;
    }

    /**
     * Make a task list from the inner per-node load list, or an <b>empty</b>
     * one if another type of {@link WorkloadGenerator} is in the list.
     *
     * @param qn Queue network in which the tasks will be used
     * @return {@code List} with all, flattened, tasks in the per-node
     * workloads in {@link #workloadList}, or an empty {@code ArrayList} if
     * the type of workloads in the list is not {@link PerNodeWorkloadGenerator}.
     */
    @Override
    public List<Tarefa> makeTaskList(final RedeDeFilas qn) {
        if (this.type != WorkloadGeneratorType.PER_NODE) {
            return new ArrayList<>(0);
        }

        return this.workloadList.stream()
                .map(PerNodeWorkloadGenerator.class::cast)
                .flatMap(load -> load.makeTaskList(qn).stream())
                .collect(Collectors.toList());
    }

    /**
     * @return all string representations of the workloads in the inner list,
     * concatenated together with {@code newlines}.
     */
    @Override
    public String toString() {
        return this.workloadList.stream()
                .map(WorkloadGenerator::toString)
                .collect(Collectors.joining("\n"));
    }

    /**
     * @return the type of workload in the inner list {@link #workloadList}.
     */
    @Override
    public WorkloadGeneratorType getType() {
        return this.type;
    }
}