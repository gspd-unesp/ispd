package ispd.arquivo.interpretador.simgrid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.w3c.dom.Document;

public class InterpretadorSimGrid {

    private static String fname;
    private Document modelo;

    private void setFileName(File f) {
        fname = f.getName();
    }

    public static String getFileName() {
        return fname;
    }

    public void interpreta(File file1, File file2) {
        boolean error;
        try {
            try {
                FileInputStream application_file = new FileInputStream(file1);
                FileInputStream plataform_file = new FileInputStream(file2);
                SimGrid parser = SimGrid.getInstance(application_file);
                setFileName(file1);
                SimGrid.ReInit(application_file);
                SimGrid.modelo();
                setFileName(file2);
                SimGrid.ReInit(plataform_file);
                SimGrid.modelo();
                error = parser.resultadoParser();
                if (!error) {
                    parser.writefile();
                    modelo = parser.getModelo().getDescricao();
                }
                parser.reset();
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Document getModelo() {
        return modelo;
    }
    
}
