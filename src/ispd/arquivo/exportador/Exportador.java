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
 * Exportador.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Rafael Stabile;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.arquivo.exportador;

import org.w3c.dom.Document;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Classe para converter modelo do iSPD para outros simuladores
 *
 * @author Rafael Stabile
 */
public class Exportador
{
    private final Document model;

    public Exportador (final Document model)
    {
        this.model = model;
    }

    /**
     * Converte modelo iconico do iSPD no arquivo java do GridSim
     *
     * @param file arquivo na qual as classes serão salvas
     */
    public void toGridSim (final File file)
    {
        try (final var fw = new FileWriter(file);
             final var pw = new PrintWriter(fw, true))
        {
            new ExportHelper(this.model).printCodeToFile(pw);
        } catch (final Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
}