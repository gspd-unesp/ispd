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
 * LogExceptions.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Aldo Ianelo Guerra;
 * Contributor(s):   Denison Menezes;
 *
 * Changes
 * -------
 *
 * 09-Set-2014 : Version 2.0;
 * 16-Out-2014 : change the location of the iSPD base directory;
 *
 */
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

public class LogExceptions implements Thread.UncaughtExceptionHandler
{
    public static final String ERROR_FOLDER_PATH = "Erros";
    public static final String ERROR_FILE_PREFIX = "Error_ISPD";
    public static final String ERROR_MESSAGE_TEMPLATE = "\n" +
                                                        "---------- error description ----------\n" +
                                                        "%s\n" +
                                                        "---------- error description ----------\n";
    public static final String ERROR_CODE_DATE_FORMAT = "yyyyMMddHHmmss";
    private static final int SCROLL_PANE_PREF_WIDTH = 500;
    private static final int SCROLL_PANE_PREF_HEIGHT = 300;
    private final JTextArea textArea;
    private final JScrollPane scrollPane;
    private Component parentComponent;

    public LogExceptions (final Component gui)
    {
        this.parentComponent = gui;

        createErrorFolderIfNonExistent();

        // Initialize Graphical section
        this.textArea = uneditableTextArea();
        this.scrollPane = resizedScrollPaneOf(this.textArea);
    }

    private static void createErrorFolderIfNonExistent ()
    {
        final var aux = new File(ERROR_FOLDER_PATH);

        // TODO: Handle SecurityException

        if (aux.exists())
            return;

        var created = aux.mkdir();
    }

    private static JTextArea uneditableTextArea ()
    {
        final JTextArea area = new JTextArea();
        area.setEditable(false);
        return area;
    }

    private static JScrollPane resizedScrollPaneOf (final JTextArea textArea)
    {
        final JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(
                SCROLL_PANE_PREF_WIDTH,
                SCROLL_PANE_PREF_HEIGHT));
        return scroll;
    }

    private static String generateErrorFile (final String errorMessage) throws IOException
    {
        final var errorCode = buildErrorCode(new Date());
        final var filePath = String.format("%s%s%s_%s",
                ERROR_FOLDER_PATH, File.separator, ERROR_FILE_PREFIX, errorCode);

        final var file = new File(filePath);

        try (var fw = new FileWriter(file);
             var pw = new PrintWriter(fw, true))
        {
            pw.print(errorMessage);
        }

        return file.getAbsolutePath();
    }

    private static String buildErrorCode (final Date date)
    {
        final var dateFormat = new SimpleDateFormat(ERROR_CODE_DATE_FORMAT);
        return dateFormat.format(date);
    }

    @Override
    public void uncaughtException (final Thread t, final Throwable e)
    {
        final var errStream = new ByteArrayOutputStream();

        e.printStackTrace(new PrintStream(errStream));

        processError(errStream);
    }

    public void setParentComponent (final Component parentComponent)
    {
        this.parentComponent = parentComponent;
    }

    private void processError (final ByteArrayOutputStream errorStream)
    {
        try
        {
            if (errorStream.size() <= 0)
                return;

            final var errorMessage = String.format(ERROR_MESSAGE_TEMPLATE, errorStream);

            final var filePath = generateErrorFile(errorMessage);

            displayErrorInGui(errorMessage, filePath);

            errorStream.reset();

        } catch (Exception e) // TODO: Maybe IOException only?
        {
            JOptionPane.showMessageDialog(this.parentComponent, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void displayErrorInGui (String errorMessage, String filePath)
    {
        final var outputString = String.format(
                "Error encountered during system operation.\n" +
                "Error saved in the file: %s\n" +
                "Please send the error to the developers.\n" +
                "%s\n",
                filePath, errorMessage);

        this.textArea.setText(outputString);

        JOptionPane.showMessageDialog(this.parentComponent, this.scrollPane, "System Error", JOptionPane.ERROR_MESSAGE);
    }
}