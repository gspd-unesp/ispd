package ispd.gui.configuracao;

import ispd.arquivo.Escalonadores;
import ispd.gui.iconico.grade.GridItem;
import ispd.gui.iconico.grade.Machine;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MachineTable extends AbstractTableModel {
    private static final int TYPE = 0;
    private static final int VALUE = 1;
    private static final int LABEL = 0;
    private static final int OWNER = 1;
    private static final int PROCESSOR = 2;
    private static final int LOAD_FACTOR = 3;
    private static final int CORES = 4;
    private static final int RAM = 5;
    private static final int DISK = 6;
    private static final int MASTER = 7;
    private static final int SCHEDULER = 8;
    private static final int SLAVE = 9;
    private static final int ENERGY = 10;
    private static final int ROW_COUNT = 11;
    private static final int COLUMN_COUNT = 2;
    private final JButton slaves = this.setButton();
    private final JComboBox<?> schedulers =
            new JComboBox<Object>(Escalonadores.ESCALONADORES);
    private final JComboBox<String> users = new JComboBox<>();
    private final JList<GridItem> slaveList = new JList<>();
    private ResourceBundle words;
    private Machine machine = null;

    MachineTable(final ResourceBundle words) {
        this.words = words;
        new CheckListRenderer(this.slaveList);
    }

    private JButton setButton() {
        final var button = new JButton();
        button.addActionListener(new ButtonActionListener());
        return button;
    }

    void setMaquina(final Machine machine, final Iterable<String> users) {
        this.machine = machine;
        this.schedulers.setSelectedItem(this.machine.getAlgorithm());
        this.users.removeAllItems();
        for (final var s : users) {
            this.users.addItem(s);
        }
        this.users.setSelectedItem(machine.getProprietario());
        this.slaveList.setVisible(false);
        this.slaves.setText(machine.getEscravos().toString());
    }

    @Override
    public int getRowCount() {
        return MachineTable.ROW_COUNT;
    }

    @Override
    public int getColumnCount() {
        return MachineTable.COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case MachineTable.TYPE -> {
                final var name = this.returnNameForIndex(rowIndex);
                if (name != null)
                    return name;
            }
            case MachineTable.VALUE -> {
                final var val = this.getValueForMachine(rowIndex);
                if (val != null)
                    return val;
            }
        }

        throw new IndexOutOfBoundsException("ColumnIndex out of bounds");
    }

    private String returnNameForIndex(final int rowIndex) {
        return switch (rowIndex) {
            case MachineTable.LABEL -> this.words.getString("Label");
            case MachineTable.OWNER -> this.words.getString("Owner");
            case MachineTable.PROCESSOR -> "%s (Mflop/s)".formatted(
                    this.words.getString("Computing power"));
            case MachineTable.LOAD_FACTOR ->
                    this.words.getString("Load Factor");
            case MachineTable.RAM -> "Primary Storage";
            case MachineTable.DISK -> "Secondary Storage";
            case MachineTable.CORES -> "Cores";
            case MachineTable.MASTER -> this.words.getString("Master");
            case MachineTable.SCHEDULER ->
                    this.words.getString("Scheduling algorithm");
            case MachineTable.SLAVE -> "Slave Nodes";
            case MachineTable.ENERGY -> "Energy consumption";
            default -> null;
        };
    }

    private Object getValueForMachine(final int rowIndex) {
        if (this.machine == null) {
            return switch (rowIndex) {
                case MachineTable.OWNER -> this.users;
                case MachineTable.SCHEDULER -> this.schedulers;
                case MachineTable.SLAVE -> this.slaves;
                default -> "null";
            };
        }

        return switch (rowIndex) {
            case MachineTable.LABEL -> this.machine.getId().getName();
            case MachineTable.OWNER -> this.users;
            case MachineTable.PROCESSOR -> this.machine.getPoderComputacional();
            case MachineTable.LOAD_FACTOR -> this.machine.getLoadFactor();
            case MachineTable.RAM -> this.machine.getRamMemory();
            case MachineTable.DISK -> this.machine.getHardDisk();
            case MachineTable.CORES -> this.machine.getProcessorCores();
            case MachineTable.MASTER -> this.machine.isMaster();
            case MachineTable.SCHEDULER -> this.schedulers;
            case MachineTable.SLAVE -> this.slaves;
            case MachineTable.ENERGY -> this.machine.getEnergyConsumption();
            default -> null;
        };
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return switch (columnIndex) {
            case MachineTable.TYPE -> this.words.getString("Properties");
            case MachineTable.VALUE -> this.words.getString("Values");
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex != MachineTable.TYPE;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex,
                           final int columnIndex) {
        if (columnIndex != MachineTable.VALUE || this.machine == null) {
            return;
        }

        this.setValueAtIndex(aValue, rowIndex);

        this.fireTableCellUpdated(rowIndex, MachineTable.VALUE);
    }

    private void setValueAtIndex(final Object value, final int rowIndex) {
        switch (rowIndex) {
            case MachineTable.LABEL ->
                    this.machine.getId().setName(value.toString());
            case MachineTable.OWNER ->
                    this.machine.setOwner(this.users.getSelectedItem().toString());
            case MachineTable.PROCESSOR ->
                    this.machine.setComputationalPower(Double.valueOf(value.toString()));
            case MachineTable.LOAD_FACTOR ->
                    this.machine.setLoadFactor(Double.valueOf(value.toString()));
            case MachineTable.RAM ->
                    this.machine.setRamMemory(Double.valueOf(value.toString()));
            case MachineTable.DISK ->
                    this.machine.setHardDisk(Double.valueOf(value.toString()));
            case MachineTable.CORES ->
                    this.machine.setProcessorCores(Integer.valueOf(value.toString()));
            case MachineTable.ENERGY ->
                    this.machine.setEnergyConsumption(Double.valueOf(value.toString()));
            case MachineTable.MASTER ->
                    this.machine.setIsMaster(Boolean.valueOf(value.toString()));
            case MachineTable.SCHEDULER ->
                    this.machine.setAlgorithm(this.schedulers.getSelectedItem().toString());
        }
    }

    public JComboBox getEscalonadores() {
        return this.schedulers;
    }

    public void setPalavras(final ResourceBundle palavras) {
        this.words = palavras;
        this.fireTableStructureChanged();
    }

    private class ButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            this.calculateThings();
            this.updateThings();
        }

        private void calculateThings() {
            if (MachineTable.this.slaveList.isVisible()) {
                return;
            }

            final var modelList = new DefaultListModel<GridItem>();
            final var connectedList =
                    MachineTable.this.machine.getNosEscalonaveis();

            for (final var item : connectedList) {
                modelList.addElement(item);
            }

            MachineTable.this.slaveList.setModel(modelList);

            MachineTable.this.machine.getEscravos().stream()
                    .mapToInt(connectedList::indexOf)
                    .forEachOrdered(i -> MachineTable.this.slaveList.addSelectionInterval(i, i));

            MachineTable.this.slaveList.setVisible(true);
        }

        private void updateThings() {
            if (MachineTable.this.slaveList.getModel().getSize() <= 0) {
                return;
            }

            final int option = JOptionPane.showConfirmDialog(
                    MachineTable.this.slaves,
                    MachineTable.this.slaveList,
                    "Select the slaves",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            MachineTable.this.machine.setSlaves(
                    new ArrayList<>(MachineTable.this.slaveList.getSelectedValuesList())
            );

            MachineTable.this.slaves.setText(MachineTable.this.machine.getEscravos().toString());
        }
    }
}