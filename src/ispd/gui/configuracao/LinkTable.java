/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
