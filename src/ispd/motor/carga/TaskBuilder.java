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
    static final double FILE_RECEIVE_TIME = 0.0009765625;
    protected final Generator<Integer> idGenerator;
    protected final Distribution random;

    protected TaskBuilder() {
        this.idGenerator = new SequentialIntegerGenerator();
        this.random = new Distribution(System.currentTimeMillis());
    }

    public List<Tarefa> makeTasksEvenlyDistributedBetweenMasters(
            final RedeDeFilas qn, final int taskCount) {
        final var masters = qn.getMestres();

        return IntStream.range(0, taskCount)
                .map(i -> i % masters.size())
                .mapToObj(masters::get)
                .map(this::makeTaskFor)
                .collect(Collectors.toList());
    }

    public abstract Tarefa makeTaskFor(final CS_Processamento master);

    public Stream<Tarefa> makeMultipleTasksFor(
            final CS_Processamento master, final int quantity) {
        return IntStream.range(0, quantity)
                .mapToObj(i -> this.makeTaskFor(master));
    }
}