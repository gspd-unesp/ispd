/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.configuracao;

/**
 *
 * @author denison
 */
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class CellEditor extends AbstractCellEditor
        implements TableCellEditor, TableCellRenderer {

    JComponent item;

    public CellEditor(JComponent item) {
        this.item = item;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return item;
    }

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean bln, boolean bln1, int i, int i1) {
        return item;
    }

    @Override
    public Object getCellEditorValue() {
        return item;
    }
}
