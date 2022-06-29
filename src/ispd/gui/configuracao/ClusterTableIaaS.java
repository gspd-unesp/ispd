package ispd.gui.configuracao;

import ispd.arquivo.Alocadores;
import ispd.arquivo.EscalonadoresCloud;
import ispd.gui.iconico.grade.Cluster;

import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import java.io.Serializable;
import java.util.ResourceBundle;

public class ClusterTableIaaS extends AbstractTableModel {

    private static final int TYPE = 0;
    private static final int VALUE = 1;
    private static final int ROW_COUNT = 15;
    private static final int COLUMN_COUNT = 2;
    private static final String[] EMPTY_COMBO_BOX_LIST = {};
    private final JComboBox<Object> schedulers = ClusterTableIaaS.makeComboBox(
            EscalonadoresCloud.ESCALONADORES,
            "Select the task scheduling policy");
    private final JComboBox<Object> users = ClusterTableIaaS.makeComboBox(
            ClusterTableIaaS.EMPTY_COMBO_BOX_LIST,
            "Select the resource owner"
    );
    private final JComboBox<Object> vmmPolicies = ClusterTableIaaS.makeComboBox(
            Alocadores.ALOCACAO,
            "Select the virtual machine allocation policy"
    );
    private Cluster cluster = null;
    private ResourceBundle words;

    ClusterTableIaaS(final ResourceBundle words) {
        this.words = words;
    }

    private static JComboBox<Object> makeComboBox(
            final String[] comboBoxArg,
            final String toolTipText) {
        final var comboBox = new JComboBox<Object>(comboBoxArg);
        comboBox.setToolTipText(toolTipText);
        return comboBox;
    }

    void setCluster(final Cluster cluster, final Iterable<String> users) {
        this.cluster = cluster;
        this.schedulers.setSelectedItem(this.cluster.getAlgorithm());
        this.vmmPolicies.setSelectedItem(this.cluster.getVmmAllocationPolicy());
        this.users.removeAllItems();
        for (final Object object : users) {
            this.users.addItem(object);
        }
        this.users.setSelectedItem(cluster.getProprietario());
    }

    @Override
    public int getRowCount() {
        return ClusterTableIaaS.ROW_COUNT;
    }

    @Override
    public int getColumnCount() {
        return ClusterTableIaaS.COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case ClusterTableIaaS.TYPE:
                final var name = this.nameForRow(rowIndex);
                if (name != null) return name;
            case ClusterTableIaaS.VALUE:
                if (this.cluster == null) {
                    return this.comboBoxForRow(rowIndex);
                }

                final var obj = this.objectAtRow(rowIndex);
                if (obj != null) return obj;
            default:
                throw new IndexOutOfBoundsException(
                        "columnIndex out of bounds");
        }
    }

    private String nameForRow(final int rowIndex) {
        return switch (rowIndex) {
            case TableRows.LABEL -> this.words.getString("Label");
            case TableRows.OWNER -> this.words.getString("Owner");
            case TableRows.NODES -> this.words.getString("Number of nodes");
            case TableRows.PROCESSORS ->
                    this.words.getString("Computing power");
            case TableRows.CORES -> "Cores";
            case TableRows.MEMORY -> "Primary Storage";
            case TableRows.DISK -> "Secondary Storage";
            case TableRows.BANDWIDTH -> this.words.getString("Bandwidth");
            case TableRows.LATENCY -> this.words.getString("Latency");
            case TableRows.VMM -> "VMM";
            case TableRows.SCHEDULER ->
                    this.words.getString("Scheduling algorithm");
            case TableRows.COST_PER_PROCESSOR -> "Cost per Processing";
            case TableRows.COST_PER_MEMORY -> "Cost per Memory";
            case TableRows.COST_PER_DISK -> "Cost per Disk";
            case TableRows.VMM_POLICIES -> ("VMM allocated policy");
            default -> null;
        };
    }

    private Serializable comboBoxForRow(final int rowIndex) {
        return switch (rowIndex) {
            case TableRows.OWNER -> this.users;
            case TableRows.SCHEDULER -> this.schedulers;
            case TableRows.VMM_POLICIES -> this.vmmPolicies;
            default -> "null";
        };
    }

    private Serializable objectAtRow(final int rowIndex) {
        return switch (rowIndex) {
            case TableRows.LABEL -> this.cluster.getId().getName();
            case TableRows.OWNER -> this.users;
            case TableRows.NODES -> this.cluster.getSlaveCount();
            case TableRows.PROCESSORS -> this.cluster.getPoderComputacional();
            case TableRows.MEMORY -> this.cluster.getRamMemory();
            case TableRows.DISK -> this.cluster.getHardDisk();
            case TableRows.CORES -> this.cluster.getProcessorCores();
            case TableRows.BANDWIDTH -> this.cluster.getBandwidth();
            case TableRows.LATENCY -> this.cluster.getLatency();
            case TableRows.VMM -> this.cluster.isMaster();
            case TableRows.SCHEDULER -> this.schedulers;
            case TableRows.COST_PER_PROCESSOR ->
                    this.cluster.getCostPerProcessing();
            case TableRows.COST_PER_MEMORY -> this.cluster.getCostPerMemory();
            case TableRows.COST_PER_DISK -> this.cluster.getCostPerDisk();
            case TableRows.VMM_POLICIES -> this.vmmPolicies;
            default -> null;
        };
    }

    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
            case ClusterTableIaaS.TYPE:
                return this.words.getString("Properties");
            case ClusterTableIaaS.VALUE:
                return this.words.getString("Values");
        }
        return null;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex != ClusterTableIaaS.TYPE;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex,
                           final int columnIndex) {
        // Pega o sócio referente a linha especificada.
        if (columnIndex != ClusterTableIaaS.VALUE || this.cluster == null) {
            return;
        }

        switch (rowIndex) {
            case TableRows.LABEL ->
                    this.cluster.getId().setName(aValue.toString());
            case TableRows.OWNER ->
                    this.cluster.setOwner(this.users.getSelectedItem().toString());
            case TableRows.NODES ->
                    this.cluster.setSlaveCount(Integer.valueOf(aValue.toString()));
            case TableRows.PROCESSORS ->
                    this.cluster.setComputationalPower(Double.valueOf(aValue.toString()));
            case TableRows.CORES ->
                    this.cluster.setProcessorCores(Integer.valueOf(aValue.toString()));
            case TableRows.MEMORY ->
                    this.cluster.setRamMemory(Double.valueOf(aValue.toString()));
            case TableRows.DISK ->
                    this.cluster.setHardDisk(Double.valueOf(aValue.toString()));
            case TableRows.COST_PER_PROCESSOR ->
                    this.cluster.setCostPerProcessing(Double.valueOf(aValue.toString()));
            case TableRows.COST_PER_MEMORY ->
                    this.cluster.setCostPerMemory(Double.valueOf(aValue.toString()));
            case TableRows.COST_PER_DISK ->
                    this.cluster.setCostPerDisk(Double.valueOf(aValue.toString()));
            case TableRows.BANDWIDTH ->
                    this.cluster.setBandwidth(Double.valueOf(aValue.toString()));
            case TableRows.LATENCY ->
                    this.cluster.setLatency(Double.valueOf(aValue.toString()));
            case TableRows.VMM ->
                    this.cluster.setIsMaster(Boolean.valueOf(aValue.toString()));
            case TableRows.SCHEDULER ->
                    this.cluster.setAlgorithm(this.schedulers.getSelectedItem().toString());
            case TableRows.VMM_POLICIES ->
                    this.cluster.setVmmAllocationPolicy(this.vmmPolicies.getSelectedItem().toString());
        }

        this.fireTableCellUpdated(rowIndex, ClusterTableIaaS.VALUE);
    }

    public JComboBox getEscalonadores() {
        return this.schedulers;
    }

    JComboBox getAlocadores() {
        return this.vmmPolicies;
    }

    public void setPalavras(final ResourceBundle words) {
        this.words = words;
        this.fireTableStructureChanged();
    }

    private static class TableRows implements Serializable {
        private static final int LABEL = 0;
        private static final int OWNER = 1;
        private static final int NODES = 2;
        private static final int PROCESSORS = 3;
        private static final int CORES = 4;
        private static final int MEMORY = 5;
        private static final int DISK = 6;
        private static final int COST_PER_PROCESSOR = 7;
        private static final int COST_PER_MEMORY = 8;
        private static final int COST_PER_DISK = 9;
        private static final int BANDWIDTH = 10;
        private static final int LATENCY = 11;
        private static final int VMM = 12;
        private static final int SCHEDULER = 13;
        private static final int VMM_POLICIES = 14;
    }
}