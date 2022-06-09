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
    private static final int SCROLL_AREA_PREFERRED_WIDTH = 500;
    private static final int SCROLL_AREA_PREFERRED_HEIGHT = 300;
    private final JTextArea area;
    private final JScrollPane scroll;
    private Component parentComponent;

    public LogExceptions (final Component gui)
    {
        this.parentComponent = gui;

        createErrorFolder();

        // Initialize Graphical section
        this.area = uneditableTextArea();
        this.scroll = scrollPaneWithPreferredSizes();
    }

    private static void createErrorFolder ()
    {
        final var aux = new File(ERROR_FOLDER_PATH);

        // TODO: Handle SecurityException

        if (aux.exists())
            return;

        var created = aux.mkdir();
    }

    private static String generateErrorFile (final String errorMessage) throws IOException
    {
        final var errorCode = buildErrorCode(new Date());
        final var filePath = String.format("%s/%s_%s",
                ERROR_FOLDER_PATH, ERROR_FILE_PREFIX, errorCode);

        final var file = new File(filePath);

        final var writer = new FileWriter(file);
        final var output = new PrintWriter(writer, true);
        output.print(errorMessage);
        output.close();
        writer.close();

        return file.getAbsolutePath();
    }

    private static String buildErrorCode (final Date date)
    {
        final var dateFormat = new SimpleDateFormat(ERROR_CODE_DATE_FORMAT);
        return dateFormat.format(date);
    }

    private JTextArea uneditableTextArea ()
    {
        final JTextArea area = new JTextArea();
        area.setEditable(false);
        return area;
    }

    private JScrollPane scrollPaneWithPreferredSizes ()
    {
        final JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(SCROLL_AREA_PREFERRED_WIDTH, SCROLL_AREA_PREFERRED_HEIGHT));
        return scroll;
    }

    public void setParentComponent (final Component parentComponent)
    {
        this.parentComponent = parentComponent;
    }

    @Override
    public void uncaughtException (final Thread t, final Throwable e)
    {
        final var errStream = new ByteArrayOutputStream();

        e.printStackTrace(new PrintStream(errStream));

        processError(errStream);
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
            JOptionPane.showMessageDialog(parentComponent, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
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

        this.area.setText(outputString);

        JOptionPane.showMessageDialog(parentComponent, scroll, "System Error", JOptionPane.ERROR_MESSAGE);
    }
}