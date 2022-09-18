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
 * CargaList.java
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
package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Descreve como gerar tarefas na forma por nó mestre
 *
 * @author denison
 */
public class CargaList extends GerarCarga {

    private int tipo;
    private List<GerarCarga> configuracaoNo;

    public CargaList(List configuracaoNo, int tipo) {
        this.tipo = tipo;
        this.configuracaoNo = configuracaoNo;
    }

    public static GerarCarga newGerarCarga(String entrada) {
        CargaList newObj = null;
        String[] linhas = entrada.split("\n");
        List<CargaForNode> nos = new ArrayList<CargaForNode>();
        for (int i = 0; i < linhas.length; i++) {
            CargaForNode newNo = (CargaForNode) CargaForNode.newGerarCarga(linhas[i]);
            if (newNo != null) {
                nos.add(newNo);
            }
        }
        if (nos.size() > 0) {
            newObj = new CargaList(nos, GerarCarga.FORNODE);
        }
        return newObj;
    }

    public List<GerarCarga> getList() {
        return configuracaoNo;
    }

    @Override
    public List<Tarefa> toTarefaList(RedeDeFilas rdf) {
        List<Tarefa> tarefas = new ArrayList<Tarefa>();
        int inicio = 0;
        if (tipo == GerarCarga.FORNODE) {
            for (GerarCarga item : this.configuracaoNo) {
                CargaForNode carga = (CargaForNode) item;
                carga.setInicioIdentificadorTarefa(inicio);
                inicio += carga.getNumeroTarefas();
                tarefas.addAll(carga.toTarefaList(rdf));
            }
        }
        return tarefas;
    }

    @Override
    public String toString() {
        StringBuilder saida = new StringBuilder();
        for (GerarCarga cargaTaskNode : configuracaoNo) {
            saida.append(cargaTaskNode.toString()).append("\n");
        }
        return saida.toString();
    }

    @Override
    public int getTipo() {
        return tipo;
    }
}
