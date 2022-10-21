package ispd.gui;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogExceptions implements Thread.UncaughtExceptionHandler {
    private static final String ERROR_FOLDER_PATH = "Erros";
    private static final String ERROR_FILE_PREFIX = "Error_ISPD";
    private static final String ERROR_CODE_DATE_FORMAT = "yyyyMMddHHmmss";
    private static final String ERROR_FILE_MESSAGE_FORMAT = """

            ---------- error description ----------
            %s
            ---------- error description ----------
            """;
    private static final String ERROR_GUI_MESSAGE_FORMAT = """
            Error encountered during system operation.
            Error saved in the file: %s
            Please send the error to the developers.
            %s
            """;
    private static final int SCROLL_PANE_PREFERRED_WIDTH = 500;
    private static final int SCROLL_PANE_PREFERRED_HEIGHT = 300;
    private final JTextArea textArea = LogExceptions.readonlyTextArea();

    
    private final JScrollPane scrollPane =
            LogExceptions.resizedScrollPaneFrom(this.textArea);
    private Component parentComponent; 

    public LogExceptions(final Component gui) {
        this.parentComponent = gui;

        LogExceptions.createErrorFolderIfNonExistent();

    }

    private static void createErrorFolderIfNonExistent() {
        final var aux = new File(LogExceptions.ERROR_FOLDER_PATH);

        

        if (aux.exists())
            return;

        final var ignored = aux.mkdir();
    }

    private static JTextArea readonlyTextArea() {
        final JTextArea area = new JTextArea();
        area.setEditable(false);
        return area;
    }

    private static JScrollPane resizedScrollPaneFrom(final JTextArea textArea) {
        final JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(
                LogExceptions.SCROLL_PANE_PREFERRED_WIDTH,
                LogExceptions.SCROLL_PANE_PREFERRED_HEIGHT));
        return scroll;
    }

    public void setParentComponent(final Component parentComponent) {
        this.parentComponent = parentComponent;
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        
        final var errStream = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(errStream));
        this.processError(errStream);
    }

    private void processError(final ByteArrayOutputStream errorStream) {
        if (errorStream.size() == 0)
            return;

        try {
            final var errorMessage =
                    String.format(LogExceptions.ERROR_FILE_MESSAGE_FORMAT,
                            errorStream);
            this.displayError(errorMessage);

            errorStream.reset(); 

        } catch (final IOException e) {
            JOptionPane.showMessageDialog(this.parentComponent,
                    e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void displayError(final String errorMessage) throws IOException {
        final var errorFile = new File(
                LogExceptions.buildErrorFilePath(new Date()));

        LogExceptions.printErrorToFile(errorMessage, errorFile);
        this.displayErrorInGui(errorMessage, errorFile);
    }

    private static String buildErrorFilePath(final Date date) {
        final var errorCode = LogExceptions.buildErrorFileTimestamp(date);
        return String.format("%s%s%s_%s",
                LogExceptions.ERROR_FOLDER_PATH, File.separator,
                LogExceptions.ERROR_FILE_PREFIX, errorCode);
    }

    private static void printErrorToFile(
            final String errorMessage, final File file) throws IOException {
        try (final var fw = new FileWriter(file);
             final var pw = new PrintWriter(fw, true)) {
            pw.print(errorMessage);
        }
    }

    private void displayErrorInGui(final String errorMessage, final File file) {
        final var path = file.getAbsolutePath();
        final var formattedMessage = String.format(
                LogExceptions.ERROR_GUI_MESSAGE_FORMAT, path, errorMessage);

        this.textArea.setText(formattedMessage);

        
        JOptionPane.showMessageDialog(this.parentComponent, this.scrollPane,
                "System Error", JOptionPane.ERROR_MESSAGE);
    }

    private static String buildErrorFileTimestamp(final Date date) {
        final var dateFormat =
                new SimpleDateFormat(LogExceptions.ERROR_CODE_DATE_FORMAT);
        return dateFormat.format(date);
    }
}