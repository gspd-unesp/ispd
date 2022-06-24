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
 * LinkTable.java
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
package ispd.gui.configuracao;

import ispd.gui.iconico.grade.Internet;
import ispd.gui.iconico.grade.ItemGrade;
import ispd.gui.iconico.grade.Link;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author denison
 */
public class LinkTable extends AbstractTableModel {

    // Constantes representando o índice das colunas
    private static final int TYPE = 0;
    private static final int VALUE = 1;
    private static final int LABEL = 0;
    private static final int BANDW = 1;
    private static final int LATEN = 2;
    private static final int LOADF = 3;
    private static final int NUMLINHAS = 4;
    private static final int NUMCOLUNAS = 2;
    // Array com os nomes das linhas
    private ItemGrade link;
    private ResourceBundle palavras;

    public LinkTable(ResourceBundle palavras) {
        this.palavras = palavras;
    }
    
    public void setLink(ItemGrade link) {
        this.link = link;
    }

    @Override
    public int getRowCount() {
        return NUMLINHAS;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case TYPE:
                return palavras.getString("Properties");
            case VALUE:
                return palavras.getString("Values");
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == TYPE) {
            return false;
        }
        return true;
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUNAS;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // Pega o sócio referente a linha especificada.
        if (columnIndex == VALUE && link != null) {
            switch (rowIndex) {
                case LABEL:
                    link.getId().setNome(aValue.toString());
                    break;
                case BANDW:
                    if (link instanceof Link) {
                        ((Link) link).setBanda(Double.valueOf(aValue.toString()));
                    } else {
                        ((Internet) link).setBanda(Double.valueOf(aValue.toString()));
                    }
                    break;
                case LATEN:
                    if (link instanceof Link) {
                        ((Link) link).setLatencia(Double.valueOf(aValue.toString()));
                    } else {
                        ((Internet) link).setLatencia(Double.valueOf(aValue.toString()));
                    }
                    break;
                case LOADF:
                    if (link instanceof Link) {
                        ((Link) link).setTaxaOcupacao(Double.valueOf(aValue.toString()));
                    } else {
                        ((Internet) link).setTaxaOcupacao(Double.valueOf(aValue.toString()));
                    }
                    break;
            }
            fireTableCellUpdated(rowIndex, columnIndex); // Notifica a atualização da célula
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case TYPE:
                switch (rowIndex) {
                    case LABEL:
                        return palavras.getString("Label");
                    case BANDW:
                        return palavras.getString("Bandwidth");
                    case LATEN:
                        return palavras.getString("Latency");
                    case LOADF:
                        return palavras.getString("Load Factor");
                }
            case VALUE:
                if (link != null) {
                    switch (rowIndex) {
                        case LABEL:
                            return link.getId().getNome();
                        case BANDW:
                            if (link instanceof Link) {
                                return ((Link) link).getBanda();
                            } else {
                                return ((Internet) link).getBanda();
                            }
                        case LATEN:
                            if (link instanceof Link) {
                                return ((Link) link).getLatencia();
                            } else {
                                return ((Internet) link).getLatencia();
                            }
                        case LOADF:
                            if (link instanceof Link) {
                                return ((Link) link).getTaxaOcupacao();
                            } else {
                                return ((Internet) link).getTaxaOcupacao();
                            }
                    }
                } else {
                    return "null";
                }
            default:
                // Não deve ocorrer, pois só existem 2 colunas
                throw new IndexOutOfBoundsException("columnIndex out of bounds");
        }
    }

    public void setPalavras(ResourceBundle palavras) {
        this.palavras = palavras;
        fireTableStructureChanged();
    }
}
