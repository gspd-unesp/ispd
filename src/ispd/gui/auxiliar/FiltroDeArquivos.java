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
 * FiltroDeArquivos.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.gui.auxiliar;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Implementa FileFilter que permite uma ou mais extensões para um mesmo tipo de arquivo.
 * @author denison
 */
public class FiltroDeArquivos extends FileFilter {

    private String descricao;
    private String[] extensao;
    private boolean permitirDiretorio;

    public FiltroDeArquivos(String descricao, String[] extensao, boolean permitirDiretorio) {
        this.descricao = descricao;
        this.extensao = extensao;
        this.permitirDiretorio = permitirDiretorio;
    }

    public FiltroDeArquivos(String descricao, String extensao, boolean permitirDiretorio) {
        this.descricao = descricao;
        String[] exts = {extensao};
        this.extensao = exts;
        this.permitirDiretorio = permitirDiretorio;
    }

    public boolean accept(File file) {
        if (file.isDirectory() && permitirDiretorio) {
            return true;
        }
        for (String ext : extensao) {
            if (file.getName().toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public String getDescription() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setExtensao(String[] extensao) {
        this.extensao = extensao;
    }

    public void setExtensao(String string) {
        String[] exts = {string};
        this.extensao = exts;
    }
}
