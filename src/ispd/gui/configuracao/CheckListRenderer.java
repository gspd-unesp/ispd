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