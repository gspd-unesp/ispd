/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * Main.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 09-Set-2014 : Version 2.0;
 * 16-Out-2014 : change the location of the iSPD base directory;
 *
 */
package ispd;

import ispd.gui.JPrincipal;
import ispd.gui.LogExceptions;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ispd.gui.SplashWindowBuilder.visibleDefaultSplashWindow;

/**
 * Classe de inicialização do iSPD. Indetifica se deve executar comando a partir
 * do terminal ou carrega interface gráfica
 *
 * @author denison
 */
public class Main
{
    private static final Locale enUsLocale = new Locale("en", "US");
    private static final String guiLookAndFeelClassName = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
    private static final String errorFile = "Erros/Erros_Simulador";
    private static final String outputFile = "Erros/Saida_Simulador";

    public static void main (String[] args)
    {
        setDefaultLocale();

        if (args.length > 0)
        {
            runCli(args);
            return;
        }

        runGui();
    }

    private static void setDefaultLocale ()
    {
        Locale.setDefault(enUsLocale);
    }

    private static void runCli (String[] args)
    {
        new Terminal(args).executar();
        System.exit(0);
    }

    private static void runGui ()
    {
        var splashWindow = visibleDefaultSplashWindow();

        JPrincipal mainWindow = prepareMainWindow();

        splashWindow.dispose();

        mainWindow.setVisible(true);
    }

    private static JPrincipal prepareMainWindow ()
    {
        LogExceptions exceptionLogger = new LogExceptions(null);
        Thread.setDefaultUncaughtExceptionHandler(exceptionLogger);

        setErrorAndOutputStreams();
        setGuiLookAndFeel();

        JPrincipal mainWindow = buildMainWindow();

        // TODO: Study if exceptionLogger can be instantiated after creating the main window
        exceptionLogger.setParentComponent(mainWindow);

        return mainWindow;
    }

    private static void setErrorAndOutputStreams ()
    {
        FileOutputStream fosErr = null;
        FileOutputStream fosOut = null;
        try
        {
            fosErr = new FileOutputStream(errorFile);
            fosOut = new FileOutputStream(outputFile);
        } catch (FileNotFoundException ex)
        {
            logWithMainLogger(ex);
        }
        // define a impresso sobre os fluxos acima
        PrintStream psErr = new PrintStream(fosErr);
        PrintStream psOut = new PrintStream(fosOut);
        // redefine os fluxos na classe System
//        System.setErr(psErr);
//        System.setOut(psOut);
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
}