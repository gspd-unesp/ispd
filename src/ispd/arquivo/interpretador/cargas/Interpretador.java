package ispd.arquivo.interpretador.cargas;

import ispd.motor.filas.Tarefa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Responsible for outputting a simulation trace to a file path, from a
 * collection of tasks
 */
public class Interpretador {
    private static final char FILE_TYPE_SEPARATOR = '.';
    private final String path;
    private String type;

    public Interpretador(final String path) {
        this.path = path;
        final int i = path.lastIndexOf(Interpretador.FILE_TYPE_SEPARATOR);
        this.type = path.substring(i + 1).toUpperCase();
    }

    /**
     * Output simulation trace from collection of tasks
     */
    public void geraTraceSim(final Collection<? extends Tarefa> tasks) {
        this.type = "iSPD";

        try (final var out = new BufferedWriter(
                new FileWriter(this.path, StandardCharsets.UTF_8))) {

            out.write("""
                    <?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
                    <!DOCTYPE system SYSTEM "iSPDcarga.dtd">
                    <system>
                    <trace>
                    <format kind="%s" />
                    %s
                    </trace>
                    </system>""".formatted(
                    this.type, Interpretador.makeTasksDescription(tasks)
            ));

        } catch (final IOException ignored) {
        }
    }

    private static String makeTasksDescription(final Collection<?
            extends Tarefa> tasks) {
        return tasks.stream()
                .filter(Predicate.not(Tarefa::isCopy))
                .map(Interpretador::makeTaskDescription)
                .collect(Collectors.joining());
    }

    private static String makeTaskDescription(final Tarefa task) {
        return """
                <task id="%d" arr="%s" sts="1" cpsz ="%s" cmsz="%s" usr="%s" />
                """.formatted(
                task.getIdentificador(),
                task.getTimeCriacao(),
                task.getTamProcessamento(),
                task.getArquivoEnvio(),
                task.getProprietario()
        );
    }
}