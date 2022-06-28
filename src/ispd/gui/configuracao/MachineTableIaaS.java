package ispd.gui.configuracao;

import ispd.arquivo.Alocadores;
import ispd.arquivo.EscalonadoresCloud;
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

public class MachineTableIaaS extends AbstractTableModel {
    private static final int TYPE = 0;
    private static final int VALUE = 1;
    private static final int LABEL = 0;
    private static final int OWNER = 1;
    private static final int PROCESSOR = 2;
    private static final int LOAD_FACTOR = 3;
    private static final int CORES = 4;
    private static final int RAM = 5;
    private static final int DISK = 6;
    private static final int COST_PER_PROCESSOR = 7;
    private static final int COST_PER_MEMORY = 8;
    private static final int COST_PER_DISK = 9;
    private static final int VMM = 10;
    private static final int SCHEDULER = 11;
    private static final int VMM_POLICY = 12;
    private static final int SLAVE = 13;
    private static final int ROW_COUNT = 14;
    private static final int COLUMN_COUNT = 2;
    private static final String[] NO_USERS = {};
    private final JButton slaves = this.setButton();
    private final JComboBox<?> schedulers =
            MachineTableIaaS.toolTippedComboBox(
                    EscalonadoresCloud.ESCALONADORES,
                    "Select the task scheduling policy"
            );
    private final JComboBox<String> users = MachineTableIaaS.toolTippedComboBox(
            MachineTableIaaS.NO_USERS,
            "Select the resource owner"
    );
    private final JComboBox<String> vmm_policies =
            MachineTableIaaS.toolTippedComboBox(
                    Alocadores.ALOCACAO,
                    "Select the virtual machine allocation policy"
            );
    private final JList<GridItem> slaveList = new JList<>();
    private ResourceBundle words;
    private Machine machine = null;

    MachineTableIaaS(final ResourceBundle words) {
        this.words = words;
        new CheckListRenderer(this.slaveList);
    }

    private static <T> JComboBox<T> toolTippedComboBox(
            final T[] items,
            final String text) {
        final var box = new JComboBox<T>(items);
        box.setToolTipText(text);
        return box;
    }

    private JButton setButton() {
        final var button = new JButton();
        button.addActionListener(new ButtonActionListener());
        button.setToolTipText(
                "Select the nodes that will be coordinated by this VMM");
        return button;
    }

    void setMaquina(final Machine machine, final Iterable<String> users) {
        this.machine = machine;
        this.vmm_policies.setSelectedItem(this.machine.getVmmAllocationPolicy());
        // TODO: Superclass?
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
        return MachineTableIaaS.ROW_COUNT;
    }

    @Override
    public int getColumnCount() {
        return MachineTableIaaS.COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case MachineTableIaaS.TYPE -> {
                final var name = this.returnNameForIndex(rowIndex);
                if (name != null)
                    return name;
            }
            case MachineTableIaaS.VALUE -> {
                final var val = this.getValueForMachine(rowIndex);
                if (val != null)
                    return val;
            }
        }
        throw new IndexOutOfBoundsException("ColumnIndex out of bounds");
    }

    private String returnNameForIndex(final int rowIndex) {
        return switch (rowIndex) {
            case MachineTableIaaS.LABEL -> this.words.getString("Label");
            case MachineTableIaaS.OWNER -> this.words.getString("Owner");
            case MachineTableIaaS.PROCESSOR -> "%s (Mflop/s)".formatted(
                    this.words.getString("Computing power"));
            case MachineTableIaaS.LOAD_FACTOR ->
                    this.words.getString("Load Factor");
            case MachineTableIaaS.RAM -> "Primary Storage";
            case MachineTableIaaS.DISK -> "Secondary Storage";
            case MachineTableIaaS.CORES -> "Cores";
            case MachineTableIaaS.VMM -> "VMM";
            case MachineTableIaaS.SCHEDULER ->
                    this.words.getString("Scheduling algorithm");
            case MachineTableIaaS.SLAVE -> "Slave Nodes";
            case MachineTableIaaS.COST_PER_PROCESSOR -> "Cost per Processing";
            case MachineTableIaaS.COST_PER_MEMORY -> "Cost per Memory";
            case MachineTableIaaS.COST_PER_DISK -> "Cost per Disk";
            case MachineTableIaaS.VMM_POLICY -> "VMM allocated policy";
            default -> null;
        };
    }

    private Object getValueForMachine(final int rowIndex) {
        if (this.machine == null) {
            return switch (rowIndex) {
                case MachineTableIaaS.OWNER -> this.users;
                case MachineTableIaaS.SCHEDULER -> this.schedulers;
                case MachineTableIaaS.SLAVE -> this.slaves;
                case MachineTableIaaS.VMM_POLICY -> this.vmm_policies;
                default -> "null";
            };
        }

        return switch (rowIndex) {
            case MachineTableIaaS.LABEL -> this.machine.getId().getName();
            case MachineTableIaaS.OWNER -> this.users;
            case MachineTableIaaS.PROCESSOR ->
                    this.machine.getPoderComputacional();
            case MachineTableIaaS.LOAD_FACTOR -> this.machine.getLoadFactor();
            case MachineTableIaaS.RAM -> this.machine.getRamMemory();
            case MachineTableIaaS.DISK -> this.machine.getHardDisk();
            case MachineTableIaaS.CORES -> this.machine.getProcessorCores();
            case MachineTableIaaS.VMM -> this.machine.isMaster();
            case MachineTableIaaS.SCHEDULER -> this.schedulers;
            case MachineTableIaaS.SLAVE -> this.slaves;
            case MachineTableIaaS.COST_PER_PROCESSOR ->
                    this.machine.getCostPerProcessing();
            case MachineTableIaaS.COST_PER_MEMORY ->
                    this.machine.getCostPerMemory();
            case MachineTableIaaS.COST_PER_DISK ->
                    this.machine.getCostPerDisk();
            case MachineTableIaaS.VMM_POLICY -> this.vmm_policies;
            default -> null;
        };
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return switch (columnIndex) {
            case MachineTableIaaS.TYPE -> this.words.getString("Properties");
            case MachineTableIaaS.VALUE -> this.words.getString("Values");
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex != MachineTableIaaS.TYPE;
    }

    @Override
    public void setValueAt(
            final Object aValue,
            final int rowIndex,
            final int columnIndex) {
        if (columnIndex != MachineTableIaaS.VALUE || this.machine == null) {
            return;
        }

        this.setValueAtIndex(aValue, rowIndex);
        this.fireTableCellUpdated(rowIndex, MachineTableIaaS.VALUE);
    }

    private void setValueAtIndex(final Object aValue, final int rowIndex) {
        switch (rowIndex) {
            case MachineTableIaaS.LABEL ->
                    this.machine.getId().setName(aValue.toString());
            case MachineTableIaaS.OWNER ->
                    this.machine.setOwner(this.users.getSelectedItem().toString());
            case MachineTableIaaS.PROCESSOR ->
                    this.machine.setComputationalPower(Double.valueOf(aValue.toString()));
            case MachineTableIaaS.LOAD_FACTOR ->
                    this.machine.setLoadFactor(Double.valueOf(aValue.toString()));
            case MachineTableIaaS.RAM ->
                    this.machine.setRamMemory(Double.valueOf(aValue.toString()));
            case MachineTableIaaS.DISK ->
                    this.machine.setHardDisk(Double.valueOf(aValue.toString()));
            case MachineTableIaaS.CORES ->
                    this.machine.setProcessorCores(Integer.valueOf(aValue.toString()));
            case MachineTableIaaS.VMM ->
                    this.machine.setIsMaster(Boolean.valueOf(aValue.toString()));
            case MachineTableIaaS.SCHEDULER ->
                    this.machine.setAlgorithm(this.schedulers.getSelectedItem().toString());
            case MachineTableIaaS.COST_PER_PROCESSOR ->
                    this.machine.setCostPerProcessing(Double.valueOf(aValue.toString()));
            case MachineTableIaaS.COST_PER_MEMORY ->
                    this.machine.setCostPerMemory(Double.valueOf(aValue.toString()));
            case MachineTableIaaS.COST_PER_DISK ->
                    this.machine.setCostPerDisk(Double.valueOf(aValue.toString()));
            case MachineTableIaaS.VMM_POLICY ->
                    this.machine.setVmmAllocationPolicy(this.vmm_policies.getSelectedItem().toString());
        }
    }

    public JComboBox getEscalonadores() {
        return this.schedulers;
    }

    JComboBox getAlocadores() {
        return this.vmm_policies;
    }

    public void setPalavras(final ResourceBundle words) {
        this.words = words;
        this.fireTableStructureChanged();
    }

    private class ButtonActionListener implements ActionListener {
        // TODO: Identical to inner class in MachineTable, refactor!
        @Override
        public void actionPerformed(final ActionEvent evt) {
            this.calculateThings();
            this.updateThings();
        }

        private void calculateThings() {
            if (MachineTableIaaS.this.slaveList.isVisible()) {
                return;
            }

            final var modelList = new DefaultListModel<GridItem>();
            final var connectedList =
                    MachineTableIaaS.this.machine.getNosEscalonaveis();

            for (final var item : connectedList) {
                modelList.addElement(item);
            }

            MachineTableIaaS.this.slaveList.setModel(modelList);

            MachineTableIaaS.this.machine.getEscravos().stream()
                    .mapToInt(connectedList::indexOf)
                    .forEachOrdered(i -> MachineTableIaaS.this.slaveList.addSelectionInterval(i, i));

            MachineTableIaaS.this.slaveList.setVisible(true);
        }

        private void updateThings() {
            if (MachineTableIaaS.this.slaveList.getModel().getSize() <= 0) {
                return;
            }

            final int option = JOptionPane.showConfirmDialog(
                    MachineTableIaaS.this.slaves,
                    MachineTableIaaS.this.slaveList,
                    "Select the slaves",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            MachineTableIaaS.this.machine.setSlaves(
                    new ArrayList<>(MachineTableIaaS.this.slaveList.getSelectedValuesList())
            );

            MachineTableIaaS.this.slaves.setText(MachineTableIaaS.this.machine.getEscravos().toString());
        }
    }
}