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
 * CheckListRenderer.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author denison
 */
public class CheckListRenderer extends DefaultListSelectionModel implements ListCellRenderer {

    private JCheckBox jcb;
    private Color background;
    private Color selectionBackground;

    public CheckListRenderer(JList jlist) {
        jcb = new JCheckBox();
        selectionBackground = jlist.getSelectionBackground();
        jlist.setCellRenderer(this);
        jlist.setSelectionModel(this);
        jlist.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
        jlist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                JList jList1 = (JList) event.getSource();
                int index = jList1.locationToIndex(event.getPoint());
                if (jList1.isSelectedIndex(index)) {
                    jList1.removeSelectionInterval(index, index);
                } else {
                    jList1.addSelectionInterval(index, index);
                }
            }
        });
    }

    @Override
    public void setSelectionInterval(int i, int i1) {
    }

    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean hasFocus) {
        //jcb.setEnabled(list.isEnabled());
        jcb.setSelected(isSelected);
        if (isSelected) {
            jcb.setBackground(selectionBackground);
            //jcb.setForeground(list.getSelectionBackground());
        } else {
            jcb.setBackground(background);
            //jcb.setForeground(list.getForeground());
        }
        jcb.setText(value.toString());
        return jcb;
    }
}