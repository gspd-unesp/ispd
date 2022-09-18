package ispd.arquivo.exportador;

import org.w3c.dom.Document;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Convert iSPD model to other programs
 */
public class Exportador {
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
        try (final var fw = new FileWriter(file);
             final var pw = new PrintWriter(fw, true)) {
            new ExportHelper(this.model).printCodeToFile(pw);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}