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
    private final JComboBox<Object> scheduler = new JComboBox<>(Escalonadores.ESCALONADORES);
    private final JComboBox<Object> users = new JComboBox<>();
    private Cluster cluster = null;
    private ResourceBundle words;

    ClusterTable(final ResourceBundle words) {
        this.words = words;
    }

    void setCluster(final Cluster cluster, final Iterable<?> users) {
        this.cluster = cluster;
        this.scheduler.setSelectedItem(this.cluster.getAlgoritmo());
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

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case ClusterTable.TYPE:
                final var typeName = this.getColumnTypeName(rowIndex);
                if (typeName != null) return typeName;
            case ClusterTable.VALUE:
                if (this.cluster != null) {
                    switch (rowIndex) {
                        case TableColumns.LABEL:
                            return this.cluster.getId().getNome();
                        case TableColumns.OWNER:
                            return this.users;
                        case TableColumns.NODES:
                            return this.cluster.getNumeroEscravos();
                        case TableColumns.PROCESSORS:
                            return this.cluster.getPoderComputacional();
                        case TableColumns.RAM:
                            return this.cluster.getMemoriaRAM();
                        case TableColumns.HARD_DISK:
                            return this.cluster.getDiscoRigido();
                        case TableColumns.CORES:
                            return this.cluster.getNucleosProcessador();
                        case TableColumns.BANDWIDTH:
                            return this.cluster.getBanda();
                        case TableColumns.LATENCY:
                            return this.cluster.getLatencia();
                        case TableColumns.MASTER:
                            return this.cluster.isMestre();
                        case TableColumns.SCHEDULER:
                            return this.scheduler;
                        case TableColumns.ENERGY:
                            return this.cluster.getConsumoEnergia();
                    }
                } else {
                    return switch (rowIndex) {
                        case TableColumns.OWNER -> this.users;
                        case TableColumns.SCHEDULER -> this.scheduler;
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
            case TableColumns.LABEL -> this.words.getString("Label");
            case TableColumns.OWNER -> this.words.getString("Owner");
            case TableColumns.NODES -> this.words.getString("Number of nodes");
            case TableColumns.PROCESSORS ->
                    this.words.getString("Computing power");
            case TableColumns.RAM -> "Primary Storage";
            case TableColumns.HARD_DISK -> "Secondary Storage";
            case TableColumns.CORES -> "Cores";
            case TableColumns.BANDWIDTH -> this.words.getString("Bandwidth");
            case TableColumns.LATENCY -> this.words.getString("Latency");
            case TableColumns.MASTER -> this.words.getString("Master");
            case TableColumns.SCHEDULER ->
                    this.words.getString("Scheduling algorithm");
            case TableColumns.ENERGY -> "Energy consumption Per Node";
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
            case TableColumns.LABEL ->
                    this.cluster.getId().setNome(aValue.toString());
            case TableColumns.OWNER ->
                    this.cluster.setProprietario(this.users.getSelectedItem().toString());
            case TableColumns.NODES ->
                    this.cluster.setNumeroEscravos(Integer.valueOf(aValue.toString()));
            case TableColumns.PROCESSORS ->
                    this.cluster.setPoderComputacional(Double.valueOf(aValue.toString()));
            case TableColumns.RAM ->
                    this.cluster.setMemoriaRAM(Double.valueOf(aValue.toString()));
            case TableColumns.HARD_DISK ->
                    this.cluster.setDiscoRigido(Double.valueOf(aValue.toString()));
            case TableColumns.CORES ->
                    this.cluster.setNucleosProcessador(Integer.valueOf(aValue.toString()));
            case TableColumns.BANDWIDTH ->
                    this.cluster.setBanda(Double.valueOf(aValue.toString()));
            case TableColumns.LATENCY ->
                    this.cluster.setLatencia(Double.valueOf(aValue.toString()));
            case TableColumns.ENERGY ->
                    this.cluster.setConsumoEnergia(Double.valueOf(aValue.toString()));
            case TableColumns.MASTER ->
                    this.cluster.setMestre(Boolean.valueOf(aValue.toString()));
            case TableColumns.SCHEDULER ->
                    this.cluster.setAlgoritmo(this.scheduler.getSelectedItem().toString());
        }

        this.fireTableCellUpdated(rowIndex, ClusterTable.VALUE);
    }

    public JComboBox<Object> getEscalonadores() {
        return this.scheduler;
    }

    public void setPalavras(final ResourceBundle palavras) {
        this.words = palavras;
        this.fireTableStructureChanged();
    }

    private static class TableColumns {
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
