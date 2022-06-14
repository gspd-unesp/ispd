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
 * ClusterTable.java
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

import ispd.arquivo.Escalonadores;
import ispd.gui.iconico.grade.Cluster;

import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import java.util.ResourceBundle;

public class ClusterTable extends AbstractTableModel {
    private static final int ROW_COUNT = 12;
    private static final int COLUMN_COUNT = 2;
    private static final int TYPE = 0;
    private static final int VALUE = 1;
    private final JComboBox<Object> schedulers =
            new JComboBox<>(Escalonadores.ESCALONADORES);
    private final JComboBox<Object> users = new JComboBox<>();
    private Cluster cluster = null;
    private ResourceBundle words;

    ClusterTable(final ResourceBundle words) {
        this.words = words;
    }

    void setCluster(final Cluster cluster, final Iterable<?> users) {
        this.cluster = cluster;
        this.schedulers.setSelectedItem(this.cluster.getAlgoritmo());
        this.users.removeAllItems();
        for (final var o : users) {
            this.users.addItem(o);
        }
        this.users.setSelectedItem(cluster.getProprietario());
    }

    @Override
    public int getRowCount() {
        return ClusterTable.ROW_COUNT;
    }

    @Override
    public int getColumnCount() {
        return ClusterTable.COLUMN_COUNT;
    }

    /**
     * Return {@code Object} (either a String, Integer, Double, Boolean, or
     * JComboBox) in table at (rowIndex, columnIndex). Throws {@code
     * IndexOutOfBoundsException} if either index is invalid.
     *
     * @param rowIndex    Row to get value from.
     * @param columnIndex Column to get value from.
     * @return Object in table at specified row and column.
     */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case ClusterTable.TYPE:
                final var typeName = this.getColumnTypeName(rowIndex);
                if (typeName != null) return typeName;
            case ClusterTable.VALUE:
                if (this.cluster != null) {
                    switch (rowIndex) {
                        case TableRows.LABEL:
                            return this.cluster.getId().getNome();
                        case TableRows.OWNER:
                            return this.users;
                        case TableRows.NODES:
                            return this.cluster.getNumeroEscravos();
                        case TableRows.PROCESSORS:
                            return this.cluster.getPoderComputacional();
                        case TableRows.RAM:
                            return this.cluster.getMemoriaRAM();
                        case TableRows.HARD_DISK:
                            return this.cluster.getDiscoRigido();
                        case TableRows.CORES:
                            return this.cluster.getNucleosProcessador();
                        case TableRows.BANDWIDTH:
                            return this.cluster.getBanda();
                        case TableRows.LATENCY:
                            return this.cluster.getLatencia();
                        case TableRows.MASTER:
                            return this.cluster.isMestre();
                        case TableRows.SCHEDULER:
                            return this.schedulers;
                        case TableRows.ENERGY:
                            return this.cluster.getConsumoEnergia();
                    }
                } else {
                    return switch (rowIndex) {
                        case TableRows.OWNER -> this.users;
                        case TableRows.SCHEDULER -> this.schedulers;
                        default -> "null";
                    };
                }
            default:
                // Não deve ocorrer, pois só existem 2 colunas
                throw new IndexOutOfBoundsException(
                        "columnIndex out of bounds");
        }
    }

    private String getColumnTypeName(final int rowIndex) {
        return switch (rowIndex) {
            case TableRows.LABEL -> this.words.getString("Label");
            case TableRows.OWNER -> this.words.getString("Owner");
            case TableRows.NODES -> this.words.getString("Number of nodes");
            case TableRows.PROCESSORS ->
                    this.words.getString("Computing power");
            case TableRows.RAM -> "Primary Storage";
            case TableRows.HARD_DISK -> "Secondary Storage";
            case TableRows.CORES -> "Cores";
            case TableRows.BANDWIDTH -> this.words.getString("Bandwidth");
            case TableRows.LATENCY -> this.words.getString("Latency");
            case TableRows.MASTER -> this.words.getString("Master");
            case TableRows.SCHEDULER ->
                    this.words.getString("Scheduling algorithm");
            case TableRows.ENERGY -> "Energy consumption Per Node";
            default -> null;
        };
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return switch (columnIndex) {
            case ClusterTable.TYPE -> this.words.getString("Properties");
            case ClusterTable.VALUE -> this.words.getString("Values");
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex != ClusterTable.TYPE;
    }

    @Override
    public void setValueAt(
            final Object aValue,
            final int rowIndex,
            final int columnIndex) {
        if (!(columnIndex == ClusterTable.VALUE && this.cluster != null)) {
            return;
        }

        switch (rowIndex) {
            case TableRows.LABEL ->
                    this.cluster.getId().setNome(aValue.toString());
            case TableRows.OWNER ->
                    this.cluster.setProprietario(this.users.getSelectedItem().toString());
            case TableRows.NODES ->
                    this.cluster.setNumeroEscravos(Integer.valueOf(aValue.toString()));
            case TableRows.PROCESSORS ->
                    this.cluster.setPoderComputacional(Double.valueOf(aValue.toString()));
            case TableRows.RAM ->
                    this.cluster.setMemoriaRAM(Double.valueOf(aValue.toString()));
            case TableRows.HARD_DISK ->
                    this.cluster.setDiscoRigido(Double.valueOf(aValue.toString()));
            case TableRows.CORES ->
                    this.cluster.setNucleosProcessador(Integer.valueOf(aValue.toString()));
            case TableRows.BANDWIDTH ->
                    this.cluster.setBanda(Double.valueOf(aValue.toString()));
            case TableRows.LATENCY ->
                    this.cluster.setLatencia(Double.valueOf(aValue.toString()));
            case TableRows.ENERGY ->
                    this.cluster.setConsumoEnergia(Double.valueOf(aValue.toString()));
            case TableRows.MASTER ->
                    this.cluster.setMestre(Boolean.valueOf(aValue.toString()));
            case TableRows.SCHEDULER ->
                    this.cluster.setAlgoritmo(this.schedulers.getSelectedItem().toString());
        }

        this.fireTableCellUpdated(rowIndex, ClusterTable.VALUE);
    }

    public JComboBox<Object> getEscalonadores() {
        return this.schedulers;
    }

    public void setPalavras(final ResourceBundle words) {
        this.words = words;
        this.fireTableStructureChanged();
    }

    private static class TableRows {
        private static final int LABEL = 0;
        private static final int OWNER = 1;
        private static final int NODES = 2;
        private static final int PROCESSORS = 3;
        private static final int CORES = 4;
        private static final int RAM = 5;
        private static final int HARD_DISK = 6;
        private static final int BANDWIDTH = 7;
        private static final int LATENCY = 8;
        private static final int MASTER = 9;
        private static final int SCHEDULER = 10;
        private static final int ENERGY = 11;
    }
}