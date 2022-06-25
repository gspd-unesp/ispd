package ispd.gui.configuracao;

import ispd.gui.iconico.grade.Internet;
import ispd.gui.iconico.grade.GridItem;
import ispd.gui.iconico.grade.Link;

import javax.swing.table.AbstractTableModel;
import java.util.ResourceBundle;

public class LinkTable extends AbstractTableModel {
    private static final int TYPE = 0;
    private static final int VALUE = 1;
    private static final int LABEL = 0;
    private static final int BANDWIDTH = 1;
    private static final int LATENCY = 2;
    private static final int LOAD_FACTOR = 3;
    private static final int ROW_COUNT = 4;
    private static final int COLUMN_COUNT = 2;
    private GridItem link = null;
    private ResourceBundle words;

    LinkTable(final ResourceBundle words) {
        this.words = words;
    }

    public void setLink(final GridItem link) {
        this.link = link;
    }

    @Override
    public int getRowCount() {
        return LinkTable.ROW_COUNT;
    }

    @Override
    public int getColumnCount() {
        return LinkTable.COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case LinkTable.TYPE -> {
                final var name = this.getRowName(rowIndex);
                if (name != null)
                    return name;
            }
            case LinkTable.VALUE -> {
                final var value = this.getRowValue(rowIndex);
                if (value != null)
                    return value;
            }
        }

        throw new IndexOutOfBoundsException("columnIndex out of bounds");
    }

    private String getRowName(final int rowIndex) {
        return switch (rowIndex) {
            case LinkTable.LABEL -> this.words.getString("Label");
            case LinkTable.BANDWIDTH -> this.words.getString("Bandwidth");
            case LinkTable.LATENCY -> this.words.getString("Latency");
            case LinkTable.LOAD_FACTOR -> this.words.getString("Load Factor");
            default -> null;
        };
    }

    private Object getRowValue(final int rowIndex) {
        if (this.link == null) {
            return "null";
        }

        switch (rowIndex) {
            case LinkTable.LABEL:
                return this.link.getId().getName();

            case LinkTable.BANDWIDTH:
                if (this.link instanceof Link) {
                    return ((Link) this.link).getBandwidth();
                } else {
                    return ((Internet) this.link).getBandwidth();
                }

            case LinkTable.LATENCY:
                if (this.link instanceof Link) {
                    return ((Link) this.link).getLatency();
                } else {
                    return ((Internet) this.link).getLatency();
                }

            case LinkTable.LOAD_FACTOR:
                if (this.link instanceof Link) {
                    return ((Link) this.link).getLoadFactor();
                } else {
                    return ((Internet) this.link).getLoadFactor();
                }
        }

        return null;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return switch (columnIndex) {
            case LinkTable.TYPE -> this.words.getString("Properties");
            case LinkTable.VALUE -> this.words.getString("Values");
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex != LinkTable.TYPE;
    }

    @Override
    public void setValueAt(
            final Object aValue,
            final int rowIndex,
            final int columnIndex) {
        if (columnIndex != LinkTable.VALUE || this.link == null) {
            return;
        }

        this.updateValue(aValue, rowIndex);
        this.fireTableCellUpdated(rowIndex, LinkTable.VALUE);
    }

    private void updateValue(final Object aValue, final int rowIndex) {

        if (rowIndex == LinkTable.LABEL) {
            this.link.getId().setName(aValue.toString());
            return;
        }

        final var value = Double.parseDouble(aValue.toString());

        switch (rowIndex) {
            case LinkTable.BANDWIDTH:
                if (this.link instanceof Link) {
                    ((Link) this.link).setBandwidth(value);
                } else {
                    ((Internet) this.link).setBandwidth(value);
                }
                break;
            case LinkTable.LATENCY:
                if (this.link instanceof Link) {
                    ((Link) this.link).setLatency(value);
                } else {
                    ((Internet) this.link).setLatency(value);
                }
                break;
            case LinkTable.LOAD_FACTOR:
                if (this.link instanceof Link) {
                    ((Link) this.link).setLoadFactor(value);
                } else {
                    ((Internet) this.link).setLoadFactor(value);
                }
                break;
        }
    }

    public void setPalavras(final ResourceBundle words) {
        this.words = words;
        this.fireTableStructureChanged();
    }
}