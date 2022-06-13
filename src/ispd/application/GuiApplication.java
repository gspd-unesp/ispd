package ispd.application;

import ispd.gui.JPrincipal;
import ispd.gui.LogExceptions;
import ispd.gui.SplashWindowBuilder;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuiApplication implements Application
{
    private static final String GUI_LOOK_AND_FEEL_CLASS_NAME = "javax.swing.plaf.nimbus.NimbusLookAndFeel";

    @Override
    public void run ()
    {
        GuiApplication.openGui();
    }

    private static void openGui ()
    {
        final var splashWindow = SplashWindowBuilder.visibleDefaultSplashWindow();
        final var mainWindow = GuiApplication.initializeApplication();

        splashWindow.dispose();
        mainWindow.setVisible(true);
    }

    private static JPrincipal initializeApplication ()
    {
        final var exceptionLogger = new LogExceptions(null);
        Thread.setDefaultUncaughtExceptionHandler(exceptionLogger);

        GuiApplication.setGuiLookAndFeel();

        final var mainWindow = GuiApplication.buildMainWindow();

        // TODO: Study if exceptionLogger can be instantiated after creating the main window
        exceptionLogger.setParentComponent(mainWindow);

        return mainWindow;
    }

    private static void setGuiLookAndFeel ()
    {
        try
        {
            UIManager.setLookAndFeel(GuiApplication.GUI_LOOK_AND_FEEL_CLASS_NAME);
        } catch (final ClassNotFoundException | IllegalAccessException |
                       InstantiationException | UnsupportedLookAndFeelException ex)
        {
            GuiApplication.logWithMainLogger(ex);
        }
    }

    private static JPrincipal buildMainWindow ()
    {
        final var gui = new JPrincipal();
        gui.setLocationRelativeTo(null);
        return gui;
    }

    private static void logWithMainLogger (final Exception ex)
    {
        // TODO: Perhaps message instead of 'null'?
        Logger.getLogger(GuiApplication.class.getName())
                .log(Level.SEVERE, null, ex);
    }
}