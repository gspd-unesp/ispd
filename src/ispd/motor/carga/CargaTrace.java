/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 *  USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * CargaTrace.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Diogo Tavares;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.motor.carga;

import ispd.arquivo.xml.TraceXML;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;

import java.io.File;
import java.util.List;

public class CargaTrace extends GerarCarga {

    private final String type;
    private final File file;
    private final String filePath;
    private final int taskCount;

    public CargaTrace(
            final File file,
            final int taskCount,
            final String type) {

        this.file = file;
        this.filePath = file.getAbsolutePath();
        new TraceXML(this.filePath);
        this.taskCount = taskCount;
        this.type = type;
    }

    @Override
    public List<Tarefa> toTarefaList(final RedeDeFilas rdf) {
        return new TraceLoadHelper(rdf, this.type, this.taskCount).toTaskList(this.filePath);
    }

    @Override
    public String toString() {
        return this.file.getAbsolutePath();

    }

    @Override
    public int getTipo() {
        return GerarCarga.TRACE;
    }

    public String getTraceType() {
        return this.type;
    }

    public File getFile() {
        return this.file;
    }

    public Integer getNumberTasks() {
        return this.taskCount;
    }

}