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
 * InterpretadorSimGrid.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Victor Aoqui;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
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
                    //parser.writefile();
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
