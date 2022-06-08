package ispd;

import ispd.gui.JPrincipal;
import ispd.gui.LogExceptions;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ispd.gui.SplashWindowBuilder.visibleDefaultSplashWindow;

public class GuiApplication extends Application
{
    private static final String guiLookAndFeelClassName = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
    private static final String errorFile = "Erros/Erros_Simulador";
    private static final String outputFile = "Erros/Saida_Simulador";

    public GuiApplication (String[] args)
    {
        super(args);
    }

    private static JPrincipal initializeApplication ()
    {
        LogExceptions exceptionLogger = new LogExceptions(null);
        Thread.setDefaultUncaughtExceptionHandler(exceptionLogger);

        redirectSystemStreams();
        setGuiLookAndFeel();

        JPrincipal mainWindow = buildMainWindow();

        // TODO: Study if exceptionLogger can be instantiated after creating the main window
        exceptionLogger.setParentComponent(mainWindow);

        return mainWindow;
    }

    private static void redirectSystemStreams ()
    {
        redirectSystemStreamToFile(System::setErr, errorFile);
        redirectSystemStreamToFile(System::setOut, outputFile);
    }

    private static void redirectSystemStreamToFile (Consumer<PrintStream> redirection, String pathToFile)
    {
        var fileStream = getFileStreamOrNull(pathToFile);

        // TODO: Maybe optional?
        if (fileStream == null)
            return;

        var printStream = new PrintStream(fileStream);
        // TODO: Uncomment once behaviour has been validated
//        redirection.accept(printStream);
    }

    private static FileOutputStream getFileStreamOrNull (String pathToFile)
    {
        try
        {
            return new FileOutputStream(pathToFile);
        } catch (FileNotFoundException ex)
        {
            logWithMainLogger(ex);
            return null;
        }
    }

    private static void setGuiLookAndFeel ()
    {
        try
        {
            UIManager.setLookAndFeel(guiLookAndFeelClassName);
        } catch (ClassNotFoundException | IllegalAccessException |
                 InstantiationException | UnsupportedLookAndFeelException ex)
        {
            logWithMainLogger(ex);
        }
    }

    private static void logWithMainLogger (Exception ex)
    {
        // TODO: Perhaps message instead of 'null'?
        Logger.getLogger(Main.class.getName())
                .log(Level.SEVERE, null, ex);
    }

    private static JPrincipal buildMainWindow ()
    {
        JPrincipal gui = new JPrincipal();
        gui.setLocationRelativeTo(null);
        return gui;
    }

    @Override
    public void executar ()
    {
        var splashWindow = visibleDefaultSplashWindow();

        JPrincipal mainWindow = initializeApplication();

        splashWindow.dispose();

        mainWindow.setVisible(true);
    }
}