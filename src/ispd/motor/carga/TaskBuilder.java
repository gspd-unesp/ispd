package ispd.motor.carga;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class TaskBuilder {
    protected final Generator<Integer> idGenerator;
    protected final Distribution random;

    protected TaskBuilder() {
        this.idGenerator = new SequentialIntegerGenerator();
        this.random = new Distribution(System.currentTimeMillis());
    }

    public Stream<Tarefa> makeMultipleTasksFrom(
            final CS_Processamento master, final int quantity) {
        return IntStream.range(0, quantity)
                .mapToObj(i -> this.makeTaskFrom(master));
    }

    public abstract Tarefa makeTaskFrom(final CS_Processamento master);
}