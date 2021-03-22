/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.configuracao;

import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author denison
 */
public class VariedRowTable extends JTable {

    private HashMap<Integer, CellEditor> editores;

    public VariedRowTable() {
        editores = new HashMap<Integer, CellEditor>();
    }

    public VariedRowTable(TableModel model) {
        editores = new HashMap<Integer, CellEditor>();
        super.setModel(dataModel);
        for (int i = 0; i < this.getModel().getRowCount(); i++) {
            Object item = this.getModel().getValueAt(i, 1);
            if (item instanceof JComponent) {
                editores.put(i, new CellEditor((JComponent) item));
            }
        }
    }
    
    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        //editores.clear();
        for (int i = 0; i < this.getModel().getRowCount(); i++) {
            Object item = this.getModel().getValueAt(i, 1);
            if (item instanceof JComponent) {
                editores.put(i, new CellEditor((JComponent) item));
            }
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        Object value = getModel().getValueAt(row, column);
        if (value != null) {
            return getDefaultRenderer(value.getClass(), row, column);
        }
        return super.getCellRenderer(row, column);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        Object value = getModel().getValueAt(row, column);
        if (value != null) {
            return getDefaultEditor(value.getClass(), row, column);
        }
        return super.getCellEditor(row, column);
    }

    public TableCellRenderer getDefaultRenderer(Class<?> columnClass, int row, int column) {
        if (columnClass == JComboBox.class || columnClass == JButton.class) {
            return editores.get(row);
        } else {
            return super.getDefaultRenderer(columnClass);
        }
    }

    public TableCellEditor getDefaultEditor(Class<?> columnClass, int row, int column) {
        if (columnClass == JComboBox.class || columnClass == JButton.class) {
            return editores.get(row);
        } else {
            return super.getDefaultEditor(columnClass);
        }
    }
}
