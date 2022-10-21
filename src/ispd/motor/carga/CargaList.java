package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a workload from a homogeneous collection of other workloads.
 * Specifically, when used to host a collection of per-node tasks, the method
 * {@link #toTarefaList(RedeDeFilas)} can be used to collect all per-node
 * tasks into a single, flat list.
 */
public class CargaList extends GerarCarga {
    private final int loadType;
    private final List<GerarCarga> workloadList;

    /**
     * Instantiate a CargaList from a homogeneous {@code List} of other
     * {@link GerarCarga}s.
     *
     * @param workloadList list of {@link GerarCarga}s
     * @param loadType     type of {@link GerarCarga}s hosted in the given list
     */
    public CargaList(final List workloadList, final int loadType) {
        this.loadType = loadType;
        this.workloadList = workloadList;
    }

    public List<GerarCarga> getList() {
        return this.workloadList;
    }

    /**
     * Make a task list from the inner per-node load list, or an <b>empty</b>
     * one if another type of {@link GerarCarga} is in the list.
     *
     * @param rdf Queue network in which the tasks will be used
     * @return {@code List} with all, flattened, tasks in the per-node
     * workloads in {@link #workloadList}, or an empty {@code ArrayList} if
     * the type of workloads in the list is not {@link CargaForNode}.
     */
    @Override
    public List<Tarefa> toTarefaList(final RedeDeFilas rdf) {
        if (this.loadType != GerarCarga.FORNODE) {
            return new ArrayList<>(0);
        }

        final var generator = new SequentialIntegerGenerator();

        return this.workloadList.stream()
                .map(CargaForNode.class::cast)
                .flatMap(load -> load.toTaskList(rdf).stream())
                .collect(Collectors.toList());
    }

    /**
     * @return all string representations of the workloads in the inner list,
     * concatenated together with {@code newlines}.
     */
    @Override
    public String toString() {
        return this.workloadList.stream()
                .map(GerarCarga::toString)
                .collect(Collectors.joining("\n"));
    }

    /**
     * @return the type of workload in the inner list {@link #workloadList}.
     */
    @Override
    public int getTipo() {
        return this.loadType;
    }
}