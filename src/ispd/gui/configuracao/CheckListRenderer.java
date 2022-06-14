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
 * CheckListRenderer.java
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

import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CheckListRenderer extends DefaultListSelectionModel implements ListCellRenderer {

    private static final Color BACKGROUND = null;
    private final JCheckBox checkBox;
    private final Color selectionBackground;

    CheckListRenderer(final JList<?> list) {
        this.checkBox = new JCheckBox();
        this.selectionBackground = list.getSelectionBackground();
        list.setCellRenderer(this);
        list.setSelectionModel(this);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addMouseListener(new CheckListRendererMouseAdapter());
    }

    @Override
    public void setSelectionInterval(final int index0, final int index1) {
    }

    @Override
    public Component getListCellRendererComponent(
            final JList list,
            final Object value,
            final int index,
            final boolean isSelected,
            final boolean hasFocus) {
        this.checkBox.setSelected(isSelected);
        if (isSelected) {
            this.checkBox.setBackground(this.selectionBackground);
        } else {
            this.checkBox.setBackground(CheckListRenderer.BACKGROUND);
        }
        this.checkBox.setText(value.toString());
        return this.checkBox;
    }

    private static class CheckListRendererMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent event) {
            final var list = (JList<?>) event.getSource();
            final int index = list.locationToIndex(event.getPoint());
            if (list.isSelectedIndex(index)) {
                list.removeSelectionInterval(index, index);
            } else {
                list.addSelectionInterval(index, index);
            }
        }
    }
}
