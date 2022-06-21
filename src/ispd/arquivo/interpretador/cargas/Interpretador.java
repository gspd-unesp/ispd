package ispd.arquivo.interpretador.cargas;

import ispd.motor.filas.Tarefa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Interpretador {
    private static final char FILE_SEPARATOR = '\\';
    private static final char FILE_TYPE_SEPARATOR = '.';
    private final String path;
    private String type;
    private String exit;
    private int taskCount = 0;

    public Interpretador(final String path) {
        this.path = path;
        final int i = path.lastIndexOf(Interpretador.FILE_TYPE_SEPARATOR);
        this.exit = (path.substring(0, i) + ".wmsx");
        this.type = path.substring(i + 1).toUpperCase();
        System.out.printf("%s-%s-%s%n", this.path, this.exit, this.type);

    }

    public String getSaida() {
        return this.exit;
    }

    public String getTipo() {
        return this.type;
    }

    @Override
    public String toString() {
        final int i = this.exit.lastIndexOf(Interpretador.FILE_SEPARATOR);
        this.exit = this.exit.substring(i + 1);
        return """
                File %s was generated sucessfully:
                \t- Generated from the format: %s
                \t- File has a workload of %d tasks"""
                .formatted(this.exit, this.type, this.taskCount);

    }

    public void geraTraceSim(final List<? extends Tarefa> tasks) {
        try {
            this.type = "iSPD";
            final FileWriter fp = new FileWriter(this.path);
            final BufferedWriter out = new BufferedWriter(fp);
            out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" " +
                    "standalone=\"no\"?>\n"
                    + "<!DOCTYPE system SYSTEM \"iSPDcarga.dtd\">");
            out.write("\n<system>");
            out.write("\n<trace>");
            out.write("\n<format kind=\"" + this.type + "\" />\n");
            int i = 0;
            for (final Tarefa tarefa : tasks) {
                if (tarefa.isCopy()) {
                    continue;
                }
                out.write("<task " + "id=\"" + tarefa.getIdentificador()
                        + "\" arr=\"" + tarefa.getTimeCriacao()
                        + "\" sts=\"" + "1"
                        + "\" cpsz =\"" + tarefa.getTamProcessamento()
                        + "\" cmsz=\"" + tarefa.getArquivoEnvio()
                        + "\" usr=\"" + tarefa.getProprietario());
                out.write("\" />\n");
                i++;
            }

            out.write("</trace>");
            out.write("\n</system>");

            this.taskCount = i;
            this.exit = this.path;
            out.close();
            fp.close();
        } catch (final IOException ex) {
            System.out.println("ERROR");
        }
    }
}