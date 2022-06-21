package ispd.gui.configuracao;

import ispd.gui.iconico.grade.Internet;
import ispd.gui.iconico.grade.ItemGrade;
import ispd.gui.iconico.grade.Link;

import javax.swing.table.AbstractTableModel;
import java.util.ResourceBundle;

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

    public LinkTable(final ResourceBundle palavras) {
        this.palavras = palavras;
    }

    public void setLink(final ItemGrade link) {
        this.link = link;
    }

    @Override
    public int getRowCount() {
        return LinkTable.NUMLINHAS;
    }

    @Override
    public int getColumnCount() {
        return LinkTable.NUMCOLUNAS;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case LinkTable.TYPE:
                switch (rowIndex) {
                    case LinkTable.LABEL:
                        return this.palavras.getString("Label");
                    case LinkTable.BANDW:
                        return this.palavras.getString("Bandwidth");
                    case LinkTable.LATEN:
                        return this.palavras.getString("Latency");
                    case LinkTable.LOADF:
                        return this.palavras.getString("Load Factor");
                }
            case LinkTable.VALUE:
                if (this.link != null) {
                    switch (rowIndex) {
                        case LinkTable.LABEL:
                            return this.link.getId().getNome();
                        case LinkTable.BANDW:
                            if (this.link instanceof Link) {
                                return ((Link) this.link).getBanda();
                            } else {
                                return ((Internet) this.link).getBanda();
                            }
                        case LinkTable.LATEN:
                            if (this.link instanceof Link) {
                                return ((Link) this.link).getLatencia();
                            } else {
                                return ((Internet) this.link).getLatencia();
                            }
                        case LinkTable.LOADF:
                            if (this.link instanceof Link) {
                                return ((Link) this.link).getTaxaOcupacao();
                            } else {
                                return ((Internet) this.link).getTaxaOcupacao();
                            }
                    }
                } else {
                    return "null";
                }
            default:
                // Não deve ocorrer, pois só existem 2 colunas
                throw new IndexOutOfBoundsException("columnIndex out of " +
                        "bounds");
        }
    }

    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
            case LinkTable.TYPE:
                return this.palavras.getString("Properties");
            case LinkTable.VALUE:
                return this.palavras.getString("Values");
        }
        return null;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex != LinkTable.TYPE;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        // Pega o sócio referente a linha especificada.
        if (columnIndex == LinkTable.VALUE && this.link != null) {
            switch (rowIndex) {
                case LinkTable.LABEL:
                    this.link.getId().setNome(aValue.toString());
                    break;
                case LinkTable.BANDW:
                    if (this.link instanceof Link) {
                        ((Link) this.link).setBanda(Double.valueOf(aValue.toString()));
                    } else {
                        ((Internet) this.link).setBanda(Double.valueOf(aValue.toString()));
                    }
                    break;
                case LinkTable.LATEN:
                    if (this.link instanceof Link) {
                        ((Link) this.link).setLatencia(Double.valueOf(aValue.toString()));
                    } else {
                        ((Internet) this.link).setLatencia(Double.valueOf(aValue.toString()));
                    }
                    break;
                case LinkTable.LOADF:
                    if (this.link instanceof Link) {
                        ((Link) this.link).setTaxaOcupacao(Double.valueOf(aValue.toString()));
                    } else {
                        ((Internet) this.link).setTaxaOcupacao(Double.valueOf(aValue.toString()));
                    }
                    break;
            }
            this.fireTableCellUpdated(rowIndex, VALUE); // Notifica a
            // atualização da célula
        }
    }

    public void setPalavras(final ResourceBundle palavras) {
        this.palavras = palavras;
        this.fireTableStructureChanged();
    }
}