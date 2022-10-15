package ispd.arquivo.exportador;

import org.w3c.dom.Document;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Convert iSPD model to other programs
 */
public class Exportador {
    /**
     * {@link Document} with iconic model to be exported
     */
    private final Document model;

    public Exportador(final Document model) {
        this.model = model;
    }

    /**
     * Convert iconic model from iSPD to GridSim java file.
     *
     * @param file File in which to save the model.
     */
    public void toGridSim(final File file) {
        try (final var out = new PrintWriter(
                new FileWriter(file, StandardCharsets.UTF_8), true)) {
            new GridSimExporter(this.model, out).export();
        } catch (final IOException e) {
            JOptionPane.showMessageDialog(
                    null, e.getMessage(), "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}