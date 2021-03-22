package ispd.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogExceptions implements Thread.UncaughtExceptionHandler {

    private Component parentComponent;
    private JTextArea area;
    private JScrollPane scroll;

    public void setParentComponent(Component parentComponent) {
        this.parentComponent = parentComponent;
    }

    public LogExceptions(Component gui) {
        this.parentComponent = gui;
        File aux = new File("Erros");
        if (!aux.exists()) {
            aux.mkdir();
        }
        //iniciar parte grafica
        area = new JTextArea();
        area.setEditable(false);
        scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(500, 300));
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ByteArrayOutputStream fosErr = new ByteArrayOutputStream();
        PrintStream psErr = new PrintStream(fosErr);
        e.printStackTrace(psErr);
        mostrarErro(fosErr);
    }

    private void mostrarErro(ByteArrayOutputStream objErr) {
        try {
            if (objErr.size() > 0) {
                String erro = "";
                erro += "\n---------- error description ----------\n";
                erro += objErr.toString();
                erro += "\n---------- error description ----------\n";
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = new Date();
                String codigo = dateFormat.format(date);
                File file = new File("Erros/Error_iSPD_" + codigo);
                FileWriter writer = new FileWriter(file);
                PrintWriter saida = new PrintWriter(writer, true);
                saida.print(erro);
                saida.close();
                writer.close();
                String saidaString = "";
                saidaString += "Error encountered during system operation.\n";
                saidaString += "Error saved in the file: " + file.getAbsolutePath() + "\n";
                saidaString += "Please send the error to the developers.\n";
                saidaString += erro;
                area.setText(saidaString);
                JOptionPane.showMessageDialog(parentComponent, scroll,"System Error", JOptionPane.ERROR_MESSAGE);
                objErr.reset();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentComponent, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
}
