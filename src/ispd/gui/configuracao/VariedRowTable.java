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
 * VariedRowTable.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.util.HashMap;

public class VariedRowTable extends JTable {

    private final HashMap<Integer, CellEditor> editors = new HashMap<>(0);

    VariedRowTable() {
    }

    @Override
    public void setModel(final TableModel dataModel) {
        super.setModel(dataModel);
        for (int i = 0; i < this.getModel().getRowCount(); i++) {
            final var item = this.getModel().getValueAt(i, 1);
            if (item instanceof JComponent component) {
                this.editors.put(i, new CellEditor(component));
            }
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        final var value = this.getModel().getValueAt(row, column);
        if (value != null) {
            return this.getDefaultRenderer(value.getClass(), row);
        }
        return super.getCellRenderer(row, column);
    }

    @Override
    public TableCellEditor getCellEditor(final int row, final int column) {
        final var value = this.getModel().getValueAt(row, column);
        if (value != null) {
            return this.getDefaultEditor(value.getClass(), row);
        }
        return super.getCellEditor(row, column);
    }

    private TableCellEditor getDefaultEditor(
            final Class<?> columnClass, final int row) {
        if (columnClass == JComboBox.class || columnClass == JButton.class) {
            return this.editors.get(row);
        } else {
            return this.getDefaultEditor(columnClass);
        }
    }

    private TableCellRenderer getDefaultRenderer(
            final Class<?> columnClass, final int row) {
        if (columnClass == JComboBox.class || columnClass == JButton.class) {
            return this.editors.get(row);
        } else {
            return this.getDefaultRenderer(columnClass);
        }
    }
}