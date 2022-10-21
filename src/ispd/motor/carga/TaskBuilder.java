package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class TaskBuilder {
    protected final Generator<Integer> idGenerator;
    protected final Distribution random;

    protected TaskBuilder() {
        this.idGenerator = new SequentialIntegerGenerator();
        this.random = new Distribution(System.currentTimeMillis());
    }

    public List<Tarefa> makeTasksDistributedBetweenMasters(
            final RedeDeFilas qn, final int taskCount) {
        final var masters = qn.getMestres();
        final var masterCount = masters.size();
        final var perMaster = taskCount / masterCount;
        final var remainder = taskCount % masterCount;

        return Stream.concat(
                masters.stream().flatMap(
                        m -> this.makeMultipleTasksFor(m, perMaster)),
                this.makeMultipleTasksFor(masters.get(0), remainder)
        ).collect(Collectors.toList());
    }

    public Stream<Tarefa> makeMultipleTasksFor(
            final CS_Processamento master, final int quantity) {
        return IntStream.range(0, quantity)
                .mapToObj(i -> this.makeTaskFor(master));
    }

    public abstract Tarefa makeTaskFor(final CS_Processamento master);
}